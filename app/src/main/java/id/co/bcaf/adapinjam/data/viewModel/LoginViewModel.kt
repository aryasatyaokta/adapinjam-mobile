package id.co.bcaf.adapinjam.data.viewModel

import androidx.lifecycle.*
import id.co.bcaf.adapinjam.data.model.LoginRequest
import id.co.bcaf.adapinjam.data.model.LoginResponse
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginViewModel : ViewModel() {
    private val _loginResult = MutableLiveData<Result<String>>()
    val loginResult: LiveData<Result<String>> = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response: Response<LoginResponse> =
                    RetrofitClient.apiService.login(LoginRequest(username, password))

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
