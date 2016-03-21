package me.ccrama.redditslide;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by ccrama on 3/1/2015.
 */
public class TimeUtils {

    private static final long SECOND_MILLIS = 1000;
    private static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long YEAR_MILLIS = 365 * DAY_MILLIS;
    private static final long MONTH_MILLIS = 30 * DAY_MILLIS;


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
        } else if (diff < 60 * MINUTE_MILLIS) {
            Integer value = longToInt(diff / MINUTE_MILLIS);
            return  value + "m";
        } else if (diff < 24 * HOUR_MILLIS) {
            Integer value = longToInt(diff / HOUR_MILLIS);
            return value + "h";
        } else {
            Integer value = longToInt(diff / DAY_MILLIS);
            return value + "d";
        }

    }
    public static String getLengthTimeSince(long time, Context c) {
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
        if (diff < YEAR_MILLIS) {
            return longToInt(diff / MONTH_MILLIS) + " month"+ (longToInt(diff / MONTH_MILLIS) > 1?"s":"");
        } else  {
            Integer value = longToInt(diff / YEAR_MILLIS);
            return  value + " year" + (value > 1?"s":"");
        }

    }

    private static Integer longToInt(Long temp) {
        return temp.intValue();
    }

    public static String getTimeInHoursAndMins(int mins, Context c) {
        int hours = mins / 60;
        int minutes = mins - (hours * 60);
        Resources res = c.getResources();
        String hour = "";
        String minute = "";
        if (hours > 0)
            hour = res.getQuantityString(R.plurals.time_hours, hours, hours);
        if (minutes > 0)
            minute = res.getQuantityString(R.plurals.time_minutes, minutes, minutes);
        return hour.isEmpty() ? minute : hour + " " + minute ;
    }

}