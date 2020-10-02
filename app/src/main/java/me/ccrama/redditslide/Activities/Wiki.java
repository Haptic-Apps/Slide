package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.android.material.tabs.TabLayout;

import net.dean.jraw.managers.WikiManager;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.WikiPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.ToggleSwipeViewPager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Wiki extends BaseActivityAnim implements WikiPage.WikiPageListener {

    public static final String EXTRA_SUBREDDIT = "subreddit";
    public static final String EXTRA_PAGE = "page";

    private TabLayout tabs;
    private ToggleSwipeViewPager pager;
    private String subreddit;
    private Wiki.OverviewPagerAdapter adapter;
    private List<String> pages;
    private String page;
    private static String globalCustomCss;
    private static String globalCustomJavaScript;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);

        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT, "");

        setShareUrl("https://reddit.com/r/" + subreddit + "/wiki/");

        applyColorTheme(subreddit);
        createCustomCss();
        createCustomJavaScript();
        setContentView(R.layout.activity_slidetabs);
        setupSubredditAppBar(R.id.toolbar, "/r/" + subreddit + " wiki", true, subreddit);

        if(getIntent().hasExtra(EXTRA_PAGE)) {
            page = getIntent().getExtras().getString(EXTRA_PAGE);
            LogUtil.v("Page is " + page);
        } else {
            page = "index";
        }
        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setSelectedTabIndicatorColor(new ColorPreferences(Wiki.this).getColor("no sub"));

        pager = (ToggleSwipeViewPager) findViewById(R.id.content_view);
        findViewById(R.id.header).setBackgroundColor(Palette.getColor(subreddit));

        new AsyncGetWiki().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void createCustomCss() {
        StringBuilder customCssBuilder = new StringBuilder();
        customCssBuilder.append("<style>");
        TypedArray ta = obtainStyledAttributes(
                new int[]{R.attr.activity_background, R.attr.fontColor, R.attr.colorAccent});
        customCssBuilder.append("html { ")
                .append("background: ").append(getHexFromColorInt(ta.getColor(0, Color.WHITE))).append(";")
                .append("color: ").append(getHexFromColorInt(ta.getColor(1, Color.BLACK))).append(";")
                .append("; }");
        customCssBuilder.append("a { ")
                .append("color: ").append(getHexFromColorInt(ta.getColor(2, Color.BLUE))).append(";")
                .append("; }");
        ta.recycle();
        customCssBuilder.append("table, code { display: block; overflow-x: scroll; }");
        customCssBuilder.append("table { white-space: nowrap; }");
        customCssBuilder.append("</style>");
        globalCustomCss = customCssBuilder.toString();
    }

    private void createCustomJavaScript() {
        globalCustomJavaScript = "<script type=\"text/javascript\">" +
                "window.addEventListener('touchstart', function onSlideUserTouch(e) {" +
                "var element = e.target;" +
                "while(element) {" +
                "if(element.tagName && (element.tagName.toLowerCase() === 'table' || element.tagName.toLowerCase() === 'code')) {" +
                "Slide.overflowTouched();" +
                "return;" +
                "} else {" +
                "element = element.parentNode;" +
                "}}}, false)" +
                "</script>";
    }

    private static String getHexFromColorInt(@ColorInt int colorInt) {
        return String.format("#%06X", (0xFFFFFF & colorInt));
    }

    public static String getGlobalCustomCss() {
        return globalCustomCss;
    }

    public static String getGlobalCustomJavaScript() {
        return globalCustomJavaScript;
    }

    public WikiManager wiki;

    @Override
    public void embeddedWikiLinkClicked(String wikiPageTitle) {
        if (pages.contains(wikiPageTitle)) {
            pager.setCurrentItem(pages.indexOf(wikiPageTitle));
        } else {
            new AlertDialogWrapper.Builder(this)
                    .setTitle(R.string.page_not_found)
                    .setMessage(R.string.page_does_not_exist)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
    }

    @Override
    public void overflowTouched() {
        pager.disableSwipingUntilRelease();
    }

    private class AsyncGetWiki extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

           wiki = new WikiManager(Authentication.reddit);
            try {
                pages = wiki.getPages(subreddit);

                List<String> toRemove = new ArrayList<>();
                for (String s : pages) {
                    if (s.startsWith("config")) {
                        toRemove.add(s);
                    }
                }
                pages.removeAll(toRemove);


                adapter = new OverviewPagerAdapter(getSupportFragmentManager());


            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
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
                        } catch(Exception ignored){

                        }
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
                if(pages.contains(page)){
                    pager.setCurrentItem(pages.indexOf(page));
                }
            } else {
                try {
                    new AlertDialogWrapper.Builder(Wiki.this).setTitle(R.string.wiki_err)
                            .setMessage(R.string.wiki_err_msg)
                            .setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    finish();
                                }
                            })
                            .show();
                } catch(Exception e){

                }
            }
        }
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int i) {
            WikiPage f = new WikiPage();
            f.setListener(Wiki.this);
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
