package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import me.ccrama.redditslide.Notifications.CheckForMail;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/28/2015.
 */
public class DeleteFile extends Activity {

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String image;

        if (extras != null) {
            image = getIntent().getStringExtra("image");
            LogUtil.v("Deleting " + image);
            File f = new File(image);
            f.delete();
        }

        finish();
    }
}
