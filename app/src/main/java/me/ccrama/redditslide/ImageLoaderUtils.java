package me.ccrama.redditslide;

import android.content.Context;
import android.os.Environment;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Created by carlo_000 on 10/19/2015.
 */
    /*Adapted from https://github.com/Kennyc1012/Opengur */

class ImageLoaderUtils {

    public static ImageLoader imageLoader;

    private static File getCacheDirectory(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return context.getExternalCacheDir();
        }
        return context.getCacheDir();
    }

    public static void initImageLoader(Context context) {
        long discCacheSize = 1024 * 1024;
        DiskCache discCache;
        File dir = getCacheDirectory(context);
        int threadPoolSize;
        discCacheSize *= 512;
        threadPoolSize = 7;
        if (discCacheSize > 0) {
            try {
                discCache = new LruDiskCache(dir, new Md5FileNameGenerator(), discCacheSize);
            } catch (IOException e) {
                discCache = new UnlimitedDiskCache(dir);
            }
        } else {
            discCache = new UnlimitedDiskCache(dir);
        }

        final int memory = (int) (Runtime.getRuntime().maxMemory() / 8);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(threadPoolSize)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(discCache)

                .defaultDisplayImageOptions(new DisplayImageOptions.Builder()
                        .resetViewBeforeLoading(true)
                        .cacheOnDisk(true)
                        .cacheInMemory(false)
                        .build())
                .memoryCacheSize(memory)
                .build();

        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }

        ImageLoader.getInstance().init(config);

    }

}
