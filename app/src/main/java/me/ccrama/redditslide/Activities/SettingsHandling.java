package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsHandling extends BaseActivityAnim implements
        CompoundButton.OnCheckedChangeListener {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_handling);
        setupAppBar(R.id.toolbar, R.string.settings_link_handling, true, true);

        SwitchCompat web = (SwitchCompat) findViewById(R.id.web);
        SwitchCompat image = (SwitchCompat) findViewById(R.id.image);
        SwitchCompat gif = (SwitchCompat) findViewById(R.id.gif);
        SwitchCompat album = (SwitchCompat) findViewById(R.id.album);
        SwitchCompat video = (SwitchCompat) findViewById(R.id.video);

        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.chrome);

            single.setChecked(SettingValues.customtabs);
                single.setEnabled(SettingValues.web);

            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.customtabs = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_CUSTOMTABS, isChecked).apply();

                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.albumpager);

            single.setChecked(SettingValues.albumSwipe);
            single.setEnabled(SettingValues.album);

            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.albumSwipe = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ALBUM_SWIPE, isChecked).apply();

                }
            });
        }
        web.setChecked(SettingValues.web);
        image.setChecked(SettingValues.image);
        gif.setChecked(SettingValues.gif);
        album.setChecked(SettingValues.album);
        video.setChecked(SettingValues.video);

        web.setOnCheckedChangeListener(this);
        image.setOnCheckedChangeListener(this);
        gif.setOnCheckedChangeListener(this);
        album.setOnCheckedChangeListener(this);
        video.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.web:
                SettingValues.web = isChecked;
                (((SwitchCompat) findViewById(R.id.chrome))).setEnabled(isChecked);
                SettingValues.prefs.edit().putBoolean(SettingValues.PREFS_WEB, isChecked).apply();
                break;
            case R.id.image:
                SettingValues.image = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_IMAGE, isChecked).apply();
                break;
            case R.id.gif:
                SettingValues.gif = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_GIF, isChecked).apply();
                break;
            case R.id.album:
                SettingValues.album = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ALBUM, isChecked).apply();
                break;
            case R.id.video:
                SettingValues.video = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_VIDEO, isChecked).apply();
                break;
        }

    }

}