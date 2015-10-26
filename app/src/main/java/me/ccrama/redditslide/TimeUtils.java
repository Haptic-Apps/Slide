package me.ccrama.redditslide;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by ccrama on 3/1/2015.
 */
public class TimeUtils {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time, Context c) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        Resources res = c.getResources();

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return res.getString(R.string.time_just_now);
        } else if (diff < 50 * MINUTE_MILLIS) {
            Integer value = longToInt(diff / MINUTE_MILLIS);
            return res.getQuantityString(R.plurals.time_minutes, value, value);
        } else if (diff < 24 * HOUR_MILLIS) {
            Integer value = longToInt(diff / HOUR_MILLIS);
            return res.getQuantityString(R.plurals.time_hours, value, value);
        } else {
            Integer value = longToInt(diff / DAY_MILLIS);
            return res.getQuantityString(R.plurals.time_days, value, value);
        }

    }

    private static Integer longToInt(Long temp) {
        return temp.intValue();
    }
}
