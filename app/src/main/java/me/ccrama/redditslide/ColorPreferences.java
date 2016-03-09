package me.ccrama.redditslide;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

/**
 * Created by ccrama on 7/9/2015.
 */
public class ColorPreferences {
    private final static String FONT_STYLE = "THEME";

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
                    ContextCompat.getColor(context, R.color.md_light_green_200),
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
                    ContextCompat.getColor(context, R.color.md_lime_200),
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
                    ContextCompat.getColor(context, R.color.md_amber_200),
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
                    ContextCompat.getColor(context, R.color.md_orange_200),
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

    protected SharedPreferences open() {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    protected SharedPreferences.Editor edit() {
        return open().edit();
    }

    public Theme getFontStyle() {
        try {
            return Theme.valueOf(open().getString(FONT_STYLE,  Theme.dark_deeporange.name()));
        } catch (Exception e) {
            return Theme.dark_amber;
        }
    }
    public Theme getFontStyleSubreddit(String s) {
        try {
            return Theme.valueOf(open().getString(s, getFontStyle().name()));
        } catch (Exception e) {
            return Theme.dark_amber;
        }
    }
    public void setFontStyle(Theme style) {
        edit().putString(FONT_STYLE, style.name()).commit();
    }

    public int getThemeOverview() {
        switch (getFontStyle().getThemeType()) {
            case 0:
                return R.style.white_dark;
            case 1:
                return R.style.white_light;
            case 2:
                return R.style.white_amoled;
            default:
                return R.style.white_blue;
        }
    }

    public int getThemeSubreddit(String s) {

        String str = open().getString(s.toLowerCase(), getFontStyle().getTitle());

        if (Theme.valueOf(str).getThemeType() != Reddit.themeBack) {
            String name = str.split("_")[1];
            for (Theme theme : Theme.values()) {
                if (theme.toString().contains(name) && theme.getThemeType() == Reddit.themeBack) {
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

        String str = open().getString(s.toLowerCase(), getFontStyle().getTitle());

        try {
            if (Theme.valueOf(str).getThemeType() != Reddit.themeBack) {
                String name = str.split("_")[1];
                for (Theme theme : Theme.values()) {
                    if (theme.toString().contains(name) && theme.getThemeType() == Reddit.themeBack) {
                        setFontStyle(theme, s);
                        return theme;
                    }
                }
            } else {

                return Theme.valueOf(str);


            }
        } catch (Exception e) {

        }
        return getFontStyle();

    }

    public void setFontStyle(Theme style, String s) {
        edit().putString(s.toLowerCase(), style.name()).commit();
    }

    public void removeFontStyle(String subreddit){
        edit().remove(subreddit).commit();
    }

    public int getColor(String s) {
        return ContextCompat.getColor(context, getThemeSubreddit(s, true).getColor());
    }

    public enum Theme {
        dark_white(R.style.white_dark, "dark_white", R.color.md_blue_grey_200, 0),
        light_white(R.style.white_light, "light_white", R.color.md_blue_grey_200, 1),
        amoled_white(R.style.white_amoled, "amoled_white", R.color.md_blue_grey_200, 2),
        blue_white(R.style.white_blue, "blue_white", R.color.md_blue_grey_200, 3),

        dark_pink(R.style.pink_dark, "dark_pink", R.color.md_pink_A200, 0),
        light_pink(R.style.pink_light, "light_pink", R.color.md_pink_A200, 1),
        amoled_pink(R.style.pink_amoled, "amoled_pink", R.color.md_pink_A200, 2),
        blue_pink(R.style.pink_blue, "blue_pink", R.color.md_pink_A200, 3),

        dark_deeporange(R.style.deeporange_dark, "dark_deeporange", R.color.md_deep_orange_A700, 0),
        light_deeporange(R.style.deeporange_LIGHT, "light_deeporange", R.color.md_deep_orange_A700, 1),
        amoled_deeporange(R.style.deeporange_AMOLED, "amoled_deeporange", R.color.md_deep_orange_A700, 2),
        blue_deeporange(R.style.deeporange_blue, "blue_deeporange", R.color.md_deep_orange_A700, 3),

        dark_amber(R.style.amber_dark, "dark_amber", R.color.md_amber_A700, 0),
        light_amber(R.style.amber_LIGHT, "light_amber", R.color.md_amber_A700, 1),
        amoled_amber(R.style.amber_AMOLED, "amoled_amber", R.color.md_amber_A700, 2),
        blue_amber(R.style.amber_blue, "blue_amber", R.color.md_amber_A700, 3),

        dark_yellow(R.style.yellow_dark, "dark_yellow", R.color.md_yellow_A700, 0),
        light_yellow(R.style.yellow_LIGHT, "light_yellow", R.color.md_yellow_A700, 1),
        amoled_yellow(R.style.yellow_AMOLED, "amoled_yellow", R.color.md_yellow_A700, 2),
        blue_yellow(R.style.yellow_blue, "blue_yellow", R.color.md_yellow_A700, 3),

        dark_lime(R.style.lime_dark, "dark_lime", R.color.md_lime_A700, 0),
        light_lime(R.style.lime_LIGHT, "light_lime", R.color.md_lime_A700, 1),
        amoled_lime(R.style.lime_AMOLED, "amoled_lime", R.color.md_lime_A700, 2),
        blue_lime(R.style.lime_blue, "blue_lime", R.color.md_lime_A700, 3),

        dark_green(R.style.green_dark, "dark_green", R.color.md_green_A700, 0),
        light_green(R.style.green_LIGHT, "light_green", R.color.md_green_A700, 1),
        amoled_green(R.style.green_AMOLED, "amoled_green", R.color.md_green_A700, 2),
        blue_green(R.style.green_blue, "blue_green", R.color.md_green_A700, 3),

        dark_teal(R.style.teal_dark, "dark_teal", R.color.md_teal_A200, 0),
        light_teal(R.style.teal_light, "light_teal", R.color.md_teal_A200, 1),
        amoled_teal(R.style.teal_amoled, "amoled_teal", R.color.md_teal_A200, 2),
        blue_teal(R.style.teal_blue, "blue_teal", R.color.md_teal_A200, 3),

        dark_cyan(R.style.cyan_dark, "dark_cyan", R.color.md_cyan_A700, 0),
        light_cyan(R.style.cyan_LIGHT, "light_cyan", R.color.md_cyan_A700, 1),
        amoled_cyan(R.style.cyan_AMOLED, "amoled_cyan", R.color.md_cyan_A700, 2),
        blue_cyan(R.style.cyan_blue, "blue_cyan", R.color.md_cyan_A700, 3),

        dark_lightblue(R.style.lightblue_dark, "dark_lightblue", R.color.md_light_blue_A700, 0),
        light_lightblue(R.style.lightblue_LIGHT, "light_lightblue", R.color.md_light_blue_A700, 1),
        amoled_lightblue(R.style.lightblue_AMOLED, "amoled_lightblue", R.color.md_light_blue_A700, 2),
        blue_lightblue(R.style.lightblue_blue, "blue_lightblue", R.color.md_light_blue_A700, 3),

        dark_blue(R.style.blue_dark, "dark_blue", R.color.md_blue_A700, 0),
        light_blue(R.style.blue_LIGHT, "light_blue", R.color.md_blue_A700, 1),
        amoled_blue(R.style.blue_AMOLED, "amoled_blue", R.color.md_blue_A700, 2),
        blue_blue(R.style.blue_blue, "blue_blue", R.color.md_blue_A700, 3),

        dark_indigo(R.style.indigo_dark, "dark_indigo", R.color.md_indigo_A700, 0),
        light_indigo(R.style.indigo_LIGHT, "light_indigo", R.color.md_indigo_A700, 1),
        amoled_indigo(R.style.indigo_AMOLED, "amoled_indigo", R.color.md_indigo_A700, 2),
        blue_indigo(R.style.indigo_blue, "blue_indigo", R.color.md_indigo_A700, 3);


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
}