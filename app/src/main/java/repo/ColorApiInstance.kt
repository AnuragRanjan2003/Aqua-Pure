package repo

import api.ColorApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

private const val BASE_URL="https://api.sightengine.com/1.0/"
object ColorApiInstance {
    val api : ColorApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ColorApi::class.java)
    }
}