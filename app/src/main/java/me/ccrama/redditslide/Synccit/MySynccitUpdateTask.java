package me.ccrama.redditslide.Synccit;

import me.ccrama.redditslide.SettingValues;

/**
 * Created by carlo_000 on 2/16/2016.
 */
public class MySynccitUpdateTask extends SynccitUpdateTask {

    private static final String MY_DEV_NAME = "slide_for_reddit";

    public MySynccitUpdateTask() {
        super(MY_DEV_NAME);
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