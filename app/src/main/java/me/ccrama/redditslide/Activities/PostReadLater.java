package me.ccrama.redditslide.Activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.ReadLaterView;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class PostReadLater extends BaseActivityAnim {

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);

        applyColorTheme();
        setContentView(R.layout.activity_read_later);
        setupAppBar(R.id.toolbar, "Read later", true, true);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());

        ViewPager pager = (ViewPager) findViewById(R.id.content_view);
        pager.setAdapter(new ReadLaterAdaptor(getSupportFragmentManager()));
    }

    public static class ReadLaterAdaptor extends FragmentStatePagerAdapter {

        public ReadLaterAdaptor(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        }

        @Override
        public Fragment getItem(int i) {
            return new ReadLaterView();
        }

        @Override
        public int getCount() {
            return 1;
        }
    }
}
