package me.ccrama.redditslide.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ProgressBar;

import it.sephiroth.android.library.tooltip.Tooltip;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
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


    @Override
    public void onResume(){
        super.onResume();
        if(v != null && v.getDuration() > 0){
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

        if (SettingValues.imageViewerSolidBackground) {
            findViewById(R.id.root).setBackgroundColor(ContextCompat.getColor(this, R.color.darkbg));
        }

     v= (MediaVideoView) findViewById(R.id.gif);
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

        new GifUtils.AsyncLoadGif(this, (MediaVideoView) findViewById(R.id.gif), loader, findViewById(R.id.placeholder),findViewById(R.id.gifsave), true).execute(dat);
        if(!Reddit.appRestart.contains("tutorialGIF")){
            Tooltip.make(this,
                    new Tooltip.Builder(104)
                            .text("Drag from the very edge to exit")
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
    }

    @Override
      public void onDestroy(){
        super.onDestroy();
        if(!Reddit.appRestart.contains("tutorialGIF")){
            Reddit.appRestart.edit().putBoolean("tutorialGIF", true).apply();
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