package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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
public class SettingsHandling extends BaseActivityNoAnim implements
        CompoundButton.OnCheckedChangeListener {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings_handling);
        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle(R.string.settings_link_handling);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            SettingsHandling.this.setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.settings_link_handling),
                    ((BitmapDrawable) ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}