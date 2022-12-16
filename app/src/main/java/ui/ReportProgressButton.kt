package ui

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import com.example.waterquality.R

class ReportProgressButton(context : Context , view:View) {
    private val pb:ProgressBar
    private val ct : Context
    init {
        pb = view.findViewById(R.id.report_pbtn_pb)
        ct = context
    }
     fun activateButton(){
         pb.visibility = View.VISIBLE
     }
}