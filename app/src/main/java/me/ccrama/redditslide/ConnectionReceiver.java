package me.ccrama.redditslide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by carlo_000 on 10/30/2015.
 */
public class ConnectionReceiver extends BroadcastReceiver {


    public ConnectionReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Reddit.online = isOnline(context);


    }


    boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}