package me.ccrama.redditslide.Visuals;

import android.content.Context;
import android.graphics.Color;

import java.util.Locale;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Reddit;

public class Palette {
    private int fontColor;
    public  int backgroundColor;

    public static int getDefaultColor() {
        if (Reddit.colors.contains("DEFAULTCOLOR")) {
            return Reddit.colors.getInt("DEFAULTCOLOR", Color.parseColor("#e64a19"));
        } else {
            return Color.parseColor("#e64a19");
        }
    }

    /**
     * Gets the status bar color for the activity.
     *
     * @return Color-int for the status bar
     */
    public static int getStatusBarColor() {
        return getDarkerColor(getDefaultColor());
    }

    /**
     * Gets the status bar color for the activity based on the specified username.
     *
     * @param username The username to base the theme on
     * @return Color-int for the status bar
     */
    public static int getUserStatusBarColor(String username) {
        return getDarkerColor(getColorUser(username));
    }

    /**
     * Gets the status bar color for the activity based on the specified subreddit.
     *
     * @param subreddit The subreddit to base the theme on
     * @return Color-int for the status bar
     */
    public static int getSubredditStatusBarColor(String subreddit) {
        return getDarkerColor(getColor(subreddit));
    }

    public static int getDefaultAccent() {
        if (Reddit.colors.contains("ACCENTCOLOR")) {
            return Reddit.colors.getInt("ACCENTCOLOR", Color.parseColor("#ff6e40"));
        } else {
            return Color.parseColor("#ff6e40");
        }
    }

    private int mainColor;
    private int accentColor;

    private static int getColorAccent(final String subreddit) {
        if (Reddit.colors.contains("ACCENT" + subreddit.toLowerCase(Locale.ENGLISH))) {
            return Reddit.colors.getInt("ACCENT" + subreddit.toLowerCase(Locale.ENGLISH),
                    getDefaultColor());
        } else {
            return getDefaultColor();
        }
    }

    public static int getFontColorUser(final String subreddit) {
        if (Reddit.colors.contains("USER" + subreddit.toLowerCase(Locale.ENGLISH))) {
            final int color = Reddit.colors.getInt("USER" + subreddit.toLowerCase(Locale.ENGLISH),
                    getDefaultColor());

            if (color == getDefaultColor()) {
                return 0;
            } else {
                return color;
            }
        } else {
            return 0;
        }
    }

    public static int[] getColors(String subreddit, Context context) {
        int[] ints = new int[2];

        ints[0] = getColor(subreddit);
        ints[1] = new ColorPreferences(context).getColor(subreddit);

        return ints;
    }

    public static int getColor(final String subreddit) {
        if (subreddit != null && Reddit.colors.contains(subreddit.toLowerCase(Locale.ENGLISH))) {
            return Reddit.colors.getInt(subreddit.toLowerCase(Locale.ENGLISH), getDefaultColor());
        }
        return getDefaultColor();
    }

    public static void setColor(final String subreddit, int color) {
        Reddit.colors.edit().putInt(subreddit.toLowerCase(Locale.ENGLISH), color).apply();
    }

    public static void removeColor(final String subreddit) {
        Reddit.colors.edit().remove(subreddit.toLowerCase(Locale.ENGLISH)).apply();
    }

    public static int getColorUser(final String username) {
        if (Reddit.colors.contains("USER" + username.toLowerCase(Locale.ENGLISH))) {
            return Reddit.colors.getInt("USER" + username.toLowerCase(Locale.ENGLISH),
                    getDefaultColor());
        } else {
            return getDefaultColor();
        }
    }

    public static void setColorUser(final String username, int color) {
        Reddit.colors.edit().putInt("USER" + username.toLowerCase(Locale.ENGLISH), color).apply();
    }

    public static void removeUserColor(final String username) {
        Reddit.colors.edit().remove("USER" + username.toLowerCase(Locale.ENGLISH)).apply();
    }

    public static Palette getSubredditPallete(String subredditname) {
        Palette p = new Palette();

        p.theme = ThemeEnum.valueOf(Reddit.colors.getString("ThemeDefault", "DARK"));
        p.fontColor = p.theme.getFontColor();
        p.backgroundColor = p.theme.getBackgroundColor();
        p.mainColor = getColor(subredditname);
        p.accentColor = getColorAccent(subredditname);

        return p;
    }

    public static Palette getDefaultPallete() {
        Palette p = new Palette();

        p.theme = ThemeEnum.valueOf(Reddit.colors.getString("ThemeDefault", "DARK"));
        p.fontColor = p.theme.getFontColor();
        p.backgroundColor = p.theme.getBackgroundColor();

        return p;
    }

    public static int getDarkerColor(String s) {
        return getDarkerColor(getColor(s));
    }

    public static int getDarkerColor(int color) {
        float[] hsv = new float[3];

        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        color = Color.HSVToColor(hsv);

        return color;
    }

    public ThemeEnum theme;

    public enum ThemeEnum {
        DARK("Dark", Color.parseColor("#303030"), Color.parseColor("#424242"),
                Color.parseColor("#ffffff"), Color.parseColor("#B3FFFFFF")),
        LIGHT("Light", Color.parseColor("#e5e5e5"), Color.parseColor("#ffffff"),
                Color.parseColor("#de000000"), Color.parseColor("#8A000000")),
        AMOLEDBLACK("Black", Color.parseColor("#000000"), Color.parseColor("#212121"),
                Color.parseColor("#ffffff"), Color.parseColor("#B3FFFFFF")),
        SEPIA("Sepia", Color.parseColor("#cac5ad"), Color.parseColor("#e2dfd7"),
                Color.parseColor("#DE3e3d36"), Color.parseColor("#8A3e3d36")),
        BLUE("Dark Blue", Color.parseColor("#2F3D44"), Color.parseColor("#37474F"),
                Color.parseColor("#ffffff"), Color.parseColor("#B3FFFFFF")),
        PIXEL("Pixel", Color.parseColor("#3e3e3e"), Color.parseColor("#2d2d2d"),
                Color.parseColor("#ffffff"), Color.parseColor("#B3FFFFFF")),
        DEEP("Deep", Color.parseColor("#16161C"), Color.parseColor("#212026"),
                Color.parseColor("#ffffff"), Color.parseColor("#1C1B21"));

        public String getDisplayName() {
            return displayName;
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public int getCardBackgroundColor() {
            return cardBackgroundColor;
        }

        public int getFontColor() {
            return fontColor;
        }

        public int getTint() {
            return tint;
        }

        final String displayName;
        final int    backgroundColor;
        final int    cardBackgroundColor;
        final int    tint;
        final int    fontColor;

        ThemeEnum(String s, int backgroundColor, int cardBackgroundColor, int fontColor, int tint) {
            this.displayName = s;
            this.backgroundColor = backgroundColor;
            this.cardBackgroundColor = cardBackgroundColor;
            this.fontColor = fontColor;
            this.tint = tint;
        }
    }
}
