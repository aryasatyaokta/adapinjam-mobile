package id.co.bcaf.adapinjam.data.viewModel

import androidx.lifecycle.*
import com.google.firebase.messaging.FirebaseMessaging
import id.co.bcaf.adapinjam.data.model.LoginRequest
import id.co.bcaf.adapinjam.data.model.LoginResponse
import id.co.bcaf.adapinjam.data.network.ApiService
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginViewModel : ViewModel() {
    private val _loginResult = MutableLiveData<Result<String>>()
    val loginResult: LiveData<Result<String>> = _loginResult

    fun fetchFcmToken(onTokenReady: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    onTokenReady(token)
                } else {
                    onTokenReady(null)
                }
            }
    }

    fun login(username: String, password: String) {
        fetchFcmToken { fcmToken ->
            if (fcmToken == null) {
                _loginResult.postValue(Result.failure(Exception("Gagal ambil FCM Token")))
                return@fetchFcmToken
            }

            viewModelScope.launch {
                try {
                    val response: Response<LoginResponse> =
                        RetrofitClient.apiService.login(LoginRequest(username, password, fcmToken))

                    if (response.isSuccessful) {
                        val token = response.body()?.token
                        if (!token.isNullOrEmpty()) {
                            _loginResult.postValue(Result.success(token))
                        } else {
                            _loginResult.postValue(Result.failure(Exception("Token tidak valid")))
                        }
                    } else {
                        _loginResult.postValue(Result.failure(Exception("Username atau password salah")))
                    }
                } catch (e: Exception) {
                    _loginResult.postValue(Result.failure(e))
                }
            }
        }
    }

}
