package me.ccrama.redditslide.Visuals;

import android.content.Context;
import android.graphics.Color;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Reddit;

/**
 * Created by ccrama on 9/18/2015.
 */
public class Pallete {
    public int fontColor;
    public int backgroundColor;
    public static int getDefaultColor(){
        if(Reddit.colors.contains("DEFAULTCOLOR")){
            return Reddit.colors.getInt("DEFAULTCOLOR", Color.parseColor("#e64a19"));
        } else {
            return Color.parseColor("#e64a19");

        }
    }
    public static int getDefaultAccent(){
        if(Reddit.colors.contains("ACCENTCOLOR")){
            return Reddit.colors.getInt("ACCENTCOLOR", Color.parseColor("#ff6e40"));
        } else {
            return Color.parseColor("#ff6e40");

        }
    }
    public int mainColor;
    public int accentColor;
    public static int getColorAccent(final String subreddit){
        if(Reddit.colors.contains("ACCENT" + subreddit.toLowerCase())) {

            return Reddit.colors.getInt("ACCENT" + subreddit.toLowerCase(), getDefaultColor());
        } else {
            return getDefaultColor();
        }
    }
    public static int getFontColorUser(final String subreddit){
        if(Reddit.colors.contains("USER" + subreddit.toLowerCase())) {

            int color = Reddit.colors.getInt("USER" + subreddit.toLowerCase(), getDefaultColor());
            if (color == getDefaultColor()) {
                return 0;
            } else {
                return color;
            }
        } else {
            return 0;
        }
    }
    public static int[] getColors(String subreddit, Context context){
        int[] ints = new int[2];
        ints[0] = getColor(subreddit);
        ints[1] = new ColorPreferences(context).getColor(subreddit);
        return ints;
    }
    public static int getColor(final String subreddit){
        if(Reddit.colors.contains(subreddit.toLowerCase())) {
            return Reddit.colors.getInt(subreddit.toLowerCase(), getDefaultColor());
        }
        return getDefaultColor();
    }
    public static void setColor(final String subreddit, int color){
         Reddit.colors.edit().putInt(subreddit.toLowerCase(), color).apply();
    }
    public static void removeColor(final String subreddit){
        Reddit.colors.edit().remove(subreddit.toLowerCase()).apply();
    }
    public static int getColorUser(final String username){
        if(Reddit.colors.contains("USER" + username.toLowerCase())) {

            return Reddit.colors.getInt("USER" + username.toLowerCase(), getDefaultColor());
        } else {
            return getDefaultColor();
        }
    }
    public static void setColorUser(final String username, int color){
        Reddit.colors.edit().putInt("USER" + username.toLowerCase(), color).apply();
    }
    public static Pallete getSubredditPallete(String subredditname){
        Pallete p = new Pallete();
        p.theme = ThemeEnum.valueOf(Reddit.colors.getString("ThemeDefault", "DARK"));
        p.fontColor = p.theme.getFontColor();
        p.backgroundColor = p.theme.getBackgroundColor();
        p.mainColor = getColor(subredditname);
        p.accentColor = getColorAccent(subredditname);
        return p;

    }
    public static Pallete getDefaultPallete(){
        Pallete p = new Pallete();
        p.theme = ThemeEnum.valueOf(Reddit.colors.getString("ThemeDefault", "DARK"));
        p.fontColor = p.theme.getFontColor();
        p.backgroundColor = p.theme.getBackgroundColor();
        return p;

    }
    public static int getDarkerColor(String s) {
        int color = getColor(s);
        return getDarkerColor(color);
    }

    public static int getDarkerColor(int i) {
        float[] hsv = new float[3];
        int color = i;
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        color = Color.HSVToColor(hsv);
        return color;
    }
    public ThemeEnum theme;


    public enum ThemeEnum{
        DARK("Dark", Color.parseColor("#303030"), Color.parseColor("#424242"), Color.parseColor("#ffffff")),
        LIGHT("Light",Color.parseColor("#e8e8e8"), Color.parseColor("#ffffff"), Color.parseColor("#ff414141") ),
        AMOLEDBLACK("Black", Color.parseColor("#000000"), Color.parseColor("#212121"), Color.parseColor("#ffffff"));

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

        String displayName;
        int backgroundColor;
        int cardBackgroundColor;
        int fontColor;
        ThemeEnum(String s, int backgroundColor, int cardBackgroundColor, int fontColor){
            this.displayName = s;
            this.backgroundColor = backgroundColor;
            this.cardBackgroundColor = cardBackgroundColor;
            this.fontColor = fontColor;
        }
    }

}
