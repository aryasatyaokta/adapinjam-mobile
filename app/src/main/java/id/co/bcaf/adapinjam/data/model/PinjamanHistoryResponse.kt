package id.co.bcaf.adapinjam.data.model

data class PinjamanHistoryResponse (
    val id: String,
    val amount: Double,
    val tenor: Int,
    val angsuran: Double,
    val bunga: Double,
    val sisaTenor: Int,
    val sisaPokokHutang: Double,
    val lunas: Boolean,
    val biayaAdmin: Double,
    val totalDanaDidapat: Double,
)