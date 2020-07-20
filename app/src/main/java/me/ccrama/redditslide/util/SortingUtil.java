package me.ccrama.redditslide.util;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubmissionSearchPaginator;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.ccrama.redditslide.Activities.Search;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;

public class SortingUtil {
    public static final Map<String, TimePeriod>              times  = new HashMap<>();
    public static       SubmissionSearchPaginator.SearchSort search =
            SubmissionSearchPaginator.SearchSort.RELEVANCE;
    public static Sorting    defaultSorting;
    public static TimePeriod timePeriod;

    public static Integer getSortingId(Sorting sort) {
        switch (sort) {
            case NEW:
                return 1;
            case RISING:
                return 2;
            case TOP:
                return 3;
            case CONTROVERSIAL:
                return 4;
            case BEST:
                return 5;
            case HOT:
            default:
                return 0;
        }
    }

    public static String[] getSearch() {
        return new String[]{
                Reddit.getAppContext().getString(R.string.search_relevance),
                Reddit.getAppContext().getString(R.string.search_top),
                Reddit.getAppContext().getString(R.string.search_new),
                Reddit.getAppContext().getString(R.string.search_comments)
        };
    }

    private static Integer getSortingTimeId(TimePeriod time) {
        switch (time) {
            case DAY:
                return 1;
            case WEEK:
                return 2;
            case MONTH:
                return 3;
            case YEAR:
                return 4;
            case ALL:
                return 5;
            case HOUR:
            default:
                return 0;
        }
    }

    public static Integer getSortingId(String subreddit) {
        subreddit = subreddit.toLowerCase(Locale.ENGLISH);
        Sorting sort = sorting.containsKey(subreddit) ? sorting.get(subreddit) : defaultSorting;

        return getSortingId(sort);
    }

    public static Integer getSortingTimeId(String subreddit) {
        subreddit = subreddit.toLowerCase(Locale.ENGLISH);
        TimePeriod time =
                times.containsKey(subreddit) ? times.get(subreddit) : SortingUtil.timePeriod;

        return getSortingTimeId(time);
    }

    public static Spannable[] getSortingSpannables(String currentSub) {
        return getSortingSpannables(getSortingId(currentSub), currentSub);

    }

    public static Spannable[] getSortingSpannables(Sorting sorting) {
        return getSortingSpannables(getSortingId(sorting), " ");
    }

    public static Integer getSortingSearchId(Search s) {
        return s.time == TimePeriod.HOUR ? 0 : s.time == TimePeriod.DAY ? 1
                : s.time == TimePeriod.WEEK ? 2
                        : s.time == TimePeriod.MONTH ? 3 : s.time == TimePeriod.YEAR ? 4 : 5;
    }

    public static Integer getSearchType() {
        return search == SubmissionSearchPaginator.SearchSort.RELEVANCE ? 0
                : search == SubmissionSearchPaginator.SearchSort.TOP ? 1
                        : search == SubmissionSearchPaginator.SearchSort.NEW ? 2 : 3;
    }

    public static Spannable[] getSortingTimesSpannables(String currentSub) {
        return getSortingTimesSpannables(getSortingTimeId(currentSub), currentSub);
    }

    public static Spannable[] getSortingTimesSpannables(TimePeriod time) {
        return getSortingTimesSpannables(getSortingTimeId(time), " ");
    }

    public static String[] getSortingStrings() {
        return new String[]{
                Reddit.getAppContext().getString(R.string.sorting_hot),
                Reddit.getAppContext().getString(R.string.sorting_new),
                Reddit.getAppContext().getString(R.string.sorting_rising),
                Reddit.getAppContext().getString(R.string.sorting_top),
                Reddit.getAppContext().getString(R.string.sorting_controversial),
                Reddit.getAppContext().getString(R.string.sorting_best),
        };
    }

    public static String[] getSortingCommentsStrings() {
        return new String[]{
                Reddit.getAppContext().getString(R.string.sorting_best),
                Reddit.getAppContext().getString(R.string.sorting_top),
                Reddit.getAppContext().getString(R.string.sorting_new),
                Reddit.getAppContext().getString(R.string.sorting_controversial),
                Reddit.getAppContext().getString(R.string.sorting_old),
                Reddit.getAppContext().getString(R.string.sorting_ama),
        };
    }

    public static String[] getSortingTimesStrings() {
        return new String[]{
                Reddit.getAppContext().getString(R.string.sorting_hour),
                Reddit.getAppContext().getString(R.string.sorting_day),
                Reddit.getAppContext().getString(R.string.sorting_week),
                Reddit.getAppContext().getString(R.string.sorting_month),
                Reddit.getAppContext().getString(R.string.sorting_year),
                Reddit.getAppContext().getString(R.string.sorting_all),
        };
    }

    public static TimePeriod getTime(String subreddit, TimePeriod defaultTime) {
        subreddit = subreddit.toLowerCase(Locale.ENGLISH);
        if (times.containsKey(subreddit)) {
            return times.get(subreddit);
        } else {
            return defaultTime;
        }
    }

    public static void setTime(String s, TimePeriod sort) {
        times.put(s.toLowerCase(Locale.ENGLISH), sort);
    }

    private static Spannable[] getSortingSpannables(int sortingId, String sub) {
        ArrayList<Spannable> spannables = new ArrayList<>();
        String[] sortingStrings = getSortingStrings();
        for (int i = 0; i < sortingStrings.length; i++) {
            SpannableString spanString = new SpannableString(sortingStrings[i]);
            if (i == sortingId) {
                spanString.setSpan(new ForegroundColorSpan(
                                new ColorPreferences(Reddit.getAppContext()).getColor(sub)), 0,
                        spanString.length(), 0);
                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
            }
            spannables.add(spanString);
        }
        return spannables.toArray(new Spannable[0]);
    }

    private static Spannable[] getSortingTimesSpannables(int sortingId, String sub) {
        ArrayList<Spannable> spannables = new ArrayList<>();
        String[] sortingStrings = getSortingTimesStrings();
        for (int i = 0; i < sortingStrings.length; i++) {
            SpannableString spanString = new SpannableString(sortingStrings[i]);
            if (i == sortingId) {
                spanString.setSpan(new ForegroundColorSpan(
                                new ColorPreferences(Reddit.getAppContext()).getColor(sub)), 0,
                        spanString.length(), 0);
                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
            }
            spannables.add(spanString);
        }
        return spannables.toArray(new Spannable[0]);
    }

    public static void setSorting(String s, Sorting sort) {
        sorting.put(s.toLowerCase(Locale.ENGLISH), sort);
    }

    public static final Map<String, Sorting> sorting = new HashMap<>();

    public static Sorting getSorting(String subreddit, Sorting defaultSort) {
        subreddit = subreddit.toLowerCase(Locale.ENGLISH);
        if (sorting.containsKey(subreddit)) {
            return sorting.get(subreddit);
        } else {
            return defaultSort;
        }
    }
}
