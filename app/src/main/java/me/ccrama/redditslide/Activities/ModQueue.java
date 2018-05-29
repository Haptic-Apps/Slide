package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.InboxPage;
import me.ccrama.redditslide.Fragments.ModLog;
import me.ccrama.redditslide.Fragments.ModPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ModQueue extends BaseActivityAnim {

    public ModQueue.OverviewPagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);

        applyColorTheme("");
        setContentView(R.layout.activity_inbox);
        setupAppBar(R.id.toolbar, R.string.drawer_moderation, true, true);

        TabLayout tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setSelectedTabIndicatorColor(new ColorPreferences(ModQueue.this).getColor("no sub"));
        final View header = findViewById(R.id.header);
        ViewPager pager = (ViewPager) findViewById(R.id.content_view);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                header.animate()
                        .translationY(0)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));
        tabs.setupWithViewPager(pager);
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        private Fragment mCurrentFragment;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = ((Fragment) object);
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int i) {

            switch (i) {
                case 1: {
                    Fragment f = new InboxPage();
                    Bundle args = new Bundle();
                    args.putString("id", "moderator");

                    f.setArguments(args);

                    return f;
                }
                case 0: {
                    Fragment f = new InboxPage();
                    Bundle args = new Bundle();
                    args.putString("id", "moderator/unread");

                    f.setArguments(args);

                    return f;
                }
                case 3: {
                    Fragment f = new ModPage();
                    Bundle args = new Bundle();

                    args.putString("id", "unmoderated");
                    args.putString("subreddit", "mod");

                    f.setArguments(args);

                    return f;
                }
                case 4: {
                    Fragment f = new ModLog();
                    Bundle args = new Bundle();

                    f.setArguments(args);

                    return f;
                }
                case 2: {
                    Fragment f = new ModPage();
                    Bundle args = new Bundle();

                    args.putString("id", "modqueue");
                    args.putString("subreddit", "mod");

                    f.setArguments(args);

                    return f;
                }
                default: {
                    Fragment f = new ModPage();
                    Bundle args = new Bundle();

                    args.putString("id", "modqueue");
                    args.putString("subreddit", UserSubscriptions.modOf.get(i - 5));

                    f.setArguments(args);

                    return f;
                }
            }


        }


        @Override
        public int getCount() {
            return UserSubscriptions.modOf == null ? 2 : UserSubscriptions.modOf.size() + 5;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.mod_mail_unread);
                case 1:
                    return getString(R.string.mod_mail);
                case 2:
                    return getString(R.string.mod_modqueue);
                case 3:
                    return getString(R.string.mod_unmoderated);
                case 4:
                    return getString(R.string.mod_log);
                default:
                    return UserSubscriptions.modOf.get(position - 5);
            }
        }
    }

}
