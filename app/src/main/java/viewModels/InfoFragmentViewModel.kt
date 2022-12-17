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
import java.lang.Math.cos
import kotlin.math.PI
import kotlin.math.sqrt

private const val AREA_0 = 12391.88

class InfoFragmentViewModel : ViewModel() {
    private var Var : Float =0.00f
    private var reportList: MutableList<Report> = emptyList<Report>() as MutableList<Report>
    private val quality: MutableLiveData<Quality> by lazy { MutableLiveData<Quality>() }
    fun getResult(reference: DatabaseReference, lat: Int) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children) {
                    val report = item.getValue(Report::class.java)
                    if (report != null)
                        reportList.add(report)
                }
                processQuality(reportList, lat)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun processQuality(reportList: MutableList<Report>, lat: Int) {
        val area = AREA_0 * cos(toRad(lat))
        var qualInd = 0.00f
        var algae = 0.00f
        var dirty = 0.00f
        var sd = 0.00f
        val n = reportList.size
        for (report in reportList) {
            qualInd += report.drinkable!!
            algae += report.algae!!
            dirty += report.dirty!!
            sd += report.drinkable!! * report.drinkable!!
        }
        qualInd /= n
        algae /= n
        dirty /= n
        sd /= n
        Var -= qualInd * qualInd
        Var = sqrt(Var/n)
        qualInd = (qualInd * 10000 / area).toFloat()
        algae = (algae / area).toFloat()
        dirty = (dirty / area).toFloat()


        quality.value = Quality(qualInd, algae, dirty)
    }
    fun getLimit():Double{
        return 0.95 - 0.1554*Var
    }

     fun observeQuality(): LiveData<Quality> {
        return quality
    }



    private fun toRad(deg: Int): Double {
        return PI * deg / 180
    }


}