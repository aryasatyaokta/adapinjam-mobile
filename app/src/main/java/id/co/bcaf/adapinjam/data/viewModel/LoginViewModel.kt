package id.co.bcaf.adapinjam.data.viewModel

import androidx.lifecycle.*
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.bcaf.adapinjam.data.model.LoginRequest
import id.co.bcaf.adapinjam.data.model.LoginResponse
import id.co.bcaf.adapinjam.data.network.ApiService
import id.co.bcaf.adapinjam.data.provider.TokenProvider
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenProvider: TokenProvider
) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<String>>()
    val loginResult: LiveData<Result<String>> = _loginResult

    fun login(username: String, password: String) {
        tokenProvider.getToken { fcmToken ->
            if (fcmToken == null) {
                _loginResult.postValue(Result.failure(Exception("Gagal ambil FCM Token")))
                return@getToken
            }

            viewModelScope.launch {
                try {
                    val response = apiService.login(LoginRequest(username, password, fcmToken))
                    val token = response.body()?.token

                    if (response.isSuccessful && !token.isNullOrEmpty()) {
                        _loginResult.postValue(Result.success(token))
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

