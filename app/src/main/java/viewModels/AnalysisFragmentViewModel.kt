package viewModels

import android.util.Log.e
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import models.colorApi.Response
import models.interpolators.HuetoWL
import models.interpolators.RgbtoHue
import models.processedInfo
import repo.ColorApiInstance
import retrofit2.Call
import retrofit2.Callback

private const val API_USER = "432909989"
private const val API_SECRET = "ygsuZ7ipGQuyDrYFLAqU"

class AnalysisFragmentViewModel : ViewModel() {
    private val response: MutableLiveData<Response> by lazy { MutableLiveData<Response>() }
    private val processedInfo: MutableLiveData<processedInfo> by lazy { MutableLiveData<processedInfo>() }

    fun getResponse(imageUrl: String) {

        ColorApiInstance.api.getResult(imageUrl, "properties", API_USER, API_SECRET)
            .enqueue(object : Callback<Response?> {
                override fun onResponse(
                    call: Call<Response?>,
                    response: retrofit2.Response<Response?>
                ) {
                    if (response.body() != null && response.body()?.status == "success") {
                        this@AnalysisFragmentViewModel.response.value = response.body()!!
                        processInfo(response.body()!!)
                    } else return
                }

                override fun onFailure(call: Call<Response?>, t: Throwable) {
                    e("Api_error", t.message.toString())
                }
            })
    }

    private fun processInfo(response: Response) {
        val dhue = RgbtoHue(
            response.colors.dominant.r,
            response.colors.dominant.g,
            response.colors.dominant.b
        ).getHue()
        val chue = RgbtoHue(
            255 - response.colors.dominant.r,
            255 - response.colors.dominant.g,
            255 - response.colors.dominant.b
        ).getHue()
        e("dhue", dhue.toString())
        e("chue", chue.toString())
        val dw = HuetoWL(dhue).computeWl()
        val cdw = HuetoWL(chue).computeWl()
        processedInfo.value = processedInfo(dw, cdw, 0.00, 0.00)
    }

    fun observeInfo(): LiveData<processedInfo> {
        return processedInfo
    }

    fun observeResponse(): LiveData<Response> {
        return response
    }
}