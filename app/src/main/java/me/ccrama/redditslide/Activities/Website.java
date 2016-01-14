package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Palette;

public class Website extends BaseActivityAnim {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_website, menu);

        //   if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        //   else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.refresh:
                v.reload();
                return true;
            case R.id.back:
                v.goBack();
                return true;
            case R.id.chrome:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(v.getUrl()));
                startActivity(Intent.createChooser(browserIntent, getDomainName(v.getUrl())));
                return true;
            case R.id.share:
                Reddit.defaultShareText(v.getUrl(), Website.this);

                return true;

        }
        return false;
    }
    WebView v;
    String url;
    int subreddit;
    MyWebViewClient client;
    ProgressBar p;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme("");
        setContentView(R.layout.activity_web);
        url = getIntent().getExtras().getString("url", "");
        subreddit = getIntent().getExtras().getInt("color", Palette.getDefaultColor());

        setupAppBar(R.id.toolbar, "", true, subreddit, R.id.appbar);

        p = (ProgressBar) findViewById(R.id.progress);
        v = (WebView) findViewById(R.id.web);

        client = new MyWebViewClient();
        v.setWebChromeClient(client);
        v.setWebViewClient(new WebViewClient());
        v.getSettings().setJavaScriptEnabled(true);
        v.loadUrl(url);


    }
    public void setValue(int newProgress){
        p.setProgress(newProgress);
        if(newProgress == 100){
            p.setVisibility(View.GONE);
        } else if(p.getVisibility() == View.GONE){
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
            super.onReceivedTitle(view, title);
            if (!title.isEmpty()) {
                getSupportActionBar().setTitle(title);
                if(url.contains("/"))
                getSupportActionBar().setSubtitle(getDomainName(url));
            } else {
                getSupportActionBar().setTitle(getDomainName(url));

            }
        }
    }
    public static String getDomainName(String url) {
        URI uri = null;
        try {
            uri = new URI(url);

        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return url;
    }
}