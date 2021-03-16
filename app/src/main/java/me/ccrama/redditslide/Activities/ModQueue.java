package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import me.ccrama.redditslide.Fragments.InboxPage;
import me.ccrama.redditslide.Fragments.ModLog;
import me.ccrama.redditslide.Fragments.ModPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.ColorPreferences;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ModQueue extends BaseActivityAnim {

    public ModQueuePagerAdapter adapter;

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
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                header.animate()
                        .translationY(0)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180);
            }
        });
        findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
        pager.setAdapter(new ModQueuePagerAdapter(getSupportFragmentManager()));
        tabs.setupWithViewPager(pager);
    }

    private class ModQueuePagerAdapter extends FragmentStatePagerAdapter {

        private Fragment mCurrentFragment;

        ModQueuePagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (mCurrentFragment != object) {
                mCurrentFragment = (Fragment) object;
            }
            super.setPrimaryItem(container, position, object);
        }

        @NonNull
        @Override
        public Fragment getItem(int i) {
            Fragment f;
            Bundle args = new Bundle();
            switch (i) {
                case 0:
                    f = new InboxPage();
                    args.putString("id", "moderator/unread");
                    f.setArguments(args);
                    return f;
                case 1:
                    f = new InboxPage();
                    args.putString("id", "moderator");
                    f.setArguments(args);
                    return f;
                case 2:
                    f = new ModPage();
                    args.putString("id", "modqueue");
                    args.putString("subreddit", "mod");
                    f.setArguments(args);
                    return f;
                case 3:
                    f = new ModPage();
                    args.putString("id", "unmoderated");
                    args.putString("subreddit", "mod");
                    f.setArguments(args);
                    return f;
                case 4:
                    f = new ModLog();
                    f.setArguments(args);
                    return f;
                default:
                    f = new ModPage();
                    args.putString("id", "modqueue");
                    args.putString("subreddit", UserSubscriptions.modOf.get(i - 5));
                    f.setArguments(args);
                    return f;
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
