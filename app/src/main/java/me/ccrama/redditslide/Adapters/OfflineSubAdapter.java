package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by carlo_000 on 4/18/2016.
 */
public class OfflineSubAdapter extends ArrayAdapter<String> {

    private Context mContext;
    public OfflineSubAdapter(Context context, int textViewResourceId,
                            String[] objects) {
        super(context, textViewResourceId, objects);

        this.titles = objects;
        mContext = context;
    }

    String[] titles;
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(android.R.layout.simple_list_item_1, null);
            holder = new ViewHolder();
            holder.txt01 = convertView.findViewById(android.R.id.text1);
            holder.txt01.setTextColor(Color.WHITE);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txt01.setText(titles[position]);

        return convertView;
    }

    static class ViewHolder {
        TextView txt01;
    }



}
