package io.github.karino2.tegashiki

import java.nio.ByteBuffer
import java.nio.ByteOrder

// currently, for output only.
class KdFTensor(val size: Int) {
    val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(
        4*size).apply { order(ByteOrder.nativeOrder()) }

    val floatArray by lazy { FloatArray(size)}

    fun toFloatArray() :FloatArray {
        readToArray()
        return floatArray
    }

    // element wize multply must be match to size.
    // default [size].
    var shape = arrayOf(size)


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

    fun readToArray() {
        byteBuffer.rewind()
        repeat(size) {
            floatArray[it] = byteBuffer.float
        }
    }

}