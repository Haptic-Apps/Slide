package me.ccrama.redditslide.Views;

import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
        doHideObjects(v, "");
        return v;
    }
    public static View CreateView(ViewGroup viewGroup, String subreddit, String sub){
        subreddit = subreddit.toLowerCase();
        sub = sub.toLowerCase();
        CardEnum  cardEnum = CardEnum.valueOf(SettingValues.prefs.getString(subreddit + "defaultCardView" , SettingValues.defaultCardView.toString()).toUpperCase());
        View v = null;
        switch(cardEnum){
            case LARGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_largecard, viewGroup, false);
                break;

            case LIST:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_list, viewGroup, false);
                break;

        }
        doHideObjects(v, subreddit);
        colorCard(subreddit, v, sub);
        return v;
    }

    public static void resetColorCard(View v){
        ((TextView) v.findViewById(R.id.subreddit)).setTextColor(((TextView) v.findViewById(R.id.information)).getCurrentTextColor());
        TypedValue background = new TypedValue();
        v.getContext().getTheme().resolveAttribute(R.attr.card_background, background, true);
        ((CardView) v.findViewById(R.id.card)).setCardBackgroundColor(background.data);

    }
    public static void colorCard(String subreddit, View v,  String subToMatch){

        subreddit = subreddit.toLowerCase();
        subToMatch = subToMatch.toLowerCase();
        if (getColorIndicator(subreddit) != SettingValues.ColorIndicator.NONE && Pallete.getColor(subreddit) != Pallete.getDefaultColor()) {
            resetColorCard(v);
            if (getColorMatchingMode(subreddit) == SettingValues.ColorMatchingMode.ALWAYS_MATCH) {
                switch (getColorIndicator(subreddit)) {
                    case CARD_BACKGROUND:
                        ((CardView) v.findViewById(R.id.card)).setCardBackgroundColor(Pallete.getColor(subreddit));

                        break;
                    case TEXT_COLOR:
                        ((TextView) v.findViewById(R.id.subreddit)).setTextColor(Pallete.getColor(subreddit));

                        break;
                }
            } else if (!subToMatch.equals(subreddit) && getColorMatchingMode(subreddit) == SettingValues.ColorMatchingMode.MATCH_EXTERNALLY) {
                switch (getColorIndicator(subreddit)) {
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
    public static View setCardViewType(CardEnum cardEnum, ViewGroup parent, String subreddit, String sub){
        subreddit = subreddit.toLowerCase();
        sub = sub.toLowerCase();
        if(subreddit.isEmpty()) {
            SettingValues.prefs.edit().putString("defaultCardView", cardEnum.name()).apply();
            SettingValues.defaultCardView = cardEnum;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putString(subreddit + "defaultCardView", cardEnum.name()).apply();
            return CreateView(parent, subreddit, sub);

        }
    }


    public static View setInfoBarVisible(SettingValues.InfoBar b, ViewGroup parent, String subreddit, String sub){
        subreddit = subreddit.toLowerCase();
        sub = sub.toLowerCase();
        if(subreddit.isEmpty()) {


            SettingValues.prefs.edit().putString("infoBar", b.toString()).apply();

            SettingValues.infoBar = b;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putString(subreddit + "infoBar" , b.toString()).apply();
            return CreateView(parent, subreddit, sub);

        }

    }
    public static View setColorMatchingMode(SettingValues.ColorMatchingMode b, ViewGroup parent, String subreddit, String sub){
        subreddit = subreddit.toLowerCase();
        sub = sub.toLowerCase();
        if(subreddit.isEmpty()) {


            SettingValues.prefs.edit().putString("ccolorMatchingMode", b.toString()).apply();

            SettingValues.colorMatchingMode = b;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putString(subreddit + "ccolorMatchingMode" , b.toString()).apply();
            return CreateView(parent, subreddit, sub);

        }

    }
    public static View setColorIndicicator(SettingValues.ColorIndicator b, ViewGroup parent, String subreddit, String sub){
        subreddit = subreddit.toLowerCase();
        sub = sub.toLowerCase();
        if(subreddit.isEmpty()) {


            SettingValues.prefs.edit().putString("colorIndicator", b.toString()).apply();

            SettingValues.colorIndicator = b;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putString(subreddit + "colorIndicator" , b.toString()).apply();
            return CreateView(parent, subreddit, sub);

        }

    }
    public static View setActionBarVisible(boolean b, ViewGroup parent, String subreddit, String sub) {
        subreddit = subreddit.toLowerCase();
        sub = sub.toLowerCase();
        if (subreddit.isEmpty()) {


        SettingValues.prefs.edit().putBoolean("actionBarVisible", b).apply();
        SettingValues.actionBarVisible = b;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putBoolean(subreddit + "actionBarVisible" , b).apply();
            return CreateView(parent, subreddit, sub );

        }
    }
    public static View setCropped(boolean b, ViewGroup parent, String subreddit, String sub) {
        subreddit = subreddit.toLowerCase();
        sub = sub.toLowerCase();
        if (subreddit.isEmpty()) {


            SettingValues.prefs.edit().putBoolean("croppedImage", b).apply();
            SettingValues.croppedImage = b;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putBoolean(subreddit + "croppedImage" , b).apply();
            return CreateView(parent, subreddit, sub );

        }
    }
    public static void doHideObjects(View v, String subreddit){
        subreddit = subreddit.toLowerCase();
        if(subreddit.isEmpty()) {
            if (!SettingValues.actionBarVisible) {
                v.findViewById(R.id.actionbar).setVisibility(View.GONE);
            }
            if (!SettingValues.largeThumbnails) {
                v.findViewById(R.id.imagearea).setVisibility(View.GONE);
            }
            switch (SettingValues.infoBar){
                case THUMBNAIL:
                    v.findViewById(R.id.base2).setVisibility(View.GONE);
                    break;
                case INFO_BAR:
                    v.findViewById(R.id.imagearea).setVisibility(View.GONE);
                    break;
            }

            if(SettingValues.croppedImage){
                ((ImageView) v.findViewById(R.id.leadimage)).setMaxHeight(300);
            }
            if (SettingValues.croppedImage) {
                float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, v.getContext().getResources().getDisplayMetrics());
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.findViewById(R.id.leadimage).getLayoutParams();
                params.height = (int) pixels;
                v.findViewById(R.id.leadimage).setLayoutParams(params);
            }
        } else {
            if (!SettingValues.prefs.getBoolean("actionBarVisible" + subreddit, SettingValues.actionBarVisible)) {
                v.findViewById(R.id.actionbar).setVisibility(View.GONE);
            }
            if (!SettingValues.prefs.getBoolean("largeThumbnails" + subreddit, SettingValues.largeThumbnails)) {
                v.findViewById(R.id.imagearea).setVisibility(View.GONE);
            }
            if(getCroppedImage(subreddit)){
                ((ImageView) v.findViewById(R.id.leadimage)).setMaxHeight(300);
            }
            switch (getInfoBar(subreddit)){
                case THUMBNAIL:
                    v.findViewById(R.id.base2).setVisibility(View.GONE);
                    break;
                case INFO_BAR:
                    v.findViewById(R.id.imagearea).setVisibility(View.GONE);
                    break;
            }
            if (SettingValues.croppedImage) {
                float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, v.getContext().getResources().getDisplayMetrics());
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.findViewById(R.id.leadimage).getLayoutParams();
                params.height = (int) pixels;
                v.findViewById(R.id.leadimage).setLayoutParams(params);
            }
        }
    }

    public static boolean isLarge(String subreddit) {
        subreddit = subreddit.toLowerCase();
        return SettingValues.prefs.getBoolean(subreddit + "largeThumbnails" , SettingValues.largeThumbnails);
    }
    public static boolean isCard(String subreddit) {
        subreddit = subreddit.toLowerCase();

        return CardEnum.valueOf(SettingValues.prefs.getString(subreddit + "defaultCardView" , SettingValues.defaultCardView.toString())) == CardEnum.LARGE;
    }
    public static CardEnum getCardView(String subreddit) {
        subreddit = subreddit.toLowerCase();

        return CardEnum.valueOf(SettingValues.prefs.getString(subreddit + "defaultCardView"  , SettingValues.defaultCardView.toString()));
    }
    public static SettingValues.ColorIndicator getColorIndicator(String subreddit) {
        subreddit = subreddit.toLowerCase();

        return SettingValues.ColorIndicator.valueOf(SettingValues.prefs.getString(subreddit + "colorIndicator"  , SettingValues.colorIndicator.toString()));
    }
    public static SettingValues.ColorMatchingMode getColorMatchingMode(String subreddit) {
        subreddit = subreddit.toLowerCase();

        return SettingValues.ColorMatchingMode.valueOf(SettingValues.prefs.getString(subreddit + "ccolorMatchingMode" , SettingValues.colorMatchingMode.toString()));
    }
    public static SettingValues.InfoBar getInfoBar(String subreddit) {
        subreddit = subreddit.toLowerCase();

        return  SettingValues.InfoBar.valueOf(SettingValues.prefs.getString(subreddit + "infoBarType" , SettingValues.infoBar.toString()));
    }
    public static boolean isActionBar(String subreddit) {
        subreddit = subreddit.toLowerCase();

        return SettingValues.prefs.getBoolean(subreddit + "actionBarVisible" , SettingValues.actionBarVisible);
    }
    public static boolean getCroppedImage(String subreddit) {
        subreddit = subreddit.toLowerCase();

        return SettingValues.prefs.getBoolean(subreddit + "croppedImage" , SettingValues.croppedImage);
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
