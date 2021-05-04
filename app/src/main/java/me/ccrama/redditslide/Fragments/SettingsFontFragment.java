package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;

import com.devspark.robototextview.widget.RobotoRadioButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.FontPreferences;

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

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* Links */
        fontEnlargeLinksSwitch.setChecked(SettingValues.largeLinks);
        fontEnlargeLinksSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.largeLinks = isChecked;
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_LARGE_LINKS, isChecked).apply();
        });

        fontLinkTypeSwitch.setChecked(SettingValues.typeInText);
        fontLinkTypeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.typeInText = isChecked;
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_TYPE_IN_TEXT, isChecked).apply();
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* Font styles */
        fontCommentFontSizeLayout.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            popup.getMenu().add(0, R.string.font_size_huge, 0, R.string.font_size_huge);
            popup.getMenu().add(0, R.string.font_size_larger, 0, R.string.font_size_larger);
            popup.getMenu().add(0, R.string.font_size_large, 0, R.string.font_size_large);
            popup.getMenu().add(0, R.string.font_size_medium, 0, R.string.font_size_medium);
            popup.getMenu().add(0, R.string.font_size_small, 0, R.string.font_size_small);
            popup.getMenu().add(0, R.string.font_size_smaller, 0, R.string.font_size_smaller);

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                new FontPreferences(context).setCommentFontStyle(
                        FontPreferences.FontStyleComment.valueOf(getFontName(item.getItemId())));
                fontCommentFontView.setText(new FontPreferences(context).getCommentFontStyle().getTitle());
                SettingsThemeFragment.changed = true;
                return true;
            });
            popup.show();
        });
        fontCommentFontView.setText(new FontPreferences(context).getCommentFontStyle().getTitle());

        switch (new FontPreferences(context).getFontTypeComment()) {
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
        fontCommentStyleRegularButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setCommentFont(FontPreferences.FontTypeComment.Regular);
            }
        });
        fontCommentStyleSlabButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setCommentFont(FontPreferences.FontTypeComment.Slab);
            }
        });
        fontCommentStyleCondensedButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setCommentFont(FontPreferences.FontTypeComment.Condensed);
            }
        });
        fontCommentStyleLightButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setCommentFont(FontPreferences.FontTypeComment.Light);
            }
        });
        fontCommentStyleSystemButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setCommentFont(FontPreferences.FontTypeComment.System);
            }
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        fontPostFontSizeLayout.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            popup.getMenu().add(0, R.string.font_size_huge, 0, R.string.font_size_huge);
            popup.getMenu().add(0, R.string.font_size_larger, 0, R.string.font_size_larger);
            popup.getMenu().add(0, R.string.font_size_large, 0, R.string.font_size_large);
            popup.getMenu().add(0, R.string.font_size_medium, 0, R.string.font_size_medium);
            popup.getMenu().add(0, R.string.font_size_small, 0, R.string.font_size_small);
            popup.getMenu().add(0, R.string.font_size_smaller, 0, R.string.font_size_smaller);
            popup.getMenu().add(0, R.string.font_size_tiny, 0, R.string.font_size_tiny);

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                new FontPreferences(context).setPostFontStyle(
                        FontPreferences.FontStyle.valueOf(getFontName(item.getItemId())));
                fontPostFontView.setText(new FontPreferences(context).getPostFontStyle().getTitle());
                SettingsThemeFragment.changed = true;
                return true;
            });
            popup.show();
        });
        fontPostFontView.setText(new FontPreferences(context).getPostFontStyle().getTitle());

        switch (new FontPreferences(context).getFontTypeTitle()) {
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
        fontPostStyleRegularButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.Regular);
            }
        });
        fontPostStyleBoldButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.Bold);
            }
        });
        fontPostStyleMediumButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.Medium);
            }
        });
        fontPostStyleLightButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.Light);
            }
        });
        fontPostStyleSlabButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.SlabRegular);
            }
        });
        fontPostStyleSlabLightButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.SlabLight);
            }
        });
        fontPostStyleCondensedButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.CondensedRegular);
            }
        });
        fontPostStyleCondensedLightButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.CondensedLight);
            }
        });
        fontPostStyleCondensedBoldButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.CondensedBold);
            }
        });
        fontPostStyleSystemButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                SettingsThemeFragment.changed = true;
                new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.System);
            }
        });
    }
}
