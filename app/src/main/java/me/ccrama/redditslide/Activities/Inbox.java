package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.rey.material.widget.Slider;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentGrabber;
import me.ccrama.redditslide.Fragments.InboxPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Inbox extends BaseActivity {

    public Inbox.OverviewPagerAdapter adapter;

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
        getSupportActionBar().setTitle(R.string.title_inbox);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ViewPager pager = (ViewPager) findViewById(R.id.content_view);
        findViewById(R.id.header).setBackgroundColor(Pallete.getDefaultColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
        }
        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));

        tabs.setupWithViewPager(pager);

        findViewById(R.id.notifs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Inbox.this);
                final Slider landscape = (Slider) dialoglayout.findViewById(R.id.landscape);

                final CheckBox checkBox = (CheckBox) dialoglayout.findViewById(R.id.load);
                if (Reddit.notificationTime == -1) {
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                    landscape.setValue(Reddit.notificationTime / 15, false);
                    checkBox.setText(getString(R.string.settings_notification,
                            TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, getBaseContext())));

                }
                landscape.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                    @Override
                    public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                        checkBox.setText(getString(R.string.settings_notification,
                                TimeUtils.getTimeInHoursAndMins(i1 * 15, getBaseContext())));
                    }
                });
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!isChecked) {
                            Reddit.notificationTime = -1;
                            Reddit.seen.edit().putInt("notificationOverride", -1).apply();
                            if (Reddit.notifications != null)
                                Reddit.notifications.cancel(getApplication());
                        } else {
                            Reddit.notificationTime = 15;
                            landscape.setValue(1, true);
                        }
                    }
                });
                dialoglayout.findViewById(R.id.title).setBackgroundColor(Pallete.getDefaultColor());
                //todo final Slider portrait = (Slider) dialoglayout.findViewById(R.id.portrait);

                //todo  portrait.setBackgroundColor(Pallete.getDefaultColor());


                final Dialog dialog = builder.setView(dialoglayout).create();
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (checkBox.isChecked()) {
                            Reddit.notificationTime = landscape.getValue() * 15;
                            Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                            Reddit.notifications.cancel(getApplication());
                            Reddit.notifications.start(getApplication());
                        }
                    }
                });
                dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View d) {
                        if (checkBox.isChecked()) {
                            Reddit.notificationTime = landscape.getValue() * 15;
                            Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                            Reddit.notifications.cancel(getApplication());
                            Reddit.notifications.start(getApplication());
                            dialog.dismiss();
                        }
                    }
                });

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
