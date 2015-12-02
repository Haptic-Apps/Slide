package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.widget.CompoundButton;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsFab extends BaseActivityNoAnim  {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings_fab);
        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle(R.string.settings_title_fab);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            SettingsFab.this.setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.settings_title_fab),
                    ((BitmapDrawable) ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }
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