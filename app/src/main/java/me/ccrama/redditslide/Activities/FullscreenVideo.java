package me.ccrama.redditslide.Activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import me.ccrama.redditslide.R;


/**
 * Created by ccrama on 3/5/2015.
 */
public class FullscreenVideo extends BaseActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void finish() {
        super.finish();
        v.loadUrl("about:blank");
        overridePendingTransition(0, R.anim.fade_out);
    }
    private WebView v;
    public void onCreate(Bundle savedInstanceState) {


// set an exit transition
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);


        String data = getIntent().getExtras().getString("html");
        String url;
        if(data.endsWith("/")){
            data = data.substring(0, data.length() -1);
        }
        v = (WebView) findViewById(R.id.webgif);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
        if(!data.contains("cdn.embedly.com")){
            if(data.contains("?v=")){

                url = "https://www.youtube.com/embed/" + data.substring(data.indexOf("?v=") + 3, data.length());
            } else {
                url = "https://www.youtube.com/embed/" + data.substring(data.lastIndexOf("/") + 1, data.length());
            }
        } else {
            Log.v("Slide", data);
            Log.v("Sliide", Html.fromHtml(data).toString());
            String dataurl = Html.fromHtml(data).toString().replace("%2F", "/").replace("%3A", ":").replace("%3F", "?").replace("%3D", "=");

            // String cut = dataurl.substring(dataurl.indexOf("src=\""));
            // Log.v("Slide", "FINAL:" + cut.substring(dataurl.indexOf("\"")));
            Log.v("Slide", dataurl);
            String cut = dataurl.substring(dataurl.indexOf("src=\""));
            String secondCut = cut.substring(7, cut.indexOf("width") - 2);
            Log.v("Slide", secondCut);
            url = "http://" + secondCut;
        }
        WebSettings wbset= v.getSettings();
        wbset.setJavaScriptEnabled(true);
        v.setWebChromeClient(new WebChromeClient());


        v.loadUrl(url);


    }
}