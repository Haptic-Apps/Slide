package me.ccrama.redditslide.Activities;

import android.os.Bundle;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.OpenImgurLink;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Imgur extends FullScreenActivity {


    public static final String EXTRA_URL = "url";

    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();

        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(""), true);

        String url = getIntent().getExtras().getString(EXTRA_URL);

        OpenImgurLink.openImgurLink(this, url);


        setContentView(R.layout.activity_imgur);


    }


}