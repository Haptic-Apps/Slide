package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/1/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.gson.JsonElement;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.MakeTextviewClickable;


public class AlbumViewPager extends FragmentStatePagerAdapter {
    private final ArrayList<JsonElement> users;
    public class AlbumFragment extends Fragment {

        int position;
        public AlbumFragment(int i){
            this.position = i;
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View v = LayoutInflater.from(container.getContext()).inflate(R.layout.album_image_pager, container, false);
            ViewHolder holder = new ViewHolder(v);
            final JsonElement user = users.get(position);

            final String url = list.get(position);

            final SubsamplingScaleImageView i = (SubsamplingScaleImageView) v.findViewById(R.id.image);

            ((TextView) v.findViewById(R.id.page)).setText(position + "/" + getCount());

            final ProgressBar bar = (ProgressBar) v.findViewById(R.id.progress);
            bar.setIndeterminate(false);
            bar.setProgress(0);

            ImageView fakeImage = new ImageView(container.getContext());
            fakeImage.setLayoutParams(new LinearLayout.LayoutParams(i.getWidth(), i.getHeight()));
            fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);


            ((Reddit) ((Activity)(container.getContext())).getApplication()).getImageLoader()
                    .displayImage(url, new ImageViewAware(fakeImage), ImageLoaderUtils.options, new ImageLoadingListener() {
                        private View mView;

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            mView = view;
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            Log.v("Slide", "LOADING FAILED");

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            i.setImage(ImageSource.bitmap(loadedImage));
                            (v.findViewById(R.id.progress)).setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                            Log.v("Slide", "LOADING CANCELLED");

                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            ((ProgressBar) v.findViewById(R.id.progress)).setProgress(Math.round(100.0f * current / total));
                        }
                    });

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
                        if (Reddit.gif) {
                            Intent myIntent = new Intent(main, GifView.class);
                            myIntent.putExtra("url", url);
                            main.startActivity(myIntent);
                        } else {
                            Reddit.defaultShare(url, main);
                        }
                    } else {
                        if (Reddit.image) {
                            Intent myIntent = new Intent(main, FullscreenImage.class);
                            myIntent.putExtra("url", url);
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
            return v;
        }
    }
    private final Context main;
    private final ArrayList<String> list;

    public AlbumViewPager(FragmentManager manager, Context context, ArrayList<JsonElement> users, boolean gallery) {
        super(manager);
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
    public void destroyItem(ViewGroup container, int position, Object object){
        super.destroyItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        return new AlbumFragment(position);
    }



    @Override
    public int getCount()  {
        return users == null ? 0 : users.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final SpoilerRobotoTextView text;
        final SpoilerRobotoTextView body;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (SpoilerRobotoTextView) itemView.findViewById(R.id.imagetitle);
            body = (SpoilerRobotoTextView) itemView.findViewById(R.id.imageCaption);


        }
    }

}
