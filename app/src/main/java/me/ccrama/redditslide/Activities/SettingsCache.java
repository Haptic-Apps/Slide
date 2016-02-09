package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsCache extends BaseActivityAnim {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_cache);
        setupAppBar(R.id.toolbar, R.string.settings_title_caching, true, true);

        SwitchCompat fab = (SwitchCompat) findViewById(R.id.cache);
        final SwitchCompat fabType = (SwitchCompat) findViewById(R.id.cacheDefault);

        fab.setChecked(SettingValues.cache);
        fab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.cache = isChecked;
                fabType.setEnabled(SettingValues.cache);
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_CACHE, isChecked).apply();
            }
        });

        fabType.setChecked(SettingValues.cacheDefault);
        fabType.setEnabled(SettingValues.cache);
        fabType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.cacheDefault = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_CACHE_DEFAULT, isChecked).apply();
            }


        });


    }



}