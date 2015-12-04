package me.ccrama.redditslide;

import android.os.Environment;

import com.google.common.io.Files;

import net.dean.jraw.models.Submission;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by carlo_000 on 11/18/2015.
 */
public final class Cache {
    private Cache() {
    }

    public static void writeSubreddit(ArrayList<Submission> objects, String subreddit)  {
        StringBuilder s = new StringBuilder();
        s.append(System.currentTimeMillis() + "<SEPARATOR>");
        for (Submission sub : objects) {
            s.append(sub.getDataNode().toString());
            s.append("<SEPARATOR>");
        }
        String finals = s.toString();
        finals = finals.substring(0, finals.length() - 11);
        Reddit.appRestart.edit().putString(subreddit , finals).commit();
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "saved.txt");
        try {
            f.createNewFile();

            Files.write(finals, f, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static OfflineSubreddit getSubreddit(String s) {
        if (Reddit.appRestart.contains(s)) {
            return new OfflineSubreddit(Reddit.appRestart.getString(s, ""));
        } else {
            return null;
        }
    }


    public static boolean hasSub(String subredditPaginator) {
        return Reddit.appRestart.contains(subredditPaginator);
    }
}