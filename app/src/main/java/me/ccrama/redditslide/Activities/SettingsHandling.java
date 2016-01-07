package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsHandling extends BaseActivity implements
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

        web.setChecked(Reddit.web);
        image.setChecked(Reddit.image);
        gif.setChecked(Reddit.gif);
        album.setChecked(Reddit.album);
        video.setChecked(Reddit.video);

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
                Reddit.web = isChecked;
                SettingValues.prefs.edit().putBoolean("web", isChecked).apply();
                break;
            case R.id.image:
                Reddit.image = isChecked;
                SettingValues.prefs.edit().putBoolean("image", isChecked).apply();
                break;
            case R.id.gif:
                Reddit.gif = isChecked;
                SettingValues.prefs.edit().putBoolean("gif", isChecked).apply();
                break;
            case R.id.album:
                Reddit.album = isChecked;
                SettingValues.prefs.edit().putBoolean("album", isChecked).apply();
                break;
            case R.id.video:
                Reddit.video = isChecked;
                SettingValues.prefs.edit().putBoolean("video", isChecked).apply();
                break;
        }

    }

}