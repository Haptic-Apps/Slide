package me.ccrama.redditslide.util;

import android.app.Activity;

import java.io.File;

import me.ccrama.redditslide.ImageLoaderUtils;

/**
 * Created by carlo_000 on 3/6/2016.
 *
 * Adapted from http://stackoverflow.com/a/10069679/3697225
 */
public class CacheUtil {

    private static final long MAX_SIZE = 75000000L; // 75MB

    private CacheUtil() {
    }

    public static void makeRoom(Activity context, int length) {

        File cacheDir = ImageLoaderUtils.getCacheDirectoryGif(context);
        long size = getDirSize(cacheDir);
        long newSize = length + size;

        LogUtil.v("Comparing " + newSize + " to " + MAX_SIZE);
        if (newSize > MAX_SIZE) {
            cleanDir(cacheDir, newSize - MAX_SIZE);
        }

    }


    private static void cleanDir(File dir, long bytes) {

        long bytesDeleted = 0;
        File[] files = dir.listFiles();

        for (File file : files) {
            if(file.getName().contains(".")) {
                bytesDeleted += file.length();
                file.delete();

                if (bytesDeleted >= bytes) {
                    break;
                }
            }
        }
    }

    private static long getDirSize(File dir) {

        long size = 0;
        if(!dir.exists()){
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