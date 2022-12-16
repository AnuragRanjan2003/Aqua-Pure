package viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log.e
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.waterquality.ml.ModelUnquant
import models.Quality
import models.colorApi.Dominant
import models.colorApi.Response
import models.interpolators.HuetoWL
import models.interpolators.RgbtoHue
import models.processedInfo
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import repo.ColorApiInstance
import retrofit2.Call
import retrofit2.Callback

private const val API_USER = "432909989"
private const val API_SECRET = "ygsuZ7ipGQuyDrYFLAqU"

class AnalysisFragmentViewModel : ViewModel() {
    private val response: MutableLiveData<Response> by lazy { MutableLiveData<Response>() }
    private val processedInfo: MutableLiveData<processedInfo> by lazy { MutableLiveData<processedInfo>() }
    private val prediction: MutableLiveData<FloatArray> by lazy { MutableLiveData<FloatArray>() }
    private val quality:MutableLiveData<Quality> by lazy { MutableLiveData<Quality>() }

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
                        getQuality(response.body()!!.colors.dominant)
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

    private fun getQuality(color: Dominant){
        TODO("predict and set the value of quality")
    }

    fun getPrediction(context: Context, uri: Uri) {
        val model = ModelUnquant.newInstance(context)

        var bitmap: Bitmap? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                bitmap = ImageDecoder.decodeBitmap(source)
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            println("Could not convert image to BitMap")
            e.printStackTrace()
        }
        bitmap = Bitmap.createScaledBitmap(bitmap!! , 224 ,224 ,true)
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888 , true)
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        val tImg = TensorImage(DataType.FLOAT32)
        tImg.load(bitmap)
        val byteBuffer = tImg.buffer
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        model.close()

        prediction.value = outputFeature0.floatArray
    }


    fun observeInfo(): LiveData<processedInfo> {
        return processedInfo
    }

    fun observeResponse(): LiveData<Response> {
        return response
    }
    fun observePrediction():LiveData<FloatArray>{
        return prediction
    }
    fun observeQuality():LiveData<Quality>{
        return quality
    }
}