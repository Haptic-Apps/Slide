package me.ccrama.redditslide.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentGrabber;
import me.ccrama.redditslide.Fragments.InboxPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;

/**
 * Created by carlo_000 on 9/17/2015.
 */
public class Inbox extends BaseActivity {

    TabLayout tabs;
    ViewPager pager;
    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);

        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit("", true).getBaseId(), true);

        setContentView(R.layout.activity_slidetabs);


        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Inbox");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pager = (ViewPager) findViewById(R.id.contentView);
        findViewById(R.id.header).setBackgroundColor(Pallete.getDefaultColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
        }
        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));

        tabs.setupWithViewPager(pager);
    }
    public Inbox.OverviewPagerAdapter adapter;

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
