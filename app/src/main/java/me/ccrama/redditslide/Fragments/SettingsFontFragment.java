package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;

import com.devspark.robototextview.widget.RobotoRadioButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.FontPreferences;

public class SettingsFontFragment {

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

    private Activity context;

    public SettingsFontFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        final TextView colorComment = context.findViewById(R.id.settings_font_commentFont);
        colorComment.setText(new FontPreferences(context).getCommentFontStyle().getTitle());
        context.findViewById(R.id.settings_font_commentfontsize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenu().add(0, R.string.font_size_huge, 0, R.string.font_size_huge);
                popup.getMenu().add(0, R.string.font_size_larger, 0, R.string.font_size_larger);
                popup.getMenu().add(0, R.string.font_size_large, 0, R.string.font_size_large);
                popup.getMenu().add(0, R.string.font_size_medium, 0, R.string.font_size_medium);
                popup.getMenu().add(0, R.string.font_size_small, 0, R.string.font_size_small);
                popup.getMenu().add(0, R.string.font_size_smaller, 0, R.string.font_size_smaller);

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        new FontPreferences(context).setCommentFontStyle(
                                FontPreferences.FontStyleComment.valueOf(getFontName(item.getItemId())));
                        colorComment.setText(new FontPreferences(context).getCommentFontStyle().getTitle());
                        SettingsThemeFragment.changed = true;
                        return true;
                    }
                });

                popup.show();
            }
        });
        final TextView colorPost = context.findViewById(R.id.settings_font_postFont);
        colorPost.setText(new FontPreferences(context).getPostFontStyle().getTitle());
        context.findViewById(R.id.settings_font_postfontsize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenu().add(0, R.string.font_size_huge, 0, R.string.font_size_huge);
                popup.getMenu().add(0, R.string.font_size_larger, 0, R.string.font_size_larger);
                popup.getMenu().add(0, R.string.font_size_large, 0, R.string.font_size_large);
                popup.getMenu().add(0, R.string.font_size_medium, 0, R.string.font_size_medium);
                popup.getMenu().add(0, R.string.font_size_small, 0, R.string.font_size_small);
                popup.getMenu().add(0, R.string.font_size_smaller, 0, R.string.font_size_smaller);
                popup.getMenu().add(0, R.string.font_size_tiny, 0, R.string.font_size_tiny);

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        new FontPreferences(context).setPostFontStyle(
                                FontPreferences.FontStyle.valueOf(getFontName(item.getItemId())));
                        colorPost.setText(new FontPreferences(context).getPostFontStyle().getTitle());
                        SettingsThemeFragment.changed = true;
                        return true;
                    }
                });

                popup.show();
            }
        });

        switch (new FontPreferences(context).getFontTypeComment()) {
            case Regular:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_creg)).setChecked(true);
                break;
            case Slab:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_cslab)).setChecked(true);
                break;
            case Condensed:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_ccond)).setChecked(true);
                break;
            case Light:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_clight)).setChecked(true);
                break;
            case System:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_cnone)).setChecked(true);
                break;

        }
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_ccond)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setCommentFont(FontPreferences.FontTypeComment.Condensed);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_cslab)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setCommentFont(FontPreferences.FontTypeComment.Slab);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_creg)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setCommentFont(FontPreferences.FontTypeComment.Regular);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_clight)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setCommentFont(FontPreferences.FontTypeComment.Light);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_cnone)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setCommentFont(FontPreferences.FontTypeComment.System);
                }
            }
        });
        switch (new FontPreferences(context).getFontTypeTitle()) {
            case Regular:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_sreg)).setChecked(true);
                break;
            case Light:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_sregl)).setChecked(true);
                break;
            case Slab:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_sslabl)).setChecked(true);
                break;
            case SlabReg:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_sslab)).setChecked(true);
                break;
            case CondensedReg:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_scond)).setChecked(true);
                break;
            case CondensedBold:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_scondb)).setChecked(true);
                break;
            case Condensed:
                ((RobotoRadioButton) context.findViewById(R.id.scondl)).setChecked(true);
                break;
            case Bold:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_sbold)).setChecked(true);
                break;
            case Medium:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_smed)).setChecked(true);
                break;
            case System:
                ((RobotoRadioButton) context.findViewById(R.id.settings_font_snone)).setChecked(true);
                break;
        }
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_scond)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.CondensedReg);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_sslab)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.SlabReg);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.scondl)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.Condensed);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_sbold)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.Bold);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_smed)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.Medium);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_sslabl)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.Slab);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_sreg)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.Regular);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_sregl)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.Light);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_snone)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.System);
                }
            }
        });
        ((RobotoRadioButton) context.findViewById(R.id.settings_font_scondb)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsThemeFragment.changed = true;
                    new FontPreferences(context).setTitleFont(FontPreferences.FontTypeTitle.CondensedBold);
                }
            }
        });

        {
            SwitchCompat single = context.findViewById(R.id.settings_font_linktype);
            single.setChecked(SettingValues.typeInText);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.typeInText = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_TYPE_IN_TEXT, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = context.findViewById(R.id.settings_font_enlarge_links);
            single.setChecked(SettingValues.largeLinks);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.largeLinks = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_LARGE_LINKS, isChecked).apply();
                }
            });
        }
    }

}
