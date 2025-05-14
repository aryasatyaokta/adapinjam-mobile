package id.co.bcaf.adapinjam.data.viewModel

import android.util.Log
import androidx.lifecycle.*
import id.co.bcaf.adapinjam.data.model.UserCustomerRequest
import id.co.bcaf.adapinjam.data.model.UserCustomerResponse
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response

class EditProfilViewModel : ViewModel() {

    private val _editProfileResult = MutableLiveData<Result<UserCustomerResponse>>()
    val editProfileResult: LiveData<Result<UserCustomerResponse>> = _editProfileResult

    private val _fetchProfileResult = MutableLiveData<Result<UserCustomerResponse>>()
    val fetchProfileResult: LiveData<Result<UserCustomerResponse>> = _fetchProfileResult

    private val _uploadFotoResult = MutableLiveData<Result<String>>()
    val uploadFotoResult: LiveData<Result<String>> = _uploadFotoResult


    fun fetchProfileData(token: String) {
        viewModelScope.launch {
            try {
                val response: Response<UserCustomerResponse> =
                    RetrofitClient.apiService.getCustomerProfile("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    _fetchProfileResult.postValue(Result.success(response.body()!!))
                } else {
                    // Log the error body or status code for debugging
                    val errorBody = response.errorBody()?.string()
                    _fetchProfileResult.postValue(Result.failure(Exception("Gagal memuat profil pengguna: $errorBody")))
                }
            } catch (e: Exception) {
                _fetchProfileResult.postValue(Result.failure(e))
            }
        }
    }


    fun editProfile(token: String, request: UserCustomerRequest) {
        viewModelScope.launch {
            try {
                val response: Response<UserCustomerResponse> =
                    RetrofitClient.apiService.editCustomerDetails("Bearer $token", request)

                if (response.isSuccessful && response.body() != null) {
                    _editProfileResult.postValue(Result.success(response.body()!!))
                } else {
                    _editProfileResult.postValue(Result.failure(Exception("Gagal memperbarui profil")))
                }
            } catch (e: Exception) {
                _editProfileResult.postValue(Result.failure(e))
            }
        }
    }

    fun uploadFotoProfil(token: String, id: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.uploadFoto("Bearer $token", id, file)
                if (response.isSuccessful) {
                    _uploadFotoResult.postValue(Result.success("Upload foto berhasil"))
                } else {
                    _uploadFotoResult.postValue(Result.failure(Exception("Gagal upload foto: ${response.code()}")))
                }
            } catch (e: Exception) {
                _uploadFotoResult.postValue(Result.failure(e))
            }
        }
    }
}
