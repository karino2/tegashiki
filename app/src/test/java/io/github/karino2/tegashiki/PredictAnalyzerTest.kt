package io.github.karino2.tegashiki

import org.junit.Test

import org.junit.Assert.*

class PredictAnalyzerTest {
    val target = PredictAnalyzer(Model.DECODER_END_TOKEN, Model.MAX_TOKEN_LEN)

    fun newDefault() = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)


    @Test
    fun next_normal() {
        target.init(newDefault())
        val nextRes = newDefault()
        nextRes[1] = 100
        target.next(nextRes)
        assertEquals(1, target.currentPos)
    }

    @Test
    fun next_normalTwice() {
        target.init(newDefault())
        val nextRes = newDefault()
        nextRes[1] = 100
        target.next(nextRes)
        nextRes[2] = 120
        target.next(nextRes)
        assertEquals(2, target.currentPos)
    }


    @Test
    fun next_sameUntilThird() {
        target.init(newDefault())
        val res = newDefault()
        res[3] = 100
        target.next(res)
        assertEquals(3, target.currentPos)
    }

    @Test
    fun isEnd_initialFalse() {
        target.init(newDefault())
        assertFalse(target.isEnd)
    }

    @Test
    fun isEnd_normalNextFalse() {
        target.init(newDefault())
        val nextRes = newDefault()
        nextRes[1] = 100
        target.next(nextRes)

        assertFalse(target.isEnd)
    }

    @Test
    fun isEnd_sameAsPrev_shouldTrue() {
        target.init(newDefault())
        val nextRes = newDefault()
        target.next(nextRes)

        assertTrue(target.isEnd)
    }
}