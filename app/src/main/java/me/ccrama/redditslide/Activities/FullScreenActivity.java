package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.WindowManager;

import me.ccrama.redditslide.Reddit;

/**
 * Created by tomer aka rosenpin on 11/27/15.
 */
public class FullScreenActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Reddit.fullscreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}
