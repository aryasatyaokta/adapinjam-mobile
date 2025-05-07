package id.co.bcaf.adapinjam.data.viewModel

import androidx.lifecycle.*
import id.co.bcaf.adapinjam.data.model.UserCustomerResponse
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response

class ProfileViewModel : ViewModel() {

    private val _profileData = MutableLiveData<UserCustomerResponse?>()
    val profileData: LiveData<UserCustomerResponse?> = _profileData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchProfileData(token: String) {
        viewModelScope.launch {
            try {
                val response: Response<UserCustomerResponse> =
                    RetrofitClient.apiService.getCustomerProfile(token)

                if (response.isSuccessful) {
                    _profileData.postValue(response.body())
                } else {
                    _error.postValue("Gagal ambil data profil")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }
}
