package io.github.karino2.tegashiki

import java.nio.ByteBuffer
import java.nio.ByteOrder

// currently, for output only.
class KdFTensor(val size: Int) {
    val byteBuffer = ByteBuffer.allocateDirect(
        4*size).apply { order(ByteOrder.nativeOrder()) }

    val floatArray by lazy { FloatArray(size)}

    fun toFloatArray() :FloatArray {
        byteBuffer.rewind()
        repeat(size) {
            floatArray[it] = byteBuffer.float
        }
        return floatArray
    }

}