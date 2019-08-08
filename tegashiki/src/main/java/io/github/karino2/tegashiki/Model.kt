package io.github.karino2.tegashiki

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class Model(val assets: AssetManager) {
    companion object {
        const val MAX_STROKE_NUM=22
        const val MAX_ONE_STROKE_LEN=50
        const val INPUT_DIM=3
        const val MAX_TOKEN_LEN=12
        const val VOCAB_SIZE=115

        const val DECODER_START_TOKEN=113
        const val DECODER_END_TOKEN=114


        val id2sym = mapOf(
            1 to "\\geq ",
            2 to "\\sqrt ",
            3 to "\\leq ",
            4 to "/",
            5 to "\\infty ",
            6 to "(",
            7 to ",",
            8 to "0",
            9 to "\\cdot ",
            10 to "8",
            11 to "\\sigma ",
            12 to "<",
            13 to "\\pm ",
            14 to "\\log ",
            15 to "\\pi ",
            16 to "H",
            17 to "L",
            18 to "P",
            19 to "\\limits ",
            20 to "T",
            21 to "X",
            22 to "d",
            23 to "h",
            24 to "\\} ",
            25 to "l",
            26 to "p",
            27 to "t",
            28 to "\\tan ",
            29 to "x",
            30 to "|",
            31 to "\\gamma ",
            32 to "\\{",
            33 to "'",
            34 to "+",
            35 to "\\theta ",
            36 to "\\forall ",
            37 to "3",
            38 to "7",
            39 to "\\int ",
            40 to "\\sin ",
            41 to "\\prime ",
            42 to "C",
            43 to "G",
            44 to "\\ldots ",
            45 to "S",
            46 to "[",
            47 to "_",
            48 to "c",
            49 to "z",
            50 to "\\cdots ",
            51 to "g",
            52 to "k",
            53 to "o",
            54 to "s",
            55 to "w",
            56 to "\\cos ",
            57 to "{",
            58 to "\\Delta ",
            59 to "\\neq ",
            60 to "\\in ",
            61 to "\\alpha ",
            62 to "\\times ",
            63 to "\\lim ",
            64 to ".",
            65 to "2",
            66 to "6",
            67 to "\\lambda ",
            68 to "4",
            69 to "B",
            70 to "F",
            71 to "\\exists ",
            72 to "N",
            73 to "R",
            74 to "V",
            75 to "^",
            76 to "b",
            77 to "f",
            78 to "j",
            79 to "n",
            80 to "r",
            81 to "v",
            82 to ">",
            83 to "\\frac ",
            84 to "\\rightarrow ",
            85 to "\\div ",
            86 to "!",
            87 to "\\phi ",
            88 to ")",
            89 to "-",
            90 to "1",
            91 to "5",
            92 to "9",
            93 to "=",
            94 to "A",
            95 to "E",
            96 to "\\beta ",
            97 to "I",
            98 to "M",
            99 to "Y",
            100 to "]",
            101 to "\\mu ",
            102 to "a",
            103 to "e",
            104 to "i",
            105 to "\\sum ",
            106 to "m",
            107 to "q",
            108 to "u",
            109 to "y",
            110 to "}",
            111 to "\\to ",
            112 to "\\mathbb{R} ",
            113 to "<sos>",
            114 to "<eos>",
            0 to "<pad>"
        )
    }

    val modelFile: MappedByteBuffer by lazy {
        val afd = assets.openFd("tegashiki.tflite")
        val inputStream = FileInputStream(afd.fileDescriptor)
        val fc = inputStream.channel
        fc.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
    }


    val interpreter by lazy {
        Interpreter(modelFile, Interpreter.Options())
    }

   val inputStroke by lazy {
        KdTensor(1 * MAX_STROKE_NUM * MAX_ONE_STROKE_LEN * INPUT_DIM)
    }

    val inputDecoder by lazy {
        KdTensor(1 * MAX_TOKEN_LEN)
    }

    val predAnalyzer = PredictAnalyzer(DECODER_END_TOKEN, MAX_TOKEN_LEN)

    val outputTensor = KdFTensor(1 * MAX_TOKEN_LEN * VOCAB_SIZE).reshape(MAX_TOKEN_LEN, VOCAB_SIZE)
    val outputBuf = outputTensor.createMirrorBuf()

    fun predictInternal() : List<Int> {
        outputBuf.rewind()
        interpreter.runForMultipleInputsOutputs(arrayOf(inputDecoder.toByteBuf(), inputStroke.toByteBuf()), mapOf(0 to outputBuf))
        outputTensor.readToArray(outputBuf)
        return outputTensor.argMaxEachRaw.toList()
    }

    var requestCancel = false

    suspend fun predict(strokeList: FloatArray) : List<Int> {
        requestCancel = false

        setupInput(strokeList)

        repeat(MAX_TOKEN_LEN) {
            if(!predAnalyzer.isEnd && !requestCancel) {
                yield() // to check cancellation other than requestCancel.

                val oneres = withContext(Dispatchers.IO) {
                    predictInternal()
                }

                predAnalyzer.next(oneres)

                oneres.forEachIndexed { index, elem ->
                    if(index+1 < MAX_TOKEN_LEN)
                        inputDecoder.put(index + 1, elem)
                }
            }
        }

        return predAnalyzer.result
    }

    fun toSymbolText(ids: List<Int>) =  ids.map { id2sym[it] }.joinToString("")

    private fun setupInput(strokeList: FloatArray) {
        strokeList.forEachIndexed { index, fl -> inputStroke.buf[index] = fl.toInt() }

        inputDecoder.put(0, DECODER_START_TOKEN)
        predAnalyzer.init(inputDecoder.buf.toList().drop(1))
    }

}