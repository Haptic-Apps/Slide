package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.widget.SwitchCompat;

import me.ccrama.redditslide.Activities.Settings;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LinkUtil;

public class SettingsRedditFragment {

    private Activity context;

    public SettingsRedditFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        {
            final SwitchCompat thumbnails = context.findViewById(R.id.settings_reddit_nsfwcontent);
            thumbnails.setChecked(SettingValues.showNSFWContent);

            thumbnails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.showNSFWContent = isChecked;
                    Settings.changed = true;

                    if (isChecked) {
                        (context.findViewById(R.id.settings_reddit_nsfwrpev)).setEnabled(true);
                        context.findViewById(R.id.settings_reddit_nsfwrpev_text).setAlpha(1f);
                        ((SwitchCompat) context.findViewById(R.id.settings_reddit_nsfwrpev)).setChecked(SettingValues.getIsNSFWEnabled());

                        (context.findViewById(R.id.settings_reddit_nsfwcollection)).setEnabled(true);
                        context.findViewById(R.id.settings_reddit_nsfwcollection_text).setAlpha(1f);
                        ((SwitchCompat) context.findViewById(R.id.settings_reddit_nsfwcollection)).setChecked(SettingValues.hideNSFWCollection);

                    } else {
                        ((SwitchCompat) context.findViewById(R.id.settings_reddit_nsfwrpev)).setChecked(false);
                        (context.findViewById(R.id.settings_reddit_nsfwrpev)).setEnabled(false);
                        context.findViewById(R.id.settings_reddit_nsfwrpev_text).setAlpha(0.25f);

                        ((SwitchCompat) context.findViewById(R.id.settings_reddit_nsfwcollection)).setChecked(false);
                        (context.findViewById(R.id.settings_reddit_nsfwcollection)).setEnabled(false);
                        context.findViewById(R.id.settings_reddit_nsfwcollection_text).setAlpha(0.25f);

                    }
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SHOW_NSFW_CONTENT, isChecked).apply();
                }
            });
        }
        {
            final SwitchCompat thumbnails = context.findViewById(R.id.settings_reddit_nsfwrpev);

            if (!((SwitchCompat) context.findViewById(R.id.settings_reddit_nsfwcontent)).isChecked()) {
                thumbnails.setChecked(true);
                thumbnails.setEnabled(false);
                context.findViewById(R.id.settings_reddit_nsfwrpev_text).setAlpha(0.25f);
            } else {
                thumbnails.setChecked(SettingValues.getIsNSFWEnabled());
            }

            thumbnails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Settings.changed = true;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_HIDE_NSFW_PREVIEW + Authentication.name, isChecked).apply();
                }
            });
        }
        {
            final SwitchCompat thumbnails = context.findViewById(R.id.settings_reddit_nsfwcollection);

            if (!((SwitchCompat) context.findViewById(R.id.settings_reddit_nsfwcontent)).isChecked()) {
                thumbnails.setChecked(true);
                thumbnails.setEnabled(false);
                context.findViewById(R.id.settings_reddit_nsfwcollection_text).setAlpha(0.25f);
            } else {
                thumbnails.setChecked(SettingValues.hideNSFWCollection);
            }

            thumbnails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Settings.changed = true;
                    SettingValues.hideNSFWCollection = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_HIDE_NSFW_COLLECTION, isChecked).apply();
                }
            });
        }
        {
            final SwitchCompat thumbnails = context.findViewById(R.id.settings_reddit_ignorepref);

            thumbnails.setChecked(SettingValues.ignoreSubSetting);

            thumbnails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Settings.changed = true;
                    SettingValues.ignoreSubSetting = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_IGNORE_SUB_SETTINGS, isChecked).apply();
                }
            });
        }
        context.findViewById(R.id.settings_reddit_viewRedditPrefs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkUtil.openUrl("https://www.reddit.com/prefs/", Palette.getDefaultColor(), context);
            }
        });
    }

}
