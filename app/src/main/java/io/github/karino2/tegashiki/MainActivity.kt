package io.github.karino2.tegashiki

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.stream.JsonWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    val DIALOG_ID_TEGASHIKI=1

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
        tegashikiDialog.rawPosListStore.forEach {
            writer.beginArray()
            it.forEach {
                writer.value(it)
            }
            writer.endArray()
        }
        writer.endArray()

        writer.beginArray()
        tegashikiDialog.strokeFloatTensor.floatArray.forEach {
            writer.value(it)
        }
        writer.endArray()

        writer.endArray()
        writer.close()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showDialog(DIALOG_ID_TEGASHIKI)
    }

    override fun onCreateDialog(id: Int): Dialog {
        when(id) {
            DIALOG_ID_TEGASHIKI-> {
                return TegashikiDialog(this).apply {
                    this.window!!.setGravity(Gravity.TOP)
                    this.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    // this.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                // return TegashikiDialog(this)
            }
        }
        return super.onCreateDialog(id)
    }

    lateinit var tegashikiDialog : TegashikiDialog

    override fun onPrepareDialog(id: Int, dialog: Dialog) {
        when(id) {
            DIALOG_ID_TEGASHIKI -> {
                tegashikiDialog = dialog as TegashikiDialog
                tegashikiDialog.setCanceledOnTouchOutside(false)
                tegashikiDialog.setCancelable(true)
                tegashikiDialog.setOnDismissListener { finish() }
                tegashikiDialog.sendResultListener = {
                    copyToClipboard(it)
                    showMessage("Copy tex to clipboard")
                }
            }
        }
        super.onPrepareDialog(id, dialog)
    }

}
