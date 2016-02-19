package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;

import me.ccrama.redditslide.ContentGrabber;
import me.ccrama.redditslide.Fragments.InboxPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Inbox extends BaseActivityAnim {

    public Inbox.OverviewPagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);
        applyColorTheme("");
        setContentView(R.layout.activity_inbox);
        setupAppBar(R.id.toolbar, R.string.title_inbox, true, true);

        TabLayout tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        ViewPager pager = (ViewPager) findViewById(R.id.content_view);
        findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));

        tabs.setupWithViewPager(pager);

        findViewById(R.id.notifs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                SettingsGeneral.setupNotificationSettings(dialoglayout, Inbox.this);
            }
        });
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                findViewById(R.id.header).animate()
                        .translationY(0)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = new InboxPage();
            Bundle args = new Bundle();

            args.putString("id", ContentGrabber.InboxValue.values()[i].getWhereName());

            f.setArguments(args);

            return f;


        }


        @Override
        public int getCount() {
            return ContentGrabber.InboxValue.values().length;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return ContentGrabber.InboxValue.values()[position].getDisplayName();
        }
    }

}
