package me.ccrama.redditslide.activity;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.visual.FontPreferences;
import me.ccrama.redditslide.visual.Pallete;

/**
 * Created by tomer AKA rosenpin aka rosenpin on 11/25/15.
 */
public class SettingsComments extends BaseActivityNoAnim {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings_comments);
        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle(R.string.settings_title_comments);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SwitchCompat username_to_profile = (SwitchCompat) findViewById(R.id.username_to_profile);
        username_to_profile.setChecked(Reddit.click_user_name_to_profile);
        username_to_profile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Reddit.click_user_name_to_profile = isChecked;
                SettingValues.prefs.edit().putBoolean("UsernameClick", isChecked).apply();
            }
        });
        SwitchCompat single = (SwitchCompat) findViewById(R.id.fastscroll);
        single.setChecked(Reddit.fastscroll);
        single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Reddit.fastscroll = isChecked;
                SettingValues.prefs.edit().putBoolean("Fastscroll", isChecked).apply();

            }
        });
        SwitchCompat check = (SwitchCompat) findViewById(R.id.swapGesture);
        check.setChecked(Reddit.swap);
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Reddit.swap = isChecked;
                SettingValues.prefs.edit().putBoolean("Swap", isChecked).apply();

            }
        });
    }
}