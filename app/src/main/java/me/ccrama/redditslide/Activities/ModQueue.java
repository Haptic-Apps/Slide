package me.ccrama.redditslide.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.InboxPage;
import me.ccrama.redditslide.Fragments.ModPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ModQueue extends BaseActivity {

    public ModQueue.OverviewPagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);

        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(""), true);

        setContentView(R.layout.activity_inbox);


        TabLayout tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_mod);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ViewPager pager = (ViewPager) findViewById(R.id.contentView);
        findViewById(R.id.header).setBackgroundColor(Pallete.getDefaultColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
        }
        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));

        tabs.setupWithViewPager(pager);

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

            if (i == 0) {
                Fragment f = new InboxPage();
                Bundle args = new Bundle();
                args.putString("id", "moderator");

                f.setArguments(args);

                return f;
            } else {
                Fragment f = new ModPage();
                Bundle args = new Bundle();

                args.putString("id", "modqueue");
                args.putString("subreddit", SubredditStorage.modOf.get(i - 1));

                f.setArguments(args);

                return f;
            }


        }


        @Override
        public int getCount() {
            return SubredditStorage.modOf.size() + 1;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.mod_mail);
            } else {
                return SubredditStorage.modOf.get(position - 1);
            }
        }
    }

}
