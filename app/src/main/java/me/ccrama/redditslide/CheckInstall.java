package me.ccrama.redditslide;

/**
 * Created by carlo_000 on 10/13/2015.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.ccrama.redditslide.util.LogUtil;

public class CheckInstall extends BroadcastReceiver {

    private Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        LogUtil.v("Package installed!");
    }
}