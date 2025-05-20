// MainActivity.kt
package id.co.bcaf.adapinjam

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.co.bcaf.adapinjam.ui.home.HomeActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        // Tidak perlu finish() karena FLAG_ACTIVITY_CLEAR_TASK sudah menghapus MainActivity dari backstack
    }
}
