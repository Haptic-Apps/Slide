package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/1/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.JsonElement;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.MakeTextviewClickable;


public class AlbumView extends RecyclerView.Adapter<AlbumView.ViewHolder> {
    private final ArrayList<JsonElement> users;

    private final Context main;
    private final ArrayList<String> list;

    public AlbumView(Context context, ArrayList<JsonElement> users, boolean gallery) {
        main = context;
        this.users = users;
        list = new ArrayList<>();
        if (gallery) {
            for (final JsonElement elem : users) {
                list.add("https://imgur.com/" + elem.getAsJsonObject().get("hash").getAsString() + ".png");
            }
        } else {
            for (final JsonElement elem : users) {
                list.add(elem.getAsJsonObject().getAsJsonObject("links").get("original").getAsString());
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final JsonElement user = users.get(position);

        final String url = list.get(position);

        ((Reddit) main.getApplicationContext()).getImageLoader().displayImage(url, holder.image);
        holder.body.setVisibility(View.VISIBLE);
        holder.text.setVisibility(View.VISIBLE);
        if (user.getAsJsonObject().has("image")) {
            {
                if (!user.getAsJsonObject().getAsJsonObject("image").get("title").isJsonNull()) {

                    new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().getAsJsonObject("image").get("title").getAsString(), holder.text, (Activity) main, "");
                    if (holder.text.getText().toString().isEmpty()) {
                        holder.text.setVisibility(View.GONE);
                    }

                } else {
                    holder.text.setVisibility(View.GONE);

                }
            }
            {
                if(! user.getAsJsonObject().getAsJsonObject("image").get("caption").isJsonNull()) {
                    holder.body.setText(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString());
                    new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString(), holder.body, (Activity) main, "");

                    if (holder.body.getText().toString().isEmpty()) {
                        holder.body.setVisibility(View.GONE);
                    }
                } else {
                    holder.body.setVisibility(View.GONE);

                }
            }
        } else {
            if(user.getAsJsonObject().has("title")){
                new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().get("title").getAsString(), holder.text, (Activity) main, "");
                if (holder.text.getText().toString().isEmpty()) {
                    holder.text.setVisibility(View.GONE);
                }

            } else {

                holder.text.setVisibility(View.GONE);

            }
            if(user.getAsJsonObject().has("description")){
                new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().get("description").getAsString(), holder.body, (Activity) main, "");
                if (holder.body.getText().toString().isEmpty()) {
                    holder.body.setVisibility(View.GONE);
                }
            } else {
                holder.body.setVisibility(View.GONE);

            }


        }
        View.OnClickListener onGifImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (url.contains("gif")) {
                    if (SettingValues.gif) {
                        Intent myIntent = new Intent(main, GifView.class);
                        myIntent.putExtra(GifView.EXTRA_URL, url);
                        main.startActivity(myIntent);
                    } else {
                        Reddit.defaultShare(url, main);
                    }
                } else {
                    if (SettingValues.image) {
                        Intent myIntent = new Intent(main, FullscreenImage.class);
                        myIntent.putExtra(FullscreenImage.EXTRA_URL, url);
                        main.startActivity(myIntent);
                    } else {
                        Reddit.defaultShare(url, main);
                    }
                }
            }
        };


        if (url.contains("gif")) {
            holder.body.setVisibility(View.VISIBLE);
            holder.body.setSingleLine(false);
            holder.body.setText(holder.text.getText() + main.getString(R.string.submission_tap_gif).toUpperCase()); //got rid of the \n thing, because it didnt parse and it was already a new line so...
            holder.body.setOnClickListener(onGifImageClickListener);
        }

        holder.itemView.setOnClickListener(onGifImageClickListener);


    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final SpoilerRobotoTextView text;
        final SpoilerRobotoTextView body;
        final ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (SpoilerRobotoTextView) itemView.findViewById(R.id.imagetitle);
            body = (SpoilerRobotoTextView) itemView.findViewById(R.id.imageCaption);
            image = (ImageView) itemView.findViewById(R.id.image);


        }
    }

}
