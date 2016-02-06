package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsAutonight extends BaseActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_night);
        setupAppBar(R.id.toolbar, R.string.settings_title_auto, true, true);

        SwitchCompat fab = (SwitchCompat) findViewById(R.id.enable);

        fab.setChecked(SettingValues.autoTime);
        fab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.autoTime = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_AUTOTHEME, isChecked).apply();
            }
        });


    }



}