package me.ccrama.redditslide.util;

/**
 * Created by ccrama on 4/10/2016.
 */

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ccrama.redditslide.Reddit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TitleExtractor {
    /* the CASE_INSENSITIVE flag accounts for
     * sites that use uppercase title tags.
     * the DOTALL flag accounts for sites that have
     * line feeds in the title text */
    private static final Pattern TITLE_TAG =
            Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private TitleExtractor() {
    }

    /**
     * @param url the HTML page
     * @return title text (null if document isn't HTML or lacks a title tag)
     * @throws IOException
     */
    public static String getPageTitle(String url) throws IOException {
        OkHttpClient client = Reddit.client;
        Request request = new Request.Builder().url(LinkUtil.formatURL(url).toString())
                .addHeader("Accept", "text/html")
                .build();
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) return null;

        Matcher matcher = TITLE_TAG.matcher(response.body().string());
        response.body().close();
        if (matcher.find()) {
            return matcher.group(1).replaceAll("\\s+", " ");
        } else {
            return null;
        }
    }
}