package me.ccrama.redditslide;

/**
 * Created by carlo_000 on 10/13/2015.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jakewharton.processphoenix.ProcessPhoenix;

public class CheckInstall extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getDataString();
        if (packageName.contains("me.ccrama.") && !packageName.startsWith("me.ccrama.redditslide")) {
            ProcessPhoenix.triggerRebirth(context.getApplicationContext());
        }
    }
}