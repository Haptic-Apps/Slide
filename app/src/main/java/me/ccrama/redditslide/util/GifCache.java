package me.ccrama.redditslide.util;

import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import me.ccrama.redditslide.ImageLoaderUtils;

/**
 * Created by carlo_000 on 5/5/2016.
 */
public class GifCache {

    public static final long discCacheSize = 100000000L; //100mb
    public static DiskCache discCache;

    public static void init(Context c) {
        File dir = ImageLoaderUtils.getCacheDirectoryGif(c);
        try {
            dir.mkdir();
            discCache = new LruDiskCache(dir, new Md5FileNameGenerator(), discCacheSize);
            ((LruDiskCache) discCache).setBufferSize(5 * 1024);

        } catch (IOException e) {
            e.printStackTrace();
            discCache = new UnlimitedDiskCache(dir);
        }
    }

    public static File getGif(URL url) {
        return discCache.get(url.toString());
    }

    public static void writeGif(String url, InputStream stream, IoUtils.CopyListener listener) {
        try {
            LogUtil.v(discCache.save(url, stream, listener) + "DONE ");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.closeSilently(stream);
        }
    }

    public static boolean fileExists(URL url) {
        return discCache.get(url.toString()) != null;
    }
}
