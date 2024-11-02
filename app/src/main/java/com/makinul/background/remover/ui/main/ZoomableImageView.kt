package com.makinul.background.remover.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs),
    OnTouchListener, OnGestureListener, OnDoubleTapListener,
    ScaleGestureDetector.OnScaleGestureListener {

    private val mScaleDetector: ScaleGestureDetector = ScaleGestureDetector(context, this)
    private val mMatrix: Matrix = Matrix()
    private val mMatrixValues: FloatArray = FloatArray(9)
    private val mGestureDetector: GestureDetector = GestureDetector(context, this)

    private val mActivePointers: SparseArray<PointF> = SparseArray()

    init {
        super.setClickable(true)

        imageMatrix = mMatrix
        scaleType = ScaleType.MATRIX

        setOnTouchListener(this)
    }

    private var viewWidth = 0
    private var viewHeight = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)

        if (mSaveScale == 1f) {
            // Fit to screen.
            fitToScreen()
        }
    }

    private var mode = NONE

    // Scales
    private var mSaveScale = 1f
    private var mMinScale = 1f
    private var mMaxScale = 4f

    // view dimensions
    private var mLast = PointF()
    private var mStart = PointF()

    private fun fitToScreen() {
        mSaveScale = 1f
        val scale: Float
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0)
            return

        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight

        val scaleX = viewWidth.toFloat() / imageWidth.toFloat()
        val scaleY = viewHeight.toFloat() / imageHeight.toFloat()

        scale = scaleX.coerceAtMost(scaleY)

        mMatrix.setScale(scale, scale)

        // Center the image
        val redundantYSpace = (viewHeight.toFloat() - (scale * imageHeight.toFloat())) / 2f
        val redundantXSpace = (viewWidth.toFloat() - (scale * imageWidth.toFloat())) / 2f

        mMatrix.postTranslate(redundantXSpace, redundantYSpace)

        origWidth = viewWidth - (2 * redundantXSpace)
        origHeight = viewHeight - (2 * redundantYSpace)

        imageMatrix = mMatrix
    }

    private var origWidth = 0f
    private var origHeight = 0f

    private fun fixTranslation() {
        mMatrix.getValues(mMatrixValues) //put matrix values into a float array so we can analyze

        val transX = mMatrixValues[Matrix.MTRANS_X] //get the most recent translation in x direction
        val transY = mMatrixValues[Matrix.MTRANS_Y] //get the most recent translation in y direction

        val fixTransX = getFixTranslation(transX, viewWidth.toFloat(), origWidth * mSaveScale)
        val fixTransY = getFixTranslation(transY, viewHeight.toFloat(), origHeight * mSaveScale)

        if (fixTransX != 0f || fixTransY != 0f)
            mMatrix.postTranslate(fixTransX, fixTransY)
    }

    private fun getFixTranslation(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) { // case: NOT ZOOMED
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else { //CASE: ZOOMED
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }
        if (trans < minTrans) { // negative x or y translation (down or to the right)
            return -trans + minTrans
        }
        if (trans > maxTrans) { // positive x or y translation (up or to the left)
            return -trans + maxTrans
        }
        return 0F
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) {
            0F
        } else delta
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v == null || event == null)
            return true

        mScaleDetector.onTouchEvent(event)
        mGestureDetector.onTouchEvent(event)

        val pointerIndex = event.actionIndex
        // get pointer ID
        val pointerId = event.getPointerId(pointerIndex)
        val maskedAction = event.actionMasked
        val eventAction = event.action

        val currentPoint = PointF(event.x, event.y)

        showLog("maskedAction $maskedAction, eventAction: $eventAction")
        // get masked (not specific to a pointer) action
        when (maskedAction) {
            MotionEvent.ACTION_DOWN -> {
//                // We have a new pointer. Lets add it to the list of pointers
//                val f = PointF()
//                f.x = event.getX(pointerIndex)
//                f.y = event.getY(pointerIndex)
//                mActivePointers!!.put(pointerId, f)
//                showLog("mActivePointers!!.put(pointerId, f) $maskedAction")

                mActivePointers.clear()
                mActivePointers.put(pointerId, currentPoint)
                mLast.set(currentPoint)
                mStart.set(mLast)
                mode = EDIT
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = DRAG
            }

            MotionEvent.ACTION_MOVE -> {
                if (mode == EDIT) {

                    // a pointer was moved
                    val size = event.pointerCount
                    var i = 0
                    while (i < size) {
                        val point = mActivePointers[event.getPointerId(i)]
                        if (point != null) {
                            point.x = event.getX(i)
                            point.y = event.getY(i)
                        }
                        i++
                    }
                } else if (mode == DRAG) {
                    val dx = currentPoint.x - mLast.x
                    val dy = currentPoint.y - mLast.y
                    val fixTransX = getFixDragTrans(dx, viewWidth.toFloat(), origWidth * mSaveScale)
                    val fixTransY =
                        getFixDragTrans(dy, viewHeight.toFloat(), origHeight * mSaveScale)
                    mMatrix.postTranslate(fixTransX, fixTransY)
                    fixTranslation()
                    mLast[currentPoint.x] = currentPoint.y
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
//                mActivePointers!!.remove(pointerId)
//                showLog("mActivePointers!!.remove(pointerId) $maskedAction")
                mode = NONE
            }
        }

//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                mLast.set(currentPoint)
//                mStart.set(mLast)
//                mode = DRAG
//            }
//
//            MotionEvent.ACTION_MOVE -> {
//                if (mode == DRAG) {
//                    val dx = currentPoint.x - mLast.x
//                    val dy = currentPoint.y - mLast.y
//                    val fixTransX = getFixDragTrans(dx, viewWidth.toFloat(), origWidth * mSaveScale)
//                    val fixTransY =
//                        getFixDragTrans(dy, viewHeight.toFloat(), origHeight * mSaveScale)
//                    mMatrix.postTranslate(fixTransX, fixTransY)
//                    fixTranslation()
//                    mLast[currentPoint.x] = currentPoint.y
//                }
//            }
//
//            MotionEvent.ACTION_POINTER_UP -> {
//                mode = NONE
//            }
//        }
        imageMatrix = mMatrix
        return false
    }

    /*
        onDoubleTap
     */
    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        showLog("onSingleTapConfirmed")
        return false
    }

    override fun onDoubleTap(motionEvent: MotionEvent): Boolean {
        showLog("onDoubleTap")
        fitToScreen()
        return false
    }

    override fun onDoubleTapEvent(motionEvent: MotionEvent): Boolean {
        showLog("onDoubleTapEvent")
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        showLog("onScaleBegin")
        mode = ZOOM
        return true
//        return super.onScaleBegin(detector)
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        showLog("onScale")
        var mScaleFactor = detector.scaleFactor
        val prevScale = mSaveScale
        mSaveScale *= mScaleFactor
        if (mSaveScale > mMaxScale) {
            mSaveScale = mMaxScale
            mScaleFactor = mMaxScale / prevScale
        } else if (mSaveScale < mMinScale) {
            mSaveScale = mMinScale
            mScaleFactor = mMinScale / prevScale
        }
        if (origWidth * mSaveScale <= viewWidth
            || origHeight * mSaveScale <= viewHeight
        ) {
            mMatrix.postScale(
                mScaleFactor, mScaleFactor, viewWidth / 2.toFloat(),
                viewHeight / 2.toFloat()
            )
        } else {
            mMatrix.postScale(
                mScaleFactor, mScaleFactor,
                detector.focusX, detector.focusY
            )
        }
        fixTranslation()
        return true
//        return super.onScale(detector)
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        showLog("onScaleEnd")
    }

    override fun onDown(motionEvent: MotionEvent): Boolean {
        showLog("onDown")
        return false
    }

    override fun onShowPress(motionEvent: MotionEvent) {
        showLog("onShowPress")
    }

    override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
        showLog("onSingleTapUp")
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        showLog("onScroll")
        return false
    }

    override fun onLongPress(motionEvent: MotionEvent) {
        showLog("onLongPress")
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        showLog("onFling")
        return false
    }

    private fun showLog(message: String = "Test message") {
        Log.v(TAG, message)
    }

    companion object {
        private const val TAG = "MyImageView3"

        // Image States
        const val NONE = 0
        const val EDIT = 1
        const val DRAG = 2
        const val ZOOM = 3
    }
}