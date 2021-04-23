package me.ccrama.redditslide.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.core.content.ContextCompat;

/**
 * Created by TacoTheDank on 01/29/2021.
 */
public class ClipboardUtil {

    /**
     * Copies the text to the clipboard.
     *
     * @param context The context to pass.
     * @param label   User-visible label for the clip data.
     * @param text    The actual text in the clip.
     */
    public static void copyToClipboard(final Context context, final CharSequence label,
                                       final CharSequence text) {
        final ClipboardManager clipboard = ContextCompat.getSystemService(context, ClipboardManager.class);
        if (clipboard != null) {
            final ClipData data = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(data);
        }
    }
}
