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
package com.makinul.background.remover.ui.main

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.ByteBufferExtractor
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenterResult
import com.makinul.background.remover.R
import com.makinul.background.remover.utils.Extensions.toAlphaColor
import com.makinul.background.remover.utils.ImageSegmentHelper
import com.makinul.background.remover.utils.PoseLandmarkHelper
import com.makinul.background.remover.utils.PreferenceHelper
import com.makinul.background.remover.data.Event
import com.makinul.background.remover.data.Resource
import com.makinul.background.remover.ui.main.MainActivity.ImageEdit
import com.makinul.background.remover.ui.main.MainActivity.ImageEditType
import com.makinul.background.remover.utils.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import javax.inject.Inject

/**
 *  This ViewModel is used to store pose landmark helper settings
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {

    private val _maskedBitmapArray by lazy { MutableLiveData<Event<Resource<IntArray>>>() }
    val maskedBitmapArray: LiveData<Event<Resource<IntArray>>> = _maskedBitmapArray

    fun processBitmapToRemoveBackground(
        rawBitmap: Bitmap,
        imageWidth: Int,
        imageHeight: Int,
        scaleFactor: Float,
        poseLandmarkHelper: PoseLandmarkHelper?,
        selfieSegmentHelper: ImageSegmentHelper?,
        selfieMulticlassSegmentHelper: ImageSegmentHelper?,
    ) = viewModelScope.launch(Dispatchers.IO) {
        _maskedBitmapArray.postValue(Event(Resource.loading()))
        val mpImage = BitmapImageBuilder(rawBitmap).build()

        val selfieSegmentResult = selfieSegmentHelper?.segmentImage(mpImage)
        val selfieMulticlassSegmentResult = selfieMulticlassSegmentHelper?.segmentImage(mpImage)
        selfieSegmentHelper?.clearImageSegment()
        selfieMulticlassSegmentHelper?.clearImageSegment()

        if (selfieMulticlassSegmentResult == null) {
            _maskedBitmapArray.postValue(Event(Resource.error(messageResId = R.string.unknown_error)))
            return@launch
        }

//        val selfieByteBuffer = getByteBuffer(selfieSegmentResult)
//        val selfiePixels = IntArray(selfieByteBuffer.capacity())
        val multiclassByteBuffer = getByteBuffer(selfieMulticlassSegmentResult)
        val multiclassPixels = IntArray(multiclassByteBuffer.capacity())

//        pointArray.clear()
//        lineArray.clear()

        var lastIndex = 0
        val steps = (imageHeight.toFloat() / 100f).toInt()

        for (y in 0 until imageHeight) {
            lastIndex = 0
            for (x in 0 until imageWidth) {
                val i = (y * imageWidth) + x
                val index = multiclassByteBuffer.get(i).toInt()

                val pixel = rawBitmap.getPixel(x, y)
                val rawColor = Color.rgb(
                    Color.red(pixel),
                    Color.green(pixel),
                    Color.blue(pixel)
                )

                val color = if (index == 0) {
                    if (lastIndex > 0) {
                        multiclassPixels[i] = rawColor.toAlphaColor(15.0)
                        var nextX = x + 1
                        if (nextX < imageWidth) {
                            var newPixel = rawBitmap.getPixel(nextX, y)
                            var newRawColor = Color.rgb(
                                Color.red(newPixel),
                                Color.green(newPixel),
                                Color.blue(newPixel)
                            )
                            multiclassPixels[i + 1] = newRawColor.toAlphaColor(30.0)

                            nextX = x + 2
                            if (nextX < imageWidth) {
                                newPixel = rawBitmap.getPixel(nextX, y)
                                newRawColor = Color.rgb(
                                    Color.red(newPixel),
                                    Color.green(newPixel),
                                    Color.blue(newPixel)
                                )
                                multiclassPixels[i + 2] = newRawColor.toAlphaColor(45.0)
                            }

                            nextX = x + 3
                            if (nextX < imageWidth) {
                                newPixel = rawBitmap.getPixel(nextX, y)
                                newRawColor = Color.rgb(
                                    Color.red(newPixel),
                                    Color.green(newPixel),
                                    Color.blue(newPixel)
                                )
                                multiclassPixels[i + 3] = newRawColor.toAlphaColor(60.0)
                            }

                            nextX = x + 4
                            if (nextX < imageWidth) {
                                newPixel = rawBitmap.getPixel(nextX, y)
                                newRawColor = Color.rgb(
                                    Color.red(newPixel),
                                    Color.green(newPixel),
                                    Color.blue(newPixel)
                                )
                                multiclassPixels[i + 4] = newRawColor.toAlphaColor(75.0)
                            }

                            nextX = x + 5
                            if (nextX < imageWidth) {
                                newPixel = rawBitmap.getPixel(nextX, y)
                                newRawColor = Color.rgb(
                                    Color.red(newPixel),
                                    Color.green(newPixel),
                                    Color.blue(newPixel)
                                )
                                multiclassPixels[i + 5] = newRawColor.toAlphaColor(90.0)
                            }
                        }
                        lastIndex = 0
                        continue
                    }
                    Color.TRANSPARENT
                } else {
                    if (lastIndex <= 0) {
                        multiclassPixels[i] = rawColor.toAlphaColor(15.0)
                        var nextX = x - 1
                        if (nextX >= 0) {
                            var newPixel = rawBitmap.getPixel(nextX, y)
                            var newRawColor = Color.rgb(
                                Color.red(newPixel),
                                Color.green(newPixel),
                                Color.blue(newPixel)
                            )
                            multiclassPixels[i - 1] = newRawColor.toAlphaColor(30.0)

                            nextX = x - 2
                            if (nextX >= 0) {
                                newPixel = rawBitmap.getPixel(nextX, y)
                                newRawColor = Color.rgb(
                                    Color.red(newPixel),
                                    Color.green(newPixel),
                                    Color.blue(newPixel)
                                )
                                multiclassPixels[i - 2] = newRawColor.toAlphaColor(45.0)
                            }

                            nextX = x - 3
                            if (nextX >= 0) {
                                newPixel = rawBitmap.getPixel(nextX, y)
                                newRawColor = Color.rgb(
                                    Color.red(newPixel),
                                    Color.green(newPixel),
                                    Color.blue(newPixel)
                                )
                                multiclassPixels[i - 3] = newRawColor.toAlphaColor(60.0)
                            }

                            nextX = x - 4
                            if (nextX >= 0) {
                                newPixel = rawBitmap.getPixel(nextX, y)
                                newRawColor = Color.rgb(
                                    Color.red(newPixel),
                                    Color.green(newPixel),
                                    Color.blue(newPixel)
                                )
                                multiclassPixels[i - 4] = newRawColor.toAlphaColor(75.0)
                            }

                            nextX = x - 5
                            if (nextX >= 0) {
                                newPixel = rawBitmap.getPixel(nextX, y)
                                newRawColor = Color.rgb(
                                    Color.red(newPixel),
                                    Color.green(newPixel),
                                    Color.blue(newPixel)
                                )
                                multiclassPixels[i - 5] = newRawColor.toAlphaColor(90.0)
                            }
                        }
                        lastIndex = index
                        continue
                    }
                    rawColor
                }

//                val color = if (index != 0) {
//                    Color.TRANSPARENT
//                } else {
//                    Color.WHITE.toAlphaColor(5.0)
//                }
                lastIndex = index
                multiclassPixels[i] = color
            }
        }
        _maskedBitmapArray.postValue(Event(Resource.success(multiclassPixels)))
    }

    private fun getByteBuffer(segmentResult: ImageSegmenterResult): ByteBuffer {
        val mpImage = segmentResult.categoryMask().get()
        return ByteBufferExtractor.extract(mpImage)
    }

    private fun storeImage(image: Bitmap, storagePath: String) {
        val folder = File(storagePath)
        if (!folder.exists()) {
            folder.mkdir()
            folder.mkdirs()
        }
        val pictureFile = File(folder.absolutePath, "masked_image.png")
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            showLog("File not found: " + e.message)
        } catch (e: IOException) {
            showLog("Error accessing file: " + e.message)
        }
    }

    private val _updatedBitmapArray by lazy { MutableLiveData<Event<Resource<IntArray>>>() }
    val updatedBitmapArray: LiveData<Event<Resource<IntArray>>> = _updatedBitmapArray

    fun updateItems(
        bitmapArray: IntArray,
        items: List<ImageEdit>,
        tansX: Float,
        tansY: Float,
        scaleFactor: Float,
        rawBitmap: Bitmap,
        bitmapWidth: Int,
        bitmapHeight: Int
    ) = viewModelScope.launch(Dispatchers.IO) {
        for (item in items) {
            for (point in item.pointArray) {
                val selectedX = point.x // * scaleFactor // image view position x
                val selectedY = point.y // * scaleFactor // image view position y

                val x = (selectedX - tansX) / scaleFactor
                val y = (selectedY - tansY) / scaleFactor

                val circleAreaPoints = getPointsAroundSelectedPoint(x, y, item.radius)
                for (circlePoint in circleAreaPoints) {
                    val circleX = circlePoint.first
                    val circleY = circlePoint.second
                    if (circleX < 0 || circleX >= bitmapWidth || circleY < 0 || circleY >= bitmapHeight)
                        continue

                    val i = ((circleY * bitmapWidth) + circleX).toInt()
                    if (i + 1 >= (bitmapWidth * bitmapHeight))
                        continue

                    if (item.type == ImageEditType.RESTORE) {
                        val pixel = rawBitmap.getPixel(circleX.toInt(), circleY.toInt())
                        val previousColor = Color.rgb(
                            Color.red(pixel),
                            Color.green(pixel),
                            Color.blue(pixel)
                        )
                        bitmapArray[i] = previousColor
                    } else {
                        bitmapArray[i] = Color.TRANSPARENT
                    }
                }
            }
        }
        _updatedBitmapArray.postValue(Event(Resource.success(bitmapArray)))
    }

    private fun getPointsAroundSelectedPoint(
        xc: Float,
        yc: Float,
        radius: Int
    ): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()

        // Define the bounding box with floating-point bounds rounded to integers
        val xMin = kotlin.math.floor(xc - radius).toInt()
        val xMax = kotlin.math.ceil(xc + radius).toInt()
        val yMin = kotlin.math.floor(yc - radius).toInt()
        val yMax = kotlin.math.ceil(yc + radius).toInt()

        // Check each point within the bounding box
        for (x in xMin..xMax) {
            for (y in yMin..yMax) {
                // Calculate the squared distance from the center to this point
                if ((x - xc) * (x - xc) + (y - yc) * (y - yc) <= (radius * radius).toFloat()) {
                    points.add(Pair(x.toFloat(), y.toFloat())) // Add point as a floating-point pair
                }
            }
        }

        return points
    }

    private fun showLog(message: String) {
        AppConstants.showLog(TAG, message)
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}