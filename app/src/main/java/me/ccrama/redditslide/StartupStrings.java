package me.ccrama.redditslide;

import android.content.Context;

/**
 * Created by carlo_000 on 10/7/2015.
 */
public class StartupStrings {
    public static String[] startupStrings(Context context) {
        return new String[]{
                context.getString(R.string.splash_cats),
                context.getString(R.string.slash_banana),
                context.getString(R.string.splash_vine)
        };
    }
}
