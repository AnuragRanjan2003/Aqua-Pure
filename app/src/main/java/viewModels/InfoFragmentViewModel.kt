package viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import models.Quality
import models.Report
import kotlin.math.PI
import kotlin.math.sqrt

private const val AREA_0 = 12391.88

class InfoFragmentViewModel : ViewModel() {
    private var Var: Float = 0.00f
    val limit : MutableLiveData<Double> by lazy { MutableLiveData<Double>() }
    private val reportList: MutableLiveData<MutableList<Report>> by lazy { MutableLiveData<MutableList<Report>>() }
    private val quality: MutableLiveData<Quality> by lazy { MutableLiveData<Quality>() }
    fun getResult(reference: DatabaseReference) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    val report = item.getValue(Report::class.java)
                    if (report != null)
                        reportList.value?.add(report)
                }
                processQuality(reportList.value)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun processQuality(reportList: MutableList<Report>?) {
        val area = AREA_0 * 2 / PI
        var qualInd = 0.00f
        var algae = 0.00f
        var dirty = 0.00f
        var sd = 0.00f
        if (!reportList.isNullOrEmpty()) {
            val n = reportList.size
            for (report in reportList) {
                qualInd += report.drinkable!!
                algae += report.algae!!
                dirty += report.dirty!!
                sd += report.drinkable * report.drinkable
            }
            qualInd /= n
            algae /= n
            dirty /= n
            sd /= n
            Var -= qualInd * qualInd
            Var = sqrt(Var / n)
            qualInd = (qualInd * 10000 / area).toFloat()
            algae = (algae / area).toFloat()
            dirty = (dirty / area).toFloat()


            quality.value = Quality(qualInd, algae, dirty)
            limit.value = 0.95 - 0.1554 * Var
        }
    }

    fun observeQuality(): LiveData<Quality> {
        return quality
    }

    fun observeReportList(): LiveData<MutableList<Report>> {
        return reportList
    }


}