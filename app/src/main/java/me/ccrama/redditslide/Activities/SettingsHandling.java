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
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsHandling extends BaseActivityAnim implements
        CompoundButton.OnCheckedChangeListener {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_handling);
        setupAppBar(R.id.toolbar, R.string.settings_link_handling, true, true);

        TextView web = (TextView) findViewById(R.id.browser);

        //todo web stuff
        SwitchCompat image = (SwitchCompat) findViewById(R.id.image);
        SwitchCompat gif = (SwitchCompat) findViewById(R.id.gif);
        SwitchCompat album = (SwitchCompat) findViewById(R.id.album);
        SwitchCompat video = (SwitchCompat) findViewById(R.id.video);


        image.setChecked(SettingValues.image);
        gif.setChecked(SettingValues.gif);
        album.setChecked(SettingValues.album);
        video.setChecked(SettingValues.video);

        image.setOnCheckedChangeListener(this);
        gif.setOnCheckedChangeListener(this);
        album.setOnCheckedChangeListener(this);
        video.setOnCheckedChangeListener(this);

        ((TextView) findViewById(R.id.browser)).setText(SettingValues.web ? (SettingValues.customtabs ? getString(R.string.settings_link_chrome) : getString(R.string.handling_internal_browser)) : getString(R.string.handling_external_browser));

        findViewById(R.id.select_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsHandling.this, v);
                popup.getMenuInflater().inflate(R.menu.browser_type, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.chrome:
                                SettingValues.customtabs = true;
                                SettingValues.web = true;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREFS_WEB, true).apply();
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_CUSTOMTABS, true).apply();
                                break;
                            case R.id.internal:
                                SettingValues.customtabs = false;
                                SettingValues.web = true;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREFS_WEB, true).apply();
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_CUSTOMTABS, false).apply();
                                break;
                            case R.id.external:
                                SettingValues.web = false;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREFS_WEB, false).apply();
                                break;
                        }
                        ((TextView) findViewById(R.id.browser)).setText(SettingValues.web ? (SettingValues.customtabs ? getString(R.string.settings_link_chrome) : getString(R.string.handling_internal_browser)) : getString(R.string.handling_external_browser));

                        return true;
                    }
                });

                popup.show();
            }
        });

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