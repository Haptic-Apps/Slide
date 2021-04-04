package me.ccrama.redditslide.Views;

import android.animation.ValueAnimator;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.AnimatorUtil;
import me.ccrama.redditslide.util.DisplayUtil;

/**
 * Created by ccrama on 9/18/2015.
 */
public class CreateCardView {

    public static View CreateViewNews(ViewGroup viewGroup){
        return LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_news, viewGroup, false);
    }
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

                //if the radius is set to 0 on KitKat--it crashes.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((CardView) v.findViewById(R.id.card)).setRadius(0f);
                }
                break;
            case DESKTOP:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_list_desktop, viewGroup, false);

                //if the radius is set to 0 on KitKat--it crashes.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((CardView) v.findViewById(R.id.card)).setRadius(0f);
                }
                break;
        }

        View thumbImage = v.findViewById(R.id.thumbimage2);
        /**
         * If the user wants small thumbnails, revert the list style to the "old" list view.
         * The "old" thumbnails were (70dp x 70dp).
         * Adjusts the paddingTop of the innerrelative, and adjusts the margins on the thumbnail.
         */
        if (!SettingValues.bigThumbnails) {
            if(SettingValues.defaultCardView == CardEnum.DESKTOP){
                final int SQUARE_THUMBNAIL_SIZE = 48;

                thumbImage.getLayoutParams().height = DisplayUtil.dpToPxVertical(SQUARE_THUMBNAIL_SIZE);
                thumbImage.getLayoutParams().width = DisplayUtil.dpToPxHorizontal(SQUARE_THUMBNAIL_SIZE);
            } else {
                final int SQUARE_THUMBNAIL_SIZE = 70;
                thumbImage.getLayoutParams().height = DisplayUtil.dpToPxVertical(SQUARE_THUMBNAIL_SIZE);
                thumbImage.getLayoutParams().width = DisplayUtil.dpToPxHorizontal(SQUARE_THUMBNAIL_SIZE);

                final int EIGHT_DP_Y = DisplayUtil.dpToPxVertical(8);
                final int EIGHT_DP_X = DisplayUtil.dpToPxHorizontal(8);
                ((RelativeLayout.LayoutParams) thumbImage.getLayoutParams())
                        .setMargins(EIGHT_DP_X * 2, EIGHT_DP_Y, EIGHT_DP_X, EIGHT_DP_Y);
                v.findViewById(R.id.innerrelative).setPadding(0, EIGHT_DP_Y, 0, 0);
            }
        }

        if (SettingValues.noThumbnails) {
            final int SQUARE_THUMBNAIL_SIZE = 0;
            thumbImage.getLayoutParams().height = DisplayUtil.dpToPxVertical(SQUARE_THUMBNAIL_SIZE);
            thumbImage.getLayoutParams().width = DisplayUtil.dpToPxHorizontal(SQUARE_THUMBNAIL_SIZE);
        }

        doHideObjects(v);
        return v;
    }

    public static void resetColorCard(View v) {
        v.setTag(v.getId(), "none");

        TypedValue background = new TypedValue();
        v.getContext().getTheme().resolveAttribute(R.attr.card_background, background, true);
        ((CardView) v.findViewById(R.id.card)).setCardBackgroundColor(background.data);
        if (!SettingValues.actionbarVisible) {
            for (View v2 : getViewsByTag((ViewGroup) v, "tintactionbar")) {
                v2.setVisibility(View.GONE);
            }
        }

        doColor(getViewsByTag((ViewGroup) v, "tint"));
        doColorSecond(getViewsByTag((ViewGroup) v, "tintsecond"));
        doColorSecond(getViewsByTag((ViewGroup) v, "tintactionbar"));
    }

    public static void doColor(ArrayList<View> v) {
        for (View v2 : v) {
            if (v2 instanceof TextView) {
                ((TextView) v2).setTextColor(Palette.getCurrentFontColor(v2.getContext()));
            } else if (v2 instanceof ImageView) {
                ((ImageView) v2).setColorFilter(Palette.getCurrentTintColor(v2.getContext()));

            }
        }
    }

    public static void doColorSecond(ArrayList<View> v) {
        for (View v2 : v) {
            if (v2 instanceof TextView) {
                ((TextView) v2).setTextColor(Palette.getCurrentTintColor(v2.getContext()));
            } else if (v2 instanceof ImageView) {
                ((ImageView) v2).setColorFilter(Palette.getCurrentTintColor(v2.getContext()));

            }
        }
    }

    public static void resetColor(ArrayList<View> v) {
        for (View v2 : v) {
            if (v2 instanceof TextView) {
                ((TextView) v2).setTextColor(Palette.getWhiteFontColor());
            } else if (v2 instanceof ImageView) {
                ((ImageView) v2).setColorFilter(Palette.getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

            }
        }
    }

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    public static void colorCard(String sec, View v, String subToMatch, boolean secondary) {
        resetColorCard(v);
        if ((SettingValues.colorBack && !SettingValues.colorSubName && Palette.getColor(sec) != Palette.getDefaultColor()) || (subToMatch.equals("nomatching") && (SettingValues.colorBack && !SettingValues.colorSubName && Palette.getColor(sec) != Palette.getDefaultColor()))) {
            if (secondary || !SettingValues.colorEverywhere) {
                ((CardView) v.findViewById(R.id.card)).setCardBackgroundColor(Palette.getColor(sec));
                v.setTag(v.getId(), "color");
                resetColor(getViewsByTag((ViewGroup) v, "tint"));
                resetColor(getViewsByTag((ViewGroup) v, "tintsecond"));
                resetColor(getViewsByTag((ViewGroup) v, "tintactionbar"));
            }
        }
    }

    public static View setActionbarVisible(boolean isChecked, ViewGroup parent) {

        SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ACTIONBAR_VISIBLE, isChecked).apply();
        SettingValues.actionbarVisible = isChecked;
        return CreateView(parent);

    }

    public static View setSmallTag(boolean isChecked, ViewGroup parent) {

        SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SMALL_TAG, isChecked).apply();
        SettingValues.smallTag = isChecked;
        return CreateView(parent);

    }

    public static View setCardViewType(CardEnum cardEnum, ViewGroup parent) {
        SettingValues.prefs.edit().putBoolean("middleCard", false).apply();
        SettingValues.middleImage = false;

        SettingValues.prefs.edit().putString("defaultCardViewNew", cardEnum.name()).apply();
        SettingValues.defaultCardView = cardEnum;

        return CreateView(parent);
    }

    public static View setBigPicEnabled(Boolean b, ViewGroup parent) {
        SettingValues.prefs.edit().putBoolean("noThumbnails", b).apply();
        SettingValues.noThumbnails = false;

        SettingValues.prefs.edit().putBoolean("bigPicEnabled", b).apply();
        SettingValues.bigPicEnabled = b;

        SettingValues.prefs.edit().putBoolean("bigPicCropped", false).apply();
        SettingValues.bigPicCropped = false;

        return CreateView(parent);
    }

    public static View setBigPicCropped(Boolean b, ViewGroup parent) {
        SettingValues.prefs.edit().putBoolean("noThumbnails", b).apply();
        SettingValues.noThumbnails = false;

        SettingValues.prefs.edit().putBoolean("bigPicCropped", b).apply();
        SettingValues.bigPicCropped = b;

        SettingValues.prefs.edit().putBoolean("bigPicEnabled", b).apply();
        SettingValues.bigPicEnabled = b;

        return CreateView(parent);
    }

    public static View setNoThumbnails(Boolean b, ViewGroup parent) {
        SettingValues.prefs.edit().putBoolean("bigPicEnabled", b).apply();
        SettingValues.bigPicEnabled = false;

        SettingValues.prefs.edit().putBoolean("bigPicCropped", false).apply();
        SettingValues.bigPicCropped = false;

        SettingValues.prefs.edit().putBoolean("noThumbnails", b).apply();
        SettingValues.noThumbnails = b;

        return CreateView(parent);
    }

    public static View setMiddleCard(boolean b, ViewGroup parent) {
        SettingValues.prefs.edit().putString("defaultCardViewNew", CardEnum.LARGE.name()).apply();
        SettingValues.defaultCardView = CardEnum.LARGE;

        SettingValues.prefs.edit().putBoolean("middleCard", b).apply();
        SettingValues.middleImage = b;

        return CreateView(parent);
    }

    public static View setSwitchThumb(boolean b, ViewGroup parent) {


        SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SWITCH_THUMB, b).apply();
        SettingValues.switchThumb = b;

        return CreateView(parent);


    }

    public static void toggleActionbar(View v) {
        if (!SettingValues.actionbarVisible) {

            ValueAnimator a = AnimatorUtil.flipAnimatorIfNonNull(
                    v.findViewById(R.id.upvote).getVisibility() == View.VISIBLE, v.findViewById(R.id.secondMenu));
            if (a != null)
                a.start();
            for (View v2 : getViewsByTag((ViewGroup) v, "tintactionbar")) {
                if (v2.getId() != R.id.mod && v2.getId() != R.id.edit) {
                    if (v2.getId() == R.id.save) {
                        if (SettingValues.saveButton) {
                            if (v2.getVisibility() == View.VISIBLE) {
                                AnimatorUtil.animateOut(v2);
                            } else {
                                AnimatorUtil.animateIn(v2, 36);
                            }
                        }
                    } else if (v2.getId() == R.id.hide) {
                        if (SettingValues.hideButton) {
                            if (v2.getVisibility() == View.VISIBLE) {
                                AnimatorUtil.animateOut(v2);
                            } else {
                                AnimatorUtil.animateIn(v2, 36);
                            }
                        }
                    } else {
                        if (v2.getVisibility() == View.VISIBLE) {
                            AnimatorUtil.animateOut(v2);
                        } else {
                            AnimatorUtil.animateIn(v2, 36);
                        }
                    }
                }
            }
        }
    }

    private static void doHideObjects(final View v) {
        if (SettingValues.smallTag) {
            v.findViewById(R.id.base).setVisibility(View.GONE);
            v.findViewById(R.id.tag).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.tag).setVisibility(View.GONE);
        }
        if (SettingValues.bigPicCropped) {
            ((ImageView) v.findViewById(R.id.leadimage)).setMaxHeight(900);
            ((ImageView) v.findViewById(R.id.leadimage)).setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        if (!SettingValues.actionbarVisible && !SettingValues.actionbarTap) {
            for (View v2 : getViewsByTag((ViewGroup) v, "tintactionbar")) {
                v2.setVisibility(View.GONE);
            }
            v.findViewById(R.id.secondMenu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v3) {
                    toggleActionbar(v);
                }
            });
        } else {
            v.findViewById(R.id.secondMenu).setVisibility(View.GONE);
            if (SettingValues.actionbarTap) {
                for (View v2 : getViewsByTag((ViewGroup) v, "tintactionbar")) {
                    v2.setVisibility(View.GONE);
                }
                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        toggleActionbar(v);
                        return true;
                    }
                });
            }
        }
        if (SettingValues.switchThumb) {
            RelativeLayout.LayoutParams picParams = (RelativeLayout.LayoutParams) v.findViewById(R.id.thumbimage2).getLayoutParams();
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.findViewById(R.id.inside).getLayoutParams();

            if (!SettingValues.actionbarVisible && !SettingValues.actionbarTap) {
                picParams.addRule(RelativeLayout.LEFT_OF, R.id.secondMenu);
            } else {
                picParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            }

            picParams.setMargins(picParams.rightMargin, picParams.topMargin, picParams.leftMargin, picParams.bottomMargin);

            layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.thumbimage2);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                layoutParams.removeRule(RelativeLayout.RIGHT_OF);
            } else {
                layoutParams.addRule(RelativeLayout.RIGHT_OF, 0);
            }
        }
        if (!SettingValues.bigPicEnabled) {
            v.findViewById(R.id.thumbimage2).setVisibility(View.VISIBLE);
            v.findViewById(R.id.headerimage).setVisibility(View.GONE);
        } else if (SettingValues.bigPicEnabled) {
            v.findViewById(R.id.thumbimage2).setVisibility(View.GONE);
        }


    }

    public static boolean isCard() {
        return CardEnum.valueOf(SettingValues.prefs.getString("defaultCardViewNew", SettingValues.defaultCardView.toString())) == CardEnum.LARGE;
    }

    public static boolean isMiddle() {
        return SettingValues.prefs.getBoolean("middleCard", false);
    }

    public static boolean isDesktop() {
        return CardEnum.valueOf(SettingValues.prefs.getString("defaultCardViewNew", SettingValues.defaultCardView.toString())) == CardEnum.DESKTOP;
    }

    public enum CardEnum {
        LARGE("Big Card"),
        LIST("List"),
        DESKTOP("Desktop");
        final String displayName;

        CardEnum(String s) {
            this.displayName = s;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
