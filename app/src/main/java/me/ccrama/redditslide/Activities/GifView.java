package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;

import java.io.File;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.StreamableUtil;
import me.ccrama.redditslide.util.VidMeUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class GifView extends FullScreenActivity implements FolderChooserDialog.FolderCallback {
    public static final String EXTRA_STREAMABLE = "streamable";


    @Override
    public void onFolderSelection(FolderChooserDialog dialog, File folder) {
        if (folder != null) {
            Reddit.appRestart.edit().putString("giflocation", folder.getAbsolutePath().toString()).apply();
            Toast.makeText(this, "Gifs will be saved to " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    public static final String EXTRA_URL = "url";
    public ProgressBar loader;
    SharedPreferences prefs;

    @Override
    public void onResume() {
        super.onResume();
        if (v != null && v.getDuration() > 0) {
            v.start();
        }
    }


    /**
     * Called when the activity is first created.
     */
    public MediaVideoView v;

    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gif);

        v = (MediaVideoView) findViewById(R.id.gif);
        v.clearFocus();

        prefs = getSharedPreferences("DATA", 0);

        loader = (ProgressBar) findViewById(R.id.gifprogress);
        final String dat;
        if (getIntent().hasExtra(EXTRA_STREAMABLE)) {
            dat = getIntent().getStringExtra(EXTRA_STREAMABLE);
            if (dat.contains("vid.me")) {
                new VidMeUtil.AsyncLoadVidMe(this, (MediaVideoView) findViewById(R.id.gif), loader, findViewById(R.id.placeholder), findViewById(R.id.gifsave), true, false).execute(dat);
            } else {
                new StreamableUtil.AsyncLoadStreamable(this, (MediaVideoView) findViewById(R.id.gif), loader, findViewById(R.id.placeholder), findViewById(R.id.gifsave), true, false).execute(dat);
            }
        } else {
            dat = getIntent().getExtras().getString(EXTRA_URL);
            new GifUtils.AsyncLoadGif(this, (MediaVideoView) findViewById(R.id.gif), loader, findViewById(R.id.placeholder), findViewById(R.id.gifsave), true, false, true).execute(dat);
        }
        findViewById(R.id.external).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reddit.defaultShare(dat, GifView.this);

            }
        });
        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reddit.defaultShareText(dat, GifView.this);

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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out);
    }


    class Video {
        public String ext = "";
        public String type = "";
        public String url = "";

        Video(String ext, String type, String url) {
            this.ext = ext;
            this.type = type;
            this.url = url;
        }
    }


}