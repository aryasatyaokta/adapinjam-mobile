package id.co.bcaf.adapinjam.data.network

import id.co.bcaf.adapinjam.data.model.*
import okhttp3.Call
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/login-google")
    suspend fun loginWithGoogle(@Body request: GoogleAuthRequest): LoginResponse

    @POST("api/v1/auth/register-customer")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/v1/customer/get-customer")
    suspend fun getCustomerProfile(@Header("Authorization") token: String): Response<UserCustomerResponse>

    @GET("api/v1/customer/check-profile")
    suspend fun checkProfile(
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    @POST("api/v1/customer/add-customer-details")
    suspend fun addCustomerDetails(
        @Header("Authorization") token: String,
        @Body customerRequest: UserCustomerRequest
    ): Response<UserCustomerResponse>

    @Multipart
    @POST("api/v1/customer/{id}/upload-foto")
    suspend fun uploadFoto(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Void>

    @GET("api/v1/plafon/all")
    suspend fun getAllPlafon(@Header("Authorization") token: String): Response<List<Plafon>>

    @GET("api/v1/pengajuan/history-customer")
    suspend fun getPengajuanHistory(@Header("Authorization") token: String): List<PengajuanHistoryResponse>

    @PUT("api/v1/customer/edit-customer-details")
    suspend fun editCustomerDetails(
        @Header("Authorization") token: String,
        @Body request: UserCustomerRequest
    ): Response<UserCustomerResponse>

    @PUT("api/v1/auth/update-password")
    suspend fun updatePassword(
        @Header("Authorization") token: String,
        @Body request: UpdatePasswordRequest
    ): Response<Void>

    @GET("api/v1/pinjaman/customer")
    suspend fun getHistoryPinjaman(
        @Header("Authorization") token: String
    ): List<PinjamanHistoryResponse>
}
