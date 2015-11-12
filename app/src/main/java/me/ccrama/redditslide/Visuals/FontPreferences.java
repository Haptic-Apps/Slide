package me.ccrama.redditslide.Visuals;

import android.content.Context;
import android.content.SharedPreferences;

import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 7/9/2015.
 */
public class FontPreferences {
    private final static String FONT_STYLE = "FONT_STYLE";

    private final Context context;

    public FontPreferences(Context context) {
        this.context = context;
    }

    private SharedPreferences open() {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor edit() {
        return open().edit();
    }

    public FontStyle getFontStyle() {
        return FontStyle.valueOf(open().getString(FONT_STYLE,
                FontStyle.Medium.name()));
    }

    public void setFontStyle(FontStyle style) {
        edit().putString(FONT_STYLE, style.name()).commit();
    }

    public enum FontStyle {
        Small(R.style.FontStyle_Small, "Small"),
        Medium(R.style.FontStyle_Medium, "Medium"),
        Large(R.style.FontStyle_Large, "Large");

        private final int resId;
        private final String title;

        FontStyle(int resId, String title) {
            this.resId = resId;
            this.title = title;
        }

        public int getResId() {
            return resId;
        }

        public String getTitle() {
            return title;
        }
    }
}
