package me.ccrama.redditslide.util;

import android.app.Activity;

import java.io.File;

import me.ccrama.redditslide.ImageLoaderUtils;

/**
 * Created by carlo_000 on 3/6/2016.
 * <p/>
 * Adapted from http://stackoverflow.com/a/10069679/3697225
 */
public class CacheUtil {

    private static final long MAX_SIZE = 75000000L; // 75MB

    private CacheUtil() {
    }

    public static void makeRoom(Activity context, int length) {

        File cacheDir = ImageLoaderUtils.getCacheDirectoryGif(context);

        cleanDir(cacheDir);


    }


    private static void cleanDir(File dir) {

        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.lastModified() + 1000 < System.currentTimeMillis()) { //more than a day old
                file.delete();
            }
        }
    }

    private static long getDirSize(File dir) {

        long size = 0;
        if (!dir.exists()) {
            dir.mkdir();
        }
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            }
        }

        return size;
    }
}