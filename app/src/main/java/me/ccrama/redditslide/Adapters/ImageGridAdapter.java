package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.GalleryImage;
import me.ccrama.redditslide.ImgurAlbum.Image;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Tumblr.Photo;

/**
 * Created by carlo_000 on 3/20/2016.
 */
public class ImageGridAdapter extends android.widget.BaseAdapter {
    private Context      mContext;
    private List<String> jsons;
    public static final DisplayImageOptions options =
            new DisplayImageOptions.Builder().cacheOnDisk(true)
                    .resetViewBeforeLoading(true)
                    .bitmapConfig(SettingValues.highColorspaceImages ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .cacheInMemory(false)
                    .displayer(new FadeInBitmapDisplayer(250))
                    .build();

    public ImageGridAdapter(Context c, List<Image> imgurAlbum) {
        mContext = c;
        jsons = new ArrayList<>();
        for (Image i : imgurAlbum) {
            jsons.add(i.getThumbnailUrl());
        }
    }

    public ImageGridAdapter(Context c, boolean gallery, List<GalleryImage> redditGallery) {
        mContext = c;
        jsons = new ArrayList<>();
        for (GalleryImage i : redditGallery) {
            jsons.add(i.url);
        }
    }

    public ImageGridAdapter(Context c, List<Photo> tumblrAlbum, boolean tumblr) {
        mContext = c;
        jsons = new ArrayList<>();
        for (Photo i : tumblrAlbum) {
            jsons.add((i.getAltSizes() != null && !i.getAltSizes().isEmpty()) ? i.getAltSizes()
                    .get(0)
                    .getUrl() : i.getOriginalSize().getUrl());
        }
    }

    public int getCount() {
        return jsons.size();
    }

    public String getItem(int position) {
        return jsons.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        GridView grid = (GridView) parent;
        int size = grid.getColumnWidth();
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setLayoutParams(new GridView.LayoutParams(size, size));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        ((Reddit) mContext.getApplicationContext()).getImageLoader()
                .displayImage(getItem(position), imageView, options);
        return imageView;
    }
}