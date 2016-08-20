package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsData extends BaseActivityAnim {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_datasaving);
        setupAppBar(R.id.toolbar, R.string.settings_data, true, true);
        //Image mode multi choice
        ((TextView) findViewById(R.id.currentmode)).setText(SettingValues.noImages ? getString(R.string.never_load_images) : (SettingValues.imgurLq ? getString(R.string.load_medium_quality_imgur_images) : getString(R.string.low_quality_through_reddit_lower_resolution)));

        findViewById(R.id.datasavequality).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!((TextView) findViewById(R.id.lowquality)).getText().equals(getString(R.string.never))) {
                    PopupMenu popup = new PopupMenu(SettingsData.this, v);
                    popup.getMenuInflater().inflate(R.menu.imagequality_mode, popup.getMenu());

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.never:
                                    SettingValues.noImages = true;
                                    SettingValues.loadImageLq = true;
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_NO_IMAGES, true)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_IMAGE_LQ, true)
                                            .apply();
                                    break;
                                case R.id.reddit:
                                    SettingValues.loadImageLq = true;
                                    SettingValues.imgurLq = false;
                                    SettingValues.noImages = false;
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_IMAGE_LQ, true)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_NO_IMAGES, false)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_IMGUR_LQ, false)
                                            .apply();
                                    break;
                                case R.id.imgur:
                                    SettingValues.loadImageLq = true;
                                    SettingValues.imgurLq = true;
                                    SettingValues.noImages = false;
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_NO_IMAGES, false)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_IMAGE_LQ, true)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_IMGUR_LQ, true)
                                            .apply();
                                    break;
                            }
                            ((TextView) findViewById(R.id.currentmode)).setText(SettingValues.noImages ? getString(R.string.never_load_images) : (SettingValues.imgurLq ? getString(R.string.load_medium_quality_imgur_images) : getString(R.string.low_quality_through_reddit_lower_resolution)));
                            return true;
                        }
                    });

                    popup.show();
                }
            }
        });
        //Datasaving type multi choice
        ((TextView) findViewById(R.id.lowquality)).setText(SettingValues.lowResMobile ? (SettingValues.lowResAlways ? getString(R.string.datasave_always) : getString(R.string.datasave_mobile)) : getString(R.string.never));

        findViewById(R.id.datasavetype).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsData.this, v);
                popup.getMenuInflater().inflate(R.menu.imagequality_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.never:
                                SettingValues.lowResMobile = false;
                                SettingValues.lowResAlways = false;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_LOW_RES_MOBILE, false).apply();
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_LOW_RES_ALWAYS, false).apply();
                                break;
                            case R.id.mobile:
                                SettingValues.lowResMobile = true;
                                SettingValues.lowResAlways = false;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_LOW_RES_MOBILE, true).apply();
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_LOW_RES_ALWAYS, false).apply();
                                break;
                            case R.id.always:
                                SettingValues.lowResMobile = true;
                                SettingValues.lowResAlways = true;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_LOW_RES_MOBILE, true).apply();
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_LOW_RES_ALWAYS, true).apply();
                                break;
                        }
                        ((TextView) findViewById(R.id.lowquality)).setText(SettingValues.lowResMobile ? (SettingValues.lowResAlways ? getString(R.string.datasave_always) : getString(R.string.datasave_mobile)) : getString(R.string.never));
                        if (((TextView) findViewById(R.id.lowquality)).getText().equals(getString(R.string.never))) {
                            findViewById(R.id.datasavequality).setAlpha(0.25f);
                            ((TextView) findViewById(R.id.currentmode)).setText("Enable datasaving mode");
                        } else {
                            findViewById(R.id.datasavequality).setAlpha(1f);
                            ((TextView) findViewById(R.id.currentmode)).setText(SettingValues.noImages ? getString(R.string.never_load_images) : (SettingValues.imgurLq ? getString(R.string.load_medium_quality_imgur_images) : getString(R.string.low_quality_through_reddit_lower_resolution)));
                        }
                        return true;
                    }
                });

                popup.show();
            }
        });
        if (((TextView) findViewById(R.id.lowquality)).getText().equals(getString(R.string.never))) {
            findViewById(R.id.datasavequality).setAlpha(0.25f);
            ((TextView) findViewById(R.id.currentmode)).setText("Enable datasaving mode");
        }

    }
}