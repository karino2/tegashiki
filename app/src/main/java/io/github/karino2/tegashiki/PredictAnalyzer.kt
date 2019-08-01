package io.github.karino2.tegashiki

import kotlin.math.min

class PredictAnalyzer(val EOS: Int, val maxSeqLen: Int) {
    var currentPos = -1
    val prevInput = ArrayList<Int>()

    val result: List<Int>
        get() {
            if(currentPos == -1)
                return emptyList()

            val eosPos = prevInput.indexOf(EOS)
            if(eosPos == -1)
                return prevInput.subList(0, currentPos+1)
            return prevInput.subList(0, min(eosPos, currentPos+1))
        }


    fun init(input: List<Int>) {
        currentPos = -1
        setupPrevInput(input)
    }

    private fun setupPrevInput(input: List<Int>) {
        prevInput.clear()
        prevInput.addAll(input)
    }

    // Last element must be EOS, and prevInput does not contain last input (because they might come from input[1:])
    val isEnd : Boolean
        get() = (currentPos != -1) && ((currentPos == maxSeqLen-2) || (prevInput[currentPos] == EOS))

    fun next(result: List<Int>) {
        currentPos++
        while(result[currentPos] == prevInput[currentPos]) {
            currentPos++
            if(currentPos == maxSeqLen-2)
                return
        }
        setupPrevInput(result)
    }

}