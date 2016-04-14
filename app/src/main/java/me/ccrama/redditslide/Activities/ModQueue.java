package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.InboxPage;
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
        setupAppBar(R.id.toolbar, R.string.title_mod, true, true);

        TabLayout tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setSelectedTabIndicatorColor(new ColorPreferences(ModQueue.this).getColor("no sub"));

        ViewPager pager = (ViewPager) findViewById(R.id.content_view);
        findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));
        tabs.setupWithViewPager(pager);

        findViewById(R.id.compose).setVisibility(View.GONE);
        findViewById(R.id.notifs).setVisibility(View.GONE);
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

            if (i == 1) {
                Fragment f = new InboxPage();
                Bundle args = new Bundle();
                args.putString("id", "moderator");

                f.setArguments(args);

                return f;
            } else if (i == 0) {
                Fragment f = new InboxPage();
                Bundle args = new Bundle();
                args.putString("id", "moderator/unread");

                f.setArguments(args);

                return f;
            } else if (i == 2) {
                Fragment f = new ModPage();
                Bundle args = new Bundle();

                args.putString("id", "unmoderated");
                args.putString("subreddit", "mod");

                f.setArguments(args);

                return f;
            } else if (i == 3) {
                Fragment f = new ModPage();
                Bundle args = new Bundle();

                args.putString("id", "modqueue");
                args.putString("subreddit", "mod");

                f.setArguments(args);

                return f;
            } else {
                Fragment f = new ModPage();
                Bundle args = new Bundle();

                args.putString("id", "modqueue");
                args.putString("subreddit", UserSubscriptions.modOf.get(i - 4));

                f.setArguments(args);

                return f;
            }


        }


        @Override
        public int getCount() {
            return UserSubscriptions.modOf == null ? 2 : UserSubscriptions.modOf.size() + 4;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.mod_mail_unread);
            } else if (position == 1) {
                return getString(R.string.mod_mail);
            } else if (position == 2) {
                return "/r/mod unmoderated";
            } else if (position == 3) {
                return "modqueue";
            } else {
                return UserSubscriptions.modOf.get(position - 4);
            }
        }
    }

}
