package id.co.bcaf.adapinjam.data.model

import java.time.LocalDateTime
import java.util.UUID

data class PengajuanResponse(
    val pengajuanId: UUID,
    val userCustomerResponse: UserCustomerResponse,
    val amount: Double,
    val tenor: Int,
    val bunga: Double,
    val angsuran: Double,
    val status: String,
    val branchId: UUID,
    val namaMarketing: String,
    val createdAt: String,
    val marketingApprovedAt: String?,
    val branchManagerApprovedAt: String?,
    val backOfficeApprovedAt: String?
)
