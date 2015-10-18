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
    public static InfoBar infoBar;
    public static CreateCardView.CardEnum defaultCardView;
    public static boolean secondaryActionBarVisible;
    public static InfoBar secondaryInfoBar;
    public static CreateCardView.CardEnum secondaryDefaultCardView;
    public static Sorting defaultSorting;
    public static TimePeriod timePeriod;
    public static CommentSort defaultCommentSorting;
    public static boolean NSFWPreviews;
    public static ColorMatchingMode colorMatchingMode;
    public static ColorIndicator colorIndicator;
    public static Pallete.ThemeEnum theme;
    public  enum ColorIndicator{
       CARD_BACKGROUND, TEXT_COLOR, NONE

    }
    public  enum ColorMatchingMode{
        ALWAYS_MATCH, MATCH_EXTERNALLY

    }
    public  enum InfoBar{
        BIG_PICTURE, BIG_PICTURE_CROPPED, INFO_BAR, THUMBNAIL, NONE

    }
    public static SharedPreferences prefs;
    public static void setAllValues(SharedPreferences settings){prefs = settings;
        actionBarVisible = settings.getBoolean("actionBarVisibleNew", true);
        defaultCardView = CreateCardView.CardEnum.valueOf(settings.getString("defaultCardViewNew", "LARGE").toUpperCase());
        infoBar = InfoBar.valueOf(settings.getString("infoBarTypeNew", "BIG_PICTURE"));
        secondaryActionBarVisible = settings.getBoolean("secondactionBarVisibleNew", true);
        secondaryDefaultCardView = CreateCardView.CardEnum.valueOf(settings.getString("seconddefaultCardViewNew", "LARGE").toUpperCase());
        secondaryInfoBar = InfoBar.valueOf(settings.getString("secondinfoBarTypeNew", "BIG_PICTURE"));

        NSFWPreviews = settings.getBoolean("NSFWPreviewsNew", false);
        colorMatchingMode = ColorMatchingMode.valueOf(settings.getString("ccolorMatchingModeNew", "MATCH_EXTERNALLY"));
        colorIndicator = ColorIndicator.valueOf(settings.getString("colorIndicatorNew", "CARD_BACKGROUND"));
        defaultSorting = Sorting.valueOf(settings.getString("defaultSorting", "HOT"));
        timePeriod = TimePeriod.valueOf(settings.getString("timePeriod", "DAY"));
        defaultCommentSorting = CommentSort.valueOf(settings.getString("defaultCommentSorting", "TOP"));
    }
}
