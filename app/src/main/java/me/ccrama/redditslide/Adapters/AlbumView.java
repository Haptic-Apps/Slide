package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.util.SubmissionParser;

public class AlbumView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<JsonElement> users;

    private final Activity main;
    private final ArrayList<String> list;

    public boolean paddingBottom;
    public int height;

    public AlbumView(Activity context, ArrayList<JsonElement> users, boolean gallery) {
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

        paddingBottom = main.findViewById(R.id.toolbar) == null;
    }
    public AlbumView(Activity context, ArrayList<JsonElement> users, boolean gallery, int height) {

        this.height = height;
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

        paddingBottom = main.findViewById(R.id.toolbar) == null;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_image, parent, false);
            return new AlbumViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.spacer, parent, false);
            return new SpacerViewHolder(v);
        }
    }

    public double getHeightFromAspectRatio(int imageHeight, int imageWidth, View v) {
        double ratio = (double) imageHeight / (double) imageWidth;
        double width = v.getWidth();
        return (width * ratio);

    }

    private final int SPACER = 6;

    @Override
    public int getItemViewType(int position) {
        if (!paddingBottom && position == 0) {
            return SPACER;
        } else if (paddingBottom && position == getItemCount() - 1) {
            return SPACER;
        } else {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder2, int i) {
        if (holder2 instanceof AlbumViewHolder) {
            int position = paddingBottom ? i : i - 1;

            AlbumViewHolder holder = (AlbumViewHolder) holder2;

            final JsonElement user = users.get(position);
            final String url = list.get(position);
            ((Reddit) main.getApplicationContext()).getImageLoader().displayImage(url, holder.image);
            holder.body.setVisibility(View.VISIBLE);
            holder.text.setVisibility(View.VISIBLE);
            if (!user.isJsonNull() && user.getAsJsonObject().has("height") && holder.image.getLayoutParams() instanceof LinearLayout.LayoutParams && holder.image.getHeight() != 0) {
                holder.image.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) getHeightFromAspectRatio(user.getAsJsonObject().get("height").getAsInt(), user.getAsJsonObject().get("width").getAsInt(), holder.image)));
            }
            if (user.getAsJsonObject().has("image")) {
                {
                    if (!user.getAsJsonObject().getAsJsonObject("image").get("title").isJsonNull()) {
                        List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().getAsJsonObject("image").get("title").getAsString());
                        holder.text.setText(Html.fromHtml(text.get(0))); // TODO deadleg determine behaviour. Add overflow
                        if (holder.text.getText().toString().isEmpty()) {
                            holder.text.setVisibility(View.GONE);
                        }

                    } else {
                        holder.text.setVisibility(View.GONE);

                    }
                }
                {
                    if (!user.getAsJsonObject().getAsJsonObject("image").get("caption").isJsonNull()) {
                        List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString());
                        holder.body.setText(Html.fromHtml(text.get(0))); // TODO deadleg determine behaviour. Add overflow
                        if (holder.body.getText().toString().isEmpty()) {
                            holder.body.setVisibility(View.GONE);
                        }
                    } else {
                        holder.body.setVisibility(View.GONE);

                    }
                }
            } else {
                if (user.getAsJsonObject().has("title")) {
                    List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().get("title").getAsString());
                    holder.text.setText(Html.fromHtml(text.get(0))); // TODO deadleg determine behaviour. Add overflow
                    if (holder.text.getText().toString().isEmpty()) {
                        holder.text.setVisibility(View.GONE);
                    }

                } else {

                    holder.text.setVisibility(View.GONE);

                }
                if (user.getAsJsonObject().has("description")) {
                    List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().get("description").getAsString());
                    holder.body.setText(Html.fromHtml(text.get(0))); // TODO deadleg determine behaviour. Add overflow
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
                            Intent myIntent = new Intent(main, MediaView.class);
                            myIntent.putExtra(MediaView.EXTRA_URL, url);
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
        } else if (holder2 instanceof SpacerViewHolder) {
            holder2.itemView.findViewById(R.id.height).setLayoutParams(new LinearLayout.LayoutParams(holder2.itemView.getWidth(), paddingBottom ? height : main.findViewById(R.id.toolbar).getHeight()));
        }

    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size() + 1;
    }

    public class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        final SpoilerRobotoTextView text;
        final SpoilerRobotoTextView body;
        final ImageView image;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            text = (SpoilerRobotoTextView) itemView.findViewById(R.id.imagetitle);
            body = (SpoilerRobotoTextView) itemView.findViewById(R.id.imageCaption);
            image = (ImageView) itemView.findViewById(R.id.image);


        }
    }

}
