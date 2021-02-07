package me.ccrama.redditslide.util;

import android.content.Context;
import android.os.AsyncTask;
import android.webkit.WebResourceResponse;

import androidx.annotation.WorkerThread;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import okio.BufferedSource;
import okio.Okio;

/**
 * Created by Carlos on 8/12/2016.
 *
 * Code adapted from http://www.hidroh.com/2016/05/19/hacking-up-ad-blocker-android/
 */
public class AdBlocker {
    private static final String      DOMAINS_FILE = "adblocksources.txt";
    private static final Set<String> DOMAINS      = new HashSet<>(); //Use hash set for performance

    public static void init(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    loadFromAssets(context);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    @WorkerThread
    private static void loadFromAssets(Context context) throws IOException {
        try {
            InputStream stream = context.getAssets().open(DOMAINS_FILE);
            BufferedSource buffer = Okio.buffer(Okio.source(stream));
            String line;
            while ((line = buffer.readUtf8Line()) != null) {
                DOMAINS.add(line);
            }
            buffer.close();
            stream.close();
        } catch(Exception ignored){

        }
    }

    public static boolean isAd(String url, Context context) {
        init(context);
        try {
            String host = new URL(url).getHost();
            return host != null && hostMatches(host);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean hostMatches(String host) {
        if(host.isEmpty())
            return false;
        int firstPeriod = host.indexOf(".");
        return DOMAINS.contains(host) || firstPeriod + 1 < host.length() && DOMAINS.contains(
                host.substring(firstPeriod + 1));
    }

    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }
}
