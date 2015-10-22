package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsGeneral extends BaseActivityNoAnim {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings_general);
        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle("General Settings");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            SettingsGeneral.this.setTaskDescription(new ActivityManager.TaskDescription("General Settings", ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }
        {
            CheckBox single = (CheckBox) findViewById(R.id.single);

            single.setChecked(Reddit.single);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.single = isChecked;
                    SettingValues.prefs.edit().putBoolean("Single", isChecked).apply();

                }
            });
        }
        {
            CheckBox check = (CheckBox) findViewById(R.id.swapGesture);

            check.setChecked(Reddit.swap);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.swap = isChecked;
                    SettingValues.prefs.edit().putBoolean("Swap", isChecked).apply();

                }
            });
        }
        {
            CheckBox check = (CheckBox) findViewById(R.id.web);

            check.setChecked(Reddit.web);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.web = isChecked;
                    SettingValues.prefs.edit().putBoolean("web", isChecked).apply();

                }
            });
        }
        {
            CheckBox check = (CheckBox) findViewById(R.id.image);

            check.setChecked(Reddit.image);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.image = isChecked;
                    SettingValues.prefs.edit().putBoolean("image", isChecked).apply();

                }
            });
        }
        {
            CheckBox check = (CheckBox) findViewById(R.id.gif);

            check.setChecked(Reddit.gif);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.gif = isChecked;
                    SettingValues.prefs.edit().putBoolean("gif", isChecked).apply();

                }
            });
        }
        {
            CheckBox check = (CheckBox) findViewById(R.id.album);

            check.setChecked(Reddit.album);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.album = isChecked;
                    SettingValues.prefs.edit().putBoolean("album", isChecked).apply();

                }
            });
        }
        {
            CheckBox check = (CheckBox) findViewById(R.id.video);

            check.setChecked(Reddit.video);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.video = isChecked;
                    SettingValues.prefs.edit().putBoolean("video", isChecked).apply();

                }
            });
        }




    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}