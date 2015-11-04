package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.TypedValue;

import me.ccrama.redditslide.R;

/**
 * Created by carlo_000 on 10/8/2015.
 */
public class GeneralSwipeRefreshLayout extends SwipeRefreshLayout {
    //TODO more to come

    public GeneralSwipeRefreshLayout(Context context) {

        super(context);
        setProgressBackgroundColorSchemeColor(getStyleAttribColorValue(getContext(), R.attr.card_background, Color.parseColor("#ffffff")));
    }

    public GeneralSwipeRefreshLayout(Context context, AttributeSet attrs) {

        super(context, attrs);
        setProgressBackgroundColorSchemeColor(getStyleAttribColorValue(getContext(), R.attr.card_background, Color.parseColor("#ffffff")));

    }
    public static int getStyleAttribColorValue(final Context context, final int attribResId, final int defaultValue) {
        final TypedValue tv = new TypedValue();
        final boolean found = context.getTheme().resolveAttribute(attribResId, tv, true);
        return found ? tv.data : defaultValue;
    }
}