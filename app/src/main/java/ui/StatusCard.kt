package ui

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.waterquality.R


const val DIRTY = "dirty"
const val ALGAE  = "algae"
class StatusChip(view: View, context: Context) {
    private val text: TextView
    private val chip: View
    private val card : CardView
    private val cl: ConstraintLayout
    private val activity: Context

    init {
        chip = view
        card = chip.findViewById(R.id.card1)
        cl = card.findViewById(R.id.cl1)
        text = cl.findViewById(R.id.status)

        activity = context
    }

    fun setText(status: String) {
        text.text = status
        setColor(status)
    }

    private fun setColor(status: String) {
        when (status) {
            DIRTY-> setBrown()
            else -> setGreen()
        }
    }


    private fun setBrown() {
        text.setTextColor(activity.resources.getColor(R.color.text_brown, null))
        cl.setBackgroundColor(activity.resources.getColor(R.color.light_brown, null))
    }

    private fun setGreen() {
        text.setTextColor(activity.resources.getColor(R.color.green_text, null))
        cl.setBackgroundColor(activity.resources.getColor(R.color.green_light, null))
    }


}