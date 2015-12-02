package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

public class SettingsComments extends BaseActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        applyColorTheme();
        setContentView(R.layout.activity_settings_comments);
        setupAppBar(R.id.toolbar, R.string.settings_title_comments, true);

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