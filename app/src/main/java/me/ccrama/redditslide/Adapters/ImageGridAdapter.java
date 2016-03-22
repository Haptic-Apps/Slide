package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

import me.ccrama.redditslide.Reddit;

/**
 * Created by carlo_000 on 3/20/2016.
 */
public class ImageGridAdapter extends android.widget.BaseAdapter {
    private Context mContext;
    private ArrayList<String> jsons;

    public ImageGridAdapter(Context c, ArrayList<String> jsons) {
        mContext = c;
        this.jsons = jsons;
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
        GridView grid = (GridView)parent;
        int size = grid.getColumnWidth();
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setLayoutParams(new GridView.LayoutParams(size, size));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(getItem(position), imageView);
        return imageView;
    }

}