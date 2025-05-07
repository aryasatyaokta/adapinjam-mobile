package id.co.bcaf.adapinjam.data.network

import id.co.bcaf.adapinjam.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/register-customer")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/v1/customer/get-customer")
    suspend fun getCustomerProfile(@Header("Authorization") token: String): Response<UserCustomerResponse>

    @GET("api/v1/customer/check-profile")
    suspend fun checkProfile(
        @Header("Authorization") token: String
    ): Response<ResponseBody>


}
