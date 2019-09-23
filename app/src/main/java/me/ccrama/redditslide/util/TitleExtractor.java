package me.ccrama.redditslide.util;

/**
 * Created by ccrama on 4/10/2016.
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class TitleExtractor {
    private TitleExtractor() {
    }

    /**
     * @param url the HTML page
     * @return title text (null if document isn't HTML or lacks a title tag)
     * @throws IOException if the request fails
     */
    public static String getPageTitle(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        return doc.title();
    }
}
