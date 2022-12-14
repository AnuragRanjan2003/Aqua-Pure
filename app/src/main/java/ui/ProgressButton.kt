package ui

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.waterquality.R
import java.security.AccessController.getContext

class ProgressButton(context: Context, view: View, title: String) {
    private val cardView: CardView
    private val text: TextView
    private val pb: ProgressBar
    private val cl: ConstraintLayout
    private val Title: String
    private val ct : Context
    init {
        cardView = view.findViewById(R.id.pbtn_card)
        text = view.findViewById(R.id.pbtn_text)
        pb = view.findViewById(R.id.pbtn_pb)
        cl = view.findViewById(R.id.pbtn_cl)
        ct = context
        Title = title
    }

    fun activateButton() {
        pb.visibility = View.VISIBLE
        cl.setBackgroundColor(cardView.resources.getColor(R.color.green, null))
        text.text = "please wait.."
    }

    fun deactivateButton() {
        pb.visibility = View.GONE
        cl.setBackgroundColor(cardView.resources.getColor(R.color.purple_500, null))
        cl.setPadding(dptoInt(45),dptoInt(10),dptoInt(45),dptoInt(10))
        text.text = Title
    }

    fun finish() {
        pb.visibility = View.GONE
        cardView.visibility = View.GONE
    }
    private fun dptoInt(dps : Int):Int{
        val scale: Float = ct.resources.displayMetrics.density
        val pixels = (dps * scale + 0.5f).toInt()
        return pixels
    }
}