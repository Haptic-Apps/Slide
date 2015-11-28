package me.ccrama.redditslide.activity;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;

import java.util.ArrayList;

import me.ccrama.redditslide.adapter.SettingsSubAdapter;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.visual.FontPreferences;
import me.ccrama.redditslide.visual.Pallete;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsSubreddit extends BaseActivityNoAnim {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            Intent i = new Intent(SettingsSubreddit.this, SettingsSubreddit.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            overridePendingTransition(0, 0);

            finish();
            overridePendingTransition(0, 0);


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings_subreddit);
        Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle(R.string.title_subreddit_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            SettingsSubreddit.this.setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.title_settings), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));


        }

        ListView l = (ListView) findViewById(R.id.subslist);
        ArrayList<String> done = new ArrayList<>();
        for (String s : SubredditStorage.alphabeticalSubscriptions) {
            if (Pallete.getColor(s) != Pallete.getDefaultColor()) {
                done.add(s);
            } else if (SettingValues.prefs.contains("PRESET" + s)) {
                done.add(s);
            }
        }
        final SettingsSubAdapter adapter = new SettingsSubAdapter(this, done);
        l.setAdapter(adapter);
    }

}