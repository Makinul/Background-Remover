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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.makinul.background.remover.data.model.Line
import com.makinul.background.remover.data.model.Point

class ImageEditOverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var eraseBarSize = 0

    private var blueLinePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
        style = Paint.Style.FILL
    }
    private var blueCirclePaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }
    private var jointCirclePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private var pointPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val pointRadius = 10f

    private var greenLinePaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private var redLinePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private var yellowLinePaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private var segmentPaint = Paint().apply {
        color = Color.MAGENTA
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private var eraserProgressPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private var eraserLinePaint = Paint().apply {
        color = Color.RED
        strokeWidth = eraseBarSize.toFloat()
        style = Paint.Style.FILL
    }

    fun clear() {
        pointArray.clear()
        lineArray.clear()
        maskBitmap = null
        invalidate()
    }

    private var startX = -1f
    private var startY = -1f
    private var endX = -1f
    private var endY = -1f

    fun segmentRect(startX: Float, startY: Float, endX: Float, endY: Float) {
        this.startX = startX
        this.startY = startY
        this.endX = endX
        this.endY = endY
    }

    private var startMidX: Float = -1f
    private var startMidY: Float = -1f
    private var endMidX: Float = -1f
    private var endMidY: Float = -1f

    fun verticalMidPoint(startMidX: Float, startMidY: Float, endMidX: Float, endMidY: Float) {
        this.startMidX = startMidX
        this.startMidY = startMidY
        this.endMidX = endMidX
        this.endMidY = endMidY
    }

    private var topMidX: Float = -1f
    private var topMidY: Float = -1f
    private var bottomMidX: Float = -1f
    private var bottomMidY: Float = -1f

    fun horizontalMidPoint(topMidX: Float, topMidY: Float, bottomMidX: Float, bottomMidY: Float) {
        this.topMidX = topMidX
        this.topMidY = topMidY
        this.bottomMidX = bottomMidX
        this.bottomMidY = bottomMidY
    }

    init {
        prepareInitialData()
    }

    private fun prepareInitialData() {
        startX = 5f
        startY = 5f
        endX = width - 5f
        endY = height - 5f

        startMidX = 0f
        startMidY = height / 2f
        endMidX = width.toFloat()
        endMidY = startMidY

        topMidX = width / 2f
        topMidY = 0f
        bottomMidX = topMidX
        bottomMidY = height.toFloat()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        prepareInitialData()

//        showLog("startX $startX, startY $startY, endX $endX, endY $endY")
//        showLog("draw")
        maskBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
//        // surrounding border line
//        canvas.drawRect(startX, startY, endX, endY, segmentPaint)
//        // mid vertical main line
//        canvas.drawLine(startMidX, startMidY, endMidX, endMidY, blueLinePaint)
//        // mid horizontal main line
//        canvas.drawLine(topMidX, topMidY, bottomMidX, bottomMidY, blueLinePaint)

        for (point in pointArray) {
            // draw shoulder point
            canvas.drawCircle(
                point.x,
                point.y,
                eraseBarSize.toFloat(),
                eraserLinePaint
            )
        }

//        for (line in lineArray) {
//            // draw shoulder point
//            canvas.drawLine(
//                line.pointA.x,
//                line.pointA.y,
//                line.pointB.x,
//                line.pointB.y,
//                eraserLinePaint
//            )
//        }

        if (seekBarProgress > 0) {
            canvas.drawCircle(
                width / 2f,
                height / 2f,
                seekBarProgress.toFloat(),
                eraserProgressPaint
            )
        }

//        showLog("seekBarProgress $seekBarProgress, height/2 ${height / 2f}, width/2 ${width / 2f}")
    }

    private var maskBitmap: Bitmap? = null

    fun setMaskBitmap(maskBitmap: Bitmap) {
        this.maskBitmap = maskBitmap
    }

    fun getMaskBitmap(): Bitmap? {
        return maskBitmap
    }

    private val pointArray: ArrayList<Point> = ArrayList()

    fun setPoints(pointArray: List<Point>) {
        this.pointArray.clear()
        this.pointArray.addAll(pointArray)
    }

    private val lineArray: ArrayList<Line> = ArrayList()

    fun setLines(lineArray: List<Line>) {
        this.lineArray.clear()
        this.lineArray.addAll(lineArray)
    }

    private var seekBarProgress = 0

    fun setSeekBarProgress(progress: Int) {
        seekBarProgress = progress

        invalidate()
    }

    fun setEraseBarSize(size: Int) {
        eraseBarSize = size
        eraserLinePaint.strokeWidth = eraseBarSize.toFloat()
    }

    private fun showLog(message: String = "Test message") {
        Log.v(TAG, message)
    }

    companion object {
        private const val TAG = "ImageEditOverlayView"
    }
}