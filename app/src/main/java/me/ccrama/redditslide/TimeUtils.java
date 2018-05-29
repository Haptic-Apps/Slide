package me.ccrama.redditslide;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by ccrama on 3/1/2015.
 */
public class TimeUtils {

    private static final long SECOND_MILLIS = 1000;
    private static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final long HOUR_MILLIS   = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS    = 24 * HOUR_MILLIS;
    private static final long YEAR_MILLIS   = 365 * DAY_MILLIS;
    private static final long MONTH_MILLIS  = 30 * DAY_MILLIS;

    private TimeUtils() {
    }


    public static String getTimeAgo(long time, Context c) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return c.getString(R.string.time_just_now);
        } else if (diff < HOUR_MILLIS) {
            int minutes = longToInt(diff / MINUTE_MILLIS);
            return c.getString(R.string.time_minutes_short, minutes);
        } else if (diff < DAY_MILLIS) {
            int hours = longToInt(diff / HOUR_MILLIS);
            return c.getString(R.string.time_hours_short, hours);
        } else if (diff < YEAR_MILLIS) {
            int days = longToInt(diff / DAY_MILLIS);
            return c.getString(R.string.time_days_short, days);
        } else {
            int years = longToInt(diff / YEAR_MILLIS);
            return c.getString(R.string.time_years_short, years);
        }
    }

    public static String getTimeSince(long time, Context c) {
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
        if (diff < SECOND_MILLIS) {
            return res.getQuantityString(R.plurals.time_seconds, 0, 0);
        } else if (diff < MINUTE_MILLIS) {
            int seconds = longToInt(diff / MINUTE_MILLIS);
            return res.getQuantityString(R.plurals.time_seconds, seconds, seconds);
        } else if (diff < HOUR_MILLIS) {
            int minutes = longToInt(diff / MINUTE_MILLIS);
            return res.getQuantityString(R.plurals.time_minutes, minutes, minutes);
        } else if (diff < DAY_MILLIS) {
            int hours = longToInt(diff / HOUR_MILLIS);
            return res.getQuantityString(R.plurals.time_hours, hours, hours);
        } else if (diff < MONTH_MILLIS) {
            int days = longToInt(diff / DAY_MILLIS);
            return res.getQuantityString(R.plurals.time_days, days, days);
        } else if (diff < YEAR_MILLIS) {
            int months = longToInt(diff / MONTH_MILLIS);
            return res.getQuantityString(R.plurals.time_months, months, months);
        } else {
            int years = longToInt(diff / YEAR_MILLIS);
            return res.getQuantityString(R.plurals.time_years, years, years);
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

        if (hours > 0) hour = res.getQuantityString(R.plurals.time_hours, hours, hours);
        if (minutes > 0) minute = res.getQuantityString(R.plurals.time_minutes, minutes, minutes);
        return hour.isEmpty() ? minute : hour + " " + minute;
    }

}