package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;

import com.devspark.robototextview.widget.RobotoRadioButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.FontPreferences.FontTypeComment;
import me.ccrama.redditslide.Visuals.FontPreferences.FontTypeTitle;

public class SettingsFontFragment {

    private final Activity context;

    public SettingsFontFragment(Activity context) {
        this.context = context;
    }

    private static String getFontName(int resource) {
        switch (resource) {
            case R.string.font_size_huge:
                return "Huge";
            case R.string.font_size_larger:
                return "Larger";
            case R.string.font_size_large:
                return "Large";
            case R.string.font_size_small:
                return "Small";
            case R.string.font_size_smaller:
                return "Smaller";
            case R.string.font_size_tiny:
                return "Tiny";
            case R.string.font_size_medium:
            default:
                return "Medium";
        }
    }

    public void Bind() {
        final SwitchCompat fontEnlargeLinksSwitch = context.findViewById(R.id.settings_font_enlarge_links);
        final SwitchCompat fontLinkTypeSwitch = context.findViewById(R.id.settings_font_linktype);

        final LinearLayout fontCommentFontSizeLayout = context.findViewById(R.id.settings_font_commentfontsize);
        final TextView fontCommentFontView = context.findViewById(R.id.settings_font_commentFont);

        final RobotoRadioButton fontCommentStyleRegularButton = context.findViewById(R.id.settings_font_creg);
        final RobotoRadioButton fontCommentStyleSlabButton = context.findViewById(R.id.settings_font_cslab);
        final RobotoRadioButton fontCommentStyleCondensedButton = context.findViewById(R.id.settings_font_ccond);
        final RobotoRadioButton fontCommentStyleLightButton = context.findViewById(R.id.settings_font_clight);
        final RobotoRadioButton fontCommentStyleSystemButton = context.findViewById(R.id.settings_font_cnone);

        final LinearLayout fontPostFontSizeLayout = context.findViewById(R.id.settings_font_postfontsize);
        final TextView fontPostFontView = context.findViewById(R.id.settings_font_postFont);

        final RobotoRadioButton fontPostStyleRegularButton = context.findViewById(R.id.settings_font_sreg);
        final RobotoRadioButton fontPostStyleBoldButton = context.findViewById(R.id.settings_font_sbold);
        final RobotoRadioButton fontPostStyleMediumButton = context.findViewById(R.id.settings_font_smed);
        final RobotoRadioButton fontPostStyleLightButton = context.findViewById(R.id.settings_font_slight);
        final RobotoRadioButton fontPostStyleSlabButton = context.findViewById(R.id.settings_font_sslab);
        final RobotoRadioButton fontPostStyleSlabLightButton = context.findViewById(R.id.settings_font_sslabl);
        final RobotoRadioButton fontPostStyleCondensedButton = context.findViewById(R.id.settings_font_scond);
        final RobotoRadioButton fontPostStyleCondensedLightButton = context.findViewById(R.id.settings_font_scondl);
        final RobotoRadioButton fontPostStyleCondensedBoldButton = context.findViewById(R.id.settings_font_scondb);
        final RobotoRadioButton fontPostStyleSystemButton = context.findViewById(R.id.settings_font_snone);

        final FontPreferences newFontPrefs = new FontPreferences(context);
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* Links */
        fontEnlargeLinksSwitch.setChecked(SettingValues.largeLinks);
        fontEnlargeLinksSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.largeLinks = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_LARGE_LINKS, isChecked);
        });

        fontLinkTypeSwitch.setChecked(SettingValues.typeInText);
        fontLinkTypeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.typeInText = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_TYPE_IN_TEXT, isChecked);
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* Font styles */
        fontCommentFontSizeLayout.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            Menu getPopupMenu = popup.getMenu();
            getPopupMenu.add(0, R.string.font_size_huge, 0, R.string.font_size_huge);
            getPopupMenu.add(0, R.string.font_size_larger, 0, R.string.font_size_larger);
            getPopupMenu.add(0, R.string.font_size_large, 0, R.string.font_size_large);
            getPopupMenu.add(0, R.string.font_size_medium, 0, R.string.font_size_medium);
            getPopupMenu.add(0, R.string.font_size_small, 0, R.string.font_size_small);
            getPopupMenu.add(0, R.string.font_size_smaller, 0, R.string.font_size_smaller);

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                newFontPrefs.setCommentFontStyle(
                        FontPreferences.FontStyleComment.valueOf(getFontName(item.getItemId())));
                fontCommentFontView.setText(newFontPrefs.getCommentFontStyle().getTitle());
                SettingsThemeFragment.changed = true;
                return true;
            });
            popup.show();
        });
        fontCommentFontView.setText(newFontPrefs.getCommentFontStyle().getTitle());

        switch (newFontPrefs.getFontTypeComment()) {
            case Regular:
                fontCommentStyleRegularButton.setChecked(true);
                break;
            case Slab:
                fontCommentStyleSlabButton.setChecked(true);
                break;
            case Condensed:
                fontCommentStyleCondensedButton.setChecked(true);
                break;
            case Light:
                fontCommentStyleLightButton.setChecked(true);
                break;
            case System:
                fontCommentStyleSystemButton.setChecked(true);
                break;
        }
        setCommentFontTypeListener(newFontPrefs, fontCommentStyleRegularButton, FontTypeComment.Regular);
        setCommentFontTypeListener(newFontPrefs, fontCommentStyleSlabButton, FontTypeComment.Slab);
        setCommentFontTypeListener(newFontPrefs, fontCommentStyleCondensedButton, FontTypeComment.Condensed);
        setCommentFontTypeListener(newFontPrefs, fontCommentStyleLightButton, FontTypeComment.Light);
        setCommentFontTypeListener(newFontPrefs, fontCommentStyleSystemButton, FontTypeComment.System);

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        fontPostFontSizeLayout.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            Menu getPopupMenu = popup.getMenu();
            getPopupMenu.add(0, R.string.font_size_huge, 0, R.string.font_size_huge);
            getPopupMenu.add(0, R.string.font_size_larger, 0, R.string.font_size_larger);
            getPopupMenu.add(0, R.string.font_size_large, 0, R.string.font_size_large);
            getPopupMenu.add(0, R.string.font_size_medium, 0, R.string.font_size_medium);
            getPopupMenu.add(0, R.string.font_size_small, 0, R.string.font_size_small);
            getPopupMenu.add(0, R.string.font_size_smaller, 0, R.string.font_size_smaller);
            getPopupMenu.add(0, R.string.font_size_tiny, 0, R.string.font_size_tiny);

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                newFontPrefs.setPostFontStyle(
                        FontPreferences.FontStyle.valueOf(getFontName(item.getItemId())));
                fontPostFontView.setText(newFontPrefs.getPostFontStyle().getTitle());
                SettingsThemeFragment.changed = true;
                return true;
            });
            popup.show();
        });
        fontPostFontView.setText(newFontPrefs.getPostFontStyle().getTitle());

        switch (newFontPrefs.getFontTypeTitle()) {
            case Regular:
                fontPostStyleRegularButton.setChecked(true);
                break;
            case Bold:
                fontPostStyleBoldButton.setChecked(true);
                break;
            case Medium:
                fontPostStyleMediumButton.setChecked(true);
                break;
            case Light:
                fontPostStyleLightButton.setChecked(true);
                break;
            case SlabRegular:
                fontPostStyleSlabButton.setChecked(true);
                break;
            case SlabLight:
                fontPostStyleSlabLightButton.setChecked(true);
                break;
            case CondensedRegular:
                fontPostStyleCondensedButton.setChecked(true);
                break;
            case CondensedLight:
                fontPostStyleCondensedLightButton.setChecked(true);
                break;
            case CondensedBold:
                fontPostStyleCondensedBoldButton.setChecked(true);
                break;
            case System:
                fontPostStyleSystemButton.setChecked(true);
                break;
        }
        setTitleFontTypeListener(newFontPrefs, fontPostStyleRegularButton, FontTypeTitle.Regular);
        setTitleFontTypeListener(newFontPrefs, fontPostStyleBoldButton, FontTypeTitle.Bold);
        setTitleFontTypeListener(newFontPrefs, fontPostStyleMediumButton, FontTypeTitle.Medium);
        setTitleFontTypeListener(newFontPrefs, fontPostStyleLightButton, FontTypeTitle.Light);
        setTitleFontTypeListener(newFontPrefs, fontPostStyleSlabButton, FontTypeTitle.SlabRegular);
        setTitleFontTypeListener(newFontPrefs, fontPostStyleSlabLightButton, FontTypeTitle.SlabLight);
        setTitleFontTypeListener(newFontPrefs, fontPostStyleCondensedButton, FontTypeTitle.CondensedRegular);
        setTitleFontTypeListener(newFontPrefs, fontPostStyleCondensedLightButton, FontTypeTitle.CondensedLight);
        setTitleFontTypeListener(newFontPrefs, fontPostStyleCondensedBoldButton, FontTypeTitle.CondensedBold);
        setTitleFontTypeListener(newFontPrefs, fontPostStyleSystemButton, FontTypeTitle.System);
    }

    private void setCommentFontTypeListener(final FontPreferences newFontPrefs,
                                            final RobotoRadioButton button, final FontTypeComment fontTypeComment) {
        button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                newFontPrefs.setCommentFont(fontTypeComment);
            }
        });
    }

    private void setTitleFontTypeListener(final FontPreferences newFontPrefs,
                                          final RobotoRadioButton button, final FontTypeTitle fontTypeTitle) {
        button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                newFontPrefs.setTitleFont(fontTypeTitle);
            }
        });
    }

    private void editSharedBooleanPreference(final String settingValueString, final boolean isChecked) {
        SettingValues.prefs.edit().putBoolean(settingValueString, isChecked).apply();
    }
}
