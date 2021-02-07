package me.ccrama.redditslide.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Created by TacoTheDank on 01/29/2021.
 */
public class ClipboardUtil {

    /**
     * Copies the text to the clipboard
     *
     * @param text The text to place in the clipboard
     */
    public static void copyToClipboard(final Context context, @NonNull final CharSequence label, @NonNull final CharSequence text) {
        final ClipboardManager clipboard = ContextCompat.getSystemService(context, ClipboardManager.class);
        if (clipboard != null) {
            final ClipData data = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(data);
        }
    }
}
