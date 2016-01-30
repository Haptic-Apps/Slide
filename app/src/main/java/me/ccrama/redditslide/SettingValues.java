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
    public static final String PREF_SINGLE = "Single";
    public static final String PREF_ANIMATION = "Animation";
    public static final String PREF_FAB = "Fab";
    public static final String PREF_FAB_TYPE = "FabType";
    public static final String PREF_DAY_TIME = "day";
    public static final String PREF_NIGHT_TIME = "night";
    public static final String PREF_AUTOTHEME = "autotime";
    public static final String PREF_COLOR_BACK = "colorBack";
    public static final String PREF_COLOR_NAV_BAR = "colorNavBar";
    public static final String PREF_IMAGE_SOLID_BACKGROUND = "imageViewerSolidBackground";
    public static final String PREF_COLOR_EVERYWHERE = "colorEverywhere";
    public static final String PREF_ALPHABETICAL_DRAWER = "alphabetical_home";
    public static final String PREF_USERNAME_CLICK = "UsernameClick";
    public static final String PREF_SWAP = "Swap";
    public static final String PREFS_WEB = "web";
    public static final String PREF_CACHE = "cache";
    public static final String PREF_CACHE_DEFAULT = "cacheDefault";
    public static final String PREF_CUSTOMTABS = "customtabs";
    public static final String PREF_SCROLL_SEEN = "scrollSeen";
    public static final String PREF_HIDE_HEADER = "hideHeader";
    public static final String PREF_TITLE_FILTERS = "titleFilters";
    public static final String PREF_TEXT_FILTERS = "textFilters";
    public static final String PREF_DOMAIN_FILTERS = "domainFilters";
    public static final String PREF_DUAL_PORTRAIT = "dualPortrait";
    public static final String PREF_CROP_IMAGE = "cropImage";
    public static final String PREF_SWIPE_ANYWHERE = "swipeAnywhere";
    public static final String PREF_ALBUM = "album";
    public static final String PREF_GIF = "gif";
    public static final String PREF_VIDEO = "video";
    public static final String PREF_EXIT = "Exit";
    public static final String PREF_FASTSCROLL = "Fastscroll";
    public static final String PREF_FAB_CLEAR = "fabClear";
    public static final String PREF_HIDEBUTTON = "Hidebutton";
    public static final String PREF_SAVE_BUTTON = "saveButton";
    public static final String PREF_IMAGE = "image";
    public static final String PREF_BLUR = "blur";
    public static final String PREF_ALBUM_SWIPE = "albumswipe";

    public static boolean actionBarVisible;
    public static CreateCardView.CardEnum defaultCardView;
    public static Sorting defaultSorting;
    public static TimePeriod timePeriod;
    public static CommentSort defaultCommentSorting;
    public static boolean NSFWPreviews;
    public static boolean middleImage;
    public static boolean bigPicEnabled;
    public static boolean bigPicCropped;
    public static ColorMatchingMode colorMatchingMode;
    public static ColorIndicator colorIndicator;
    public static Palette.ThemeEnum theme;
    public static SharedPreferences prefs;
    public static boolean single;
    public static boolean animation;
    public static boolean swap;
    public static boolean album;
    public static boolean cache;
    public static boolean expandedSettings;
    public static boolean hideHeader;
    public static boolean alphabetical_home;
    public static boolean cacheDefault;
    public static boolean image;
    public static boolean video;
    public static boolean colorBack;
    public static boolean colorNavBar;
    public static boolean imageViewerSolidBackground;
    public static boolean fullscreen;
    public static boolean blurCheck;
    public static boolean swipeAnywhere;
    public static boolean scrollSeen;
    public static boolean saveButton;
    public static boolean colorEverywhere;
    public static boolean gif;
    public static boolean web;
    public static boolean exit;
    public static boolean cropImage;
    public static String titleFilters;
    public static String textFilters;
    public static String domainFilters;
    public static boolean fastscroll;
    public static boolean fab = true;
    public static int fabType = R.integer.FAB_POST;
    public static boolean click_user_name_to_profile = true;
    public static boolean hideButton;
    public static boolean tabletUI;
    public static boolean customtabs;
    public static boolean dualPortrait;
    public static int nighttime;
    public static int daytime;
    public static boolean autoTime;
    public static boolean albumSwipe;

    public static void setAllValues(SharedPreferences settings) {
        prefs = settings;
        actionBarVisible = settings.getBoolean("actionBarVisibleNew", true);
        defaultCardView = CreateCardView.CardEnum.valueOf(settings.getString("defaultCardViewNew", "LARGE").toUpperCase());
        middleImage = settings.getBoolean("middleImage", false);

        bigPicCropped = settings.getBoolean("bigPicCropped", false);
        bigPicEnabled = settings.getBoolean("bigPicEnabled", true);

        NSFWPreviews = settings.getBoolean("NSFWPreviewsNew", false);
        colorMatchingMode = ColorMatchingMode.valueOf(settings.getString("ccolorMatchingModeNew", "MATCH_EXTERNALLY"));
        colorIndicator = ColorIndicator.valueOf(settings.getString("colorIndicatorNew", "CARD_BACKGROUND"));
        defaultSorting = Sorting.valueOf(settings.getString("defaultSorting", "HOT"));
        timePeriod = TimePeriod.valueOf(settings.getString("timePeriod", "DAY"));
        defaultCommentSorting = CommentSort.valueOf(settings.getString("defaultCommentSorting", "CONFIDENCE"));

        single = prefs.getBoolean(PREF_SINGLE, false);
        animation = prefs.getBoolean(PREF_ANIMATION, false);
        blurCheck = prefs.getBoolean(PREF_BLUR, false);

        fab = prefs.getBoolean(PREF_FAB, false);
        fabType = prefs.getInt(PREF_FAB_TYPE, R.integer.FAB_POST);
        nighttime = prefs.getInt(PREF_DAY_TIME, 20);
        daytime = prefs.getInt(PREF_NIGHT_TIME, 6);
        autoTime = prefs.getBoolean(PREF_AUTOTHEME, false);
        colorBack = prefs.getBoolean(PREF_COLOR_BACK, false);
        colorNavBar = prefs.getBoolean(PREF_COLOR_NAV_BAR, false);
        imageViewerSolidBackground = prefs.getBoolean(PREF_IMAGE_SOLID_BACKGROUND, false);
        alphabetical_home = prefs.getBoolean(PREF_ALPHABETICAL_DRAWER, true);
        colorEverywhere = prefs.getBoolean(PREF_COLOR_EVERYWHERE, true);

        click_user_name_to_profile = prefs.getBoolean(PREF_USERNAME_CLICK, true);
        swap = prefs.getBoolean(PREF_SWAP, false);
        web = prefs.getBoolean(PREFS_WEB, true);
        image = prefs.getBoolean(PREF_IMAGE, true);
        cache = prefs.getBoolean(PREF_CACHE, true);
        cacheDefault = prefs.getBoolean(PREF_CACHE_DEFAULT, false);
        customtabs = prefs.getBoolean(PREF_CUSTOMTABS, true);
        scrollSeen = prefs.getBoolean(PREF_SCROLL_SEEN, false);
        hideHeader = prefs.getBoolean(PREF_HIDE_HEADER, false);
        titleFilters = prefs.getString(PREF_TITLE_FILTERS, "");
        textFilters = prefs.getString(PREF_TEXT_FILTERS, "");
        domainFilters = prefs.getString(PREF_DOMAIN_FILTERS, "");
        dualPortrait = prefs.getBoolean(PREF_DUAL_PORTRAIT, false);

        cropImage = prefs.getBoolean(PREF_CROP_IMAGE, true);

        swipeAnywhere = prefs.getBoolean(PREF_SWIPE_ANYWHERE, false);
        album = prefs.getBoolean(PREF_ALBUM, true);
        albumSwipe = prefs.getBoolean(PREF_ALBUM_SWIPE, true);

        gif = prefs.getBoolean(PREF_GIF, true);
        video = prefs.getBoolean(PREF_VIDEO, true);
        exit = prefs.getBoolean(PREF_EXIT, true);
        fastscroll = prefs.getBoolean(PREF_FASTSCROLL, false);

        hideButton = prefs.getBoolean(PREF_HIDEBUTTON, false);
        saveButton = prefs.getBoolean(PREF_SAVE_BUTTON, false);
    }

    public enum ColorIndicator {
        CARD_BACKGROUND, TEXT_COLOR, NONE

    }

    public enum ColorMatchingMode {
        ALWAYS_MATCH, MATCH_EXTERNALLY

    }


}
