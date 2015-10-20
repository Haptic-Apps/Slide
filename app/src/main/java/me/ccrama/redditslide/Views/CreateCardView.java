package me.ccrama.redditslide.Views;

import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Pallete;

/**
 * Created by ccrama on 9/18/2015.
 */
public class CreateCardView {
    public static View CreateView(ViewGroup viewGroup){
        CardEnum  cardEnum = SettingValues.defaultCardView;
        View v = null;
        switch(cardEnum){
            case LARGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_largecard, viewGroup, false);
                break;
            case LIST:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_list, viewGroup, false);
                break;

        }
        doHideObjects(v, false);
        return v;
    }
    public static View CreateView(ViewGroup viewGroup, Boolean secondary, String sub){
        String subreddit = (secondary) ? "second" : "";
        sub = sub.toLowerCase();
        CardEnum  cardEnum = CardEnum.valueOf(SettingValues.prefs.getString(subreddit + "defaultCardViewNew" , SettingValues.defaultCardView.toString()).toUpperCase());
        View v = null;
        switch(cardEnum){
            case LARGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_largecard, viewGroup, false);
                break;

            case LIST:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_list, viewGroup, false);
                break;

        }
        doHideObjects(v, secondary);
        colorCard(subreddit, v, sub, secondary);
        return v;
    }

    public static void resetColorCard(View v){
        ((TextView) v.findViewById(R.id.subreddit)).setTextColor(((TextView) v.findViewById(R.id.information)).getCurrentTextColor());
        TypedValue background = new TypedValue();
        v.getContext().getTheme().resolveAttribute(R.attr.card_background, background, true);
        ((CardView) v.findViewById(R.id.card)).setCardBackgroundColor(background.data);

    }
    public static void colorCard(String sec, View v,  String subToMatch, boolean secondary){

        String subreddit = (secondary) ? "second" : "";
        subToMatch = subToMatch.toLowerCase();
        if (getColorIndicator(secondary) != SettingValues.ColorIndicator.NONE && Pallete.getColor(subreddit) != Pallete.getDefaultColor()) {
            resetColorCard(v);
            if (getColorMatchingMode(secondary) == SettingValues.ColorMatchingMode.ALWAYS_MATCH) {
                switch (getColorIndicator(secondary)) {
                    case CARD_BACKGROUND:
                        ((CardView) v.findViewById(R.id.card)).setCardBackgroundColor(Pallete.getColor(subreddit));

                        break;
                    case TEXT_COLOR:
                        ((TextView) v.findViewById(R.id.subreddit)).setTextColor(Pallete.getColor(subreddit));

                        break;
                }
            } else if (!subToMatch.equals(sec) && getColorMatchingMode(secondary) == SettingValues.ColorMatchingMode.MATCH_EXTERNALLY) {
                switch (getColorIndicator(secondary)) {
                    case CARD_BACKGROUND:
                        ((CardView) v.findViewById(R.id.card)).setCardBackgroundColor(Pallete.getColor(subreddit));

                        break;
                    case TEXT_COLOR:
                        ((TextView) v.findViewById(R.id.subreddit)).setTextColor(Pallete.getColor(subreddit));

                        break;
                }
            }
        }
    }
    public static View setCardViewType(CardEnum cardEnum, ViewGroup parent, Boolean secondary, String sub){
        String subreddit = (secondary) ? "second" : "";
        sub = sub.toLowerCase();
        if(subreddit.isEmpty()) {
            SettingValues.prefs.edit().putString("defaultCardViewNew", cardEnum.name()).apply();
            SettingValues.defaultCardView = cardEnum;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putString(subreddit + "defaultCardViewNew", cardEnum.name()).apply();
            return CreateView(parent, secondary, sub);

        }
    }


    public static View setInfoBarVisible(SettingValues.InfoBar b, ViewGroup parent, Boolean secondary, String sub){
        String subreddit = (secondary) ? "second" : "";
        sub = sub.toLowerCase();
        if(subreddit.isEmpty()) {


            SettingValues.prefs.edit().putString("infoBarTypeNew", b.toString()).apply();

            SettingValues.infoBar = b;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putString(subreddit + "infoBarTypeNew" , b.toString()).apply();
            return CreateView(parent, secondary, sub);

        }

    }
    public static void setColorMatchingMode(SettingValues.ColorMatchingMode b,  Boolean secondary){
        String subreddit = (secondary) ? "second" : "";
        if(subreddit.isEmpty()) {


            SettingValues.prefs.edit().putString("ccolorMatchingModeNew", b.toString()).apply();

            SettingValues.colorMatchingMode = b;

        } else {
            SettingValues.prefs.edit().putString(subreddit + "ccolorMatchingModeNew" , b.toString()).apply();

        }

    }
    public static void setColorIndicicator(SettingValues.ColorIndicator b,  Boolean secondary){
        String subreddit = (secondary) ? "second" : "";
        if(subreddit.isEmpty()) {


            SettingValues.prefs.edit().putString("colorIndicatorNew", b.toString()).apply();

            SettingValues.colorIndicator = b;

        } else {
            SettingValues.prefs.edit().putString(subreddit + "colorIndicatorNew", b.toString()).apply();
        }

    }
    public static View setActionBarVisible(boolean b, ViewGroup parent, Boolean secondary, String sub) {
        String subreddit = (secondary) ? "second" : "";
        sub = sub.toLowerCase();
        if (subreddit.isEmpty()) {


        SettingValues.prefs.edit().putBoolean("actionBarVisibleNew", b).apply();
        SettingValues.actionBarVisible = b;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putBoolean(subreddit + "actionBarVisibleNew" , b).apply();
            return CreateView(parent, secondary, sub );

        }
    }

    public static void doHideObjects(View v, Boolean secondary){
        String subreddit = (secondary) ? "second" : "";
        if(subreddit.isEmpty()) {
            if (!SettingValues.actionBarVisible) {
                v.findViewById(R.id.actionbar).setVisibility(View.GONE);
            }

            switch (SettingValues.infoBar){
                case THUMBNAIL:
                    v.findViewById(R.id.thumbimage2).setVisibility(View.VISIBLE);

                    v.findViewById(R.id.base2).setVisibility(View.GONE);
                    v.findViewById(R.id.imagearea).setVisibility(View.GONE);
                    break;
                case INFO_BAR:
                    v.findViewById(R.id.thumbimage2).setVisibility(View.GONE);

                    v.findViewById(R.id.imagearea).setVisibility(View.GONE);
                    break;
                case NONE:
                    v.findViewById(R.id.thumbimage2).setVisibility(View.GONE);
                    v.findViewById(R.id.imagearea).setVisibility(View.GONE);
                    v.findViewById(R.id.thumbimage).setVisibility(View.GONE);
                    v.findViewById(R.id.base2).setVisibility(View.GONE);
                    break;
                case BIG_PICTURE:
                    v.findViewById(R.id.thumbimage2).setVisibility(View.GONE);
                    break;
                case BIG_PICTURE_CROPPED:
                    v.findViewById(R.id.thumbimage2).setVisibility(View.GONE);
                    break;
            }

            if(SettingValues.infoBar == SettingValues.InfoBar.BIG_PICTURE_CROPPED){
                ((ImageView) v.findViewById(R.id.leadimage)).setMaxHeight(300);
                ((ImageView) v.findViewById(R.id.leadimage)).setScaleType(ImageView.ScaleType.CENTER_CROP);

            }

        } else {
            if (!SettingValues.prefs.getBoolean(subreddit  + "actionBarVisibleNew", SettingValues.actionBarVisible)) {
                v.findViewById(R.id.actionbar).setVisibility(View.GONE);
            }
            
            if(getInfoBar(secondary) == SettingValues.InfoBar.BIG_PICTURE_CROPPED){
                ((ImageView) v.findViewById(R.id.leadimage)).setMaxHeight(300);
                ((ImageView) v.findViewById(R.id.leadimage)).setScaleType(ImageView.ScaleType.CENTER_CROP);

            }
            switch (getInfoBar(secondary)){
                case THUMBNAIL:
                    v.findViewById(R.id.thumbimage2).setVisibility(View.VISIBLE);

                    v.findViewById(R.id.base2).setVisibility(View.GONE);
                    v.findViewById(R.id.imagearea).setVisibility(View.GONE);
                    break;
                case INFO_BAR:
                    v.findViewById(R.id.thumbimage2).setVisibility(View.GONE);

                    v.findViewById(R.id.imagearea).setVisibility(View.GONE);
                    break;
                case NONE:
                    v.findViewById(R.id.thumbimage2).setVisibility(View.GONE);
                    v.findViewById(R.id.imagearea).setVisibility(View.GONE);
                    v.findViewById(R.id.thumbimage).setVisibility(View.GONE);
                    v.findViewById(R.id.base2).setVisibility(View.GONE);
                    break;
                case BIG_PICTURE:
                    v.findViewById(R.id.thumbimage2).setVisibility(View.GONE);
                    break;
                case BIG_PICTURE_CROPPED:
                    v.findViewById(R.id.thumbimage2).setVisibility(View.GONE);
                    break;
            }

        }
    }


    public static boolean isCard(Boolean secondary) {
        String subreddit = (secondary) ? "second" : "";

        return CardEnum.valueOf(SettingValues.prefs.getString(subreddit + "defaultCardViewNew" , SettingValues.defaultCardView.toString())) == CardEnum.LARGE;
    }
    public static CardEnum getCardView(Boolean secondary) {
        String subreddit = (secondary) ? "second" : "";

        return CardEnum.valueOf(SettingValues.prefs.getString(subreddit + "defaultCardViewNew"  , SettingValues.defaultCardView.toString()));
    }
    public static SettingValues.ColorIndicator getColorIndicator(Boolean secondary) {
        String subreddit = (secondary) ? "second" : "";

        return SettingValues.ColorIndicator.valueOf(SettingValues.prefs.getString(subreddit + "colorIndicatorNew"  , SettingValues.colorIndicator.toString()));
    }
    public static SettingValues.ColorMatchingMode getColorMatchingMode(Boolean secondary) {
        String subreddit = (secondary) ? "second" : "";

        return SettingValues.ColorMatchingMode.valueOf(SettingValues.prefs.getString(subreddit + "ccolorMatchingModeNew" , SettingValues.colorMatchingMode.toString()));
    }
    public static SettingValues.InfoBar getInfoBar(Boolean secondary) {
        String subreddit = (secondary) ? "second" : "";

        return  SettingValues.InfoBar.valueOf(SettingValues.prefs.getString(subreddit + "infoBarTypeNew", SettingValues.infoBar.toString()));
    }
    public static boolean isActionBar(Boolean secondary) {
        String subreddit = (secondary) ? "second" : "";

        return SettingValues.prefs.getBoolean(subreddit + "actionBarVisibleNew" , SettingValues.actionBarVisible);
    }

    public enum CardEnum{
        LARGE("Big Card"),
        LIST("List");
        String displayName;
        CardEnum(String s){
            this.displayName = s;
        }
        public String getDisplayName(){
            return displayName;
        }
    }


}
