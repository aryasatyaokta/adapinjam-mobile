package id.co.bcaf.adapinjam.data.provider

interface TokenProvider {
    fun getToken(callback: (String?) -> Unit)
}
