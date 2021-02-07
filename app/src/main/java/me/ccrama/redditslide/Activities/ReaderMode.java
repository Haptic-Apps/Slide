package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.wuman.jreadability.Readability;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LinkUtil;

public class ReaderMode extends BaseActivityAnim {
    private       int    mSubredditColor;
    public static String html;
    SpoilerRobotoTextView v;
    private String url;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstanceState);
        applyColorTheme("");
        setContentView(R.layout.activity_reader);

        mSubredditColor =
                getIntent().getExtras().getInt(LinkUtil.EXTRA_COLOR, Palette.getDefaultColor());

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setupAppBar(R.id.toolbar, "", true, mSubredditColor, R.id.appbar);

        if (getIntent().hasExtra("url")) {
            url = getIntent().getExtras().getString(LinkUtil.EXTRA_URL, "");
            ((Toolbar) findViewById(R.id.toolbar)).setTitle(url);

        }

        v = (SpoilerRobotoTextView) findViewById(R.id.body);
        final SwipeRefreshLayout mSwipeRefreshLayout =
                ((SwipeRefreshLayout) ReaderMode.this.findViewById(R.id.refresh));
        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors("", this));

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we estimate the height of the header in dp.
        mSwipeRefreshLayout.setProgressViewOffset(false,
                Constants.SINGLE_HEADER_VIEW_OFFSET - Constants.PTR_OFFSET_TOP,
                Constants.SINGLE_HEADER_VIEW_OFFSET + Constants.PTR_OFFSET_BOTTOM);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        new AsyncGetArticle().execute();

    }

    private void display(String title, String web) {
        v.setTextHtml(web, "nosub");
        if (title != null && !title.isEmpty()) {
            ((Toolbar) findViewById(R.id.toolbar)).setTitle(title);
        } else {
            int index = v.getText().toString().indexOf("\n");
            if (index < 0) {
                index = 0;
            }
            ((Toolbar) findViewById(R.id.toolbar)).setTitle(
                    v.getText().toString().substring(0, index));
        }
    }

    public class AsyncGetArticle extends AsyncTask<Void, Void, Void> {
        String articleText;
        String title;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (url != null) {
                    Connection connection = Jsoup.connect(ReaderMode.this.url);
                    Document document = connection.get();

                    html = document.html();
                    title = document.title();

                    Readability readability = new Readability(document);
                    readability.init();

                    articleText = readability.outerHtml();
                } else {
                    Readability readability =
                            new Readability(StringEscapeUtils.unescapeJava(html));  // URL
                    readability.init();
                    articleText = readability.outerHtml();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ((SwipeRefreshLayout) ReaderMode.this.findViewById(R.id.refresh)).setRefreshing(false);
            ReaderMode.this.findViewById(R.id.refresh).setEnabled(false);

            if (articleText != null) {
                display(title, articleText);
            } else {
                new AlertDialogWrapper.Builder(ReaderMode.this).setTitle(
                        R.string.internal_browser_extracting_error)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNeutralButton("Open in web view",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(ReaderMode.this, Website.class);
                                        i.putExtra(LinkUtil.EXTRA_URL, url);
                                        startActivity(i);
                                        finish();
                                    }
                                })
                        .setCancelable(false)
                        .show();
            }
        }

        @Override
        protected void onPreExecute() {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (url != null) {
            inflater.inflate(R.menu.menu_reader, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;
            case R.id.web:
                LinkUtil.openUrl(url, mSubredditColor, this);
                finish();
                return true;
            case R.id.share:
                Reddit.defaultShareText(
                        ((Toolbar) findViewById(R.id.toolbar)).getTitle().toString(), url,
                        ReaderMode.this);

                return true;

        }
        return false;
    }
}
