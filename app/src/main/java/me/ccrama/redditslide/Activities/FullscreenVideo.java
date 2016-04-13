package me.ccrama.redditslide.Activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class FullscreenVideo extends FullScreenActivity {

    public static final String EXTRA_HTML = "html";
    private WebView v;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void finish() {
        super.finish();
        v.loadUrl("about:blank");
        overridePendingTransition(0, R.anim.fade_out);
    }

    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);


        String data = getIntent().getExtras().getString(EXTRA_HTML);
        v = (WebView) findViewById(R.id.webgif);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }

        String dat = data;

        LogUtil.v(dat);
        final WebSettings settings = v.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setPluginState(WebSettings.PluginState.ON);

        v.setWebChromeClient(new WebChromeClient());

        v.loadDataWithBaseURL("", dat, "text/html", "utf-8", "");
    }
}