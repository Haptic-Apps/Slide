package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/1/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;


public class AlbumView extends RecyclerView.Adapter<AlbumView.ViewHolder> {
    ArrayList<JsonElement> users;

    Context main;
    ArrayList<String> list;

    public AlbumView(Context context, ArrayList<JsonElement> users) {
        main = context;
        this.users = users;
        list = new ArrayList<>();
        for (final JsonElement elem : users) {
            list.add(elem.getAsJsonObject().getAsJsonObject("links").get("original").getAsString());
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView text;
        TextView body;
        ImageView image;
        public ViewHolder(View itemView)
        {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.imagetitle);
            body = (TextView) itemView.findViewById(R.id.imageCaption);
            image = (ImageView) itemView.findViewById(R.id.image);


        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        final JsonElement user = users.get(position) ;

        final String url = list.get(position);

        ((Reddit)main.getApplicationContext()).getImageLoader().displayImage(url, holder.image);
        {
            holder.text.setText(user.getAsJsonObject().getAsJsonObject("image").get("title").getAsString());
            if(holder.text.getText().toString().isEmpty()){
                holder.text.setVisibility(View.GONE);
            }
        }

        {
            holder.body.setText(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString());
            if(holder.body.getText().toString().isEmpty()){
                holder.body.setVisibility(View.GONE);
            }
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Reddit.image) {
                    Intent myIntent = new Intent(main, FullscreenImage.class);
                    myIntent.putExtra("url", url);
                    main.startActivity(myIntent);
                } else {
                    Reddit.defaultShare(url, main);
                }
            }
        });


    }


    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

}