package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;

import net.dean.jraw.managers.InboxManager;

import java.util.HashSet;
import java.util.Set;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Autocache.AutoCacheScheduler;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentGrabber;
import me.ccrama.redditslide.Fragments.InboxPage;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Inbox extends BaseActivityAnim {

    public static final String EXTRA_UNREAD = "unread";
    public Inbox.OverviewPagerAdapter adapter;
    private TabLayout tabs;
    private ViewPager pager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inbox, menu);

        //   if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        //   else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }

    private boolean changed;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.home):
                onBackPressed();
                break;
            case (R.id.notifs):
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                SettingsGeneral.setupNotificationSettings(dialoglayout, Inbox.this);
                break;
            case (R.id.compose):
                Intent i = new Intent(Inbox.this, Sendmessage.class);
                startActivity(i);
                break;
            case (R.id.read):
                changed = false;
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            new InboxManager(Authentication.reddit).setAllRead();
                            changed = true;
                        } catch (Exception ignored) {
                            ignored.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        if (changed) { //restart the fragment
                            adapter.notifyDataSetChanged();

                            try {
                                final int CURRENT_TAB = tabs.getSelectedTabPosition();
                                adapter = new OverviewPagerAdapter(getSupportFragmentManager());
                                pager.setAdapter(adapter);
                                tabs.setupWithViewPager(pager);

                                scrollToTabAfterLayout(CURRENT_TAB);
                                pager.setCurrentItem(CURRENT_TAB);
                            } catch(Exception e){
                                
                            }
                        }
                    }
                }.execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to scroll the TabLayout to a specific index
     *
     * @param tabPosition index to scroll to
     */
    private void scrollToTabAfterLayout(final int tabPosition) {
        if (tabs != null) {
            final ViewTreeObserver observer = tabs.getViewTreeObserver();

            if (observer.isAlive()) {
                observer.dispatchOnGlobalLayout(); // In case a previous call is waiting when this call is made
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        tabs.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        tabs.getTabAt(tabPosition).select();
                    }
                });
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);
        applyColorTheme("");
        setContentView(R.layout.activity_inbox);
        setupAppBar(R.id.toolbar, R.string.title_inbox, true, true);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());

        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setSelectedTabIndicatorColor(new ColorPreferences(Inbox.this).getColor("no sub"));

        pager = (ViewPager) findViewById(R.id.content_view);
        findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
        adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_UNREAD)) {
            pager.setCurrentItem(1);
        }

        tabs.setupWithViewPager(pager);

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
                if (position == 3 && findViewById(R.id.read) != null) {
                    findViewById(R.id.read).setVisibility(View.GONE);
                } else if(findViewById(R.id.read) != null){
                    findViewById(R.id.read).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (Authentication.me == null) {
                    if (Authentication.reddit == null) {
                        new Authentication(getApplicationContext());
                    }
                    Authentication.me = Authentication.reddit.me();
                    Authentication.mod = Authentication.me.isMod();
                    Reddit.over18 = Authentication.me.isOver18();

                    Authentication.authentication.edit().putBoolean(Reddit.SHARED_PREF_IS_MOD, Authentication.mod).apply();
                    Authentication.authentication.edit().putBoolean(Reddit.SHARED_PREF_IS_OVER_18, Reddit.over18).apply();

                    if (Reddit.notificationTime != -1) {
                        Reddit.notifications = new NotificationJobScheduler(Inbox.this);
                        Reddit.notifications.start(getApplicationContext());
                    }

                    if (Reddit.cachedData.contains("toCache")) {
                        Reddit.autoCache = new AutoCacheScheduler(Inbox.this);
                        Reddit.autoCache.start(getApplicationContext());
                    }

                    final String name = Authentication.me.getFullName();
                    Authentication.name = name;
                    LogUtil.v("AUTHENTICATED");

                    if (Authentication.reddit.isAuthenticated()) {
                        final Set<String> accounts = Authentication.authentication.getStringSet("accounts", new HashSet<String>());
                        if (accounts.contains(name)) { //convert to new system
                            accounts.remove(name);
                            accounts.add(name + ":" + Authentication.refresh);
                            Authentication.authentication.edit().putStringSet("accounts", accounts).apply(); //force commit
                        }
                        Authentication.isLoggedIn = true;
                        Reddit.notFirst = true;
                    }
                }
                return null;
            }
        }.execute();
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
            return getString(ContentGrabber.InboxValue.values()[position].getDisplayName());
        }
    }
}
