package me.ccrama.redditslide.Visuals;

import android.content.Context;
import android.content.SharedPreferences;

import com.devspark.robototextview.RobotoTypefaces;

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
    public void setTitleFont(FontTypeTitle style) {
        edit().putString(FONT_TITLE, style.name()).commit();
    }
    public enum FontStyle {
        Tiny(R.style.FontStyle_TinyPost, R.string.font_size_tiny),
        Smaller(R.style.FontStyle_SmallerPost, R.string.font_size_smaller),
        Small(R.style.FontStyle_SmallPost, R.string.font_size_small),
        Medium(R.style.FontStyle_MediumPost, R.string.font_size_medium),
        Large(R.style.FontStyle_LargePost, R.string.font_size_large),
        Larger(R.style.FontStyle_LargerPost, R.string.font_size_larger),
        Huge(R.style.FontStyle_HugePost, R.string.font_size_huge);

        private final int resId;
        private final int title;

        public int getResId() {
            return resId;
        }

        public int getTitle() {
            return title;
        }

        FontStyle(int resId, int title) {
            this.resId = resId;
            this.title = title;
        }
    }

    public enum FontStyleComment {
        Smaller(R.style.FontStyle_SmallerComment, R.string.font_size_smaller),
        Small(R.style.FontStyle_SmallComment, R.string.font_size_small),
        Medium(R.style.FontStyle_MediumComment, R.string.font_size_medium),
        Large(R.style.FontStyle_LargeComment, R.string.font_size_large),
        Larger(R.style.FontStyle_LargerComment, R.string.font_size_larger),
        Huge(R.style.FontStyle_HugeComment, R.string.font_size_huge);

        private final int resId;
        private final int title;

        public int getResId() {
            return resId;
        }

        public int getTitle() {
            return title;
        }

        FontStyleComment(int resId, int title) {
            this.resId = resId;
            this.title = title;
        }
    }



    public enum FontTypeComment {
        Slab(RobotoTypefaces.TYPEFACE_ROBOTO_SLAB_REGULAR, "Slab"),
        Condensed(RobotoTypefaces.TYPEFACE_ROBOTO_CONDENSED_REGULAR, "Condensed"),
        Light(RobotoTypefaces.TYPEFACE_ROBOTO_LIGHT, "Light"),
        Regular(RobotoTypefaces.TYPEFACE_ROBOTO_REGULAR, "Regular"),
        System(-1, "System");

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
        SlabLight(RobotoTypefaces.TYPEFACE_ROBOTO_SLAB_LIGHT, "Slab Light"),
        SlabRegular(RobotoTypefaces.TYPEFACE_ROBOTO_SLAB_REGULAR, "Slab Regular"),
        CondensedBold(RobotoTypefaces.TYPEFACE_ROBOTO_CONDENSED_BOLD, "Condensed Bold"),
        CondensedLight(RobotoTypefaces.TYPEFACE_ROBOTO_CONDENSED_LIGHT, "Condensed Light"),
        CondensedRegular(RobotoTypefaces.TYPEFACE_ROBOTO_CONDENSED_REGULAR, "Condensed Regular"),
        Light(RobotoTypefaces.TYPEFACE_ROBOTO_LIGHT, "Light"),
        Regular(RobotoTypefaces.TYPEFACE_ROBOTO_REGULAR, "Regular"),
        Bold(RobotoTypefaces.TYPEFACE_ROBOTO_BOLD, "Bold"),
        Medium(RobotoTypefaces.TYPEFACE_ROBOTO_MEDIUM, "Medium"),
        System(-1, "System");

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
