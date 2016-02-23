package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.util.SubmissionParser;

public class AlbumView extends RecyclerView.Adapter<AlbumView.ViewHolder> {
    private final ArrayList<JsonElement> users;

    private final Context main;
    private final ArrayList<String> list;

    public AlbumView(Context context, ArrayList<JsonElement> users) {
        main = context;
        this.users = users;
        list = new ArrayList<>();
        for (final JsonElement elem : users) {
            list.add(elem.getAsJsonObject().get("link").getAsString());
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
        final boolean isGif = user.getAsJsonObject().get("animated").getAsBoolean();

        ((Reddit) main.getApplicationContext()).getImageLoader().displayImage(url, holder.image);
        holder.body.setVisibility(View.VISIBLE);
        holder.text.setVisibility(View.VISIBLE);

        JsonObject resultData = user.getAsJsonObject();
        if (resultData.has("title") && resultData.get("title") != null && !resultData.get("title").isJsonNull()) {
            List<String> text = SubmissionParser.getBlocks(resultData.getAsJsonObject("image").get("title").getAsString());
            holder.text.setText(Html.fromHtml(text.get(0))); // TODO deadleg determine behaviour. Add overflow

            if (holder.text.getText().toString().isEmpty()) {
                holder.text.setVisibility(View.GONE);
            }
        } else {

            holder.text.setVisibility(View.GONE);


        }



        if (resultData.has("description") && resultData.get("description") != null && !resultData.get("description").isJsonNull()) {
            List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().getAsJsonObject("image").get("description").getAsString());
            holder.body.setText(Html.fromHtml(text.get(0))); // TODO deadleg determine behaviour. Add overflow

            if (holder.body.getText().toString().isEmpty()) {
                holder.body.setVisibility(View.GONE);
            }
        } else {
            holder.body.setVisibility(View.GONE);
        }


        View.OnClickListener onGifImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGif) {
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


        if (isGif) {
            holder.body.setVisibility(View.VISIBLE);
            holder.body.setSingleLine(false);
            holder.body.setText(holder.text.getText() + main.getString(R.string.submission_tap_gif).toUpperCase());
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
