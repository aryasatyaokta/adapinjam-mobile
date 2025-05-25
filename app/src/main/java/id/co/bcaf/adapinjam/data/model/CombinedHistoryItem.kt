package id.co.bcaf.adapinjam.data.model

data class CombinedHistoryItem(
    val pinjaman: PinjamanHistoryResponse,
    val backOfficeApprovedAt: String?
)
