package me.ccrama.redditslide.Visuals;

import android.content.Context;
import android.content.SharedPreferences;

import com.devspark.robototextview.util.RobotoTypefaceManager;

import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 7/9/2015.
 */
public class FontPreferences {
    private final static String FONT_STYLE_POST = "FONT_STYLE_POST";
    private final static String FONT_STYLE_COMMENT = "FONT_STYLE_COMMENT";
    private final static String FONT_COMMENT = "FONT_COMMENT";
    private final static String FONT_TITLE = "FONT_TITLE";

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

    public FontStyle getPostFontStyle() {
        return FontStyle.valueOf(open().getString(FONT_STYLE_POST,
                FontStyle.Medium.name()));
    }

    public FontStyleComment getCommentFontStyle() {
        return FontStyleComment.valueOf(open().getString(FONT_STYLE_COMMENT,
                FontStyleComment.Medium.name()));
    }


    public FontTypeComment getFontTypeComment() {
        return FontTypeComment.valueOf(open().getString(FONT_COMMENT,
                FontTypeComment.Regular.name()));
    }
    public FontTypeTitle getFontTypeTitle() {
        return FontTypeTitle.valueOf(open().getString(FONT_TITLE,
                FontTypeTitle.Regular.name()));
    }

    public void setPostFontStyle(FontStyle style) {
        edit().putString(FONT_STYLE_POST, style.name()).commit();
    }
    public void setCommentFontStyle(FontStyleComment style) {
        edit().putString(FONT_STYLE_COMMENT, style.name()).commit();
    }
    public void setCommentFont(FontTypeComment style) {
        edit().putString(FONT_COMMENT, style.name()).commit();
    }
    public void setTitlFont(FontTypeTitle style) {
        edit().putString(FONT_TITLE, style.name()).commit();
    }
    public enum FontStyle {
        Smaller(R.style.FontStyle_SmallerPost, "Smaller"),
        Small(R.style.FontStyle_SmallPost, "Small"),
        Medium(R.style.FontStyle_MediumPost, "Medium"),
        Large(R.style.FontStyle_LargePost, "Large"),
        Larger(R.style.FontStyle_LargerPost, "Larger");

        private final int resId;
        private final String title;

        public int getResId() {
            return resId;
        }

        public String getTitle() {
            return title;
        }

        FontStyle(int resId, String title) {
            this.resId = resId;
            this.title = title;
        }
    }

    public enum FontStyleComment {
        Smaller(R.style.FontStyle_SmallerComment, "Smaller"),
        Small(R.style.FontStyle_SmallComment, "Small"),
        Medium(R.style.FontStyle_MediumComment, "Medium"),
        Large(R.style.FontStyle_LargeComment, "Large"),
        Larger(R.style.FontStyle_LargerComment, "Larger");

        private final int resId;
        private final String title;

        public int getResId() {
            return resId;
        }

        public String getTitle() {
            return title;
        }

        FontStyleComment(int resId, String title) {
            this.resId = resId;
            this.title = title;
        }
    }



    public enum FontTypeComment {
        Slab(RobotoTypefaceManager.Typeface.ROBOTO_SLAB_REGULAR, "Slab"),
        Condensed(RobotoTypefaceManager.Typeface.ROBOTO_CONDENSED_REGULAR, "Condensed"),
        Regular(RobotoTypefaceManager.Typeface.ROBOTO_REGULAR, "Regular");

        private final int typeface;
        private final String title;

        public int getTypeface() {
            return typeface;
        }

        public String getTitle() {
            return title;
        }

        FontTypeComment(int resId, String title) {
            this.typeface = resId;
            this.title = title;
        }
    }
    public enum FontTypeTitle {
        Slab(RobotoTypefaceManager.Typeface.ROBOTO_SLAB_LIGHT, "Slab Light"),
        SlabReg(RobotoTypefaceManager.Typeface.ROBOTO_SLAB_REGULAR, "Slab Regular"),
        Condensed(RobotoTypefaceManager.Typeface.ROBOTO_CONDENSED_LIGHT, "Condensed Light"),
        CondensedReg(RobotoTypefaceManager.Typeface.ROBOTO_CONDENSED_REGULAR, "Condensed Regular"),
        Light(RobotoTypefaceManager.Typeface.ROBOTO_LIGHT, "Light"),
        Regular(RobotoTypefaceManager.Typeface.ROBOTO_REGULAR, "Regular");

        private final int typeface;
        private final String title;

        public int getTypeface() {
            return typeface;
        }

        public String getTitle() {
            return title;
        }

        FontTypeTitle(int resId, String title) {
            this.typeface = resId;
            this.title = title;
        }
    }
}
