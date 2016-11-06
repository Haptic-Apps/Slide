package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import me.ccrama.redditslide.Notifications.CheckForMail;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/28/2015.
 */
public class CancelSubNotifs extends Activity {

    public static final String EXTRA_SUB = "sub";

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String subName;

        if (extras != null) {
            subName = extras.getString(EXTRA_SUB, "");
            subName = subName.toLowerCase();

            ArrayList<String> subs = Reddit.stringToArray(
                    Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, "").toLowerCase());
            String toRemove = "";

            for(String s : subs){
                if(s.startsWith(subName + ":")){
                    toRemove = s;
                }
            }
            if(!toRemove.isEmpty()){
                subs.remove(toRemove);
            }
            Reddit.appRestart.edit().putString(CheckForMail.SUBS_TO_GET, Reddit.arrayToString(subs)).apply();
        }

        finish();
    }
}
