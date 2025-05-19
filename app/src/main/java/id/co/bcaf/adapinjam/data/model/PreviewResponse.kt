package id.co.bcaf.adapinjam.data.model

data class PreviewResponse(
    val amount: Double,
    val tenor: Int,
    val bunga: Double,
    val angsuran: Double,
    val biayaAdmin: Double,
    val totalDanaDidapat: Double
)
