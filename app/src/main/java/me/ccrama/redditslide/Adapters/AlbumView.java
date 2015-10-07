package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/1/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.R;


public class AlbumView extends ArrayAdapter<JsonElement> {
    ArrayList<JsonElement> users;

    Context main;
    ArrayList<String> list;

    public AlbumView(Context context, ArrayList<JsonElement> users) {
        super(context, 0, users);
        main = context;
        this.users = users;
        list = new ArrayList<>();
        for (final JsonElement elem : users) {
            list.add(elem.getAsJsonObject().getAsJsonObject("links").get("original").getAsString());
        }
    }



    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final JsonElement user = getItem(position);

        final String url = list.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.album_image, parent, false);

        }
        final ImageView iv = (ImageView) convertView.findViewById(R.id.image);

        if(user.getAsJsonObject().has("height") && !user.getAsJsonObject().getAsJsonObject("height").isJsonNull()) {

            int height = user.getAsJsonObject().getAsJsonObject("height").getAsInt();
            iv.setMaxHeight(height);
            iv.setMinimumHeight(height);
        }

        Picasso.with(getContext()).load(url).into(iv);
        {
            TextView tv = (TextView) convertView.findViewById(R.id.imagetitle);
            tv.setText(user.getAsJsonObject().getAsJsonObject("image").get("title").getAsString());
            if(tv.getText().toString().isEmpty()){
                tv.setVisibility(View.GONE);
            }
        }

        {
            TextView tv = (TextView) convertView.findViewById(R.id.imageCaption);
            tv.setText(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString());
            if(tv.getText().toString().isEmpty()){
                tv.setVisibility(View.GONE);
            }
        }


        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getContext(), FullscreenImage.class);
                myIntent.putExtra("url", url);
                getContext().startActivity(myIntent);
            }
        });


        return convertView;
    }


    @Override
    public int getCount() {
        return users == null ? 0 : users.size();
    }

}