package io.github.karino2.tegashiki

import android.content.ClipData
import android.content.ClipboardManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.IOException
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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


    val strokeCanvas by lazy { findViewById<StrokeCanvas>(R.id.canvas)!! }


    val strokeFloatTensor = KdFTensor(1* Model.MAX_STROKE_NUM * Model.MAX_ONE_STROKE_LEN * Model.INPUT_DIM).reshape(Model.MAX_STROKE_NUM, Model.MAX_ONE_STROKE_LEN, Model.INPUT_DIM)


    val strokeTracker by lazy {
        StrokeTracker(strokeFloatTensor)
    }

    fun onCopyButtonClick(v: View) {
        copyToClipboard("\$\$${resultTextView.text.toString()}\$\$")
    }

    fun onUndoButtonClick(v: View) {
        strokeCanvas.undo()
        undo()
    }

    private fun copyToClipboard(content: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("math", content)
        clipboard.setPrimaryClip(clip)
        showMessage("TeX copied to clipboard")
    }

    fun showMessage(msg : String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    fun onDumpMenuItemClick(item: MenuItem) {
        dumpInput()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
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
        model.inputStroke.buf.fill(0)
        resultTextView.text = ""
        rawPosListStore.clear()
    }

    val rawPosListStore = ArrayList<List<Float>>()

    val mainScope = MainScope()
    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }

    val channel = Channel<FloatArray>(Channel.CONFLATED)

    fun undo() {
        if(rawPosListStore.size == 0)
            return
        rawPosListStore.removeAt(rawPosListStore.size-1)
        strokeTracker.undo()
        mainScope.launch {
            channel.send(strokeFloatTensor.floatArray)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        strokeCanvas.strokeListener = { one ->
            model.requestCancel = true


            val clonedOne = one.clone()

            rawPosListStore.add(clonedOne)
            strokeTracker.addStroke(clonedOne)

            mainScope.launch {
                channel.send(strokeFloatTensor.floatArray)
            }
        }

        mainScope.launch {
            for(arr in channel){
                val res = model.predict(arr)
                if (!model.requestCancel) {
                    Log.d("Tegashiki", "final - ${res.toString()}")
                    resultTextView.text = model.toSymbolText(res)
                }
            }
        }


    }

    val resultTextView by lazy { findViewById<TextView>(R.id.textViewResult) }
}
