package me.ccrama.redditslide.Views;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;

/**
 * Created by carlo_000 on 9/18/2015.
 */
public class CreateCardView {
    public static View CreateView(ViewGroup viewGroup){
        CardEnum  cardEnum = SettingValues.defaultCardView;
        View v = null;
        switch(cardEnum){
            case LARGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_largecard, viewGroup, false);
                break;
            case SMALL:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_smallcard, viewGroup, false);
                break;
            case LIST:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_list, viewGroup, false);
                break;

        }
        doHideObjects(v, "");
        return v;
    }
    public static View CreateView(ViewGroup viewGroup, String subreddit){
        CardEnum  cardEnum = CardEnum.valueOf(SettingValues.prefs.getString("defaultCardView" + subreddit, SettingValues.defaultCardView.toString()).toUpperCase());
        View v = null;
        switch(cardEnum){
            case LARGE:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_largecard, viewGroup, false);
                break;
            case SMALL:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_smallcard, viewGroup, false);
                break;
            case LIST:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_list, viewGroup, false);
                break;

        }
        doHideObjects(v, subreddit);
        return v;
    }
    public static View setCardViewType(CardEnum cardEnum, ViewGroup parent, String subreddit){
        if(subreddit.isEmpty()) {
            SettingValues.prefs.edit().putString("defaultCardView", cardEnum.name()).apply();
            SettingValues.defaultCardView = cardEnum;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putString("defaultCardView" + subreddit, cardEnum.name()).apply();
            return CreateView(parent, subreddit);

        }
    }

    public static View setLargeThumbnails(boolean b, ViewGroup parent, String subreddit){
        if(subreddit.isEmpty()) {

            SettingValues.prefs.edit().putBoolean("largeThumbnails", b).apply();
            SettingValues.largeThumbnails = b;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putBoolean("largeThumbnails" + subreddit, b).apply();
            return CreateView(parent, subreddit);

        }
    }
    public static View setInfoBarVisible(boolean b, ViewGroup parent, String subreddit){
        if(subreddit.isEmpty()) {


            SettingValues.prefs.edit().putBoolean("infoBar", b).apply();
            SettingValues.infoBar = b;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putBoolean("infoBar" + subreddit, b).apply();
            return CreateView(parent, subreddit);

        }

    }
    public static View setActionBarVisible(boolean b, ViewGroup parent, String subreddit) {
        if (subreddit.isEmpty()) {


        SettingValues.prefs.edit().putBoolean("actionBarVisible", b).apply();
        SettingValues.actionBarVisible = b;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putBoolean("actionBarVisible" + subreddit, b).apply();
            return CreateView(parent, subreddit);

        }
    }
    public static void doHideObjects(View v, String subreddit){
        if(subreddit.isEmpty()) {
            if (!SettingValues.actionBarVisible) {
                v.findViewById(R.id.actionbar).setVisibility(View.GONE);
            }
            if (!SettingValues.largeThumbnails) {
                v.findViewById(R.id.imagearea).setVisibility(View.GONE);
            }
            if (!SettingValues.infoBar) {
                v.findViewById(R.id.previewContent).setVisibility(View.GONE);
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
            if (!SettingValues.prefs.getBoolean("infoBar" + subreddit, SettingValues.infoBar)) {
                v.findViewById(R.id.previewContent).setVisibility(View.GONE);
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
        return SettingValues.prefs.getBoolean("largeThumbnails" + subreddit, SettingValues.largeThumbnails);
    }
    public static CardEnum getCardView(String subreddit) {
        return CardEnum.valueOf(SettingValues.prefs.getString("defaultCardView" + subreddit, SettingValues.defaultCardView.toString()));
    }
    public static boolean isInfoBar(String subreddit) {
        return SettingValues.prefs.getBoolean("infoBar" + subreddit, SettingValues.infoBar);
    }
    public static boolean isActionBar(String subreddit) {
        return SettingValues.prefs.getBoolean("actionBarVisible" + subreddit, SettingValues.actionBarVisible);
    }
    public enum CardEnum{
        LARGE("Big Card"),
        SMALL("Small Card"),
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
