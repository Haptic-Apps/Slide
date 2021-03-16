package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import net.dean.jraw.managers.InboxManager;

import java.util.HashSet;
import java.util.Set;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Autocache.AutoCacheScheduler;
import me.ccrama.redditslide.ContentGrabber;
import me.ccrama.redditslide.Fragments.InboxPage;
import me.ccrama.redditslide.Fragments.SettingsGeneralFragment;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.ColorPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.KeyboardUtil;
import me.ccrama.redditslide.util.LayoutUtils;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Inbox extends BaseActivityAnim {

    public static final String EXTRA_UNREAD = "unread";
    public InboxPagerAdapter adapter;
    private TabLayout                  tabs;
    private ViewPager                  pager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_inbox, menu);

        //   if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        //   else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }

    private boolean changed;
    public  long    last;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.home):
                onBackPressed();
                break;
            case (R.id.notifs):
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                SettingsGeneralFragment.setupNotificationSettings(dialoglayout, Inbox.this);
                break;
            case (R.id.compose):
                Intent i = new Intent(Inbox.this, SendMessage.class);
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
                                adapter = new InboxPagerAdapter(getSupportFragmentManager());
                                pager.setAdapter(adapter);
                                tabs.setupWithViewPager(pager);

                                LayoutUtils.scrollToTabAfterLayout(tabs, CURRENT_TAB);
                                pager.setCurrentItem(CURRENT_TAB);
                            } catch (Exception e) {

                            }
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();
        if (Authentication.reddit == null || !Authentication.reddit.isAuthenticated() || Authentication.me == null) {
            LogUtil.v("Reauthenticating");

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    if (Authentication.reddit == null) {
                        new Authentication(getApplicationContext());
                    }

                    try {
                        Authentication.me = Authentication.reddit.me();
                        Authentication.mod = Authentication.me.isMod();

                        Authentication.authentication.edit()
                                .putBoolean(Reddit.SHARED_PREF_IS_MOD, Authentication.mod)
                                .apply();

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
                        UserSubscriptions.doCachedModSubs();

                        if (Authentication.reddit.isAuthenticated()) {
                            final Set<String> accounts =
                                    Authentication.authentication.getStringSet("accounts", new HashSet<String>());
                            if (accounts.contains(name)) { //convert to new system
                                accounts.remove(name);
                                accounts.add(name + ":" + Authentication.refresh);
                                Authentication.authentication.edit()
                                        .putStringSet("accounts", accounts)
                                        .apply(); //force commit
                            }
                            Authentication.isLoggedIn = true;
                            Reddit.notFirst = true;
                        }

                    } catch (Exception ignored){

                    }
                    return null;
                }
            }.execute();
        }

        super.onCreate(savedInstance);
        last = SettingValues.prefs.getLong("lastInbox",
                System.currentTimeMillis() - (60 * 1000 * 60));
        SettingValues.prefs.edit().putLong("lastInbox", System.currentTimeMillis()).apply();
        applyColorTheme("");
        setContentView(R.layout.activity_inbox);
        setupAppBar(R.id.toolbar, R.string.title_inbox, true, true);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());

        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setSelectedTabIndicatorColor(new ColorPreferences(Inbox.this).getColor("no sub"));

        pager = (ViewPager) findViewById(R.id.content_view);
        findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
        adapter = new InboxPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_UNREAD)) {
            pager.setCurrentItem(1);
        }

        tabs.setupWithViewPager(pager);

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                findViewById(R.id.header).animate()
                        .translationY(0)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180);
                if (position == 3 && findViewById(R.id.read) != null) {
                    findViewById(R.id.read).setVisibility(View.GONE);
                } else if (findViewById(R.id.read) != null) {
                    findViewById(R.id.read).setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private class InboxPagerAdapter extends FragmentStatePagerAdapter {

        InboxPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
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

    @Override
    public void onResume(){
        super.onResume();
        KeyboardUtil.hideKeyboard(this, getWindow().getAttributes().token, 0);
    }
}
