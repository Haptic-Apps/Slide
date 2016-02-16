package me.ccrama.redditslide.Synccit;

/**
 * Created by carlo_000 on 2/16/2016.
 */

import android.app.Activity;

import com.synccit.android.SynccitReadTask;

import java.util.HashSet;

import me.ccrama.redditslide.SettingValues;

public class MySynccitReadTask extends SynccitReadTask {

    private static final String MY_DEV_NAME = "slide_for_reddit";

    private Activity mActivity;

    public MySynccitReadTask(Activity activity) {
        super(MY_DEV_NAME);
        mActivity = activity;
    }

    @Override
    protected void onVisited(HashSet<String> visitedThreadIds) {
        SynccitRead.setVisited(visitedThreadIds);
    }


    @Override
    protected String getUsername() {
        return SettingValues.synccitName;
    }

    @Override
    protected String getAuth() {
        return SettingValues.synccitAuth;
    }

    @Override
    protected String getUserAgent() {
        return "slide_for_reddit";
    }

}
