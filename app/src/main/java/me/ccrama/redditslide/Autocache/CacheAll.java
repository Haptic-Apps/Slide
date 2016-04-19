package me.ccrama.redditslide.Autocache;

/**
 * Created by carlo_000 on 10/13/2015.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.CommentCacheAsync;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.util.NetworkUtil;

public class CacheAll extends BroadcastReceiver {

    private Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        if (NetworkUtil.isConnected(c)) {
           for(String s : Reddit.cachedData.getString("toCache", "").split(",")){
               try {
                   new CommentCacheAsync(c, s).execute().get();
               } catch (InterruptedException e) {
                   e.printStackTrace();
               } catch (ExecutionException e) {
                   e.printStackTrace();
               }
           }
        }
    }
}