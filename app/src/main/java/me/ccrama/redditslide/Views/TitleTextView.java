package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.devspark.robototextview.util.RobotoTypefaceManager;

import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Visuals.FontPreferences;

/**
 * Created by carlo_000 on 1/10/2016.
 */
public class TitleTextView extends SpoilerRobotoTextView {
    public TitleTextView(Context c) {
        super(c);
        if(!isInEditMode()) {
            int type = new FontPreferences(getContext()).getFontTypeTitle().getTypeface();
            if (type >= 0) {
                Typeface typeface = RobotoTypefaceManager.obtainTypeface(
                        c,
                        type);
                setTypeface(typeface);
            }
        }
    }

    public TitleTextView(Context c, AttributeSet attrs) {
        super(c, attrs);
        if(!isInEditMode()) {
            int type = new FontPreferences(getContext()).getFontTypeTitle().getTypeface();
            if (type >= 0) {
                Typeface typeface = RobotoTypefaceManager.obtainTypeface(
                        c,
                        type);
                setTypeface(typeface);
            }
        }
    }

    public TitleTextView(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
        if(!isInEditMode()) {
            int type = new FontPreferences(getContext()).getFontTypeTitle().getTypeface();
            if (type >= 0) {
                Typeface typeface = RobotoTypefaceManager.obtainTypeface(
                        c,
                        type);
                setTypeface(typeface);
            }
        }
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // set fitting lines to prevent cut text
        int fittingLines = h / this.getLineHeight();
        if (fittingLines > 0) {
            this.setLines(fittingLines);
        }
    }
}
