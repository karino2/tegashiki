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

    @Test
    fun reshape_guess() {
        val target = KdFTensor.arange(10).reshape(-1, 2)

        assertEquals(Shape(5, 2), target.shape)
    }

    @Test
    fun set_partial() {
        val target = KdFTensor.arange(10).reshape(5, 2)
        val input = KdFTensor((35..40).map { it.toFloat() }.toList()).reshape(3, 2)

        target[r(1, 4), all] = input
        assertEquals(0f, target[0, 0])
        assertEquals(1f, target[0, 1])
        assertEquals(35f, target[1, 0])
        assertEquals(36f, target[1, 1])
        assertEquals(40f, target[3, 1])
        assertEquals(8f, target[4, 0])

    }

    @Test
    fun times_test() {
        val target = KdFTensor.arange(4).reshape(2, 2)
        val actual = target*2f
        assertEquals(0f, actual[0, 0])
        assertEquals(2f, actual[0, 1])
        assertEquals(4f, actual[1, 0])
        assertEquals(6f, actual[1, 1])
    }
    @Test
    fun clear_test() {
        val target = KdFTensor.arange(12).reshape(4, 3)
        target[all, all] = 0f

        assertEquals(0f, target[3, 2])
    }
}