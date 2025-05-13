package id.co.bcaf.adapinjam.data.model

data class UserCustomerResponse(
    val gaji: String? = null,
    val namaIbuKandung: String? = null,
    val noTelp: String? = null,
    val tempatLahir: String? = null,
    val tanggalLahir: String? = null,
    val jenisKelamin: String? = null,
    val alamat: String? = null,
    val nik: String? = null,
    val sisaPlafon: Any? = null,
    val pekerjaan: String? = null,
    val statusRumah: String? = null,
    val noRek: String? = null,
    val id: String? = null,
    val user: User? = null,
    val plafon: Plafon? = null,
    val fotoUrl: String? = null

)

data class User(
    val password: String? = null,
    val role: Role? = null,
    val name: String? = null,
    val id: String? = null,
    val email: String? = null
)

data class Plafon(
    val jenisPlafon: String? = null,
    val deleted: Boolean? = null,
    val jumlahPlafon: Any? = null,
    val idPlafon: Int? = null,
    val bunga: Any? = null
)

data class Role(
    val nameRole: String? = null,
    val id: Int? = null
)