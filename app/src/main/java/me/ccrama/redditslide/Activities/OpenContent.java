package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/28/2015.
 */
public class OpenContent extends Activity {

    public static final String EXTRA_URL = "url";

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.clear);
        Intent intent = getIntent();
        Uri data = intent.getData();
        Bundle extras = intent.getExtras();
        String url;

        if (data != null) {
            url = data.toString();
        } else if (extras != null) {
            url = extras.getString(EXTRA_URL, "");
        } else {
            Toast.makeText(OpenContent.this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        url = url.toLowerCase(Locale.ENGLISH);
        new OpenRedditLink(this, url);
    }

    boolean second = false;

    @Override
    public void onResume(){
        super.onResume();
        if(second){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                finish();
            }
        } else {
            second = true;
        }
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        Uri data = intent.getData();
        Bundle extras = intent.getExtras();
        String url;

        if (data != null) {
            url = data.toString();
        } else if (extras != null) {
            url = extras.getString(EXTRA_URL, "");
        } else {
            Toast.makeText(OpenContent.this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        url = url.toLowerCase(Locale.ENGLISH);

        Log.v(LogUtil.getTag(), url);


        new OpenRedditLink(this, url);
    }
}
