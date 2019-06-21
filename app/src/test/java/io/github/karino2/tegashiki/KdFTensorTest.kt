package io.github.karino2.tegashiki

import org.junit.Test

import org.junit.Assert.*


class KdFTensorTest {
    @Test
    fun get_intarg() {
        val target = KdFTensor.arange(10).reshape(2, 5)

        assertEquals(0.0F, target[0, 0])
        assertEquals(3.0F, target[0, 3])
        assertEquals(9.0F, target[1, 4])
    }

    @Test
    fun subTensor() {
        val target = KdFTensor.arange(30).reshape(2, 3, 5)
        val sub = target[n(1), r(0, 2), r(1, 4)]

        assertEquals(Shape(2, 3), sub.shape)
        assertEquals(16.0F, sub[0, 0])
        assertEquals(17.0F, sub[0, 1])
        assertEquals(18.0F, sub[0, 2])
        assertEquals(21.0F, sub[1, 0])
        assertEquals(22.0F, sub[1, 1])
        assertEquals(23.0F, sub[1, 2])
    }
}