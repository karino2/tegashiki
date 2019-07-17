package io.github.karino2.tegashiki

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import java.io.File
import java.io.IOException
import com.google.gson.stream.JsonWriter
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    companion object {
        fun ensureDirExist(dir: File) {
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    throw IOException()
                }
            }
        }

        fun getStoreDirectory(): File {
            // getExternalStoragePublicDirectory
            val dir = File(Environment.getExternalStorageDirectory(), "Tegashiki")!!
            // val dir = getExternalFilesDir("Tegashiki")!!
            ensureDirExist(dir)
            return dir
        }

        fun createStoreFile() : File {
            val fname = createFileName()
            return File(getStoreDirectory(), fname)
        }

        fun createFileName(): String {
            val timeStampFormat = SimpleDateFormat("yyyyMMdd_HHmmssSS")
            return timeStampFormat.format(Date()) + ".json"
        }

    }

    val model by lazy { Model(assets) }


    val DECODER_INPUT_INIT = listOf(Model.DECODER_START_TOKEN,  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    val strokeCanvas by lazy { findViewById<StrokeCanvas>(R.id.canvas)!! }


    val strokeFloatTensor = KdFTensor(1* Model.MAX_STROKE_NUM * Model.MAX_ONE_STROKE_LEN * Model.INPUT_DIM).reshape(Model.MAX_STROKE_NUM, Model.MAX_ONE_STROKE_LEN, Model.INPUT_DIM)


    val strokeTracker by lazy {
        StrokeTracker(strokeFloatTensor)
    }

    fun onDebugButtonClick(v: View) {
        // predictHardCoard()
        dumpInput()
    }

    fun dumpInput() {
        val writer = JsonWriter(FileWriter(createStoreFile()))
        writer.beginArray()

        writer.beginArray()
        rawPosListStore.forEach {
            writer.beginArray()
            it.forEach {
                writer.value(it)
            }
            writer.endArray()
        }
        writer.endArray()

        writer.beginArray()
        strokeFloatTensor.floatArray.forEach {
            writer.value(it)
        }
        writer.endArray()

        writer.endArray()
        writer.close()
    }






    fun onClearClick(v: View) {
        strokeCanvas.clearCanvas()
        strokeTracker.clear()
        DECODER_INPUT_INIT.forEachIndexed { index, i ->  model.inputDecoder.put(index, i) }
        model.inputStroke.buf.fill(0)
        resultTextView.text = ""
        rawPosListStore.clear()
    }

    val rawPosListStore = ArrayList<List<Float>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DECODER_INPUT_INIT.forEachIndexed { index, i ->  model.inputDecoder.put(index, i) }

        strokeCanvas.strokeListener = {one ->
            rawPosListStore.add(mutableListOf<Float>().apply { addAll(one) })
            strokeTracker.addStroke(one)
            strokeFloatTensor.floatArray.forEachIndexed { index, fl -> model.inputStroke.buf[index] = fl.toInt() }

            repeat(Model.MAX_TOKEN_LEN) {
                model.inputDecoder.put(it, 0)
            }
            model.inputDecoder.put(0, Model.DECODER_START_TOKEN)


            val res = ArrayList<Int>().apply {addAll(0 until Model.MAX_TOKEN_LEN)}

            var endReached = false
            val buf = StringBuilder()

            repeat(Model.MAX_TOKEN_LEN) {
                if(!endReached) {
                    val oneres = model.predict()
                    Log.d("Tegashiki", "$it - inputdec- ${model.inputDecoder.buf.toList().toString()}")
                    Log.d("Tegashiki", "$it - ${oneres.toString()}")
                    res[it] = oneres[it]
                    if (oneres[it] == Model.DECODER_END_TOKEN)
                        endReached = true
                    if (it != Model.MAX_TOKEN_LEN - 1)
                        model.inputDecoder.put(it + 1, oneres[it])
                    buf.append(Model.id2sym[oneres[it]])
                }
            }
            Log.d("Tegashiki", "final - ${res.toString()}")
            resultTextView.text = buf.toString()
        }
    }

    val resultTextView by lazy { findViewById<TextView>(R.id.textViewResult) }
}
