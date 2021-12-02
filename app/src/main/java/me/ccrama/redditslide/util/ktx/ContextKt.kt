package me.ccrama.redditslide.util.ktx

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Displays a toast to the viewer.
 *
 * @param string  The text displayed in the toast.
 */
fun Context.displayToast(@StringRes string: Int) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
}
