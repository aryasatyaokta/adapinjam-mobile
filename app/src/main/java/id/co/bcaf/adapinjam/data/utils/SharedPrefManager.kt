package id.co.bcaf.adapinjam.data.utils

import android.content.Context

class SharedPrefManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)

    fun setToken(token: String?) {
        sharedPreferences.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun clearToken() {
        sharedPreferences.edit().remove("token").apply()
    }
}

