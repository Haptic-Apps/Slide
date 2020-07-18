package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;

public class SettingsDataFragment {

    private Activity context;

    public SettingsDataFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        //Image mode multi choice
        ((TextView) context.findViewById(R.id.settings_datasaving_currentmode)).setText(SettingValues.noImages ? context.getString(R.string.never_load_images)
                : (SettingValues.lqLow ? context.getString(R.string.load_low_quality)
                        : (SettingValues.lqMid ? context.getString(R.string.load_medium_quality) : context.getString(R.string.load_high_quality))));

        context.findViewById(R.id.settings_datasaving_datasavequality).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((TextView) context.findViewById(R.id.settings_datasaving_lowquality)).getText().equals(context.getString(R.string.never))) {
                    PopupMenu popup = new PopupMenu(context, v);
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
                                case R.id.low:
                                    SettingValues.loadImageLq = true;
                                    SettingValues.noImages = false;
                                    SettingValues.lqLow = true;
                                    SettingValues.lqMid = false;
                                    SettingValues.lqHigh = false;
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_IMAGE_LQ, true)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_NO_IMAGES, false)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_LQ_LOW, true)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_LQ_MID, false)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_LQ_HIGH, false)
                                            .apply();
                                    break;
                                case R.id.medium:
                                    SettingValues.loadImageLq = true;
                                    SettingValues.noImages = false;
                                    SettingValues.lqLow = false;
                                    SettingValues.lqMid = true;
                                    SettingValues.lqHigh = false;
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_IMAGE_LQ, true)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_NO_IMAGES, false)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_LQ_LOW, false)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_LQ_MID, true)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_LQ_HIGH, false)
                                            .apply();
                                    break;
                                case R.id.high:
                                    SettingValues.loadImageLq = true;
                                    SettingValues.noImages = false;
                                    SettingValues.lqLow = false;
                                    SettingValues.lqMid = false;
                                    SettingValues.lqHigh = true;
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_IMAGE_LQ, true)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_NO_IMAGES, false)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_LQ_LOW, false)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_LQ_MID, false)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_LQ_HIGH, true)
                                            .apply();
                                    break;
                            }
                            ((TextView) context.findViewById(R.id.settings_datasaving_currentmode)).setText(
                                    SettingValues.noImages ? context.getString(R.string.never_load_images)
                                            : (SettingValues.lqLow ? context.getString(R.string.load_low_quality)
                                                    : (SettingValues.lqMid ? context.getString(R.string.load_medium_quality)
                                                            : context.getString(R.string.load_high_quality))));
                            return true;
                        }
                    });

                    popup.show();
                }
            }
        });
        //Datasaving type multi choice
        ((TextView) context.findViewById(R.id.settings_datasaving_lowquality)).setText(
                SettingValues.lowResMobile ? (SettingValues.lowResAlways ? context.getString(R.string.datasave_always) : context.getString(R.string.datasave_mobile))
                        : context.getString(R.string.never));

        context.findViewById(R.id.settings_datasaving_datasavetype).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.imagequality_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.never:
                                SettingValues.lowResMobile = false;
                                SettingValues.lowResAlways = false;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_LOW_RES_MOBILE, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_LOW_RES_ALWAYS, false)
                                        .apply();
                                break;
                            case R.id.mobile:
                                SettingValues.lowResMobile = true;
                                SettingValues.lowResAlways = false;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_LOW_RES_MOBILE, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_LOW_RES_ALWAYS, false)
                                        .apply();
                                break;
                            case R.id.always:
                                SettingValues.lowResMobile = true;
                                SettingValues.lowResAlways = true;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_LOW_RES_MOBILE, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_LOW_RES_ALWAYS, true)
                                        .apply();
                                break;
                        }
                        ((TextView) context.findViewById(R.id.settings_datasaving_lowquality)).setText(
                                SettingValues.lowResMobile ? (SettingValues.lowResAlways ? context.getString(R.string.datasave_always)
                                        : context.getString(R.string.datasave_mobile)) : context.getString(R.string.never));
                        if (((TextView) context.findViewById(R.id.settings_datasaving_lowquality)).getText().equals(context.getString(R.string.never))) {
                            context.findViewById(R.id.settings_datasaving_datasavequality).setAlpha(0.25f);
                            ((TextView) context.findViewById(R.id.settings_datasaving_currentmode)).setText(
                                    "Enable datasaving mode");
                            context.findViewById(R.id.settings_datasaving_videoquality).setEnabled(false);
                        } else {
                            context.findViewById(R.id.settings_datasaving_datasavequality).setAlpha(1f);
                            ((TextView) context.findViewById(R.id.settings_datasaving_currentmode)).setText(
                                    SettingValues.noImages ? context.getString(R.string.never_load_images)
                                            : (SettingValues.lqLow ? context.getString(R.string.load_low_quality)
                                                    : (SettingValues.lqMid ? context.getString(R.string.load_medium_quality)
                                                            : context.getString(R.string.load_high_quality))));
                            context.findViewById(R.id.settings_datasaving_videoquality).setEnabled(true);
                        }
                        return true;
                    }
                });

                popup.show();
            }
        });
        if (((TextView) context.findViewById(R.id.settings_datasaving_lowquality)).getText().equals(context.getString(R.string.never))) {
            context.findViewById(R.id.settings_datasaving_datasavequality).setAlpha(0.25f);
            ((TextView) context.findViewById(R.id.settings_datasaving_currentmode)).setText("Enable datasaving mode");
        }

        SwitchCompat video = context.findViewById(R.id.settings_datasaving_videoquality);
        video.setChecked(SettingValues.lqVideos);
        video.setEnabled(SettingValues.lowResMobile || SettingValues.lowResAlways);

        video.setOnCheckedChangeListener((v, checked) -> {
            SettingValues.lqVideos = checked;
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_LQ_VIDEOS, checked).apply();
        });
    }

}
