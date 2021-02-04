package me.ccrama.redditslide.Activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import me.ccrama.redditslide.Fragments.ReadLaterView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.ColorPreferences;

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
        pager.setAdapter(new ReadLaterPagerAdapter(getSupportFragmentManager()));
    }

    private static class ReadLaterPagerAdapter extends FragmentStatePagerAdapter {

        ReadLaterPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
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
