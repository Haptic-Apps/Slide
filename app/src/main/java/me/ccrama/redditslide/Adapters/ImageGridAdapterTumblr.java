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

import java.util.List;

import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Tumblr.Photo;

/**
 * Created by carlo_000 on 3/20/2016.
 */
public class ImageGridAdapterTumblr extends android.widget.BaseAdapter {
    private Context     mContext;
    private List<Photo> jsons;
    public static final DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheInMemory(false)
            .displayer(new FadeInBitmapDisplayer(250))
            .build();

    public ImageGridAdapterTumblr(Context c, List<Photo> jsons) {
        mContext = c;
        this.jsons = jsons;
    }

    public int getCount() {
        return jsons.size();
    }

    public String getItem(int position) {
        return jsons.get(position).getAltSizes().get(jsons.get(position).getAltSizes().size() - 1).getUrl();
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

        ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(getItem(position), imageView, options);
        return imageView;
    }
}