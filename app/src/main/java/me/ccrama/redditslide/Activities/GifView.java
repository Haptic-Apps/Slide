package me.ccrama.redditslide.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ProgressBar;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.util.GifUtils;


/**
 * Created by ccrama on 3/5/2015.
 */
public class GifView extends FullScreenActivity {

    public static final String EXTRA_URL = "url";
    public ProgressBar loader;
    SharedPreferences prefs;


    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gif);

        if (SettingValues.imageViewerSolidBackground) {
            findViewById(R.id.root).setBackgroundColor(ContextCompat.getColor(this, R.color.darkbg));
        }

        final MediaVideoView v = (MediaVideoView) findViewById(R.id.gif);
        v.clearFocus();


        String dat = getIntent().getExtras().getString(EXTRA_URL);

        findViewById(R.id.exitComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GifView.this.finish();
            }
        });




        prefs = getSharedPreferences("DATA", 0);

        loader = (ProgressBar) findViewById(R.id.gifprogress);

        new GifUtils.AsyncLoadGif(this, (MediaVideoView) findViewById(R.id.gif), loader, null,findViewById(R.id.gifsave), true).execute(dat);

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