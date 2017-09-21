package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LinkUtil;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsReddit extends BaseActivityAnim {


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_reddit);
        setupAppBar(R.id.toolbar, R.string.settings_reddit_prefs, true, true);
        {
        final SwitchCompat thumbnails = (SwitchCompat) findViewById(R.id.nsfwcontent);
        thumbnails.setChecked(SettingValues.showNSFWContent);

        thumbnails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.showNSFWContent = isChecked;
                Settings.changed = true;

                if (isChecked) {
                    (findViewById(R.id.nsfwrpev)).setEnabled(true);
                    findViewById(R.id.nsfwrpev_text).setAlpha(1f);
                    ((SwitchCompat) findViewById(R.id.nsfwrpev)).setChecked(SettingValues.getIsNSFWEnabled());

                    (findViewById(R.id.nsfwcollection)).setEnabled(true);
                    findViewById(R.id.nsfwcollection_text).setAlpha(1f);
                    ((SwitchCompat) findViewById(R.id.nsfwcollection)).setChecked(SettingValues.hideNSFWCollection);

                } else {
                    ((SwitchCompat) findViewById(R.id.nsfwrpev)).setChecked(false);
                    (findViewById(R.id.nsfwrpev)).setEnabled(false);
                    findViewById(R.id.nsfwrpev_text).setAlpha(0.25f);

                    ((SwitchCompat) findViewById(R.id.nsfwcollection)).setChecked(false);
                    (findViewById(R.id.nsfwcollection)).setEnabled(false);
                    findViewById(R.id.nsfwcollection_text).setAlpha(0.25f);

                }
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_HIDE_NSFW_CONTENT, isChecked).apply();
            }
        });
    }
    {
        final SwitchCompat thumbnails = (SwitchCompat) findViewById(R.id.nsfwrpev);

        if (!((SwitchCompat) findViewById(R.id.nsfwcontent)).isChecked()) {
            thumbnails.setChecked(true);
            thumbnails.setEnabled(false);
            findViewById(R.id.nsfwrpev_text).setAlpha(0.25f);
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
            final SwitchCompat thumbnails = (SwitchCompat) findViewById(R.id.nsfwcollection);

            if (!((SwitchCompat) findViewById(R.id.nsfwcontent)).isChecked()) {
                thumbnails.setChecked(true);
                thumbnails.setEnabled(false);
                findViewById(R.id.nsfwcollection_text).setAlpha(0.25f);
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
            final SwitchCompat thumbnails = (SwitchCompat) findViewById(R.id.ignorepref);

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
        findViewById(R.id.viewRedditPrefs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkUtil.openUrl("https://www.reddit.com/prefs/", Palette.getDefaultColor(),
                        SettingsReddit.this);
            }
        });
    }
}