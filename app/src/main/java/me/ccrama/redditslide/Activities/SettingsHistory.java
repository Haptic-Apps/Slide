package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;

public class SettingsHistory extends BaseActivityAnim {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_history);
        setupAppBar(R.id.toolbar, R.string.settings_title_history, true, true);

        {
            SwitchCompat storeHistory = ((SwitchCompat) findViewById(R.id.storehistory));
            storeHistory.setChecked(SettingValues.storeHistory);
            storeHistory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.storeHistory = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_STORE_HISTORY, isChecked).apply();

                    if (isChecked) {
                        findViewById(R.id.scrollseen).setEnabled(true);
                        findViewById(R.id.storensfw).setEnabled(true);
                    } else {
                        ((SwitchCompat) findViewById(R.id.storensfw)).setChecked(false);
                        ((SwitchCompat) findViewById(R.id.storensfw)).setEnabled(false);
                        SettingValues.storeNSFWHistory = false;
                        SettingValues.prefs.edit().putBoolean(SettingValues.PREF_STORE_NSFW_HISTORY, false).apply();

                        ((SwitchCompat) findViewById(R.id.scrollseen)).setChecked(false);
                        ((SwitchCompat) findViewById(R.id.scrollseen)).setEnabled(false);
                        SettingValues.scrollSeen = false;
                        SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SCROLL_SEEN, false).apply();
                    }
                }
            });
        }

        {
            SwitchCompat nsfw = ((SwitchCompat) findViewById(R.id.storensfw));
            nsfw.setChecked(SettingValues.storeNSFWHistory);
            nsfw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.storeNSFWHistory = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_STORE_NSFW_HISTORY, isChecked).apply();
                }
            });
        }

        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.scrollseen);
            single.setChecked(SettingValues.scrollSeen);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.scrollSeen = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SCROLL_SEEN, isChecked).apply();

                }
            });
        }
    }
}
