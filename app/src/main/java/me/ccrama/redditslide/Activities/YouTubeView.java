package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.sephiroth.android.library.tooltip.Tooltip;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SecretConstants;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class YouTubeView extends BaseYoutubePlayer implements
        YouTubePlayer.OnInitializedListener {

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    // YouTube player view
    private YouTubePlayerView youTubeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        Log.v(LogUtil.getTag(), "Using youtube player");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_youtube);

        video = getIntent().getExtras().getString("url", "");

        Log.v(LogUtil.getTag(), video);
        video = extractYTId(video);
        Log.v(LogUtil.getTag(), video);

        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);


        // Initializing video player with developer key
        youTubeView.initialize(SecretConstants.getApiKey(this), this);
        if (!Reddit.appRestart.contains("tutorialYT")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Tooltip.make(YouTubeView.this,
                            new Tooltip.Builder(106)
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
            }, 250);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!Reddit.appRestart.contains("tutorialYT")) {
            Reddit.appRestart.edit().putBoolean("tutorialYT", true).apply();
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        }
    }

    String video;

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {

            // loadVideo() will auto play video
            // Use cueVideo() method, if you don't want to play it automatically
            player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
            player.loadVideo(video);
            player.play();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(SecretConstants.getApiKey(this), this);
        }
    }

    private YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.youtube_view);
    }

    //Adapted http://stackoverflow.com/questions/24048308/how-to-get-the-video-id-from-a-youtube-url-with-regex-in-java
    public static String extractYTId(String ytUrl) {
        String vId = null;
        Pattern pattern = Pattern.compile(
                "youtu(?:\\.be/|be\\.com/(?:watch\\?v=|v/|embed/|user/(?:[\\w#]+/)+))([^&#?\\n]+)",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(ytUrl);
        matcher.find();
        vId = matcher.group(1);

        return vId;
    }


}