package me.ccrama.redditslide.Synccit;

import me.ccrama.redditslide.util.preference.PreferenceHelper;

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
        return PreferenceHelper.synccitUsername();
    }

    @Override
    protected String getAuth() {
        return PreferenceHelper.synccitAuthcode();
    }

    @Override
    protected String getUserAgent() {
        return "slide_for_reddit";
    }

}
