package id.co.bcaf.adapinjam.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import id.co.bcaf.adapinjam.data.model.LoginRequest
import id.co.bcaf.adapinjam.data.model.LoginResponse
import id.co.bcaf.adapinjam.data.model.RegisterRequest
import id.co.bcaf.adapinjam.data.model.RegisterResponse

interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/v1/auth/register-customer")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}
