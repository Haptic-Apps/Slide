package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.devspark.robototextview.util.RobotoTypefaceManager;
import com.devspark.robototextview.widget.RobotoTextView;

import me.ccrama.redditslide.Visuals.FontPreferences;

/**
 * Created by carlo_000 on 1/10/2016.
 */
public class TitleTextView extends RobotoTextView {
    public TitleTextView(Context c) {
        super(c);
        Typeface typeface = RobotoTypefaceManager.obtainTypeface(
                c,
                new FontPreferences(c).getFontTypeTitle().getTypeface());
        setTypeface(typeface);
    }

    public TitleTextView(Context c, AttributeSet attrs) {
        super(c, attrs);
        Typeface typeface = RobotoTypefaceManager.obtainTypeface(
                c,
                new FontPreferences(c).getFontTypeTitle().getTypeface());
        setTypeface(typeface);
    }

    public TitleTextView(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
        Typeface typeface = RobotoTypefaceManager.obtainTypeface(
                c,
                new FontPreferences(c).getFontTypeTitle().getTypeface());
        setTypeface(typeface);
    }
}
