package me.ccrama.redditslide.Activities;

import android.os.Bundle;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.OpenImgurLink;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Imgur extends FullScreenActivity {



    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();

        super.onCreate(savedInstanceState);

        String url = getIntent().getExtras().getString("url");

        OpenImgurLink.openImgurLink(this, url);


        setContentView(R.layout.activity_imgur);


    }


}