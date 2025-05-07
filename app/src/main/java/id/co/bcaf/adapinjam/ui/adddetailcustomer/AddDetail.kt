package id.co.bcaf.adapinjam.ui.adddetailcustomer

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import id.co.bcaf.adapinjam.R
import id.co.bcaf.adapinjam.data.model.UserCustomerRequest
import id.co.bcaf.adapinjam.data.utils.RetrofitClient
import id.co.bcaf.adapinjam.data.utils.SharedPrefManager
import id.co.bcaf.adapinjam.ui.home.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddDetail : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var progressDialog: ProgressDialog

    private lateinit var tanggalLahir: EditText
    private lateinit var jenisKelamin: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_detail)

        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Set an OnClickListener to go back to HomeActivity
        btnBack.setOnClickListener {
            // Navigate to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Optional: This will finish AddDetail activity and remove it from the back stack
        }

        sharedPrefManager = SharedPrefManager(this)
        progressDialog = ProgressDialog(this)

        tanggalLahir = findViewById(R.id.TanggalLahir)
        jenisKelamin = findViewById(R.id.JenisKelamin)

        // Set up the Spinner for Jenis Kelamin (Gender)
        val genderOptions = listOf("Pria", "Wanita")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jenisKelamin.adapter = genderAdapter

        // Set up the Date Picker for Tanggal Lahir
        tanggalLahir.setOnClickListener {
            showDatePickerDialog()
        }

        val btnSubmit = findViewById<Button>(R.id.btnAddDetail)

        btnSubmit.setOnClickListener {
            val nik = findViewById<EditText>(R.id.Nik).text.toString()
            val tempatLahir = findViewById<EditText>(R.id.TempatLahir).text.toString()
            val tanggalLahirStr = tanggalLahir.text.toString()
            val jenisKelaminStr = jenisKelamin.selectedItem.toString()
            val noTelp = findViewById<EditText>(R.id.Telepon).text.toString()
            val alamat = findViewById<EditText>(R.id.Alamat).text.toString()
            val ibuKandung = findViewById<EditText>(R.id.IbuKandung).text.toString()
            val pekerjaan = findViewById<EditText>(R.id.Pekerjaan).text.toString()
            val gaji = findViewById<EditText>(R.id.Gaji).text.toString().toIntOrNull() ?: 0
            val noRek = findViewById<EditText>(R.id.Rekening).text.toString()
            val statusRumah = findViewById<EditText>(R.id.StatusRumah).text.toString()

            val customerRequest = UserCustomerRequest(
                nik = nik,
                tempatLahir = tempatLahir,
                tanggalLahir = tanggalLahirStr,
                jenisKelamin = jenisKelaminStr,
                noTelp = noTelp,
                alamat = alamat,
                namaIbuKandung = ibuKandung,
                pekerjaan = pekerjaan,
                gaji = gaji,
                noRek = noRek,
                statusRumah = statusRumah
            )

            val token = sharedPrefManager.getToken() ?: ""
            if (token.isNotEmpty()) {
                progressDialog.setMessage("Submitting...")
                progressDialog.show()
                submitCustomerDetails(token, customerRequest)
            } else {
                showSnackbar("Token is missing", true)
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Format the date as a string (e.g., "dd/MM/yyyy")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            calendar.set(selectedYear, selectedMonth, selectedDay)
            val selectedDate = dateFormat.format(calendar.time)
            tanggalLahir.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun submitCustomerDetails(token: String, customerRequest: UserCustomerRequest) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.apiService.addCustomerDetails("Bearer $token", customerRequest)

                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    showSnackbar("Customer details added successfully")
                    val intent = Intent(this@AddDetail, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    // Redirect or update UI accordingly
                } else {
                    progressDialog.dismiss()
                    showSnackbar("Error: ${response.message()}", true)
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                showSnackbar("Exception: ${e.message}", true)
            }
        }
    }

    private fun showSnackbar(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(if (isError) Color.RED else Color.GREEN)
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }
}
