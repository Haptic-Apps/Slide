package me.ccrama.redditslide.Views;

import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by ccrama on 9/18/2015.
 */
public class CreateCardView {
    public static View CreateView(ViewGroup viewGroup) {
        CardEnum cardEnum = SettingValues.defaultCardView;
        View v = null;
        switch (cardEnum) {
            case LARGE:
                if (SettingValues.middleImage) {
                    v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_largecard_middle, viewGroup, false);

                } else {
                    v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_largecard, viewGroup, false);
                }
                break;
            case LIST:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_list, viewGroup, false);
                break;

        }

        doHideObjects(v, false);
        return v;
    }

    public static View CreateView(ViewGroup viewGroup, Boolean secondary, String sub) {
        secondary = false; //removing secondary layouts for now
        String subreddit = (secondary) ? "second" : "";
        sub = sub.toLowerCase();
        CardEnum cardEnum = CardEnum.valueOf(SettingValues.prefs.getString(subreddit + "defaultCardViewNew", SettingValues.defaultCardView.toString()).toUpperCase());
        View v = null;
        switch (cardEnum) {
            case LARGE:
                if (isMiddle(secondary)) {
                    v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_largecard_middle, viewGroup, false);

                } else {
                    v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_largecard, viewGroup, false);
                }
                break;

            case LIST:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_list, viewGroup, false);
                break;

        }
        doHideObjects(v, secondary);
        return v;
    }

    public static void resetColorCard(View v) {
        ((TextView) v.findViewById(R.id.subreddit)).setTextColor(((TextView) v.findViewById(R.id.information)).getCurrentTextColor());
        TypedValue background = new TypedValue();
        v.getContext().getTheme().resolveAttribute(R.attr.card_background, background, true);
        ((CardView) v.findViewById(R.id.card)).setCardBackgroundColor(background.data);

    }

    public static void colorCard(String sec, View v, String subToMatch, boolean secondary) {

        resetColorCard(v);
        if (Reddit.colorBack && Palette.getColor(sec) != Palette.getDefaultColor() && !subToMatch.equals("nomatching")) {
            if(!secondary && !Reddit.colorEverywhere || secondary)
                ((CardView) v.findViewById(R.id.card)).setCardBackgroundColor(Palette.getColor(sec));


        }
    }

    public static View setCardViewType(CardEnum cardEnum, ViewGroup parent, Boolean secondary, String sub) {
        secondary = false; //removing secondary layouts for now

        String subreddit = (secondary) ? "second" : "";
        sub = sub.toLowerCase();
        if (subreddit.isEmpty()) {
            SettingValues.prefs.edit().putString("defaultCardViewNew", cardEnum.name()).apply();
            SettingValues.defaultCardView = cardEnum;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putString(subreddit + "defaultCardViewNew", cardEnum.name()).apply();
            return CreateView(parent, secondary, sub);

        }
    }


    public static View setBigPicEnabled(Boolean b, ViewGroup parent) {


        SettingValues.prefs.edit().putBoolean("bigPicEnabled", b).apply();

        SettingValues.bigPicEnabled = b;
        return CreateView(parent);


    }

    public static View setBigPicCropped(Boolean b, ViewGroup parent) {


        SettingValues.prefs.edit().putBoolean("bigPicCropped", b).apply();

        SettingValues.bigPicCropped = b;
        return CreateView(parent);


    }

    public static void setColorIndicicator(SettingValues.ColorIndicator b) {
        String subreddit = false ? "second" : "";
        if (subreddit.isEmpty()) {


            SettingValues.prefs.edit().putString("colorIndicatorNew", b.toString()).apply();

            SettingValues.colorIndicator = b;

        } else {
            SettingValues.prefs.edit().putString(subreddit + "colorIndicatorNew", b.toString()).apply();
        }

    }

    public static View setActionBarVisible(boolean b, ViewGroup parent, Boolean secondary, String sub) {
        secondary = false; //removing secondary layouts for now

        String subreddit = (secondary) ? "second" : "";
        sub = sub.toLowerCase();
        if (subreddit.isEmpty()) {


            SettingValues.prefs.edit().putBoolean("actionBarVisibleNew", b).apply();
            SettingValues.actionBarVisible = b;
            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putBoolean(subreddit + "actionBarVisibleNew", b).apply();
            return CreateView(parent, secondary, sub);

        }
    }

    public static View setMiddleCard(boolean b, ViewGroup parent, Boolean secondary, String sub) {
        secondary = false; //removing secondary layouts for now

        String subreddit = (secondary) ? "second" : "";
        sub = sub.toLowerCase();
        if (subreddit.isEmpty()) {


            SettingValues.prefs.edit().putBoolean("middleCard", b).apply();
            SettingValues.middleImage = b;

            return CreateView(parent);

        } else {
            SettingValues.prefs.edit().putBoolean(subreddit + "middleCard", b).apply();
            return CreateView(parent, secondary, sub);

        }
    }

    private static void doHideObjects(View v, Boolean secondary) {
        secondary = false; //removing secondary layouts for now

        String subreddit = (secondary) ? "second" : "";
        if (subreddit.isEmpty()) {
            if (!SettingValues.actionBarVisible) {
                v.findViewById(R.id.actionbar).setVisibility(View.GONE);

            }

            if (SettingValues.bigPicCropped) {
                ((ImageView) v.findViewById(R.id.leadimage)).setMaxHeight(900);
                ((ImageView) v.findViewById(R.id.leadimage)).setScaleType(ImageView.ScaleType.CENTER_CROP);

            }
            if (!SettingValues.bigPicEnabled) {
                v.findViewById(R.id.thumbimage2).setVisibility(View.VISIBLE);

                v.findViewById(R.id.base2).setVisibility(View.GONE);
                v.findViewById(R.id.imagearea).setVisibility(View.GONE);

            } else if (SettingValues.bigPicEnabled) {
                v.findViewById(R.id.thumbimage2).setVisibility(View.GONE);

            }
        } else {
            if (!SettingValues.prefs.getBoolean(subreddit + "actionBarVisibleNew", SettingValues.actionBarVisible)) {
                v.findViewById(R.id.actionbar).setVisibility(View.GONE);
            }

            if (SettingValues.bigPicCropped) {
                ((ImageView) v.findViewById(R.id.leadimage)).setMaxHeight(900);
                ((ImageView) v.findViewById(R.id.leadimage)).setScaleType(ImageView.ScaleType.CENTER_CROP);

            }
            if (!SettingValues.bigPicEnabled) {
                v.findViewById(R.id.thumbimage2).setVisibility(View.VISIBLE);

                v.findViewById(R.id.base2).setVisibility(View.GONE);
                v.findViewById(R.id.imagearea).setVisibility(View.GONE);

            } else if (SettingValues.bigPicEnabled) {
                v.findViewById(R.id.thumbimage2).setVisibility(View.GONE);

            }

        }
    }

    public static boolean isCard(Boolean secondary) {
        secondary = false; //removing secondary layouts for now

        String subreddit = (secondary) ? "second" : "";

        return CardEnum.valueOf(SettingValues.prefs.getString(subreddit + "defaultCardViewNew", SettingValues.defaultCardView.toString())) == CardEnum.LARGE;
    }

    public static boolean isMiddle(Boolean secondary) {
        secondary = false; //removing secondary layouts for now

        String subreddit = (secondary) ? "second" : "";

        return SettingValues.prefs.getBoolean(subreddit + "middleCard", false);
    }

    public static CardEnum getCardView(Boolean secondary) {
        secondary = false; //removing secondary layouts for now

        String subreddit = (secondary) ? "second" : "";

        return CardEnum.valueOf(SettingValues.prefs.getString(subreddit + "defaultCardViewNew", SettingValues.defaultCardView.toString()));
    }

    public static SettingValues.ColorIndicator getColorIndicator() {
        String subreddit = false ? "second" : "";

        return SettingValues.ColorIndicator.valueOf(SettingValues.prefs.getString(subreddit + "colorIndicatorNew", SettingValues.colorIndicator.toString()));
    }

    public static SettingValues.ColorMatchingMode getColorMatchingMode() {
        String subreddit = false ? "second" : "";

        return SettingValues.ColorMatchingMode.valueOf(SettingValues.prefs.getString(subreddit + "ccolorMatchingModeNew", SettingValues.colorMatchingMode.toString()));
    }

    public static void setColorMatchingMode(SettingValues.ColorMatchingMode b) {
        String subreddit = false ? "second" : "";
        if (subreddit.isEmpty()) {


            SettingValues.prefs.edit().putString("ccolorMatchingModeNew", b.toString()).apply();

            SettingValues.colorMatchingMode = b;

        } else {
            SettingValues.prefs.edit().putString(subreddit + "ccolorMatchingModeNew", b.toString()).apply();

        }

    }


    public static boolean isActionBar(Boolean secondary) {
        secondary = false; //removing secondary layouts for now

        String subreddit = (secondary) ? "second" : "";

        return SettingValues.prefs.getBoolean(subreddit + "actionBarVisibleNew", SettingValues.actionBarVisible);
    }

    public enum CardEnum {
        LARGE("Big Card"),
        LIST("List");
        final String displayName;

        CardEnum(String s) {
            this.displayName = s;
        }

        public String getDisplayName() {
            return displayName;
        }
    }


}
