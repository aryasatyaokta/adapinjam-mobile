package id.co.bcaf.adapinjam.data.model

data class PengajuanHistoryResponse(
    val id: String,
    val amount: Long,
    val tenor: Int,
    val bunga: Double,
    val angsuran: Double,
    val status: String,
    val marketingApprovedAt: String?,
    val branchManagerApprovedAt: String?,
    val backOfficeApprovedAt: String?,
    val disbursementAt: String?
)