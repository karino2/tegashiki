package io.github.karino2.tegashiki

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class TegashikiDialog(context: Context) : AlertDialog(context) {
    init {
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    //     var strokeListener : (List<Float>)->Unit = {_->}
    var sendResultListener: (String)->Unit = {_->}

    val model by lazy { Model(context.assets) }
    val strokeFloatTensor = KdFTensor(1* Model.MAX_STROKE_NUM * Model.MAX_ONE_STROKE_LEN * Model.INPUT_DIM).reshape(Model.MAX_STROKE_NUM, Model.MAX_ONE_STROKE_LEN, Model.INPUT_DIM)
    val strokeTracker by lazy {
        StrokeTracker(strokeFloatTensor)
    }

    lateinit var strokeCanvas: StrokeCanvas
    lateinit var resultTextView: TextView

    fun showMessage(msg : String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    fun onSendButtonClick(v: View) {
        sendResultListener("\$\$${resultTextView.text.toString()}\$\$")
    }

    fun onUndoButtonClick(v: View) {
        strokeCanvas.undo()
        undo()
    }

    fun onClearButtonClick(v: View) {
        strokeCanvas.clearCanvas()
        strokeTracker.clear()
        model.inputStroke.buf.fill(0)
        resultTextView.text = ""
    }

    val mainScope = MainScope()

    val channel = Channel<FloatArray>(Channel.CONFLATED)
    fun undo() {
        if(strokeTracker.canUndo) {
            strokeTracker.undo()
            mainScope.launch {
                channel.send(strokeFloatTensor.floatArray)
            }
        }
    }

    val rawPosListStore: ArrayList<List<Float>>
        get() = strokeTracker.rawPosListStore


    override fun onCreate(savedInstanceState: Bundle?) {
        val view = layoutInflater.inflate(R.layout.dialog_tegashiki, null)
        setView(view)
        // setContentView(R.layout.dialog_tegashiki)

        setOnDismissListener {
            mainScope.cancel()
        }


        view.findViewById<Button>(R.id.buttonClear)!!.setOnClickListener(::onClearButtonClick)
        view.findViewById<Button>(R.id.buttonUndo)!!.setOnClickListener(::onUndoButtonClick)
        view.findViewById<Button>(R.id.buttonSend)!!.setOnClickListener(::onSendButtonClick)

        strokeCanvas = view.findViewById<StrokeCanvas>(R.id.canvas)!!
        resultTextView = view.findViewById<TextView>(R.id.textViewResult)!!


        strokeCanvas.strokeListener = { one ->
            model.requestCancel = true


            val clonedOne = one.clone()

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



        super.onCreate(savedInstanceState)
    }
}