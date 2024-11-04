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

    private var blueLinePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 2f
        style = Paint.Style.STROKE
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
        style = Paint.Style.STROKE
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

    private var rectLinePaint = Paint().apply {
        color = Color.MAGENTA
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private var eraserPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    fun clear() {
        pointArray.clear()
        lineArray.clear()
        maskBitmap = null
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        maskBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        canvas.drawRect(firstX, firstY, lastX, lastY, rectLinePaint)

        // mid vertical main line
        canvas.drawLine(startMidX, startMidY, endMidX, endMidY, blueLinePaint)

        for (point in pointArray) {
            // draw shoulder point
            canvas.drawCircle(
                point.x,
                point.y,
                pointRadius,
                pointPaint
            )
        }
        for (line in lineArray) {
            // draw shoulder point
            canvas.drawLine(
                line.pointA.x,
                line.pointA.y,
                line.pointB.x,
                line.pointB.y,
                blueLinePaint
            )
        }

        if (seekBarProgress > 0) {
            canvas.drawCircle(
                height / 2f,
                width / 2f,
                seekBarProgress * 2f,
                eraserPaint
            )
        }
    }

    private var firstX = -1f
    private var firstY = -1f
    private var lastX = -1f
    private var lastY = -1f

    fun segmentRect(firstX: Float, firstY: Float, lastX: Float, lastY: Float) {
        this.firstX = firstX
        this.firstY = firstY
        this.lastX = lastX
        this.lastY = lastY
    }

    private var maskBitmap: Bitmap? = null

    fun setMaskBitmap(maskBitmap: Bitmap) {
        this.maskBitmap = maskBitmap
    }

    fun getMaskBitmap(): Bitmap? {
        return maskBitmap
    }

    private var startMidX: Float = -1f
    private var startMidY: Float = -1f
    private var endMidX: Float = -1f
    private var endMidY: Float = -1f

    fun midPoint(startMidX: Float, startMidY: Float, endMidX: Float, endMidY: Float) {
        this.startMidX = startMidX
        this.startMidY = startMidY
        this.endMidX = endMidX
        this.endMidY = endMidY
    }

    private val pointArray: ArrayList<Point> = ArrayList()

    fun setAllPoints(pointArray: ArrayList<Point>) {
        this.pointArray.clear()
        this.pointArray.addAll(pointArray)
    }

    private val lineArray: ArrayList<Line> = ArrayList()

    fun setLines(lineArray: ArrayList<Line>) {
        this.lineArray.clear()
        this.lineArray.addAll(lineArray)
    }

    private fun showLog(message: String = "Test message") {
        Log.v(TAG, message)
    }

    private var seekBarProgress = 0
    fun setSeekBarProgress(progress: Int) {
        seekBarProgress = progress

        invalidate()
    }

    companion object {
        private const val TAG = "NewOverlayView"
    }
}