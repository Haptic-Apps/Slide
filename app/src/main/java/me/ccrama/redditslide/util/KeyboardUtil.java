package me.ccrama.redditslide.util;

import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

/**
 * Created by TacoTheDank on 03/15/2021.
 */
public class KeyboardUtil {

    /**
     * Hides the keyboard.
     *
     * @param context     The context to be passed.
     * @param windowToken The token of the window that is making the request.
     * @param flags       Provides additional operating flags.
     */
    public static void hideKeyboard(final Context context, final IBinder windowToken, final int flags) {
        final InputMethodManager imm = ContextCompat.getSystemService(context, InputMethodManager.class);
        if (imm != null) {
            imm.hideSoftInputFromWindow(windowToken, flags);
        }
    }

    /**
     * Toggles the keyboard.
     * If the keyboard is already displayed, it gets hidden.
     * If not, the keyboard will be displayed.
     *
     * @param context   The context to be passed.
     * @param showFlags Provides additional operating flags for showing.
     * @param hideFlags Provides additional operating flags for hiding.
     */
    public static void toggleKeyboard(final Context context, final int showFlags, final int hideFlags) {
        final InputMethodManager imm = ContextCompat.getSystemService(context, InputMethodManager.class);
        if (imm != null) {
            imm.toggleSoftInput(showFlags, hideFlags);
        }
    }
}
