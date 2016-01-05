package me.ccrama.redditslide;

import android.content.Context;

/**
 * Created by carlo_000 on 10/7/2015.
 */
class StartupStrings {
    public static String[] startupStrings(Context context) {
        return new String[]{
                context.getString(R.string.startup_cats),
                context.getString(R.string.startup_banana),
                context.getString(R.string.startup_vine),
                context.getString(R.string.startup_duARTe), //Praise him!
                context.getString(R.string.startup_karma),
                context.getString(R.string.startup_pitchforks),
                context.getString(R.string.startup_downvotes),
                context.getString(R.string.startup_science),
                context.getString(R.string.startup_gold),
                context.getString(R.string.startup_shower),
                context.getString(R.string.startup_ftfy),
                context.getString(R.string.startup_frontpage),
                context.getString(R.string.startup_upvotes)
        };
    }
}
