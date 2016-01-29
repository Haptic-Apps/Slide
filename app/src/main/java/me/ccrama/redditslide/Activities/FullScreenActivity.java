package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by tomer aka rosenpin on 11/27/15.
 */
public class FullScreenActivity extends BaseActivityAnim {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if (SettingValues.fullscreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setRecentBar(null, Palette.getDefaultColor());
    }
}
