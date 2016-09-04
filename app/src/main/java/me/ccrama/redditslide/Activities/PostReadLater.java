package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.ReadLaterView;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class PostReadLater extends BaseActivityAnim {

    private ViewPager pager;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);

        applyColorTheme();
        setContentView(R.layout.activity_read_later);
        setupAppBar(R.id.toolbar, "Read later", true, true);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());

        pager = (ViewPager) findViewById(R.id.content_view);
        pager.setAdapter(new ReadLaterAdaptor(getSupportFragmentManager()));
    }

    public class ReadLaterAdaptor extends FragmentStatePagerAdapter {

        public ReadLaterAdaptor(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {
            Fragment f = new ReadLaterView();
            return f;
        }

        @Override
        public int getCount() {
            return 1;
        }
    }
}
