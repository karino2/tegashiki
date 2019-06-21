package io.github.karino2.tegashiki

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class StrokeCanvas(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var strokeListener : (List<Float>)->Unit = {_->}

    val xyList = ArrayList<Float>()


    private lateinit var offscreenBitmap : Bitmap
    private lateinit var offscreenCanvas: Canvas
    private val strokePath = Path()
    private val offscreenToScreenPaint = Paint(Paint.DITHER_FLAG)
    private val strokePaint = Paint()

    init {

        strokePaint.isAntiAlias = true
        strokePaint.isDither = true
        strokePaint.color = -0x1000000
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeJoin = Paint.Join.ROUND
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.strokeWidth = 3f

    }

    var currentWidth: Int = 0
    var currentHeight: Int = 0
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        currentWidth = w
        currentHeight = h
        resetCanvas(w, h)
    }

    fun resetCanvas() {
        resetCanvas(offscreenBitmap.width, offscreenBitmap.height)
        invalidate()
    }

    fun resetCanvas(w: Int, h: Int) {
        offscreenBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        offscreenCanvas = Canvas(offscreenBitmap)
        offscreenBitmap.eraseColor(Color.WHITE)
    }

    fun clearCanvas() {
        offscreenBitmap.eraseColor(Color.WHITE)
        undoList.clear()
        xyList.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(-0x1)

        canvas.drawBitmap(offscreenBitmap, 0f, 0f, offscreenToScreenPaint)

        canvas.drawPath(strokePath, strokePaint)
    }


    private var currentX = 0f
    private var currentY = 0f
    private val TOUCH_TOLERANCE = 4f
    private var left = -1f
    private var top = -1f
    private var right = -1f
    private var bottom = -1f

    internal inner class UndoList {

        var commandList = ArrayList<UndoCommand>()
        var currentPos = -1

        val COMMAND_MAX_SIZE = 1024 * 1024 // 1M

        fun clear() {
            currentPos = -1
            commandList.clear()
        }

        val commandsSize: Int
            get() {
                var res = 0
                for (cmd in commandList) {
                    res += cmd.size
                }
                return res
            }

        internal inner class UndoCommand(
            var x: Float,
            var y: Float,
            var undoBmp: Bitmap,
            var redoBmp: Bitmap,
            var effectiveUndo: RectF,
            var effectiveRedo: RectF
        ) {
            val size: Int
                get() = getBitmapSize(undoBmp) + getBitmapSize(redoBmp)

            fun undo(target: Canvas, outEffective: RectF) {
                target.drawBitmap(undoBmp, x, y, null)
                outEffective.set(effectiveUndo)
            }

            fun redo(target: Canvas, outEffective: RectF) {
                target.drawBitmap(redoBmp, x, y, null)
                outEffective.set(effectiveRedo)
            }

            fun getBitmapSize(bmp: Bitmap): Int {
                return 4 * bmp.width * bmp.height
            }

        }


        fun pushUndoCommand(x: Float, y: Float, undo: Bitmap, redo: Bitmap, effectiveUndo: RectF, effectiveRedo: RectF) {
            discardLaterCommand()
            commandList.add(UndoCommand(x, y, undo, redo, effectiveUndo, effectiveRedo))
            currentPos++
            discardUntilSizeFit()
        }

        fun discardLaterCommand() {
            for (i in commandList.size - 1 downTo currentPos + 1) {
                commandList.removeAt(i)
            }
        }

        private fun discardUntilSizeFit() {
            // currentPos ==0, then do not remove even though it bigger than threshold (I guess never happen, though).
            while (currentPos > 0 && commandsSize > COMMAND_MAX_SIZE) {
                commandList.removeAt(0)
                currentPos--
            }
        }

        fun canUndo(): Boolean {
            return currentPos >= 0
        }

        fun canRedo(): Boolean {
            return currentPos < commandList.size - 1
        }

        fun redo(target: Canvas, outEffective: RectF) {
            if (!canRedo())
                return
            currentPos++
            commandList[currentPos].redo(target, outEffective)
        }

        fun undo(target: Canvas, outEffective: RectF) {
            if (!canUndo())
                return
            commandList[currentPos].undo(target, outEffective)
            currentPos--
        }


    }

    private val undoList = UndoList()

    fun redo() {
        undoList.redo(offscreenCanvas, tempRegion)
        assignEffective(tempRegion)
        invalidate()
    }

    private fun assignEffective(rect: RectF) {
        if (rect.left < 0)
            return
        left = rect.left
        bottom = rect.bottom
        right = rect.right
        top = rect.top
    }

    fun undo() {
        undoList.undo(offscreenCanvas, tempRegion)
        assignEffective(tempRegion)
        invalidate()
    }

    fun fitInsideScreen(region: RectF) {
        region.intersect(0f, 0f, currentWidth.toFloat(), currentHeight.toFloat())
    }


    private fun pathBound(): RectF {
        strokePath.computeBounds(tempRegion, false)
        widen(tempRegion, 5)
        fitInsideScreen(tempRegion)
        return tempRegion
    }

    private fun widen(tmpInval: RectF, width: Int) {
        val newLeft = max(0f, tmpInval.left - width)
        val newTop = max(0f, tmpInval.top - width)
        val newRight = min(currentWidth.toFloat(), tmpInval.right + width)
        val newBottom = min(currentHeight.toFloat(), tmpInval.bottom + width)
        tmpInval.set(newLeft, newTop, newRight, newBottom)
    }


    var mDownHandled = false
    var tempRegion = RectF()

    fun ArrayList<Float>.addXY(x: Float, y:Float) {
        if(this.size == 0 || (this[this.size-1] != y || this[this.size-2] != x)) {
            this.add(x)
            this.add(y)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownHandled = true
                strokePath.reset()
                xyList.clear()
                strokePath.moveTo(x, y)
                xyList.addXY(x, y)

                currentX = x
                currentY = y
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mDownHandled)
                    return true
                val dx = abs(x - currentX)
                val dy = abs(y - currentY)
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    strokePath.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2)
                    currentX = x
                    currentY = y
                    xyList.addXY(x, y)
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (!mDownHandled)
                    return true
                mDownHandled = false

                val undoRegion = RectF(left, top, right, bottom)

                strokePath.lineTo(currentX, currentY)
                xyList.addXY(currentX, currentY)

                strokePath.computeBounds(tempRegion, true)
                updateEffectiveRegion(tempRegion)

                val redoRegion = RectF(left, top, right, bottom)

                val region = pathBound()
                val undo = Bitmap.createBitmap(offscreenBitmap, region.left.toInt(), region.top.toInt(), region.width().toInt(), region.height().toInt())
                offscreenCanvas.drawPath(strokePath, strokePaint)
                strokeListener(xyList)
                xyList.clear()

                val redo = Bitmap.createBitmap(offscreenBitmap, region.left.toInt(), region.top.toInt(), region.width().toInt(), region.height().toInt())

                undoList.pushUndoCommand(region.left, region.top, undo, redo, undoRegion, redoRegion)

                strokePath.reset()
                invalidate()
            }
        }
        return true
    }

    private fun updateEffectiveRegion(region: RectF) {
        if (region.width() == 0f)
            return
        updateEffectiveRegion(region.left, region.top)
        updateEffectiveRegion(region.right, region.bottom)
    }

    private fun updateEffectiveRegion(x: Float, y: Float) {
        if (left == -1f)
            left = x
        if (right == -1f)
            right = x
        if (top == -1f)
            top = y
        if (bottom == -1f)
            bottom = y

        left = min(left, x)
        right = max(right, x)
        top = min(top, y)
        bottom = max(bottom, y)
    }


}