package me.ccrama.redditslide.util;

import android.content.Context;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import me.ccrama.redditslide.R;

/**
 * Created by Carlos on 8/12/2016.
 *
 * Code adapted from http://www.hidroh.com/2016/05/19/hacking-up-ad-blocker-android/
 */
public class AdBlocker {
    private static HashSet<String> domains; //Use hash set for preformance
    public static void init(Context context){
        String allDomains = context.getString(R.string.domains);
        domains = new HashSet<>();
        domains.addAll(Arrays.asList(allDomains.split(",")));
    }

    public static boolean isAd(String url, Context context) {
        if(domains == null)
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
        return domains.contains(host) || firstPeriod + 1 < host.length() && domains.contains(host.substring(firstPeriod + 1));
    }

    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }
}
