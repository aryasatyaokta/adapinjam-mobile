package id.co.bcaf.adapinjam.data.provider

import com.google.firebase.messaging.FirebaseMessaging

class FirebaseTokenProvider : TokenProvider {
    override fun getToken(callback: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                callback(if (task.isSuccessful) task.result else null)
            }
    }
}

