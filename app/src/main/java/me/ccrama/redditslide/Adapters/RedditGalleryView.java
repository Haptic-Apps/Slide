package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.devspark.robototextview.RobotoTypefaces;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.GalleryImage;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.util.LinkUtil;

import static me.ccrama.redditslide.Notifications.ImageDownloadNotificationService.EXTRA_SUBMISSION_TITLE;

public class RedditGalleryView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<GalleryImage> images;

    private final Activity main;

    public boolean paddingBottom;
    public int height;
    public String subreddit;
    private final String submissionTitle;

    public RedditGalleryView(final Activity context, final List<GalleryImage> images, int height,
                             String subreddit, String submissionTitle) {
        this.height = height;
        main = context;
        this.images = images;
        this.subreddit = subreddit;
        this.submissionTitle = submissionTitle;

        paddingBottom = main.findViewById(R.id.toolbar) == null;
        if (context.findViewById(R.id.grid) != null)
            context.findViewById(R.id.grid).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater l = context.getLayoutInflater();
                    View body = l.inflate(R.layout.album_grid_dialog, null, false);
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(context);
                    GridView gridview = body.findViewById(R.id.images);
                    gridview.setAdapter(new ImageGridAdapter(context, true, images));


                    b.setView(body);
                    final Dialog d = b.create();
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            if (context instanceof Album) {
                                ((LinearLayoutManager) ((Album) context).album.album.recyclerView.getLayoutManager()).scrollToPositionWithOffset(position + 1, context.findViewById(R.id.toolbar).getHeight());


                            } else {
                                ((LinearLayoutManager) ((RecyclerView) context.findViewById(R.id.images)).getLayoutManager()).scrollToPositionWithOffset(position + 1, context.findViewById(R.id.toolbar).getHeight());
                            }
                            d.dismiss();
                        }
                    });
                    d.show();
                }
            });
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

    public double getHeightFromAspectRatio(int imageHeight, int imageWidth, int viewWidth) {
        double ratio = (double) imageHeight / (double) imageWidth;
        return (viewWidth * ratio);

    }

    @Override
    public int getItemViewType(int position) {
        int SPACER = 6;
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
            final int position = paddingBottom ? i : i - 1;

            AlbumViewHolder holder = (AlbumViewHolder) holder2;

            final GalleryImage image = images.get(position);
            ((Reddit) main.getApplicationContext()).getImageLoader().displayImage(image.url, holder.image, ImageGridAdapter.options);
            holder.body.setVisibility(View.VISIBLE);
            holder.text.setVisibility(View.VISIBLE);
            View imageView = holder.image;
            if (imageView.getWidth() == 0) {
                holder.image.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            } else {
                holder.image.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) getHeightFromAspectRatio(image.height, image.width, imageView.getWidth())));
            }
            {
                int type = new FontPreferences(holder.body.getContext()).getFontTypeComment().getTypeface();
                Typeface typeface;
                if (type >= 0) {
                    typeface = RobotoTypefaces.obtainTypeface(holder.body.getContext(), type);
                } else {
                    typeface = Typeface.DEFAULT;
                }
                holder.body.setTypeface(typeface);
            }
            {
                int type = new FontPreferences(holder.body.getContext()).getFontTypeTitle().getTypeface();
                Typeface typeface;
                if (type >= 0) {
                    typeface = RobotoTypefaces.obtainTypeface(holder.body.getContext(), type);
                } else {
                    typeface = Typeface.DEFAULT;
                }
                holder.text.setTypeface(typeface);
            }
            holder.text.setVisibility(View.GONE);
            holder.body.setVisibility(View.GONE);


            View.OnClickListener onGifImageClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SettingValues.image) {
                        Intent myIntent = new Intent(main, MediaView.class);
                        myIntent.putExtra(MediaView.EXTRA_URL, image.url);
                        myIntent.putExtra(MediaView.SUBREDDIT, subreddit);
                        if(submissionTitle != null) {
                            myIntent.putExtra(EXTRA_SUBMISSION_TITLE, submissionTitle);
                        }
                        myIntent.putExtra("index", position);
                        main.startActivity(myIntent);
                    } else {
                        LinkUtil.openExternally(image.url);
                    }
                }
            };

            holder.itemView.setOnClickListener(onGifImageClickListener);
        } else if (holder2 instanceof SpacerViewHolder) {
            holder2.itemView.findViewById(R.id.height).setLayoutParams(new LinearLayout.LayoutParams(holder2.itemView.getWidth(), paddingBottom ? height : main.findViewById(R.id.toolbar).getHeight()));
        }

    }

    public static void setTextWithLinks(String s, SpoilerRobotoTextView text) {
        String[] parts = s.split("\\s+");

        StringBuilder b = new StringBuilder();
        for (String item : parts)
            try {
                URL url = new URL(item);
                b.append(" <a href=\"").append(url).append("\">").append(url).append("</a>");
            } catch (MalformedURLException e) {
                b.append(" ").append(item);
            }
        text.setTextHtml(b.toString(), "no sub");
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size() + 1;
    }

    public static class SpacerViewHolder extends RecyclerView.ViewHolder {
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
            text = itemView.findViewById(R.id.imagetitle);
            body = itemView.findViewById(R.id.imageCaption);
            image = itemView.findViewById(R.id.image);


        }
    }

}
