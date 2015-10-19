package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.managers.WikiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.WikiPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.Pallete;
import me.ccrama.redditslide.Visuals.StyleView;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Wiki extends BaseActivity {

    TabLayout tabs;
    ViewPager pager;
    String subreddit;
    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        subreddit = getIntent().getExtras().getString("subreddit", "");

        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(subreddit, true).getBaseId(), true);

        setContentView(R.layout.activity_slidetabs);

        StyleView.styleActivity(this);


        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("/r/" + subreddit + " wiki");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pager = (ViewPager) findViewById(R.id.contentView);
        findViewById(R.id.header).setBackgroundColor(Pallete.getColor(subreddit));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(subreddit));
        }

        new AsyncGetWiki().execute();
    }
    public Wiki.OverviewPagerAdapter adapter;
    public List<String> pages;

    public class AsyncGetWiki extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            WikiManager wiki = new WikiManager(Authentication.reddit);
            try {
                pages = wiki.getPages(subreddit);
                adapter = new OverviewPagerAdapter(getSupportFragmentManager());

                values = new WeakHashMap<>();
                ArrayList<String> toRemove = new ArrayList<String>();
                for (String s : pages) {
                    values.put(s, wiki.get(subreddit, s).getDataNode().get("content_html").asText());
                    if (values.get(s).isEmpty() || s.startsWith("config")) {
                        toRemove.add(s);
                        values.remove(s);
                    }
                }
                pages.removeAll(toRemove);
            } catch(Exception e){

            }
            return null;
        }
        @Override
        public void onPostExecute(Void d){
            if(adapter != null) {
                pager.setAdapter(adapter);
                tabs.setupWithViewPager(pager);
            } else {
                new AlertDialogWrapper.Builder(Wiki.this).setTitle("No wiki found").setMessage("This subreddit doesn't have a wiki!").setPositiveButton("Close", new DialogInterface.OnClickListener() {
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
    public WeakHashMap<String, String> values;
    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = new WikiPage();
            Bundle args = new Bundle();

            args.putString("text", values.get(pages.get(i)));
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
