package io.github.karino2.tegashiki

import android.content.res.AssetManager
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class Model(val assets: AssetManager) {
    companion object {
        const val MAX_STROKE_NUM=13
        const val MAX_ONE_STROKE_LEN=50
        const val INPUT_DIM=3
        const val MAX_TOKEN_LEN=5
        const val VOCAB_SIZE=114
    }

    val modelFile: MappedByteBuffer by lazy {
        // from convencdec_myembed_small.tflite
        val afd = assets.openFd("convencdec.tflite")
        val inputStream = FileInputStream(afd.fileDescriptor)
        val fc = inputStream.channel
        fc.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
    }


    val interpreter by lazy {
        Interpreter(modelFile, Interpreter.Options())
    }

   val inputStroke by lazy {
        KdTensor(1* Model.MAX_STROKE_NUM * Model.MAX_ONE_STROKE_LEN * Model.INPUT_DIM)
    }

    val inputDecoder by lazy {
        KdTensor(1*MAX_TOKEN_LEN)
    }


    val outputTensor = KdFTensor(1*MAX_TOKEN_LEN*VOCAB_SIZE).reshape(MAX_TOKEN_LEN, VOCAB_SIZE)
    val outputBuf = outputTensor.createMirrorBuf()


    fun predict() : List<Int> {
        outputBuf.rewind()
        interpreter.runForMultipleInputsOutputs(arrayOf(inputDecoder.toByteBuf(), inputStroke.toByteBuf()), mapOf(0 to outputBuf))
        outputTensor.readToArray(outputBuf)
        return outputTensor.argMaxEachRaw.toList()
    }

}