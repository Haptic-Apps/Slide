package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.devspark.robototextview.RobotoTypefaces;

import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Visuals.FontPreferences;

/**
 * Created by carlo_000 on 1/10/2016.
 */
public class TitleTextView extends SpoilerRobotoTextView {
    public TitleTextView(Context c) {
        super(c);
        setTypeface(c);
    }

    public TitleTextView(Context c, AttributeSet attrs) {
        super(c, attrs);
        setTypeface(c);
    }

    public TitleTextView(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
        setTypeface(c);
    }

    private void setTypeface(Context c) {
        if (!isInEditMode()) {
            int type = new FontPreferences(getContext()).getFontTypeTitle().getTypeface();
            Typeface typeface;
            if (type >= 0) {
                typeface = RobotoTypefaces.obtainTypeface(c, type);
            } else {
                typeface = Typeface.DEFAULT;
            }
            setTypeface(typeface);
        }
    }

}
