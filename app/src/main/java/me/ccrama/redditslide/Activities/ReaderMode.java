package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.wuman.jreadability.Readability;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.TitleTextView;
import me.ccrama.redditslide.util.LogUtil;

public class ReaderMode extends BaseActivityAnim {

    public static final String EXTRA_URL = "url";
    public static String html;
    SpoilerRobotoTextView v;
    String                url;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstanceState);
        applyColorTheme("");
        setContentView(R.layout.activity_reader);
        if(getIntent().hasExtra("url")) {
            url = getIntent().getExtras().getString(EXTRA_URL, "");
        }

        v = (SpoilerRobotoTextView) findViewById(R.id.body);

        new AsyncGetArticle().execute();
    }

    public class AsyncGetArticle extends AsyncTask<Void, Void, Void> {
        String title = "";
        String articleText;
        Dialog d;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if(url != null) {
                    URL url = new URL(ReaderMode.this.url);
                    URLConnection con = url.openConnection();
                    Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
                    Matcher m = p.matcher(con.getContentType());
/* If Content-Type doesn't match this pre-conception, choose default and
 * hope for the best. */
                    String charset = m.matches() ? m.group(1) : "ISO-8859-1";
                    Reader r = new InputStreamReader(con.getInputStream(), charset);
                    StringBuilder buf = new StringBuilder();
                    while (true) {
                        int ch = r.read();
                        if (ch < 0) break;
                        buf.append((char) ch);
                    }
                    html = buf.toString();
                    Readability readability = new Readability(html);  // URL
                    readability.init();
                    articleText = readability.outerHtml();
                } else {
                    Readability readability = new Readability(StringEscapeUtils.unescapeJava(html));  // URL
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
            try {
                d.dismiss();
            } catch (Exception e) {

            }
            if (articleText != null) {
                v.setTextHtml(articleText, "nosub");
            } else {
                new AlertDialogWrapper.Builder(ReaderMode.this)
                        .setTitle(R.string.internal_browser_extracting_error)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }

        @Override
        protected void onPreExecute() {
            d = new MaterialDialog.Builder(ReaderMode.this)
                    .title(R.string.internal_browser_extracting_progress)
                    .progress(true, 100)
                    .content(R.string.misc_please_wait)
                    .show();
        }
    }
}
