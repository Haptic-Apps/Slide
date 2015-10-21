package me.ccrama.redditslide;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ccrama on 7/9/2015.
 */
public class ColorPreferences {
    private final static String FONT_STYLE = "THEME";

    private final Context context;

    public ColorPreferences(Context context) {
        this.context = context;
    }

    protected SharedPreferences open() {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    protected SharedPreferences.Editor edit() {
        return open().edit();
    }

    public Theme getFontStyle() {
        try {
            return Theme.valueOf(open().getString(FONT_STYLE,
                    Theme.dark_amber
                            .name()));
        } catch (Exception e){
            return Theme.dark_amber;
        }
    }

    public int getThemeOverview(){
        switch(getFontStyle().getThemeType()){
            case 0: return R.style.white_dark;
            case 1: return R.style.white_light;
            case 2: return R.style.white_amoled;
            default: return R.style.white_dark;
        }
    }

    public int getThemeSubreddit(String s){

        String str = open().getString(s.toLowerCase(), getFontStyle().getTitle());

        if(Theme.valueOf(str).getThemeType() != Reddit.themeBack){
            String name = str.split("_")[1];
            for(Theme theme : Theme.values()){
                if(theme.toString().contains(name) && theme.getThemeType() == Reddit.themeBack){
                    setFontStyle(theme, s);
                    return theme.baseId;
                }
            }
        } else {

            return Theme.valueOf(str).baseId;


        }
        return getFontStyle().baseId;

    }
    public Theme getThemeSubreddit(String s, boolean b){

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
        } catch(Exception e){

        }
        return getFontStyle();

    }
    public void setFontStyle(Theme style, String s) {
        edit().putString(s.toLowerCase(), style.name()).commit();



    }

    public int getColor(String s){return context.getResources().getColor(getThemeSubreddit(s, true).getColor());}




    public void setFontStyle(Theme style) {
        edit().putString(FONT_STYLE, style.name()).commit();
    }
    public enum Theme {
        dark_white(R.style.white_dark, "dark_white",R.color.md_blue_grey_200,0),
        light_white(R.style.white_light, "light_white",R.color.md_blue_grey_200,1),
        amoled_white(R.style.white_amoled, "amoled_white",R.color.md_blue_grey_200,2),
        dark_deeporange(R.style.deeporange_dark, "dark_deeporange",R.color.md_deep_orange_A700,0),
        light_deeporange(R.style.deeporange_LIGHT, "light_deeporange",R.color.md_deep_orange_A700,1),
        amoled_deeporange(R.style.deeporange_AMOLED, "amoled_deeporange",R.color.md_deep_orange_A700,2),
        dark_amber(R.style.amber_dark, "dark_amber",R.color.md_amber_A700,0),
        light_amber(R.style.amber_LIGHT, "light_amber",R.color.md_amber_A700,1),
        amoled_amber(R.style.amber_AMOLED, "amoled_amber",R.color.md_amber_A700,2),
        dark_yellow(R.style.yellow_dark, "dark_yellow", R.color.md_yellow_A700,0),
        light_yellow(R.style.yellow_LIGHT, "light_yellow",R.color.md_yellow_A700,1),
        amoled_yellow(R.style.yellow_AMOLED, "amoled_yellow",R.color.md_yellow_A700,2),
        dark_lime(R.style.lime_dark, "dark_lime",R.color.md_lime_A700,0),
        light_lime(R.style.lime_LIGHT, "light_lime",R.color.md_lime_A700,1),
        amoled_lime(R.style.lime_AMOLED, "amoled_lime",R.color.md_lime_A700,2),
        dark_green(R.style.green_dark, "dark_green",R.color.md_green_A700,0),
        light_green(R.style.green_LIGHT, "light_green(",R.color.md_green_A700,1),
        amoled_green(R.style.green_AMOLED, "amoled_green",R.color.md_green_A700,2),
        dark_cyan(R.style.cyan_dark, "dark_cyan",R.color.md_cyan_A700,0),
        light_cyan(R.style.cyan_LIGHT, "light_cyan",R.color.md_cyan_A700,1),
        amoled_cyan(R.style.cyan_AMOLED, "amoled_cyan",R.color.md_cyan_A700,2),
        dark_lightblue(R.style.lightblue_dark, "dark_lightblue",R.color.md_light_blue_A700,0),
        light_lightblue(R.style.lightblue_LIGHT, "light_lightblue",R.color.md_light_blue_A700,1),
        amoled_lightblue(R.style.lightblue_AMOLED, "amoled_lightblue",R.color.md_light_blue_A700,2),
        dark_blue(R.style.blue_dark, "dark_blue",R.color.md_blue_A700,0),
        light_blue(R.style.blue_LIGHT, "light_blue",R.color.md_blue_A700,1),
        amoled_blue(R.style.blue_AMOLED, "amoled_blue",R.color.md_blue_A700,2),

        dark_indigo(R.style.indigo_dark, "dark_indigo",R.color.md_indigo_A700,0),
        light_indigo(R.style.indigo_LIGHT, "light_indigo",R.color.md_indigo_A700,1),
        amoled_indigo(R.style.indigo_AMOLED, "amoled_indigo", R.color.md_indigo_A700,2);


        private int baseId;
        private String title;


        public int getBaseId() {
            return baseId;
        }

        private int themeType;
        public int getThemeType(){return themeType;}
        public String getTitle() {
            return title;
        }

        public int getColor(){return color;}

        private int color;
        Theme(int baseId, String title, int color, int  themetype) {
            this.baseId = baseId;
            this.color = color;
            this.themeType=themetype;
            this.title = title;
        }
    }
}