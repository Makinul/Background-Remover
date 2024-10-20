/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.makinul.background.remover.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseLandmarkHelper(
    var minPoseDetectionConfidence: Float = DEFAULT_POSE_DETECTION_CONFIDENCE,
    var minPoseTrackingConfidence: Float = DEFAULT_POSE_TRACKING_CONFIDENCE,
    var minPosePresenceConfidence: Float = DEFAULT_POSE_PRESENCE_CONFIDENCE,
    var currentModel: Int = MODEL_POSE_LANDMARK_FULL,
    var currentDelegate: Int = DELEGATE_CPU,
    var runningMode: RunningMode = RunningMode.IMAGE,
    val context: Context,
    // this listener is only used when running in RunningMode.LIVE_STREAM
    val poseLandmarkHelperListener: LandmarkListener? = null
) {

    // For this example this needs to be a var so it can be reset on changes.
    // If the Pose Landmark will not change, a lazy val would be preferable.
    private var poseLandmark: PoseLandmarker? = null
    private val bitmapList: ArrayList<Bitmap> = ArrayList()
    private val timestampList: ArrayList<Long> = ArrayList()

    init {
        bitmapList.clear()
        timestampList.clear()
        setupPoseLandmark()
    }

    private var imageStoringFlag = false
    fun setImageStoringFlag(flag: Boolean) {
        imageStoringFlag = flag
    }

    fun clearBitmapList() {
        bitmapList.clear()
        timestampList.clear()
    }

    fun clearPoseLandmark() {
        poseLandmark?.close()
        poseLandmark = null
    }

    // Return running status of PoseLandmarkHelper
    fun isClose(): Boolean {
        return poseLandmark == null
    }

    // Initialize the Pose landmark using current settings on the
    // thread that is using it. CPU can be used with Landmark
    // that are created on the main thread and used on a background thread, but
    // the GPU delegate needs to be used on the thread that initialized the
    // Landmark
    fun setupPoseLandmark() {
        // Set general pose landmark options
        val baseOptionBuilder = BaseOptions.builder()

        // Use the specified hardware for running the model. Default to CPU
        when (currentDelegate) {
            DELEGATE_CPU -> {
                baseOptionBuilder.setDelegate(Delegate.CPU)
            }

            DELEGATE_GPU -> {
                baseOptionBuilder.setDelegate(Delegate.GPU)
            }
        }

        val modelName = when (currentModel) {
            MODEL_POSE_LANDMARK_FULL -> "pose_landmarker_full.task"
            MODEL_POSE_LANDMARK_LITE -> "pose_landmarker_lite.task"
            MODEL_POSE_LANDMARK_HEAVY -> "pose_landmarker_heavy.task"
            else -> "pose_landmarker_full.task"
        }

        baseOptionBuilder.setModelAssetPath(modelName)

        // Check if runningMode is consistent with poseLandmarkHelperListener
        when (runningMode) {
            RunningMode.LIVE_STREAM -> {
                if (poseLandmarkHelperListener == null) {
                    throw IllegalStateException(
                        "poseLandmarkHelperListener must be set when runningMode is LIVE_STREAM."
                    )
                }
            }

            else -> {
                // no-op
            }
        }

        try {
            val baseOptions = baseOptionBuilder.build()
            // Create an option builder with base options and specific
            // options only use for Pose Landmark.
            val optionsBuilder =
                PoseLandmarker.PoseLandmarkerOptions.builder().setBaseOptions(baseOptions)
                    .setMinPoseDetectionConfidence(minPoseDetectionConfidence)
                    .setMinTrackingConfidence(minPoseTrackingConfidence)
                    .setMinPosePresenceConfidence(minPosePresenceConfidence)
//                    .setOutputSegmentationMasks(true)
                    .setRunningMode(runningMode)
                    .setNumPoses(1)

            // The ResultListener and ErrorListener only use for LIVE_STREAM mode.
            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder.setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }

            val options = optionsBuilder.build()
            poseLandmark = PoseLandmarker.createFromOptions(context, options)
        } catch (e: IllegalStateException) {
            poseLandmarkHelperListener?.onError(
                "Pose Landmark failed to initialize. See error logs for " + "details"
            )
            Log.e(
                TAG, "MediaPipe failed to load the task with error: " + e.message
            )
        } catch (e: RuntimeException) {
            // This occurs if the model being used does not support GPU
            poseLandmarkHelperListener?.onError(
                "Pose Landmark failed to initialize. See error logs for " + "details", GPU_ERROR
            )
            Log.e(
                TAG, "Image classifier failed to load model with error: " + e.message
            )
        }
    }

    // Convert the ImageProxy to MP Image and feed it to PoselandmakerHelper.
    fun detectLiveStream(
        imageProxy: ImageProxy, isFrontCamera: Boolean
    ) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw IllegalArgumentException(
                "Attempting to call detectLiveStream" + " while not using RunningMode.LIVE_STREAM"
            )
        }
        val frameTime = SystemClock.uptimeMillis()

        // Copy out RGB bits from the frame to a bitmap buffer
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        )

        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()

        val matrix = Matrix().apply {
            // Rotate the frame received from the camera to be in the same direction as it'll be shown
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            // flip image if user use front camera
            if (isFrontCamera) {
                postScale(
                    -1f, 1f, imageProxy.width.toFloat(), imageProxy.height.toFloat()
                )
            }
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height, matrix, true
        )

        if (imageStoringFlag) {
            bitmapList.add(rotatedBitmap)
            timestampList.add(frameTime)
        }

        Log.v(TAG, "imageStoringFlag $imageStoringFlag")

        // Convert the input Bitmap object to an MPImage object to run inference
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        detectAsync(mpImage, frameTime)
    }

    // Run pose landmark using MediaPipe Pose Landmark API
    @VisibleForTesting
    fun detectAsync(mpImage: MPImage, frameTime: Long) {
        poseLandmark?.detectAsync(mpImage, frameTime)
        // As we're using running mode LIVE_STREAM, the landmark result will
        // be returned in returnLivestreamResult function
    }

    // Accepts the URI for a video file loaded from the user's gallery and attempts to run
    // pose landmark inference on the video. This process will evaluate every
    // frame in the video and attach the results to a bundle that will be
    // returned.
    fun detectVideoFile(
        videoUri: Uri, inferenceIntervalMs: Long
    ): ResultBundle? {
        if (runningMode != RunningMode.VIDEO) {
            throw IllegalArgumentException(
                "Attempting to call detectVideoFile" + " while not using RunningMode.VIDEO"
            )
        }

        // Inference time is the difference between the system time at the start and finish of the
        // process
        val startTime = SystemClock.uptimeMillis()

        var didErrorOccurred = false

        // Load frames from the video and run the pose landmark.
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val videoLengthMs =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()

        // Note: We need to read width/height from frame instead of getting the width/height
        // of the video directly because MediaRetriever returns frames that are smaller than the
        // actual dimension of the video file.
        val firstFrame = retriever.getFrameAtTime(0)
        val width = firstFrame?.width
        val height = firstFrame?.height

        // If the video is invalid, returns a null detection result
        if ((videoLengthMs == null) || (width == null) || (height == null)) return null

        // Next, we'll get one frame every frameInterval ms, then run detection on these frames.
        val resultList = mutableListOf<PoseLandmarkerResult>()
        val numberOfFrameToRead = videoLengthMs.div(inferenceIntervalMs)

        for (i in 0..numberOfFrameToRead) {
            val timestampMs = i * inferenceIntervalMs // ms

            retriever.getFrameAtTime(
                timestampMs * 1000, // convert from ms to micro-s
                MediaMetadataRetriever.OPTION_CLOSEST
            )?.let { frame ->
                // Convert the video frame to ARGB_8888 which is required by the MediaPipe
                val argb8888Frame = if (frame.config == Bitmap.Config.ARGB_8888) frame
                else frame.copy(Bitmap.Config.ARGB_8888, false)

                // Convert the input Bitmap object to an MPImage object to run inference
                val mpImage = BitmapImageBuilder(argb8888Frame).build()

                // Run pose landmark using MediaPipe Pose Landmark API
                poseLandmark?.detectForVideo(mpImage, timestampMs)?.let { detectionResult ->
                    resultList.add(detectionResult)
                } ?: {
                    didErrorOccurred = true
                    poseLandmarkHelperListener?.onError(
                        "ResultBundle could not be returned" + " in detectVideoFile"
                    )
                }
            } ?: run {
                didErrorOccurred = true
                poseLandmarkHelperListener?.onError(
                    "Frame at specified time could not be" + " retrieved when detecting in video."
                )
            }
        }

        retriever.release()

        val inferenceTimePerFrameMs =
            (SystemClock.uptimeMillis() - startTime).div(numberOfFrameToRead)

        return if (didErrorOccurred) {
            null
        } else {
            ResultBundle(resultList, inferenceTimePerFrameMs, height, width)
        }
    }

    // Accepted a Bitmap and runs pose landmark inference on it to return
    // results back to the caller
    fun detectImage(image: Bitmap): ResultBundle? {
        if (runningMode != RunningMode.IMAGE) {
            throw IllegalArgumentException(
                "Attempting to call detectImage" + " while not using RunningMode.IMAGE"
            )
        }


        // Inference time is the difference between the system time at the
        // start and finish of the process
        val startTime = SystemClock.uptimeMillis()

        // Convert the input Bitmap object to an MPImage object to run inference
        val mpImage = BitmapImageBuilder(image).build()

        // Run pose landmark using MediaPipe Pose Landmark API
        poseLandmark?.detect(mpImage)?.also { landmarkResult ->
            val inferenceTimeMs = SystemClock.uptimeMillis() - startTime
            return ResultBundle(
                listOf(landmarkResult), inferenceTimeMs, image.height, image.width
            )
        }

        // If poseLandmark?.detect() returns null, this is likely an error. Returning null
        // to indicate this.
        poseLandmarkHelperListener?.onError(
            "Pose Landmark failed to detect."
        )
        return null
    }

    // Return the landmark result to this PoseLandmarkHelper's caller
    private fun returnLivestreamResult(
        result: PoseLandmarkerResult, input: MPImage
    ) {

        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        val index = timestampList.indexOf(result.timestampMs())
        var finalBitmap: Bitmap? = null
        if (index in bitmapList.indices) {
            finalBitmap = bitmapList[index]
            val tmpBitmapList = ArrayList<Bitmap>()
            tmpBitmapList.clear()

            val tmpTimestampList = ArrayList<Long>()
            tmpTimestampList.clear()

            tmpBitmapList.addAll(bitmapList.subList(index, bitmapList.size - 1))
            tmpTimestampList.addAll(timestampList.subList(index, timestampList.size - 1))

            bitmapList.clear()
            timestampList.clear()

            bitmapList.addAll(tmpBitmapList)
            timestampList.addAll(tmpTimestampList)

//            Log.v(TAG, "bitmapList ${bitmapList.size}")
//            Log.v(TAG, "timestampList ${timestampList.size}")
        }
//        if (!result.landmarks().isNullOrEmpty()) {
//            finalBitmap = BitmapExtractor.extract(input)
        poseLandmarkHelperListener?.onResults(
            ResultBundle(
                listOf(result), result.timestampMs(), input.height, input.width, finalBitmap
            )
        )
//        }
    }

    // Return errors thrown during detection to this PoseLandmarkHelper's
    // caller
    private fun returnLivestreamError(error: RuntimeException) {
        poseLandmarkHelperListener?.onError(
            error.message ?: "An unknown error has occurred"
        )
    }

    companion object {
        const val TAG = "PoseLandmarkHelper"

        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DEFAULT_POSE_DETECTION_CONFIDENCE = 0.75F
        const val DEFAULT_POSE_TRACKING_CONFIDENCE = 0.75F
        const val DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.75F
        const val DEFAULT_NUM_POSES = 1
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
        const val MODEL_POSE_LANDMARK_FULL = 0
        const val MODEL_POSE_LANDMARK_LITE = 1
        const val MODEL_POSE_LANDMARK_HEAVY = 2
    }

    data class ResultBundle(
        val results: List<PoseLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
        val finalBitmap: Bitmap? = null
    )

    interface LandmarkListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
        fun onResults(resultBundle: ResultBundle)
    }
}