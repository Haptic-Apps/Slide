package me.ccrama.redditslide.Synccit;

/**
 * Created by carlo_000 on 2/16/2016.
 */

import com.synccit.android.SynccitReadTask;

import java.util.HashSet;

import me.ccrama.redditslide.SettingValues;

public class MySynccitReadTask extends SynccitReadTask {

    private static final String MY_DEV_NAME = "slide_for_reddit";


    public MySynccitReadTask() {
        super(MY_DEV_NAME);
    }

    @Override
    protected void onVisited(HashSet<String> visitedThreadIds) {
        SynccitRead.visitedIds.addAll(visitedThreadIds);


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
