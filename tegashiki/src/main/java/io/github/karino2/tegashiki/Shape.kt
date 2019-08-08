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


class Shape(vararg val dimArray: Int) {
    operator fun get(i : Int) = dimArray[i]

    fun clone() : Shape {
        return Shape(*dimArray)
    }

    val size = dimArray.size

    val elementNum
    get() = dimArray.fold(1, { acc, cur-> acc*cur})

    override operator fun equals(other: Any?) : Boolean {
        return when(other) {
            is Shape -> {
                dimArray.contentEquals(other.dimArray)
            }
            else -> false
        }
    }

    fun toIndex(vararg poses: Int) : Int {
        val rowOffsets = IntArray(dimArray.size).apply { fill(1) }
        repeat(dimArray.size-1) {reverseCol->
            val col = dimArray.size - reverseCol-1
            repeat(col) {
                rowOffsets[it] *= dimArray[col]
            }
        }

        return poses.foldIndexed(0) {index, acc, pos->
            acc+pos*rowOffsets[index]
        }
    }

    fun toIndices(vararg ranges: ShapeIndex) : Indices {
        myassert(ranges.size == dimArray.size)

        val res = ArrayList<Int>()

        fun buildRes(curIdx: Int, start:Int) {
            val one :(Int, Int)->Unit =
                if(curIdx == dimArray.size-1) { _:Int, cur:Int -> res.add(cur) }
                else {cur:Int, start2:Int-> buildRes(cur, start2) }
            val curStart = start*dimArray[curIdx]

            when(val curRange = ranges[curIdx]) {
                is AllIndex -> {
                    repeat(dimArray[curIdx]) {
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
                    resShape.add(dimArray[index])
                is RangeIndex ->
                    resShape.add(shapeIndex.size)
                is NumberIndex ->
                    // squeeze 1 dimension
                    Unit
                // resShape.add(1)
            }
        }

        // only one dimension, return [1] dimArray.
        if(resShape.size == 0) resShape.add(1)

        return Indices(
            Shape(*resShape.toIntArray()),
            res
        )
    }
}

data class Indices(val shape: Shape, val indices: List<Int>)