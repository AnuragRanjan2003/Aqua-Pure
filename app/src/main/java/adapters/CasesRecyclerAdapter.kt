package adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.waterquality.R
import models.Report
import ui.ALGAE
import ui.DIRTY
import ui.StatusChip
import java.util.*

class CasesRecyclerAdapter(list: List<Report>, context: Context) :
    RecyclerView.Adapter<CasesRecyclerAdapter.MyViewHolder>() {
    private var list: List<Report>
    private val context: Context

    init {
        this.list = list
        this.context = context
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val place: TextView = itemView.findViewById(R.id.place)
        val date: TextView = itemView.findViewById(R.id.date)
        val problem: View =
            itemView
                .findViewById<CardView>(R.id.cardView6)
                .findViewById<ConstraintLayout>(R.id.cll)
                .findViewById(R.id.include)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cases_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val status = StatusChip(holder.problem,context)
        val report = list[position]
        holder.place.text = parsePlace(report.place)
        holder.date.text = report.date
        var text = ALGAE
        if (report.algae!! < report.dirty!!) text = DIRTY

        status.setText(text)

    }

    private fun parsePlace(place: String?): String? {
        return place?.replace(',','\n',false)
    }

    override fun getItemCount(): Int {
        return list.size
    }


}