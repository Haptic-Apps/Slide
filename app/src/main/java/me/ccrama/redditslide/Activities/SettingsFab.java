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
public class SettingsFab extends BaseActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_fab);
        setupAppBar(R.id.toolbar, R.string.settings_title_fab, true, true);

        final SwitchCompat fabType = (SwitchCompat) findViewById(R.id.fab_type);
        fabType.setChecked(Reddit.fabType == R.integer.FAB_DISMISS);
        fabType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Reddit.fabType = R.integer.FAB_DISMISS;
                    SettingValues.prefs.edit().putInt("FabType", R.integer.FAB_DISMISS).apply();
                } else {
                    Reddit.fabType = R.integer.FAB_POST;
                    SettingValues.prefs.edit().putInt("FabType", R.integer.FAB_POST).apply();
                }
            }
        });
        SwitchCompat fab = (SwitchCompat) findViewById(R.id.fab_visible);
        fab.setChecked(Reddit.fab);
        fabType.setEnabled(Reddit.fab);
        fab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fabType.setEnabled(isChecked);
                Reddit.fab = isChecked;
                SettingValues.prefs.edit().putBoolean("Fab", isChecked).apply();
            }
        });


    }



}