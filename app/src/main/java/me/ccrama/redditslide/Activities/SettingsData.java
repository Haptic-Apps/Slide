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
        setupAppBar(R.id.toolbar, "Data saving", true, true);
        final SwitchCompat single = (SwitchCompat) findViewById(R.id.imagelq);
        {

            single.setChecked(SettingValues.loadImageLq);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.loadImageLq = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_IMAGE_LQ, isChecked).apply();

                }
            });
        }
        {
            SwitchCompat single2 = (SwitchCompat) findViewById(R.id.selftextcomment);
            single2.setChecked(SettingValues.hideSelftextLeadImage);
            single2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.hideSelftextLeadImage = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SELFTEXT_IMAGE_COMMENT, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single2 = (SwitchCompat) findViewById(R.id.noload);
            single2.setChecked(SettingValues.noImages);
            single2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.noImages = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_NO_IMAGES, isChecked).apply();
                }
            });
        }
        {
            final SwitchCompat single2 = (SwitchCompat) findViewById(R.id.imgurlq);

            single2.setChecked(SettingValues.imgurLq);
            single2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.imgurLq = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_IMGUR_LQ, isChecked).apply();

                }
            });
        }
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
                            single.setEnabled(false);
                            findViewById(R.id.imagelq_text).setAlpha(0.25f);
                            findViewById(R.id.imagelq_subtext).setAlpha(0.25f);
                        } else {
                            single.setEnabled(true);
                            findViewById(R.id.imagelq_text).setAlpha(1f);
                            findViewById(R.id.imagelq_subtext).setAlpha(1f);
                        }
                        single.setChecked(SettingValues.loadImageLq);

                        return true;
                    }
                });

                popup.show();
            }
        });
        if (((TextView) findViewById(R.id.lowquality)).getText().equals(getString(R.string.never))) {
            single.setEnabled(false);
            findViewById(R.id.imagelq_text).setAlpha(0.25f);
            findViewById(R.id.imagelq_subtext).setAlpha(0.25f);
        }

    }
}