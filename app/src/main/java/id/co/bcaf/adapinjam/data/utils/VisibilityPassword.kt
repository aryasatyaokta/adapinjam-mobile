package id.co.bcaf.adapinjam.utils

import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText
import id.co.bcaf.adapinjam.R

fun EditText.setupPasswordToggle(
    iconStart: Int = R.drawable.iconpassword,
    iconVisible: Int = R.drawable.ic_visibility,
    iconInvisible: Int = R.drawable.ic_visibility_off
) {
    this.setCompoundDrawablesWithIntrinsicBounds(iconStart, 0, iconInvisible, 0)

    this.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val drawableEnd = 2
            if (event.rawX >= (this.right - this.compoundDrawables[drawableEnd].bounds.width())) {
                val isPasswordVisible = this.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

                if (isPasswordVisible) {
                    // Hide password
                    this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    this.setCompoundDrawablesWithIntrinsicBounds(iconStart, 0, iconInvisible, 0)
                } else {
                    // Show password
                    this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    this.setCompoundDrawablesWithIntrinsicBounds(iconStart, 0, iconVisible, 0)
                }

                // Set cursor at the end
                this.setSelection(this.text.length)
                return@setOnTouchListener true
            }
        }
        false
    }
}
