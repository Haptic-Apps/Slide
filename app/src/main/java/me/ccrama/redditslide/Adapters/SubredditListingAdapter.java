package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by ccrama on 8/17/2015.
 */
public class SubredditListingAdapter extends ArrayAdapter<String> {


    private final ArrayList<String> fitems;


    public SubredditListingAdapter(Context context, ArrayList<String> objects) {
        super(context, 0, objects);
        List<String> objects1 = new ArrayList<>(objects);
        fitems = new ArrayList<>(objects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.subforsublist, parent, false);
        }
        final TextView t =
                convertView.findViewById(R.id.name);
        t.setText(fitems.get(position));

        convertView.findViewById(R.id.color).setBackgroundResource(R.drawable.circle);
        convertView.findViewById(R.id.color).getBackground().setColorFilter(new PorterDuffColorFilter(
                Palette.getColor(fitems.get(position)), PorterDuff.Mode.MULTIPLY));

        return convertView;
    }
}
