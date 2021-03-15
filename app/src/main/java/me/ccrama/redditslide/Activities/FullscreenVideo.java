package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AlertDialog;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class FullscreenVideo extends FullScreenActivity {

    public static final String EXTRA_HTML = "html";
    private WebView v;

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
        final WebSettings settings = v.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setPluginState(WebSettings.PluginState.ON);

        v.setWebChromeClient(new WebChromeClient());
        LogUtil.v(dat);

        if (dat.contains("src=\"")) {
            int start = dat.indexOf("src=\"") + 5;
            dat = dat.substring(start, dat.indexOf("\"", start));
            if(dat.startsWith("//")){
                dat = "https:" + dat;
            }
            LogUtil.v(dat);
            setShareUrl(dat);
            v.loadUrl(dat);
            if ((dat.contains("youtube.co" ) || dat.contains("youtu.be")) && !Reddit.appRestart.contains("showYouTubePopup")) {
                new AlertDialog.Builder(FullscreenVideo.this).setTitle(getString(R.string.load_videos_internally))
                        .setMessage(getString(R.string.load_videos_internally_content))
                        .setPositiveButton(getString(R.string.btn_sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                            "market://details?id=" + getString(
                                                    R.string.youtube_plugin_package))));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=ccrama.me.slideyoutubeplugin")));
                                }
                            }
                        }).setNegativeButton(getString(R.string.btn_no), null)
                        .setNeutralButton(getString(R.string.do_not_show_again), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Reddit.appRestart.edit().putBoolean("showYouTubePopup", false).apply();
                            }
                        }).show();
            }
        } else {
            LogUtil.v(dat);
            v.loadDataWithBaseURL("", dat, "text/html", "utf-8", "");
        }
    }
}
