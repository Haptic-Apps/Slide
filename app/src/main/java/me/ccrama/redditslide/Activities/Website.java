package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.net.URI;
import java.net.URISyntaxException;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Palette;

public class Website extends BaseActivityAnim {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_COLOR = "color";
    WebView v;
    String url;
    int subredditColor;
    MyWebViewClient client;
    ProgressBar p;

    public static String getDomainName(String url) {
        URI uri;
        try {
            uri = new URI(url);

            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_website, menu);

        //   if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        //   else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }

    @Override
    public void onBackPressed(){
        if(v.canGoBack()){
            v.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;
            case R.id.refresh:
                v.reload();
                return true;
            case R.id.back:
                v.goBack();
                return true;
            case R.id.external:
                Intent inte = new Intent(this, MakeExternal.class);
                inte.putExtra("url", v.getUrl());
                sendBroadcast(inte);
                return true;
            case R.id.chrome:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(v.getUrl()));
                startActivity(Intent.createChooser(browserIntent, getDomainName(v.getUrl())));
                return true;
            case R.id.share:
                Reddit.defaultShareText(v.getTitle(), v.getUrl(), Website.this);

                return true;

        }
        return false;
    }

    //Stop audio
    @Override
    public void finish() {
        super.finish();
        v.loadUrl("about:blank");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstanceState);
        applyColorTheme("");
        setContentView(R.layout.activity_web);
        url = getIntent().getExtras().getString(EXTRA_URL, "");
        subredditColor = getIntent().getExtras().getInt(EXTRA_COLOR, Palette.getDefaultColor());

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setupAppBar(R.id.toolbar, "", true, subredditColor, R.id.appbar);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());

        p = (ProgressBar) findViewById(R.id.progress);
        v = (WebView) findViewById(R.id.web);

        client = new MyWebViewClient();
        v.setWebChromeClient(client);
        v.setWebViewClient(new WebViewClient());
        v.getSettings().setBuiltInZoomControls(true);
        v.getSettings().setDisplayZoomControls(false);
        v.getSettings().setJavaScriptEnabled(true);
        v.getSettings().setUseWideViewPort(true);
        v.getSettings().setLoadWithOverviewMode(true);
        v.loadUrl(url);


    }

    public void setValue(int newProgress) {
        p.setProgress(newProgress);
        if (newProgress == 100) {
            p.setVisibility(View.GONE);
        } else if (p.getVisibility() == View.GONE) {
            p.setVisibility(View.VISIBLE);
        }
    }

    private class MyWebViewClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Website.this.setValue(newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            try {
                super.onReceivedTitle(view, title);
                if (getSupportActionBar() != null) {

                    if (!title.isEmpty()) {
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(title);

                            setShareUrl(url);

                            if (url.contains("/"))
                                getSupportActionBar().setSubtitle(getDomainName(url));
                        }
                    } else {
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(getDomainName(url));
                        }

                    }
                }
            } catch (Exception ignored) {

            }
        }
    }
}
