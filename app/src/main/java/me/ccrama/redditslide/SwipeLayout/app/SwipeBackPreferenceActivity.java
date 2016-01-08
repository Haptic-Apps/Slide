
package me.ccrama.redditslide.SwipeLayout.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;

import me.ccrama.redditslide.SwipeLayout.SwipeBackLayout;

// * By ikew0ng

public class SwipeBackPreferenceActivity extends PreferenceActivity implements me.ccrama.redditslide.SwipeLayout.app.SwipeBackActivityBase {
    private me.ccrama.redditslide.SwipeLayout.app.SwipeBackActivityHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }
    
    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }
    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        getSwipeBackLayout().scrollToFinishActivity();
    }
}
