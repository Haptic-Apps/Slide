package me.ccrama.redditslide;

import android.content.SharedPreferences;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Visuals.Pallete;

/**
 * Created by ccrama on 9/19/2015.
 */
public class SettingValues {
    public static boolean actionBarVisible;
    public static boolean largeThumbnails;
    public static boolean croppedImage;
    public static boolean infoBar;

    public static CreateCardView.CardEnum defaultCardView;
    public static Sorting defaultSorting;
    public static TimePeriod timePeriod;
    public static CommentSort defaultCommentSorting;
    public static boolean NSFWPreviews;
    public static ColorMatchingMode colorMatchingMode;
    public static ColorIndicator colorIndicator;
    public static Pallete.ThemeEnum theme;
    public static enum ColorIndicator{
       CARD_BACKGROUND, TEXT_COLOR, NONE;

    }
    public static enum ColorMatchingMode{
        ALWAYS_MATCH, MATCH_EXTERNALLY;

    }
    public static SharedPreferences prefs;
    public static void setAllValues(SharedPreferences settings){prefs = settings;
        actionBarVisible = settings.getBoolean("actionBarVisible", true);
        largeThumbnails = settings.getBoolean("largeThumbnails", true);
        croppedImage = settings.getBoolean("croppedImage", false);
        defaultCardView = CreateCardView.CardEnum.valueOf(settings.getString("defaultCardView", "LARGE").toUpperCase());
        NSFWPreviews = settings.getBoolean("NSFWPreviews", false);
        colorMatchingMode = ColorMatchingMode.valueOf(settings.getString("colorMatchingMode", "ALWAYS_MATCH"));
        colorIndicator = ColorIndicator.valueOf(settings.getString("colorIndicator", "CARD_BACKGROUND"));
        infoBar = settings.getBoolean("infoBar", true);
        defaultSorting = Sorting.valueOf(settings.getString("defaultSorting", "HOT"));
        timePeriod = TimePeriod.valueOf(settings.getString("timePeriod", "DAY"));
        defaultCommentSorting = CommentSort.valueOf(settings.getString("defaultCommentSorting", "TOP"));
    }
}
