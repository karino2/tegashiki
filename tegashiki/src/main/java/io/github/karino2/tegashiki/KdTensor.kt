package io.github.karino2.tegashiki

import java.nio.ByteBuffer
import java.nio.ByteOrder

// Fixed size with Int only for a while
class KdTensor(val size: Int) {
    val buf = IntArray(size)

    fun toByteBuf() : ByteBuffer {
        byteBuffer.rewind()
        buf.forEach { i ->
            byteBuffer.putInt(i)
        }
        return byteBuffer
    }

    fun put(index:Int, value: Int) { buf[index] = value }

    // assume Int32
    val byteBuffer: ByteBuffer by lazy {
        val buf = ByteBuffer.allocateDirect(
            4*size)
        buf.order(ByteOrder.nativeOrder())
        buf
    }
}