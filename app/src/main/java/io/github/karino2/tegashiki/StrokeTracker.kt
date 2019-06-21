package io.github.karino2.tegashiki

class StrokeTracker(val width: Int, val outputTensor: KdFTensor) {
    val NORMALIZE_MAX = 2000
    val scale = NORMALIZE_MAX.toFloat()/width.toFloat()
    val INPUT_TYPE_POS=1f

    var curIndex = 0

    fun addStroke(xylist: List<Float>) {
        tensor_ns {
            val one = tensor(xylist).reshape(-1, 2)
            val len = one.shape[0]
            outputTensor[n(curIndex), r(0, len), r(0, 2)] = one*scale
            outputTensor[n(curIndex), r(0, len), n(2)] = INPUT_TYPE_POS
        }
        curIndex++
    }

    fun clear() {
        curIndex = 0
        tensor_ns {
            outputTensor[all, all, all] = 0f
        }
    }



}