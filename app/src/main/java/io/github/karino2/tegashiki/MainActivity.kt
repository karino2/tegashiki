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


    val DECODER_INPUT = listOf(112,  76,  75,  76, 113)
    val DECODER_INPUT_INIT = listOf(112,  0, 0, 0, 0)

    val STROKE_TEXT="""
        [[[94, 1155, 1], [147, 1145, 1], [207, 1151, 1], [273, 1179, 1], [340, 1218, 1], [407, 1275, 1], [467, 1349, 1], [510, 1425, 1], [543, 1508, 1], [564, 1588, 1], [573, 1667, 1], [567, 1738, 1], [550, 1798, 1], [530, 1848, 1], [501, 1895, 1], [467, 1928, 1], [427, 1961, 1], [384, 1981, 1], [340, 1998, 1], [297, 2004, 1], [257, 2008, 1], [221, 2008, 1], [188, 2004, 1], [157, 2000, 1], [134, 1991, 1], [114, 1987, 1], [101, 1974, 1], [94, 1967, 1], [88, 1951, 1], [84, 1941, 1], [88, 1928, 1], [84, 1908, 1], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[7, 263, 1], [11, 276, 1], [14, 319, 1], [18, 382, 1], [21, 472, 1], [24, 579, 1], [24, 709, 1], [27, 842, 1], [27, 981, 1], [24, 1114, 1], [21, 1238, 1], [21, 1349, 1], [18, 1451, 1], [14, 1534, 1], [11, 1608, 1], [7, 1667, 1], [7, 1715, 1], [11, 1754, 1], [14, 1782, 1], [21, 1788, 1], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[681, 8, 1], [667, 26, 1], [615, 520, 1], [615, 516, 1], [615, 511, 1], [615, 492, 1], [615, 483, 1], [620, 446, 1], [629, 427, 1], [653, 395, 1], [662, 381, 1], [699, 353, 1], [709, 348, 1], [755, 348, 1], [765, 348, 1], [806, 353, 1], [816, 357, 1], [839, 390, 1], [839, 404, 1], [839, 436, 1], [834, 450, 1], [802, 483, 1], [788, 492, 1], [741, 516, 1], [727, 520, 1], [681, 530, 1], [667, 530, 1], [643, 525, 1], [639, 520, 1], [629, 506, 1], [629, 502, 1], [629, 497, 1], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]], [[0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0], [0, 0, 0]]]
    """.trimIndent()

    val DECODER_START_TOKEN=112
    val DECODER_END_TOKEN=113

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





    private fun predictHardCoard() {
        DECODER_INPUT.forEachIndexed { index, i -> model.inputDecoder.put(index, i) }

        val parser = JsonParser()
        val obj = parser.parse(STROKE_TEXT) as JsonArray
        obj.forEachIndexed { strokeIndex, jsonElemOneStroke ->
            val stroke = jsonElemOneStroke as JsonArray
            val strokeOffset = strokeIndex * Model.MAX_ONE_STROKE_LEN * Model.INPUT_DIM
            stroke.forEachIndexed { posIndex, jsonElemOnePosArray ->
                val pos = jsonElemOnePosArray as JsonArray
                model.inputStroke.put(strokeOffset + posIndex * Model.INPUT_DIM, pos[0].asInt)
                model.inputStroke.put(strokeOffset + posIndex * Model.INPUT_DIM + 1, pos[1].asInt)
                model.inputStroke.put(strokeOffset + posIndex * Model.INPUT_DIM + 2, pos[2].asInt)
            }
        }

        val res = model.predict()
        Log.d("Tegashiki", res.toString())
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

            model.inputDecoder.put(0, DECODER_START_TOKEN)


            val res = ArrayList<Int>().apply {addAll(0 until Model.MAX_TOKEN_LEN)}

            var endReached = false
            val buf = StringBuilder()

            repeat(Model.MAX_TOKEN_LEN) {
                if(!endReached) {
                    val oneres = model.predict()
                    Log.d("Tegashiki", "$it - ${oneres.toString()}")
                    Log.d("Tegashiki", "$it - dec- ${model.inputDecoder.buf.toList().toString()}")
                    res[it] = oneres[it]
                    if (oneres[it] == DECODER_END_TOKEN)
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
