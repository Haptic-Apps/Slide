package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/28/2015.
 */
public class OpenContent extends Activity {

    public static final String EXTRA_URL = "url";

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Intent intent = getIntent();
        Uri data = intent.getData();
        String url;

        if (data == null) {
            url = getIntent().getExtras().getString(EXTRA_URL, "");
        } else {
            url = data.toString();
        }
        url = url.toLowerCase();

        Log.v(LogUtil.getTag(), url);


        new OpenRedditLink(this, url);
        finish();
    }
}
