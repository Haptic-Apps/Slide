package me.ccrama.redditslide;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Pair;

import androidx.core.content.ContextCompat;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Activities.Slide;

/**
 * Created by ccrama on 7/9/2015.
 */
public class ColorPreferences {

    private final static String USER_THEME_DELIMITER = "$USER$";
    public final static  String FONT_STYLE           = "THEME";
    private final Context context;

    public ColorPreferences(Context context) {
        this.context = context;
    }

    public static int[] getColors(Context context, int c) {
        if (c == ContextCompat.getColor(context, R.color.md_red_500)) {
            return new int[]{
                    //  ContextCompat.getColor(context, R.color.md_red_100),
                    ContextCompat.getColor(context, R.color.md_red_200),
                    ContextCompat.getColor(context, R.color.md_red_300),
                    ContextCompat.getColor(context, R.color.md_red_400),
                    ContextCompat.getColor(context, R.color.md_red_500),
                    ContextCompat.getColor(context, R.color.md_red_600),
                    ContextCompat.getColor(context, R.color.md_red_700),
                    ContextCompat.getColor(context, R.color.md_red_800),
                    ContextCompat.getColor(context, R.color.md_red_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_pink_500)) {
            return new int[]{
                    //     ContextCompat.getColor(context, R.color.md_pink_100),
                    ContextCompat.getColor(context, R.color.md_pink_200),
                    ContextCompat.getColor(context, R.color.md_pink_300),
                    ContextCompat.getColor(context, R.color.md_pink_400),
                    ContextCompat.getColor(context, R.color.md_pink_500),
                    ContextCompat.getColor(context, R.color.md_pink_600),
                    ContextCompat.getColor(context, R.color.md_pink_700),
                    ContextCompat.getColor(context, R.color.md_pink_800),
                    ContextCompat.getColor(context, R.color.md_pink_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_purple_500)) {
            return new int[]{
                    //     ContextCompat.getColor(context, R.color.md_purple_100),
                    ContextCompat.getColor(context, R.color.md_purple_200),
                    ContextCompat.getColor(context, R.color.md_purple_300),
                    ContextCompat.getColor(context, R.color.md_purple_400),
                    ContextCompat.getColor(context, R.color.md_purple_500),
                    ContextCompat.getColor(context, R.color.md_purple_600),
                    ContextCompat.getColor(context, R.color.md_purple_700),
                    ContextCompat.getColor(context, R.color.md_purple_800),
                    ContextCompat.getColor(context, R.color.md_purple_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_deep_purple_500)) {
            return new int[]{
                    // ContextCompat.getColor(context, R.color.md_deep_purple_100),
                    ContextCompat.getColor(context, R.color.md_deep_purple_200),
                    ContextCompat.getColor(context, R.color.md_deep_purple_300),
                    ContextCompat.getColor(context, R.color.md_deep_purple_400),
                    ContextCompat.getColor(context, R.color.md_deep_purple_500),
                    ContextCompat.getColor(context, R.color.md_deep_purple_600),
                    ContextCompat.getColor(context, R.color.md_deep_purple_700),
                    ContextCompat.getColor(context, R.color.md_deep_purple_800),
                    ContextCompat.getColor(context, R.color.md_deep_purple_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_indigo_500)) {
            return new int[]{
                    //   ContextCompat.getColor(context, R.color.md_indigo_100),
                    ContextCompat.getColor(context, R.color.md_indigo_200),
                    ContextCompat.getColor(context, R.color.md_indigo_300),
                    ContextCompat.getColor(context, R.color.md_indigo_400),
                    ContextCompat.getColor(context, R.color.md_indigo_500),
                    ContextCompat.getColor(context, R.color.md_indigo_600),
                    ContextCompat.getColor(context, R.color.md_indigo_700),
                    ContextCompat.getColor(context, R.color.md_indigo_800),
                    ContextCompat.getColor(context, R.color.md_indigo_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_blue_500)) {
            return new int[]{
                    //   ContextCompat.getColor(context, R.color.md_blue_100),
                    ContextCompat.getColor(context, R.color.md_blue_200),
                    ContextCompat.getColor(context, R.color.md_blue_300),
                    ContextCompat.getColor(context, R.color.md_blue_400),
                    ContextCompat.getColor(context, R.color.md_blue_500),
                    ContextCompat.getColor(context, R.color.md_blue_600),
                    ContextCompat.getColor(context, R.color.md_blue_700),
                    ContextCompat.getColor(context, R.color.md_blue_800),
                    ContextCompat.getColor(context, R.color.md_blue_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_light_blue_500)) {
            return new int[]{
                    //  ContextCompat.getColor(context, R.color.md_light_blue_100),
                    ContextCompat.getColor(context, R.color.md_light_blue_200),
                    ContextCompat.getColor(context, R.color.md_light_blue_300),
                    ContextCompat.getColor(context, R.color.md_light_blue_400),
                    ContextCompat.getColor(context, R.color.md_light_blue_500),
                    ContextCompat.getColor(context, R.color.md_light_blue_600),
                    ContextCompat.getColor(context, R.color.md_light_blue_700),
                    ContextCompat.getColor(context, R.color.md_light_blue_800),
                    ContextCompat.getColor(context, R.color.md_light_blue_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_cyan_500)) {
            return new int[]{
                    //   ContextCompat.getColor(context, R.color.md_cyan_100),
                    ContextCompat.getColor(context, R.color.md_cyan_200),
                    ContextCompat.getColor(context, R.color.md_cyan_300),
                    ContextCompat.getColor(context, R.color.md_cyan_400),
                    ContextCompat.getColor(context, R.color.md_cyan_500),
                    ContextCompat.getColor(context, R.color.md_cyan_600),
                    ContextCompat.getColor(context, R.color.md_cyan_700),
                    ContextCompat.getColor(context, R.color.md_cyan_800),
                    ContextCompat.getColor(context, R.color.md_cyan_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_teal_500)) {
            return new int[]{
                    //  ContextCompat.getColor(context, R.color.md_teal_100),
                    ContextCompat.getColor(context, R.color.md_teal_200),
                    ContextCompat.getColor(context, R.color.md_teal_300),
                    ContextCompat.getColor(context, R.color.md_teal_400),
                    ContextCompat.getColor(context, R.color.md_teal_500),
                    ContextCompat.getColor(context, R.color.md_teal_600),
                    ContextCompat.getColor(context, R.color.md_teal_700),
                    ContextCompat.getColor(context, R.color.md_teal_800),
                    ContextCompat.getColor(context, R.color.md_teal_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_green_500)) {
            return new int[]{
                    //  ContextCompat.getColor(context, R.color.md_green_100),
                    ContextCompat.getColor(context, R.color.md_green_200),
                    ContextCompat.getColor(context, R.color.md_green_300),
                    ContextCompat.getColor(context, R.color.md_green_400),
                    ContextCompat.getColor(context, R.color.md_green_500),
                    ContextCompat.getColor(context, R.color.md_green_600),
                    ContextCompat.getColor(context, R.color.md_green_700),
                    ContextCompat.getColor(context, R.color.md_green_800),
                    ContextCompat.getColor(context, R.color.md_green_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_light_green_500)) {
            return new int[]{
                    //   ContextCompat.getColor(context, R.color.md_light_green_100),
                    //  ContextCompat.getColor(context, R.color.md_light_green_200),
                    ContextCompat.getColor(context, R.color.md_light_green_300),
                    ContextCompat.getColor(context, R.color.md_light_green_400),
                    ContextCompat.getColor(context, R.color.md_light_green_500),
                    ContextCompat.getColor(context, R.color.md_light_green_600),
                    ContextCompat.getColor(context, R.color.md_light_green_700),
                    ContextCompat.getColor(context, R.color.md_light_green_800),
                    ContextCompat.getColor(context, R.color.md_light_green_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_lime_500)) {
            return new int[]{
                    //  ContextCompat.getColor(context, R.color.md_lime_100),
                    // ContextCompat.getColor(context, R.color.md_lime_200),
                    ContextCompat.getColor(context, R.color.md_lime_300),
                    ContextCompat.getColor(context, R.color.md_lime_400),
                    ContextCompat.getColor(context, R.color.md_lime_500),
                    ContextCompat.getColor(context, R.color.md_lime_600),
                    ContextCompat.getColor(context, R.color.md_lime_700),
                    ContextCompat.getColor(context, R.color.md_lime_800),
                    ContextCompat.getColor(context, R.color.md_lime_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_yellow_500)) {
            return new int[]{
                    //  ContextCompat.getColor(context, R.color.md_yellow_100),
                    //ContextCompat.getColor(context, R.color.md_yellow_200),
                    // ContextCompat.getColor(context, R.color.md_yellow_300),
                    ContextCompat.getColor(context, R.color.md_yellow_400),
                    ContextCompat.getColor(context, R.color.md_yellow_500),
                    ContextCompat.getColor(context, R.color.md_yellow_600),
                    ContextCompat.getColor(context, R.color.md_yellow_700),
                    ContextCompat.getColor(context, R.color.md_yellow_800),
                    ContextCompat.getColor(context, R.color.md_yellow_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_amber_500)) {
            return new int[]{
                    //  ContextCompat.getColor(context, R.color.md_amber_100),
                    // ContextCompat.getColor(context, R.color.md_amber_200),
                    ContextCompat.getColor(context, R.color.md_amber_300),
                    ContextCompat.getColor(context, R.color.md_amber_400),
                    ContextCompat.getColor(context, R.color.md_amber_500),
                    ContextCompat.getColor(context, R.color.md_amber_600),
                    ContextCompat.getColor(context, R.color.md_amber_700),
                    ContextCompat.getColor(context, R.color.md_amber_800),
                    ContextCompat.getColor(context, R.color.md_amber_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_orange_500)) {
            return new int[]{
                    //  ContextCompat.getColor(context, R.color.md_orange_100),
                    //   ContextCompat.getColor(context, R.color.md_orange_200),
                    ContextCompat.getColor(context, R.color.md_orange_300),
                    ContextCompat.getColor(context, R.color.md_orange_400),
                    ContextCompat.getColor(context, R.color.md_orange_500),
                    ContextCompat.getColor(context, R.color.md_orange_600),
                    ContextCompat.getColor(context, R.color.md_orange_700),
                    ContextCompat.getColor(context, R.color.md_orange_800),
                    ContextCompat.getColor(context, R.color.md_orange_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_deep_orange_500)) {
            return new int[]{
                    // ContextCompat.getColor(context, R.color.md_deep_orange_100),
                    ContextCompat.getColor(context, R.color.md_deep_orange_200),
                    ContextCompat.getColor(context, R.color.md_deep_orange_300),
                    ContextCompat.getColor(context, R.color.md_deep_orange_400),
                    ContextCompat.getColor(context, R.color.md_deep_orange_500),
                    ContextCompat.getColor(context, R.color.md_deep_orange_600),
                    ContextCompat.getColor(context, R.color.md_deep_orange_700),
                    ContextCompat.getColor(context, R.color.md_deep_orange_800),
                    ContextCompat.getColor(context, R.color.md_deep_orange_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_brown_500)) {
            return new int[]{
                    // ContextCompat.getColor(context, R.color.md_brown_100),
                    ContextCompat.getColor(context, R.color.md_brown_200),
                    ContextCompat.getColor(context, R.color.md_brown_300),
                    ContextCompat.getColor(context, R.color.md_brown_400),
                    ContextCompat.getColor(context, R.color.md_brown_500),
                    ContextCompat.getColor(context, R.color.md_brown_600),
                    ContextCompat.getColor(context, R.color.md_brown_700),
                    ContextCompat.getColor(context, R.color.md_brown_800),
                    ContextCompat.getColor(context, R.color.md_brown_900)
            };
        } else if (c == ContextCompat.getColor(context, R.color.md_grey_500)) {
            return new int[]{
                    //    ContextCompat.getColor(context, R.color.md_grey_100),
                    //  ContextCompat.getColor(context, R.color.md_grey_200),
                    // ContextCompat.getColor(context, R.color.md_grey_300),
                    ContextCompat.getColor(context, R.color.md_grey_400),
                    ContextCompat.getColor(context, R.color.md_grey_500),
                    ContextCompat.getColor(context, R.color.md_grey_600),
                    ContextCompat.getColor(context, R.color.md_grey_700),
                    ContextCompat.getColor(context, R.color.md_grey_800),
                    ContextCompat.getColor(context, R.color.md_grey_900),
                    Color.parseColor("#000000")
            };
        } else {
            return new int[]{
                    //  ContextCompat.getColor(context, R.color.md_blue_grey_100),
                    //    ContextCompat.getColor(context, R.color.md_blue_grey_200),
                    Color.parseColor("#5C94C8"),
                    ContextCompat.getColor(context, R.color.md_blue_grey_300),
                    ContextCompat.getColor(context, R.color.md_blue_grey_400),
                    ContextCompat.getColor(context, R.color.md_blue_grey_500),
                    ContextCompat.getColor(context, R.color.md_blue_grey_600),
                    ContextCompat.getColor(context, R.color.md_blue_grey_700),
                    ContextCompat.getColor(context, R.color.md_blue_grey_800),
                    ContextCompat.getColor(context, R.color.md_blue_grey_900)
            };

        }
    }

    public static String getIconName(Context context, int c) {
        String result = Slide.class.getPackage().getName() + ".Slide";
        if (c == ContextCompat.getColor(context, R.color.md_red_200)
                || c == ContextCompat.getColor(context, R.color.md_red_300)
                || c == ContextCompat.getColor(context, R.color.md_red_400)
                || c == ContextCompat.getColor(context, R.color.md_red_500)
                || c == ContextCompat.getColor(context, R.color.md_red_600)
                || c == ContextCompat.getColor(context, R.color.md_red_700)
                || c == ContextCompat.getColor(context, R.color.md_red_800)
                || c == ContextCompat.getColor(context, R.color.md_red_900)) {
            result += "Red";

        } else if (c == ContextCompat.getColor(context, R.color.md_pink_200)
                || c == ContextCompat.getColor(context, R.color.md_pink_300)
                || c == ContextCompat.getColor(context, R.color.md_pink_400)
                || c == ContextCompat.getColor(context, R.color.md_pink_500)
                || c == ContextCompat.getColor(context, R.color.md_pink_600)
                || c == ContextCompat.getColor(context, R.color.md_pink_700)
                || c == ContextCompat.getColor(context, R.color.md_pink_800)
                || c == ContextCompat.getColor(context, R.color.md_pink_900)) {
            result += "Pink";

        } else if (c == ContextCompat.getColor(context, R.color.md_purple_200) || c == ContextCompat
                .getColor(context, R.color.md_purple_300) || c == ContextCompat.getColor(context,
                R.color.md_purple_400) || c == ContextCompat.getColor(context,
                R.color.md_purple_500) || c == ContextCompat.getColor(context,
                R.color.md_purple_600) || c == ContextCompat.getColor(context,
                R.color.md_purple_700) || c == ContextCompat.getColor(context,
                R.color.md_purple_800) || c == ContextCompat.getColor(context,
                R.color.md_purple_900)) {
            result += "Purple";

        } else if (c == ContextCompat.getColor(context, R.color.md_deep_purple_200)
                || c == ContextCompat.getColor(context, R.color.md_deep_purple_300)
                || c == ContextCompat.getColor(context, R.color.md_deep_purple_400)
                || c == ContextCompat.getColor(context, R.color.md_deep_purple_500)
                || c == ContextCompat.getColor(context, R.color.md_deep_purple_600)
                || c == ContextCompat.getColor(context, R.color.md_deep_purple_700)
                || c == ContextCompat.getColor(context, R.color.md_deep_purple_800)
                || c == ContextCompat.getColor(context, R.color.md_deep_purple_900)) {
            result += "DeepPurple";

        } else if (c == ContextCompat.getColor(context, R.color.md_indigo_200) || c == ContextCompat
                .getColor(context, R.color.md_indigo_300) || c == ContextCompat.getColor(context,
                R.color.md_indigo_400) || c == ContextCompat.getColor(context,
                R.color.md_indigo_500) || c == ContextCompat.getColor(context,
                R.color.md_indigo_600) || c == ContextCompat.getColor(context,
                R.color.md_indigo_700) || c == ContextCompat.getColor(context,
                R.color.md_indigo_800) || c == ContextCompat.getColor(context,
                R.color.md_indigo_900)) {
            result += "Indigo";

        } else if (c == ContextCompat.getColor(context, R.color.md_blue_200)
                || c == ContextCompat.getColor(context, R.color.md_blue_300)
                || c == ContextCompat.getColor(context, R.color.md_blue_400)
                || c == ContextCompat.getColor(context, R.color.md_blue_500)
                || c == ContextCompat.getColor(context, R.color.md_blue_600)
                || c == ContextCompat.getColor(context, R.color.md_blue_700)
                || c == ContextCompat.getColor(context, R.color.md_blue_800)
                || c == ContextCompat.getColor(context, R.color.md_blue_900)) {
            result += "Blue";

        } else if (c == ContextCompat.getColor(context, R.color.md_light_blue_200)
                || c == ContextCompat.getColor(context, R.color.md_light_blue_300)
                || c == ContextCompat.getColor(context, R.color.md_light_blue_400)
                || c == ContextCompat.getColor(context, R.color.md_light_blue_500)
                || c == ContextCompat.getColor(context, R.color.md_light_blue_600)
                || c == ContextCompat.getColor(context, R.color.md_light_blue_700)
                || c == ContextCompat.getColor(context, R.color.md_light_blue_800)
                || c == ContextCompat.getColor(context, R.color.md_light_blue_900)) {
            result += "LightBlue";

        } else if (c == ContextCompat.getColor(context, R.color.md_cyan_200)
                || c == ContextCompat.getColor(context, R.color.md_cyan_300)
                || c == ContextCompat.getColor(context, R.color.md_cyan_400)
                || c == ContextCompat.getColor(context, R.color.md_cyan_500)
                || c == ContextCompat.getColor(context, R.color.md_cyan_600)
                || c == ContextCompat.getColor(context, R.color.md_cyan_700)
                || c == ContextCompat.getColor(context, R.color.md_cyan_800)
                || c == ContextCompat.getColor(context, R.color.md_cyan_900)) {
            result += "Cyan";

        } else if (c == ContextCompat.getColor(context, R.color.md_teal_200)
                || c == ContextCompat.getColor(context, R.color.md_teal_300)
                || c == ContextCompat.getColor(context, R.color.md_teal_400)
                || c == ContextCompat.getColor(context, R.color.md_teal_500)
                || c == ContextCompat.getColor(context, R.color.md_teal_600)
                || c == ContextCompat.getColor(context, R.color.md_teal_700)
                || c == ContextCompat.getColor(context, R.color.md_teal_800)
                || c == ContextCompat.getColor(context, R.color.md_teal_900)) {
            result += "Teal";

        } else if (c == ContextCompat.getColor(context, R.color.md_green_200)
                || c == ContextCompat.getColor(context, R.color.md_green_300)
                || c == ContextCompat.getColor(context, R.color.md_green_400)
                || c == ContextCompat.getColor(context, R.color.md_green_500)
                || c == ContextCompat.getColor(context, R.color.md_green_600)
                || c == ContextCompat.getColor(context, R.color.md_green_700)
                || c == ContextCompat.getColor(context, R.color.md_green_800)
                || c == ContextCompat.getColor(context, R.color.md_green_900)) {
            result += "Green";

        } else if (c == ContextCompat.getColor(context, R.color.md_light_green_200)
                || c == ContextCompat.getColor(context, R.color.md_light_green_300)
                || c == ContextCompat.getColor(context, R.color.md_light_green_400)
                || c == ContextCompat.getColor(context, R.color.md_light_green_500)
                || c == ContextCompat.getColor(context, R.color.md_light_green_600)
                || c == ContextCompat.getColor(context, R.color.md_light_green_700)
                || c == ContextCompat.getColor(context, R.color.md_light_green_800)
                || c == ContextCompat.getColor(context, R.color.md_light_green_900)) {
            result += "LightGreen";

        } else if (c == ContextCompat.getColor(context, R.color.md_lime_200)
                || c == ContextCompat.getColor(context, R.color.md_lime_300)
                || c == ContextCompat.getColor(context, R.color.md_lime_400)
                || c == ContextCompat.getColor(context, R.color.md_lime_500)
                || c == ContextCompat.getColor(context, R.color.md_lime_600)
                || c == ContextCompat.getColor(context, R.color.md_lime_700)
                || c == ContextCompat.getColor(context, R.color.md_lime_800)
                || c == ContextCompat.getColor(context, R.color.md_lime_900)) {
            result += "Lime";

        } else if (c == ContextCompat.getColor(context, R.color.md_yellow_400) || c == ContextCompat
                .getColor(context, R.color.md_yellow_500) || c == ContextCompat.getColor(context,
                R.color.md_yellow_600) || c == ContextCompat.getColor(context,
                R.color.md_yellow_700) || c == ContextCompat.getColor(context,
                R.color.md_yellow_800) || c == ContextCompat.getColor(context,
                R.color.md_yellow_900)) {
            result += "Yellow";

        } else if (c == ContextCompat.getColor(context, R.color.md_amber_200)
                || c == ContextCompat.getColor(context, R.color.md_amber_300)
                || c == ContextCompat.getColor(context, R.color.md_amber_400)
                || c == ContextCompat.getColor(context, R.color.md_amber_500)
                || c == ContextCompat.getColor(context, R.color.md_amber_600)
                || c == ContextCompat.getColor(context, R.color.md_amber_700)
                || c == ContextCompat.getColor(context, R.color.md_amber_800)
                || c == ContextCompat.getColor(context, R.color.md_amber_900)) {
            result += "Amber";

        } else if (c == ContextCompat.getColor(context, R.color.md_orange_200) || c == ContextCompat
                .getColor(context, R.color.md_orange_300) || c == ContextCompat.getColor(context,
                R.color.md_orange_400) || c == ContextCompat.getColor(context,
                R.color.md_orange_500) || c == ContextCompat.getColor(context,
                R.color.md_orange_600) || c == ContextCompat.getColor(context,
                R.color.md_orange_700) || c == ContextCompat.getColor(context,
                R.color.md_orange_800) || c == ContextCompat.getColor(context,
                R.color.md_orange_900)) {
            result += "Orange";

        } else if (c == ContextCompat.getColor(context, R.color.md_deep_orange_200)
                || c == ContextCompat.getColor(context, R.color.md_deep_orange_300)
                || c == ContextCompat.getColor(context, R.color.md_deep_orange_400)
                || c == ContextCompat.getColor(context, R.color.md_deep_orange_500)
                || c == ContextCompat.getColor(context, R.color.md_deep_orange_600)
                || c == ContextCompat.getColor(context, R.color.md_deep_orange_700)
                || c == ContextCompat.getColor(context, R.color.md_deep_orange_800)
                || c == ContextCompat.getColor(context, R.color.md_deep_orange_900)) {
            result += "DeepOrange";

        } else if (c == ContextCompat.getColor(context, R.color.md_brown_200)
                || c == ContextCompat.getColor(context, R.color.md_brown_300)
                || c == ContextCompat.getColor(context, R.color.md_brown_400)
                || c == ContextCompat.getColor(context, R.color.md_brown_500)
                || c == ContextCompat.getColor(context, R.color.md_brown_600)
                || c == ContextCompat.getColor(context, R.color.md_brown_700)
                || c == ContextCompat.getColor(context, R.color.md_brown_800)
                || c == ContextCompat.getColor(context, R.color.md_brown_900)) {
            result += "Brown";

        } else if (c == ContextCompat.getColor(context, R.color.md_grey_300)
                || c == ContextCompat.getColor(context, R.color.md_grey_400)
                || c == ContextCompat.getColor(context, R.color.md_grey_500)
                || c == ContextCompat.getColor(context, R.color.md_grey_600)
                || c == ContextCompat.getColor(context, R.color.md_grey_700)
                || c == ContextCompat.getColor(context, R.color.md_grey_800)
                || c == ContextCompat.getColor(context, R.color.md_grey_900)) {
            result += "Grey";
        } else if (c == ContextCompat.getColor(context, R.color.md_blue_grey_200)
                || c == ContextCompat.getColor(context, R.color.md_blue_grey_300)
                || c == ContextCompat.getColor(context, R.color.md_blue_grey_400)
                || c == ContextCompat.getColor(context, R.color.md_blue_grey_500)
                || c == ContextCompat.getColor(context, R.color.md_blue_grey_600)
                || c == ContextCompat.getColor(context, R.color.md_blue_grey_700)
                || c == ContextCompat.getColor(context, R.color.md_blue_grey_800)
                || c == ContextCompat.getColor(context, R.color.md_blue_grey_900)) {
            result += "BlueGrey";
        } else {
            result += "Default";
        }
        return result;
    }

    public static int[] getBaseColors(Context context) {
        return new int[]{
                ContextCompat.getColor(context, R.color.md_red_500),
                ContextCompat.getColor(context, R.color.md_pink_500),
                ContextCompat.getColor(context, R.color.md_purple_500),
                ContextCompat.getColor(context, R.color.md_deep_purple_500),
                ContextCompat.getColor(context, R.color.md_indigo_500),
                ContextCompat.getColor(context, R.color.md_blue_500),
                ContextCompat.getColor(context, R.color.md_light_blue_500),
                ContextCompat.getColor(context, R.color.md_cyan_500),
                ContextCompat.getColor(context, R.color.md_teal_500),
                ContextCompat.getColor(context, R.color.md_green_500),
                ContextCompat.getColor(context, R.color.md_light_green_500),
                ContextCompat.getColor(context, R.color.md_lime_500),
                ContextCompat.getColor(context, R.color.md_yellow_500),
                ContextCompat.getColor(context, R.color.md_amber_500),
                ContextCompat.getColor(context, R.color.md_orange_500),
                ContextCompat.getColor(context, R.color.md_deep_orange_500),
                ContextCompat.getColor(context, R.color.md_brown_500),
                ContextCompat.getColor(context, R.color.md_blue_grey_500),
                ContextCompat.getColor(context, R.color.md_grey_500)
        };
    }

    public static int getNumColorsFromThemeType(int themeType) {
        int num = 0;
        for (Theme theme : Theme.values()) {
            if (themeType == theme.getThemeType()) {
                num++;
            }
        }
        return num;
    }

    protected SharedPreferences open() {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    protected SharedPreferences.Editor edit() {
        return open().edit();
    }

    private String getUserThemeName(String themeName, String defaultValue) {
        String userTheme =
                open().getString(themeName + USER_THEME_DELIMITER + Authentication.name,
                        null);
        if (userTheme != null) {
            return userTheme.split(StringEscapeUtils.escapeJava(USER_THEME_DELIMITER))[0];
        } else {
            return open().getString(themeName, defaultValue);
        }
    }

    private void setUserThemeName(String themeName, String defaultValue) {
        edit().putString(themeName + USER_THEME_DELIMITER + Authentication.name, defaultValue)
                .commit();
    }

    public Theme getFontStyle() {
        try {
            if (SettingValues.isNight()) {
                return getColoredTheme(SettingValues.nightTheme,
                        getUserThemeName(FONT_STYLE, Theme.dark_amber.name()),
                        Theme.valueOf(open().getString(FONT_STYLE, Theme.dark_amber.name())));
            }
            return Theme.valueOf(getUserThemeName(FONT_STYLE, Theme.dark_amber.name()));
        } catch (Exception e) {
            return Theme.dark_amber;
        }
    }

    public Theme getFontStyleSubreddit(String s) {
        try {
            return Theme.valueOf(getUserThemeName(s, getFontStyle().name()));
        } catch (Exception e) {
            return Theme.dark_amber;
        }
    }

    public void setFontStyle(Theme style) {
        setUserThemeName(FONT_STYLE, style.name());
    }

    public int getThemeSubreddit(String s) {
        int back = getFontStyle().getThemeType();
        String str = getUserThemeName(s.toLowerCase(Locale.ENGLISH), getFontStyle().getTitle());

        if (Theme.valueOf(str).getThemeType() != back) {
            String[] names = str.split("_");
            String name = names[names.length - 1];
            for (Theme theme : Theme.values()) {
                if (theme.toString().contains(name) && theme.getThemeType() == back) {
                    setFontStyle(theme, s);
                    return theme.baseId;
                }
            }
        } else {

            return Theme.valueOf(str).baseId;


        }
        return getFontStyle().baseId;

    }

    public Theme getThemeSubreddit(String s, boolean b) {
        if(s == null){
            s = "Promoted";
        }
        int back = getFontStyle().getThemeType();
        String str = getUserThemeName(s.toLowerCase(Locale.ENGLISH), getFontStyle().getTitle());
        try {
            if (Theme.valueOf(str).getThemeType() != back) {
                String[] names = str.split("_");
                String name = names[names.length - 1];
                for (Theme theme : Theme.values()) {
                    if (theme.toString().contains(name) && theme.getThemeType() == back) {
                        setFontStyle(theme, s);
                        return theme;
                    }
                }
            } else {

                return Theme.valueOf(str);


            }
        } catch (Exception ignored) {

        }
        return getFontStyle();

    }

    public int getDarkThemeSubreddit(String s) {
        return getColoredTheme(4,
                getUserThemeName(s.toLowerCase(Locale.ENGLISH), getFontStyle().getTitle()),
                getFontStyle()).baseId;
    }

    private Theme getColoredTheme(int i, String base, Theme defaultTheme) {
        try {
            if (Theme.valueOf(base).getThemeType() != i) {
                String[] names = base.split("_");
                String name = names[names.length - 1];
                for (Theme theme : Theme.values()) {
                    if (theme.toString().contains(name) && theme.getThemeType() == i) {
                        return theme;
                    }
                }
            } else {
                return Theme.valueOf(base);
            }
        } catch (Exception ignored) {

        }
        return defaultTheme;
    }

    public void setFontStyle(Theme style, String s) {
        setUserThemeName(s.toLowerCase(Locale.ENGLISH), style.name());
    }

    public void removeFontStyle(String subreddit) {
        edit().remove(subreddit + USER_THEME_DELIMITER + Authentication.name).commit();
    }

    public int getColor(String s) {
        return ContextCompat.getColor(context, getThemeSubreddit(s, true).getColor());
    }

    public enum Theme {
        dark_white(R.style.white_dark, "dark_white", R.color.md_blue_grey_200,
                ColorThemeOptions.Dark.getValue()), light_white(R.style.white_light, "light_white",
                R.color.md_blue_grey_200, ColorThemeOptions.Light.getValue()), amoled_white(
                R.style.white_amoled, "amoled_white", R.color.md_blue_grey_200,
                ColorThemeOptions.AMOLED.getValue()), blue_white(R.style.white_blue, "blue_white",
                R.color.md_blue_grey_200,
                ColorThemeOptions.DarkBlue.getValue()), amoled_light_white(
                R.style.white_AMOLED_lighter, "amoled_light_white", R.color.md_blue_grey_200,
                ColorThemeOptions.AMOLEDContrast.getValue()),

        dark_pink(R.style.pink_dark, "dark_pink", R.color.md_pink_A200,
                ColorThemeOptions.Dark.getValue()), light_pink(R.style.pink_light, "light_pink",
                R.color.md_pink_A200, ColorThemeOptions.Light.getValue()), amoled_pink(
                R.style.pink_amoled, "amoled_pink", R.color.md_pink_A200,
                ColorThemeOptions.AMOLED.getValue()), blue_pink(R.style.pink_blue, "blue_pink",
                R.color.md_pink_A200, ColorThemeOptions.DarkBlue.getValue()), amoled_light_pink(
                R.style.pink_AMOLED_lighter, "amoled_light_pink", R.color.md_pink_A200,
                ColorThemeOptions.AMOLEDContrast.getValue()),

        dark_deeporange(R.style.deeporange_dark, "dark_deeporange", R.color.md_deep_orange_A400,
                ColorThemeOptions.Dark.getValue()), light_deeporange(R.style.deeporange_LIGHT,
                "light_deeporange", R.color.md_deep_orange_A400,
                ColorThemeOptions.Light.getValue()), amoled_deeporange(R.style.deeporange_AMOLED,
                "amoled_deeporange", R.color.md_deep_orange_A400,
                ColorThemeOptions.AMOLED.getValue()), blue_deeporange(R.style.deeporange_blue,
                "blue_deeporange", R.color.md_deep_orange_A400,
                ColorThemeOptions.DarkBlue.getValue()), amoled_light_deeporange(
                R.style.deeporange_AMOLED_lighter, "amoled_light_deeporange",
                R.color.md_deep_orange_A400, ColorThemeOptions.AMOLEDContrast.getValue()),

        dark_amber(R.style.amber_dark, "dark_amber", R.color.md_amber_A400,
                ColorThemeOptions.Dark.getValue()), light_amber(R.style.amber_LIGHT, "light_amber",
                R.color.md_amber_A400, ColorThemeOptions.Light.getValue()), amoled_amber(
                R.style.amber_AMOLED, "amoled_amber", R.color.md_amber_A400,
                ColorThemeOptions.AMOLED.getValue()), blue_amber(R.style.amber_blue, "blue_amber",
                R.color.md_amber_A400, ColorThemeOptions.DarkBlue.getValue()), amoled_light_amber(
                R.style.amber_AMOLED_lighter, "amoled_light_amber", R.color.md_amber_A400,
                ColorThemeOptions.AMOLEDContrast.getValue()),

        dark_yellow(R.style.yellow_dark, "dark_yellow", R.color.md_yellow_A400,
                ColorThemeOptions.Dark.getValue()), light_yellow(R.style.yellow_LIGHT,
                "light_yellow", R.color.md_yellow_A400,
                ColorThemeOptions.Light.getValue()), amoled_yellow(R.style.yellow_AMOLED,
                "amoled_yellow", R.color.md_yellow_A400,
                ColorThemeOptions.AMOLED.getValue()), blue_yellow(R.style.yellow_blue,
                "blue_yellow", R.color.md_yellow_A400,
                ColorThemeOptions.DarkBlue.getValue()), amoled_light_yellow(
                R.style.yellow_AMOLED_lighter, "amoled_light_yellow", R.color.md_yellow_A400,
                ColorThemeOptions.AMOLEDContrast.getValue()),

        dark_lime(R.style.lime_dark, "dark_lime", R.color.md_lime_A400,
                ColorThemeOptions.Dark.getValue()), light_lime(R.style.lime_LIGHT, "light_lime",
                R.color.md_lime_A400, ColorThemeOptions.Light.getValue()), amoled_lime(
                R.style.lime_AMOLED, "amoled_lime", R.color.md_lime_A400,
                ColorThemeOptions.AMOLED.getValue()), blue_lime(R.style.lime_blue, "blue_lime",
                R.color.md_lime_A400, ColorThemeOptions.DarkBlue.getValue()), amoled_light_lime(
                R.style.lime_AMOLED_lighter, "amoled_light_lime", R.color.md_lime_A400,
                ColorThemeOptions.AMOLEDContrast.getValue()),

        dark_green(R.style.green_dark, "dark_green", R.color.md_green_A400,
                ColorThemeOptions.Dark.getValue()), light_green(R.style.green_LIGHT, "light_green",
                R.color.md_green_A400, ColorThemeOptions.Light.getValue()), amoled_green(
                R.style.green_AMOLED, "amoled_green", R.color.md_green_A400,
                ColorThemeOptions.AMOLED.getValue()), blue_green(R.style.green_blue, "blue_green",
                R.color.md_green_A400, ColorThemeOptions.DarkBlue.getValue()), amoled_light_green(
                R.style.green_AMOLED_lighter, "amoled_light_green", R.color.md_green_A400,
                ColorThemeOptions.AMOLEDContrast.getValue()),

        dark_teal(R.style.teal_dark, "dark_teal", R.color.md_teal_A700,
                ColorThemeOptions.Dark.getValue()), light_teal(R.style.teal_light, "light_teal",
                R.color.md_teal_A700, ColorThemeOptions.Light.getValue()), amoled_teal(
                R.style.teal_amoled, "amoled_teal", R.color.md_teal_A700,
                ColorThemeOptions.AMOLED.getValue()), blue_teal(R.style.teal_blue, "blue_teal",
                R.color.md_teal_A700, ColorThemeOptions.DarkBlue.getValue()), amoled_light_teal(
                R.style.teal_AMOLED_lighter, "amoled_light_teal", R.color.md_teal_A700,
                ColorThemeOptions.AMOLEDContrast.getValue()),

        dark_cyan(R.style.cyan_dark, "dark_cyan", R.color.md_cyan_A400,
                ColorThemeOptions.Dark.getValue()), light_cyan(R.style.cyan_LIGHT, "light_cyan",
                R.color.md_cyan_A400, ColorThemeOptions.Light.getValue()), amoled_cyan(
                R.style.cyan_AMOLED, "amoled_cyan", R.color.md_cyan_A400,
                ColorThemeOptions.AMOLED.getValue()), blue_cyan(R.style.cyan_blue, "blue_cyan",
                R.color.md_cyan_A400, ColorThemeOptions.DarkBlue.getValue()), amoled_light_cyan(
                R.style.cyan_AMOLED_lighter, "amoled_light_cyan", R.color.md_cyan_A400,
                ColorThemeOptions.AMOLEDContrast.getValue()),

        dark_lightblue(R.style.lightblue_dark, "dark_lightblue", R.color.md_light_blue_A400,
                ColorThemeOptions.Dark.getValue()), light_lightblue(R.style.lightblue_LIGHT,
                "light_lightblue", R.color.md_light_blue_A400,
                ColorThemeOptions.Light.getValue()), amoled_lightblue(R.style.lightblue_AMOLED,
                "amoled_lightblue", R.color.md_light_blue_A400,
                ColorThemeOptions.AMOLED.getValue()), blue_lightblue(R.style.lightblue_blue,
                "blue_lightblue", R.color.md_light_blue_A400,
                ColorThemeOptions.DarkBlue.getValue()), amoled_light_lightblue(
                R.style.lightblue_AMOLED_lighter, "amoled_light_lightblue",
                R.color.md_light_blue_A400, ColorThemeOptions.AMOLEDContrast.getValue()),

        dark_blue(R.style.blue_dark, "dark_blue", R.color.md_blue_A400,
                ColorThemeOptions.Dark.getValue()), light_blue(R.style.blue_LIGHT, "light_blue",
                R.color.md_blue_A400, ColorThemeOptions.Light.getValue()), amoled_blue(
                R.style.blue_AMOLED, "amoled_blue", R.color.md_blue_A400,
                ColorThemeOptions.AMOLED.getValue()), blue_blue(R.style.blue_blue, "blue_blue",
                R.color.md_blue_A400, ColorThemeOptions.DarkBlue.getValue()), amoled_light_blue(
                R.style.blue_AMOLED_lighter, "amoled_light_blue", R.color.md_blue_A400,
                ColorThemeOptions.AMOLEDContrast.getValue()),

        dark_indigo(R.style.indigo_dark, "dark_indigo", R.color.md_indigo_A400,
                ColorThemeOptions.Dark.getValue()), light_indigo(R.style.indigo_LIGHT,
                "light_indigo", R.color.md_indigo_A400,
                ColorThemeOptions.Light.getValue()), amoled_indigo(R.style.indigo_AMOLED,
                "amoled_indigo", R.color.md_indigo_A400,
                ColorThemeOptions.AMOLED.getValue()), blue_indigo(R.style.indigo_blue,
                "blue_indigo", R.color.md_indigo_A400,
                ColorThemeOptions.DarkBlue.getValue()), amoled_light_indigo(
                R.style.indigo_AMOLED_lighter, "amoled_light_indigo", R.color.md_indigo_A400,
                ColorThemeOptions.AMOLEDContrast.getValue()),


        sepia_white(R.style.white_sepia, "sepia_white", R.color.md_blue_grey_200,
                ColorThemeOptions.Sepia.getValue()), sepia_pink(R.style.pink_sepia, "sepia_pink",
                R.color.md_pink_A200, ColorThemeOptions.Sepia.getValue()), sepia_deeporange(
                R.style.deeporange_sepia, "sepia_deeporange", R.color.md_deep_orange_A400,
                ColorThemeOptions.Sepia.getValue()), sepia_amber(R.style.amber_sepia, "sepia_amber",
                R.color.md_amber_A400, ColorThemeOptions.Sepia.getValue()), sepia_yellow(
                R.style.yellow_sepia, "sepia_yellow", R.color.md_yellow_A400,
                ColorThemeOptions.Sepia.getValue()), sepia_lime(R.style.lime_sepia, "sepia_lime",
                R.color.md_lime_A400, ColorThemeOptions.Sepia.getValue()), sepia_green(
                R.style.green_sepia, "sepia_green", R.color.md_green_A400,
                ColorThemeOptions.Sepia.getValue()), sepia_teal(R.style.teal_sepia, "sepia_teal",
                R.color.md_teal_A700, ColorThemeOptions.Sepia.getValue()), sepia_cyan(
                R.style.cyan_sepia, "sepia_cyan", R.color.md_cyan_A400,
                ColorThemeOptions.Sepia.getValue()), sepia_lightblue(R.style.lightblue_sepia,
                "sepia_lightblue", R.color.md_light_blue_A400,
                ColorThemeOptions.Sepia.getValue()), sepia_blue(R.style.blue_sepia, "sepia_blue",
                R.color.md_blue_A400, ColorThemeOptions.Sepia.getValue()), sepia_indigo(
                R.style.indigo_sepia, "sepia_indigo", R.color.md_indigo_A400,
                ColorThemeOptions.Sepia.getValue()),

        night_red_white(R.style.white_night_red, "night_red_white", R.color.md_blue_grey_200,
                ColorThemeOptions.RedShift.getValue()), night_red_pink(R.style.pink_night_red,
                "night_red_pink", R.color.md_pink_A200,
                ColorThemeOptions.RedShift.getValue()), night_red_deeporange(
                R.style.deeporange_night_red, "night_red_deeporange", R.color.md_deep_orange_A400,
                ColorThemeOptions.RedShift.getValue()), night_red_amber(R.style.amber_night_red,
                "night_red_amber", R.color.md_amber_A400,
                ColorThemeOptions.RedShift.getValue()), night_red_yellow(R.style.yellow_night_red,
                "night_red_yellow", R.color.md_yellow_A400,
                ColorThemeOptions.RedShift.getValue()), night_red_lime(R.style.lime_night_red,
                "night_red_lime", R.color.md_lime_A400,
                ColorThemeOptions.RedShift.getValue()), night_red_green(R.style.green_night_red,
                "night_red_green", R.color.md_green_A400,
                ColorThemeOptions.RedShift.getValue()), night_red_teal(R.style.teal_night_red,
                "night_red_teal", R.color.md_teal_A700,
                ColorThemeOptions.RedShift.getValue()), night_red_cyan(R.style.cyan_night_red,
                "night_red_cyan", R.color.md_cyan_A400,
                ColorThemeOptions.RedShift.getValue()), night_red_lightblue(
                R.style.lightblue_night_red, "night_red_lightblue", R.color.md_light_blue_A400,
                ColorThemeOptions.RedShift.getValue()), night_red_blue(R.style.blue_night_red,
                "night_red_blue", R.color.md_blue_A400,
                ColorThemeOptions.RedShift.getValue()), night_red_indigo(R.style.indigo_night_red,
                "night_red_indigo", R.color.md_indigo_A400, ColorThemeOptions.RedShift.getValue()),

        pixel_white(R.style.white_pixel, "pixel_white", R.color.md_blue_grey_200,
                ColorThemeOptions.Pixel.getValue()), pixel_pink(R.style.pink_pixel, "pixel_pink",
                R.color.md_pink_A200, ColorThemeOptions.Pixel.getValue()), pixel_deeporange(
                R.style.deeporange_pixel, "pixel_deeporange", R.color.md_deep_orange_A400,
                ColorThemeOptions.Pixel.getValue()), pixel_amber(R.style.amber_pixel, "pixel_amber",
                R.color.md_amber_A400, ColorThemeOptions.Pixel.getValue()), pixel_yellow(
                R.style.yellow_pixel, "pixel_yellow", R.color.md_yellow_A400,
                ColorThemeOptions.Pixel.getValue()), pixel_lime(R.style.lime_pixel, "pixel_lime",
                R.color.md_lime_A400, ColorThemeOptions.Pixel.getValue()), pixel_green(
                R.style.green_pixel, "pixel_green", R.color.md_green_A400,
                ColorThemeOptions.Pixel.getValue()), pixel_teal(R.style.teal_pixel, "pixel_teal",
                R.color.md_teal_A700, ColorThemeOptions.Pixel.getValue()), pixel_cyan(
                R.style.cyan_pixel, "pixel_cyan", R.color.md_cyan_A400,
                ColorThemeOptions.Pixel.getValue()), pixel_lightblue(R.style.lightblue_pixel,
                "pixel_lightblue", R.color.md_light_blue_A400,
                ColorThemeOptions.Pixel.getValue()), pixel_blue(R.style.blue_pixel, "pixel_blue",
                R.color.md_blue_A400, ColorThemeOptions.Pixel.getValue()), pixel_indigo(
                R.style.indigo_pixel, "pixel_indigo", R.color.md_indigo_A400,
                ColorThemeOptions.Pixel.getValue()),

        deep_white(R.style.white_deep, "deep_white", R.color.md_blue_grey_200,
                ColorThemeOptions.Deep.getValue()), deep_pink(R.style.pink_deep, "deep_pink",
                R.color.md_pink_A200, ColorThemeOptions.Deep.getValue()), deep_deeporange(
                R.style.deeporange_deep, "deep_deeporange", R.color.md_deep_orange_A400,
                ColorThemeOptions.Deep.getValue()), deep_amber(R.style.amber_deep, "deep_amber",
                R.color.md_amber_A400, ColorThemeOptions.Deep.getValue()), deep_yellow(
                R.style.yellow_deep, "deep_yellow", R.color.md_yellow_A400,
                ColorThemeOptions.Deep.getValue()), deep_lime(R.style.lime_deep, "deep_lime",
                R.color.md_lime_A400, ColorThemeOptions.Deep.getValue()), deep_green(
                R.style.green_deep, "deep_green", R.color.md_green_A400,
                ColorThemeOptions.Deep.getValue()), deep_teal(R.style.teal_deep, "deep_teal",
                R.color.md_teal_A700, ColorThemeOptions.Deep.getValue()), deep_cyan(
                R.style.cyan_deep, "deep_cyan", R.color.md_cyan_A400,
                ColorThemeOptions.Deep.getValue()), deep_lightblue(R.style.lightblue_deep,
                "deep_lightblue", R.color.md_light_blue_A400,
                ColorThemeOptions.Deep.getValue()), deep_blue(R.style.blue_deep, "deep_blue",
                R.color.md_blue_A400, ColorThemeOptions.Deep.getValue()), deep_indigo(
                R.style.indigo_deep, "deep_indigo", R.color.md_indigo_A400,
                ColorThemeOptions.Deep.getValue());

        private int baseId;
        private String title;
        private int themeType;
        private int color;

        Theme(int baseId, String title, int color, int themetype) {
            this.baseId = baseId;
            this.color = color;
            this.themeType = themetype;
            this.title = title;
        }

        public int getBaseId() {
            return baseId;
        }

        public int getThemeType() {
            return themeType;
        }

        public String getTitle() {
            return title;
        }

        public int getColor() {
            return color;
        }
    }

    public static List<Pair<Integer, Integer>> themePairList = new ArrayList<>(
            Arrays.asList(new Pair<>(R.id.dark, ColorPreferences.ColorThemeOptions.Dark.getValue()),
                    new Pair<>(R.id.light, ColorPreferences.ColorThemeOptions.Light.getValue()),
                    new Pair<>(R.id.amoled, ColorPreferences.ColorThemeOptions.AMOLED.getValue()),
                    new Pair<>(R.id.blue, ColorPreferences.ColorThemeOptions.DarkBlue.getValue()),
                    new Pair<>(R.id.amoled_contrast,
                            ColorPreferences.ColorThemeOptions.AMOLEDContrast.getValue()),
                    new Pair<>(R.id.sepia, ColorPreferences.ColorThemeOptions.Sepia.getValue()),
                    new Pair<>(R.id.red, ColorPreferences.ColorThemeOptions.RedShift.getValue()),
                    new Pair<>(R.id.pixel, ColorPreferences.ColorThemeOptions.Pixel.getValue()),
                    new Pair<>(R.id.deep, ColorPreferences.ColorThemeOptions.Deep.getValue())));

    public enum ColorThemeOptions {
        Dark(0), Light(1), AMOLED(2), DarkBlue(3), AMOLEDContrast(4), Sepia(5), RedShift(6), Pixel(
                7), Deep(8);

        private final int mValue;

        ColorThemeOptions(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }
}
