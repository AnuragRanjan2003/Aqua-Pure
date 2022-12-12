package viewModels

import android.util.Log.d
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import models.colorApi.Colors
import models.colorApi.Response
import repo.ColorApiInstance
import retrofit2.Call
import retrofit2.Callback

private const val API_USER = "432909989"
private const val API_SECRET = "ygsuZ7ipGQuyDrYFLAqU"

class ImageFragmentViewModel : ViewModel() {
    private val colors: MutableLiveData<Colors> by lazy { MutableLiveData<Colors>() }

    fun getColors(imageUrl: String) {

        ColorApiInstance.api.getResult(imageUrl, "properties", API_USER, API_SECRET)
            .enqueue(object : Callback<Response?> {
                override fun onResponse(
                    call: Call<Response?>,
                    response: retrofit2.Response<Response?>
                ) {
                    if (response.body() != null && response.body()?.status == "success")
                        colors.value = response.body()!!.colors
                    else return
                }

                override fun onFailure(call: Call<Response?>, t: Throwable) {
                    d("Api_error", t.message.toString())
                }
            })
    }

    fun observeColors():LiveData<Colors>{
        return colors
    }
}