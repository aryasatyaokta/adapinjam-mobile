package id.co.bcaf.adapinjam.data.viewModel

import androidx.lifecycle.*
import id.co.bcaf.adapinjam.data.model.RegisterRequest
import id.co.bcaf.adapinjam.data.model.RegisterResponse
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    private val _registerResult = MutableLiveData<Result<String>>()
    val registerResult: LiveData<Result<String>> = _registerResult

    fun register(username: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                val response: Response<RegisterResponse> =
                    RetrofitClient.apiService.register(RegisterRequest(username, password, name))

                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (!token.isNullOrEmpty()) {
                        _registerResult.postValue(Result.success(token))
                    } else {
                        _registerResult.postValue(Result.failure(Exception("Token tidak valid")))
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Terjadi kesalahan saat registrasi"
                    _registerResult.postValue(Result.failure(Exception(errorMessage)))
                }
            } catch (e: Exception) {
                _registerResult.postValue(Result.failure(e))
            }
        }
    }
}
