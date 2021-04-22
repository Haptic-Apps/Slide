package me.ccrama.redditslide.util;

import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

/**
 * Created by TacoTheDank on 04/22/2021.
 */
public class CompatUtil {
    public static Spanned fromHtml(@NonNull String source) {
        return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }
}
