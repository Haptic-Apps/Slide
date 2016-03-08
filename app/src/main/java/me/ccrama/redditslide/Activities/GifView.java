package me.ccrama.redditslide.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;

import java.io.File;

import it.sephiroth.android.library.tooltip.Tooltip;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.util.GifUtils;


/**
 * Created by ccrama on 3/5/2015.
 */
public class GifView extends FullScreenActivity implements FolderChooserDialog.FolderCallback{
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


        final String dat = getIntent().getExtras().getString(EXTRA_URL);


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

        prefs = getSharedPreferences("DATA", 0);

        loader = (ProgressBar) findViewById(R.id.gifprogress);

        new GifUtils.AsyncLoadGif(this, (MediaVideoView) findViewById(R.id.gif), loader, findViewById(R.id.placeholder), findViewById(R.id.gifsave), true, false).execute(dat);
        if (!Reddit.appRestart.contains("tutorialSwipeGif")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Tooltip.make(GifView.this,
                            new Tooltip.Builder(106)
                                    .text("Drag from the very edge to exit")
                                    .closePolicy(new Tooltip.ClosePolicy()
                                            .insidePolicy(true, false)
                                            .outsidePolicy(true, false), 3000)
                                    .maxWidth(500)
                                    .anchor(findViewById(R.id.tutorial), Tooltip.Gravity.RIGHT)
                                    .activateDelay(800)
                                    .showDelay(300)
                                    .withArrow(true)
                                    .withOverlay(true)
                                    .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                    .build()
                    ).show();
                }
            }, 250);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!Reddit.appRestart.contains("tutorialSwipeGif")) {
            Reddit.appRestart.edit().putBoolean("tutorialSwipeGif", true).apply();
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