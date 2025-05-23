package id.co.bcaf.adapinjam.data.utils

import com.google.gson.GsonBuilder
import id.co.bcaf.adapinjam.data.network.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
//    private const val BASE_URL = "http://35.223.1.74/be/"
    private const val BASE_URL = "https://5d70-120-188-33-177.ngrok-free.app/be/" // Ganti dengan URL API kamu

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Set connect timeout
        .readTimeout(60, TimeUnit.SECONDS)    // Set read timeout
        .writeTimeout(60 , TimeUnit.SECONDS)   // Set write timeout
        .build()

    val gson = GsonBuilder().setLenient().create()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client) // Use custom client
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}