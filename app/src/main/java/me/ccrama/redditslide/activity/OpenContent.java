package me.ccrama.redditslide.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import me.ccrama.redditslide.OpenRedditLink;

/**
 * Created by ccrama on 9/28/2015.
 */
public class OpenContent extends Activity {
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Intent intent = getIntent();
        Uri data = intent.getData();
        String url;

        if (data == null) {
            url = getIntent().getExtras().getString("url", "");
        } else {
            url = data.toString();
        }
        url = url.toLowerCase();

        Log.v("Slide", url);


        new OpenRedditLink(this, url);
        finish();
    }
}
