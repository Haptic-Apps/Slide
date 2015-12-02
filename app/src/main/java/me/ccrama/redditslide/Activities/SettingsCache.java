package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsCache extends BaseActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_cache);
        setupAppBar(R.id.toolbar, R.string.settings_title_caching, true);

        SwitchCompat fab = (SwitchCompat) findViewById(R.id.cache);
        final SwitchCompat fabType = (SwitchCompat) findViewById(R.id.cacheDefault);

        fab.setChecked(Reddit.cache);
        fab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Reddit.cache = isChecked;
                fabType.setEnabled(Reddit.cache);
                SettingValues.prefs.edit().putBoolean("cache", isChecked).apply();
            }
        });

        fabType.setChecked(Reddit.cacheDefault);
        fabType.setEnabled(Reddit.cache);
        fabType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Reddit.cacheDefault = isChecked;
                SettingValues.prefs.edit().putBoolean("cacheDefault", isChecked).apply();
            }


        });

        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }



}