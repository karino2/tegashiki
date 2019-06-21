package io.github.karino2.tegashiki

import java.nio.ByteBuffer
import java.nio.ByteOrder

// only support [x1, y1, x1, x2, y2, z3, x3, y3, z3...] order.
class KdFTensor(val size: Int) {
    companion object {
        fun arange(end: Int) : KdFTensor {
            val res = KdFTensor(end)
            repeat(end) { res.floatArray[it] = it.toFloat() }
            return res
        }

        fun zeros(size: Int) = KdFTensor(size)
    }

    constructor(values: List<Float>) : this(values.size){
        values.forEachIndexed { index, fl -> this.floatArray[index] = fl }
    }

    fun readToArray(byteBuf: ByteBuffer) {
        byteBuf.rewind()
        repeat(size) {
            floatArray[it] = byteBuf.float
        }
    }

    fun createMirrorBuf() : ByteBuffer = ByteBuffer.allocateDirect(4*size).apply { order(ByteOrder.nativeOrder()) }

    val floatArray by lazy { FloatArray(size)}

    // element wize multiply must be match to size.
    // default [size].
    var shape = Shape(size)

    fun reshape(vararg shapes: Int) : KdFTensor{
        val newShape = Shape(*shapes)
        assert(shape.elementNum == newShape.elementNum)
        shape = newShape
        return this
    }

    fun subTensor(indices: Indices) :KdFTensor {
        val values = indices.indices.map { floatArray[it] }
        val res = KdFTensor(values)
        res.shape = indices.shape
        return res
    }

    operator fun get(vararg ranges: ShapeIndex) : KdFTensor {
        val indices = shape.toIndices(*ranges)
        return subTensor(indices)
    }

    operator fun get(vararg ranges: Int) : Float {
        val shapeArgs = ranges.map { NumberIndex(it) }
        val indices = shape.toIndices(*shapeArgs.toTypedArray())
        return floatArray[indices.indices[0]]
    }

    val rowSize
    get() = shape[1]

    val rowNum
    get() = size/rowSize

    // I assume shape is [seqnum, vocabsize] for a while.
    fun column(colIdx: Int) = sequence {
        repeat(rowNum) {ridx->
            yield(floatArray[ridx*rowSize+colIdx])
        }
    }.toList()

    fun row(rowIdx: Int) = sequence {
        val rowOffset = rowIdx*rowSize
        repeat(rowSize) { yield(floatArray[rowOffset+it]) }

    }.toList()

    val rows
    get() = sequence {
        repeat(rowNum) {
            yield(row(it))
        }
    }

    val List<Float>.argMax : Int
    get() = this.indices.fold(0) {bestIdx, cur->
            if(this[bestIdx] < this[cur]) cur else bestIdx
    }

    // I assume shape is [seqnum, vocabsize] for a while.
    // return [seqnum] array.
    val argMaxEachRaw
    get() = rows.map { it.argMax }


}