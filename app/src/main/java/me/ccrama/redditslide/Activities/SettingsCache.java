package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
public class SettingsCache extends BaseActivityNoAnim  {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings_cache);
        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle("Cache Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            SettingsCache.this.setTaskDescription(new ActivityManager.TaskDescription("Cache Settings",
                    ((BitmapDrawable) ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }

        SwitchCompat fab = (SwitchCompat) findViewById(R.id.cache);
        final SwitchCompat fabType = (SwitchCompat) findViewById(R.id.cacheDefault);

        fab.setChecked(Reddit.cache);
        fab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Reddit.cache = isChecked;
                fabType.setEnabled(Reddit.cache);
                SettingValues.prefs.edit().putBoolean("cache", isChecked).apply();
            }
        });

        fabType.setChecked(Reddit.cacheDefault);
        fabType.setEnabled(Reddit.cache);
        fabType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Reddit.cacheDefault = isChecked;
                SettingValues.prefs.edit().putBoolean("cacheDefault", isChecked).apply();
            }


        });

        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }



}