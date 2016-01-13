package me.ccrama.redditslide;

import android.content.SharedPreferences;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by ccrama on 9/19/2015.
 */
public class SettingValues {
    public static boolean actionBarVisible;
    public static CreateCardView.CardEnum defaultCardView;
    public static Sorting defaultSorting;
    public static TimePeriod timePeriod;
    public static CommentSort defaultCommentSorting;
    public static boolean NSFWPreviews;
    public static boolean NSFWPosts;
    public static boolean middleImage;
    public static boolean bigPicEnabled;
    public static boolean bigPicCropped;

    public static ColorMatchingMode colorMatchingMode;
    public static ColorIndicator colorIndicator;
    public static Palette.ThemeEnum theme;
    public static SharedPreferences prefs;

    public static void setAllValues(SharedPreferences settings) {
        prefs = settings;
        actionBarVisible = settings.getBoolean("actionBarVisibleNew", true);
        defaultCardView = CreateCardView.CardEnum.valueOf(settings.getString("defaultCardViewNew", "LARGE").toUpperCase());
        middleImage =settings.getBoolean("middleImage", false);

        NSFWPosts = settings.getBoolean("NSFWPostsNew", false);
        bigPicCropped = settings.getBoolean("bigPicCropped", false);
        bigPicEnabled = settings.getBoolean("bigPicEnabled", true);

        NSFWPreviews = settings.getBoolean("NSFWPreviewsNew", false);
        colorMatchingMode = ColorMatchingMode.valueOf(settings.getString("ccolorMatchingModeNew", "MATCH_EXTERNALLY"));
        colorIndicator = ColorIndicator.valueOf(settings.getString("colorIndicatorNew", "CARD_BACKGROUND"));
        defaultSorting = Sorting.valueOf(settings.getString("defaultSorting", "HOT"));
        timePeriod = TimePeriod.valueOf(settings.getString("timePeriod", "DAY"));
        defaultCommentSorting = CommentSort.valueOf(settings.getString("defaultCommentSorting", "CONFIDENCE"));
    }

    public enum ColorIndicator {
        CARD_BACKGROUND, TEXT_COLOR, NONE

    }

    public enum ColorMatchingMode {
        ALWAYS_MATCH, MATCH_EXTERNALLY

    }


}
