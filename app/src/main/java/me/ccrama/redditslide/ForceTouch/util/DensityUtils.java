package me.ccrama.redditslide.ForceTouch.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Convert between DP and PX
 */
public class DensityUtils {
    public static int toPx(Context context, int dp) {
        return convert(context, dp, TypedValue.COMPLEX_UNIT_DIP);
    }

    public static int toDp(Context context, int px) {
        return convert(context, px, TypedValue.COMPLEX_UNIT_PX);
    }

    private static int convert(Context context, int amount, int conversionUnit) {
        if (amount < 0) {
            throw new IllegalArgumentException("px should not be less than zero");
        }

        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(conversionUnit, amount, r.getDisplayMetrics());
    }
}
