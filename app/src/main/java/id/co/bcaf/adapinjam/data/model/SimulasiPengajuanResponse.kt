package id.co.bcaf.adapinjam.data.model

data class SimulasiPengajuanResponse(
    val amount: Double,
    val tenor: Int,
    val bunga: Double,
    val angsuran: Double,
    val totalPembayaran: Double,
    val biayaAdmin: Double,
    val danaCair: Double
)
