package id.co.bcaf.adapinjam.data.model

data class UpdatePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)