package me.ccrama.redditslide.Activities;

import android.os.Bundle;

import me.ccrama.redditslide.R;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;

public class BaseActivityAnim extends BaseActivity implements SwipeBackActivityBase {
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slideright, 0);
    }
}