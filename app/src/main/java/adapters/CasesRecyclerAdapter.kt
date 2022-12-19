package adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.waterquality.R
import models.Report
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.floor

class CasesRecyclerAdapter(list: List<Report>, context: Context) :
    RecyclerView.Adapter<CasesRecyclerAdapter.MyViewHolder>() {
    private var list: List<Report>
    private val context: Context

    init {
        this.list = list
        this.context = context
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lat: TextView = itemView.findViewById(R.id.tv_lat)
        val lon: TextView = itemView.findViewById(R.id.tv_lon)
        val date: TextView = itemView.findViewById(R.id.tv_date)
        val problem: TextView = itemView.findViewById(R.id.tv_problem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cases_layout, parent, false)
        return MyViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            val report = list[position]
            holder.lat.text = "Latitude : ${format(report.lat!!)}"
            holder.lon.text = "Longitude : ${format(report.lon!!)}"
            holder.date.text = "Date : ${report.date}"
            var text = "algae"
            if (report.algae!! < report.dirty!!) text = "dirty"
            holder.problem.text = "Problem : $text"

    }

    override fun getItemCount(): Int {
        return list.size
    }
    private fun format(num : Double): String {
        val df =DecimalFormat("##.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(num)
    }


}