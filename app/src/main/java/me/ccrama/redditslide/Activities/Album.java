package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.ToolbarColorizeHelper;
import me.ccrama.redditslide.util.AlbumUtils;


/**
 * Created by ccrama on 3/5/2015.
 * <p/>
 * This class is responsible for accessing the Imgur api to get the album json data
 * from a URL or Imgur hash. It extends FullScreenActivity and supports swipe from anywhere.
 */
public class Album extends FullScreenActivity {
    public static final String EXTRA_URL = "url";
    boolean gallery = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        setContentView(R.layout.album);

        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setTitle(R.string.type_album);
        ToolbarColorizeHelper.colorizeToolbar(b, Color.WHITE, this);
        setSupportActionBar(b);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        final String url = getIntent().getExtras().getString(EXTRA_URL, "");

        new AlbumUtils.LoadAlbumFromUrl(url, this, true, true, getSupportActionBar(), (RecyclerView) findViewById(R.id.images), 0).execute(url);

        findViewById(R.id.slider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingValues.albumSwipe = true;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ALBUM_SWIPE, true).apply();
                Intent i = new Intent(Album.this, AlbumPager.class);
                i.putExtra("url", url);
                startActivity(i);
                finish();
            }
        });



        if (!Reddit.appRestart.contains("tutorialSwipe")) {
            startActivityForResult(new Intent(this, SwipeTutorial.class), 3);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            Reddit.appRestart.edit().putBoolean("tutorialSwipe", true).apply();

        }
    }
}