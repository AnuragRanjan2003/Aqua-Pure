package api

import models.colorApi.Response
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

private const val GET_URl = "check.json"

interface ColorApi {

    @GET(GET_URl)
    fun getResult(
        @Query("url")
        url: String,
        @Query("models")
        model: String,
        @Query("api_user")
        apiUser: String,
        @Query("api_secret")
        apiSecret: String
    ): Call<Response>

}