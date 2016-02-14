package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.managers.WikiManager;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Fragments.WikiPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Wiki extends BaseActivityAnim {

    public static final String EXTRA_SUBREDDIT = "subreddit";

    private TabLayout tabs;
    private ViewPager pager;
    private String subreddit;
    private Wiki.OverviewPagerAdapter adapter;
    private List<String> pages;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);

        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT, "");
        applyColorTheme(subreddit);
        setContentView(R.layout.activity_slidetabs);
        setupSubredditAppBar(R.id.toolbar, "/r/" + subreddit + " wiki", true, subreddit);

        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        pager = (ViewPager) findViewById(R.id.content_view);
        findViewById(R.id.header).setBackgroundColor(Palette.getColor(subreddit));

        new AsyncGetWiki().execute();
    }
    public WikiManager wiki;
    private class AsyncGetWiki extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

           wiki = new WikiManager(Authentication.reddit);
            try {
                pages = wiki.getPages(subreddit);
                adapter = new OverviewPagerAdapter(getSupportFragmentManager());

                ArrayList<String> toRemove = new ArrayList<>();
                for (String s : pages) {
                    if (s.startsWith("config")) {
                        toRemove.add(s);
                    }
                }
                pages.removeAll(toRemove);
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(Wiki.this)
                                .setTitle(R.string.wiki_err)
                                .setMessage(R.string.wiki_err_msg)
                                .setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                finish();
                            }
                        }).show();
                    }
                });
            }
            return null;
        }

        @Override
        public void onPostExecute(Void d) {
            if (adapter != null) {
                pager.setAdapter(adapter);
                tabs.setupWithViewPager(pager);
            } else {
                new AlertDialogWrapper.Builder(Wiki.this)
                        .setTitle(R.string.wiki_err)
                        .setMessage(R.string.wiki_err_msg)
                        .setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                }).show();
            }
        }
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment f = new WikiPage();
            Bundle args = new Bundle();

            args.putString("title", pages.get(i));
            args.putString("subreddit", subreddit);

            f.setArguments(args);

            return f;
        }


        @Override
        public int getCount() {
            if (pages == null) {
                return 1;
            } else {
                return pages.size();
            }
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return pages.get(position);
        }
    }

}
