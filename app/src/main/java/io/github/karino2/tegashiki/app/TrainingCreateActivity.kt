package io.github.karino2.tegashiki.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.gson.stream.JsonWriter
import io.github.karino2.tegashiki.R
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TrainingCreateActivity : AppCompatActivity() {

    var currentStrokes = ArrayList<List<Float>>()
    val allStrokesList = ArrayList<ArrayList<List<Float>>>()


    val strokeCanvas by lazy { findViewById<io.github.karino2.tegashiki.StrokeCanvas>(R.id.canvas)!! }
    fun onClearClick(v: View) {
        strokeCanvas.clearCanvas()
        currentStrokes.clear()
    }

    fun createStoreFile() : File {
        val fname = createFileName()
        return File(MainActivity.getStoreDirectory(), fname)
    }

    private fun createFileName(): String {
        val timeStampFormat = SimpleDateFormat("yyyyMMdd_HHmmssSS")
        val fnameTime = timeStampFormat.format(Date())
        val fprefix = findViewById<EditText>(R.id.editTextPrefix).text.toString()
        val fname = "${fprefix}_${fnameTime}.json"
        return fname
    }

    fun onSaveClick(v: View) {
        if(currentStrokes.size != 0 &&
                currentStrokes[0].size != 0) {
            allStrokesList.add(currentStrokes)
            currentStrokes = ArrayList<List<Float>>()
            strokeCanvas.clearCanvas()
        }
        // save here.
        saveAll()
        allStrokesList.clear()
        showCount(allStrokesList.size)
    }

    fun showMessage(msg:String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    private fun saveAll() {
        val file = createStoreFile()

        val writer = JsonWriter(FileWriter(file))
        writer.beginArray()

        allStrokesList.forEach{strokes ->
            writer.beginArray()
            strokes.forEach {stroke->
                writer.beginArray()
                stroke.forEach {
                    writer.value(it)
                }
                writer.endArray()
            }
            writer.endArray()
        }

        writer.endArray()
        writer.close()

        showMessage("Saved (${allStrokesList.size} pats) ${file.name}")
    }

    fun onNextClick(v: View) {
        allStrokesList.add(currentStrokes)
        currentStrokes = ArrayList<List<Float>>()
        strokeCanvas.clearCanvas()
        showCount(allStrokesList.size)
    }

    fun showCount(num: Int) {
        findViewById<TextView>(R.id.textViewCount).text = num.toString()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_create)

        strokeCanvas.strokeListener = {one->
            currentStrokes.add(mutableListOf<Float>().apply { addAll(one) })
        }
    }
}
