package io.github.karino2.tegashiki

import android.util.Log
import kotlin.math.min

class StrokeTracker(val width: Int, val outputTensor: KdFTensor) {
    val NORMALIZE_MAX = 2000
    val INPUT_TYPE_POS=1f

    var curIndex = 0
    val nonNormalizeTensor = KdFTensor(outputTensor.shape)



    fun addStroke(xylistInput: List<Float>) {
        val xylist = reduceIfNecessary(xylistInput)
        tensor_ns {
            val one = tensor(xylist).reshape(-1, 2)
            val len = one.shape[0]
            nonNormalizeTensor[n(curIndex), r(0, len), r(0, 2)] = one
            nonNormalizeTensor[n(curIndex), r(0, len), n(2)] = INPUT_TYPE_POS

            val nonzeroMask = nonNormalizeTensor[all, all, n(2)].scalar_equal(INPUT_TYPE_POS)
            val nonzero = nonNormalizeTensor[nonzeroMask]
            val xmax = nonzero[all, n(0)].max()
            val xmin = nonzero[all, n(0)].min()
            val ymax = nonzero[all, n(1)].max()
            val ymin = nonzero[all, n(1)].min()
            val xdelta = xmax-xmin+0.0001f
            val ydelta = ymax-ymin+0.0001f
            val scale = min(NORMALIZE_MAX.toFloat()/xdelta, NORMALIZE_MAX.toFloat()/ydelta)

            repeat(curIndex+1) {
                val rowMask = nonNormalizeTensor[n(it), all, n(2)].scalar_equal(INPUT_TYPE_POS)
                val rowXY = nonNormalizeTensor[n(it), all, all][rowMask]
                val rowLen = rowXY.shape[0]
                val originTensorX = rowXY[all, n(0)] - xmin
                val originTensorY = rowXY[all, n(1)]-ymin
                outputTensor[n(it), r(0, rowLen), n(0)] = originTensorX*scale
                outputTensor[n(it), r(0, rowLen), n(1)] = originTensorY*scale
            }
            outputTensor[n(curIndex), r(0, len), n(2)] = INPUT_TYPE_POS
        }
        curIndex++
    }

    private fun reduceIfNecessary(xylistInput: List<Float>): List<Float> {
        if(xylistInput.size < 2*Model.MAX_ONE_STROKE_LEN)
            return xylistInput

        // always divide by 2
        val posNum = xylistInput.size/2

        val ratio = 2*posNum/Model.MAX_ONE_STROKE_LEN.toDouble()

        val step = (ratio+0.5).toInt()
        val newSize = posNum/step

        val res = sequence {
                repeat(newSize) {
                    yield(xylistInput[2 * it * step])
                    yield(xylistInput[2 * it * step + 1])
                }
        }.toList()
        Log.d("Tegashiki", "Reduce. original(${xylistInput.size}), new(${res.size})")
        return res
    }

    fun clear() {
        curIndex = 0
        tensor_ns {
            outputTensor[all, all, all] = 0f
            nonNormalizeTensor[all, all, all] = 0f
        }
    }



}