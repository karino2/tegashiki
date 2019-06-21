package io.github.karino2.tegashiki

sealed class ShapeIndex

data class NumberIndex(val number: Int) : ShapeIndex()
data class RangeIndex(val begin: Int, val end: Int) : ShapeIndex() {
    val sequence
        get() = (begin until end).toList()

    val size
        get() = end-begin
}
object AllIndex : ShapeIndex()


class Shape(vararg val shape: Int) {
    operator fun get(i : Int) = shape[i]

    val size = shape.size
    
    val elementNum
    get() = shape.fold(1, {acc, cur-> acc*cur})

    override operator fun equals(other: Any?) : Boolean {
        return when(other) {
            is Shape -> {
                shape.contentEquals(other.shape)
            }
            else -> false
        }
    }

    fun toIndices(vararg ranges: ShapeIndex) : Indices {
        assert(ranges.size == shape.size)

        val res = ArrayList<Int>()
        fun buildRes(curIdx: Int, start:Int) {
            val isLast = curIdx == shape.size
            val one :(Int, Int)->Unit =
                if(curIdx == shape.size-1) {_:Int,cur:Int -> res.add(cur) }
                else {cur:Int, start:Int-> buildRes(cur, start) }
            val curStart = start*shape[curIdx]

            when(val curRange = ranges[curIdx]) {
                is AllIndex -> {
                    repeat(shape[curIdx]) {
                        one(curIdx+1, curStart+it)
                    }
                }
                is RangeIndex -> {
                    curRange.sequence.forEach {
                        one(curIdx+1, curStart+it)
                    }
                }
                is NumberIndex -> {
                    one(curIdx+1, curStart+curRange.number)
                }
            }

        }
        buildRes(0, 0)
        val resShape = ArrayList<Int>()
        ranges.forEachIndexed { index, shapeIndex ->
            when(shapeIndex) {
                is AllIndex ->
                    resShape.add(shape[index])
                is RangeIndex ->
                    resShape.add(shapeIndex.size)
                is NumberIndex ->
                    // squeeze 1 dimension
                    Unit
                // resShape.add(1)
            }
        }

        // only one dimension, return [1] shape.
        if(resShape.size == 0) resShape.add(1)

        return Indices(Shape(*resShape.toIntArray()), res)
    }
}

data class Indices(val shape: Shape, val indices: List<Int>)