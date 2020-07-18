package me.ccrama.redditslide;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.IOException;

import me.ccrama.redditslide.util.OkHttpImageDownloader;

/**
 * Created by carlo_000 on 10/19/2015.
 */
    /*Adapted from https://github.com/Kennyc1012/Opengur */

public class ImageLoaderUtils {

    public static ImageLoaderUnescape imageLoader;

    private ImageLoaderUtils() {
    }

    public static File getCacheDirectory(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && context.getExternalCacheDir() != null) {
            return context.getExternalCacheDir();
        }
        return context.getCacheDir();
    }
    public static File getCacheDirectoryGif(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && context.getExternalCacheDir() != null) {
            return new File(context.getExternalCacheDir() + File.separator + "gifs");
        }
        return new File(context.getCacheDir() + File.separator + "gifs");
    }
    public static void initImageLoader(Context context) {
        long discCacheSize = 1024 * 1024;
        DiskCache discCache;
        File dir = getCacheDirectory(context);
        int threadPoolSize;
        discCacheSize *= 100;
        threadPoolSize = 7;
        if (discCacheSize > 0) {
            try {
                dir.mkdir();
                discCache = new LruDiskCache(dir, new Md5FileNameGenerator(), discCacheSize);
            } catch (IOException e) {
                discCache = new UnlimitedDiskCache(dir);
            }
        } else {
            discCache = new UnlimitedDiskCache(dir);
        }

        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .cacheInMemory(false)
                .resetViewBeforeLoading(false)
                .displayer(new FadeInBitmapDisplayer(250))
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(threadPoolSize)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(discCache)
                .threadPoolSize(4)
                .imageDownloader(new OkHttpImageDownloader(context))
                .defaultDisplayImageOptions(options)
                .build();

        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }

        imageLoader = ImageLoaderUnescape.getInstance();
        imageLoader.init(config);

    }
    public static DisplayImageOptions options;

}

class ImageLoaderUnescape extends  ImageLoader {

    @Override
    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options,
            ImageSize targetSize, ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        String newUri = StringEscapeUtils.unescapeHtml4(uri);
        super.displayImage(newUri, imageAware, options, targetSize, listener, progressListener);
    }

    private volatile static ImageLoaderUnescape instance;

    public static ImageLoaderUnescape getInstance() {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoaderUnescape();
                }
            }
        }
        return instance;
    }



}