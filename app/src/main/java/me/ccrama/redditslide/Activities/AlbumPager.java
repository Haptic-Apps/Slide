package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.gson.JsonElement;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.Views.TitleTextView;
import me.ccrama.redditslide.Views.ToolbarColorizeHelper;
import me.ccrama.redditslide.util.AlbumUtils;
import me.ccrama.redditslide.util.GifUtils;


/**
 * Created by ccrama on 1/25/2016.
 *
 * This is an extension of Album.java which utilizes a ViewPager for Imgur content
 * instead of a RecyclerView (horizontal vs vertical). It also supports gifs and progress
 * bars which Album.java doesn't.
 *
 */
public class AlbumPager extends FullScreenActivity {
    boolean gallery = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        applyColorTheme();
        setContentView(R.layout.album_pager);

        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setTitle(R.string.album_loading);
        ToolbarColorizeHelper.colorizeToolbar(b, Color.WHITE, this);
        setSupportActionBar(b);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AlbumUtils.GetAlbumJsonFromUrl loader = new AlbumUtils.GetAlbumJsonFromUrl(getIntent().getExtras().getString("url", ""), this);
        try {
            loader.get();
            ViewPager p = (ViewPager) findViewById(R.id.images_horizontal);

            getSupportActionBar().setSubtitle(1 + "/" + images.size());

            AlbumViewPager adapter = new AlbumViewPager(getSupportFragmentManager());
            p.setAdapter(adapter);
            p.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    getSupportActionBar().setSubtitle((position + 1)+ "/" + images.size());
                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            adapter.notifyDataSetChanged();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



    }



    public ArrayList<JsonElement> images;

    public class AlbumViewPager extends FragmentStatePagerAdapter {


        public AlbumViewPager(FragmentManager m) {
            super(m);
        }


        @Override
        public Fragment getItem(int i) {

            String url;
            if (gallery) {
                url = ("https://imgur.com/" + images.get(i).getAsJsonObject().get("hash").getAsString() + ".png");

            } else {
                url = (images.get(i).getAsJsonObject().getAsJsonObject("links").get("original").getAsString());

            }

            if (url.contains("gif")) {
                //do gif stuff
                Fragment f = new Gif();
                Bundle args = new Bundle();
                args.putInt("page", i);
                f.setArguments(args);

                return f;
            } else {
                Fragment f = new ImageFullNoSubmission();
                Bundle args = new Bundle();
                args.putInt("page", i);
                f.setArguments(args);

                return f;
            }


        }


        @Override
        public int getCount() {
            if (images == null) {
                return 0;
            }
            return images.size();
        }
    }

    public class Gif extends Fragment {

        private int i = 0;
        private View gif;

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (this.isVisible()) {
                if (!isVisibleToUser)   // If we are becoming invisible, then...
                {
                    ((MediaVideoView) gif).pause();
                    gif.setVisibility(View.GONE);
                }

                if (isVisibleToUser) // If we are becoming visible, then...
                {
                    ((MediaVideoView) gif).start();
                    gif.setVisibility(View.VISIBLE);

                }
            }
        }

        ViewGroup rootView;
        ProgressBar loader;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = (ViewGroup) inflater.inflate(
                    R.layout.submission_gifcard_album, container, false);
            loader = (ProgressBar) rootView.findViewById(R.id.gifprogress);

            TitleTextView title = (TitleTextView) rootView.findViewById(R.id.title);
            SpoilerRobotoTextView desc = (SpoilerRobotoTextView) rootView.findViewById(R.id.desc);

            title.setVisibility(View.VISIBLE);
            desc.setVisibility(View.VISIBLE);
            if (user.getAsJsonObject().has("image")) {
                {
                    if (!user.getAsJsonObject().getAsJsonObject("image").get("title").isJsonNull()) {

                        new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().getAsJsonObject("image").get("title").getAsString(), desc, (Activity) inflater.getContext(), "");
                        if (desc.getText().toString().isEmpty()) {
                            desc.setVisibility(View.GONE);
                        }

                    } else {
                        desc.setVisibility(View.GONE);

                    }
                }
                {
                    if (!user.getAsJsonObject().getAsJsonObject("image").get("caption").isJsonNull()) {
                        title.setText(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString());
                        new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString(), title, (Activity) inflater.getContext(), "");

                        if (title.getText().toString().isEmpty()) {
                            title.setVisibility(View.GONE);
                        }
                    } else {
                        title.setVisibility(View.GONE);

                    }
                }
            } else {
                if (user.getAsJsonObject().has("title")) {
                    new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().get("title").getAsString(), desc, (Activity) inflater.getContext(), "");
                    if (desc.getText().toString().isEmpty()) {
                        desc.setVisibility(View.GONE);
                    }

                } else {

                    desc.setVisibility(View.GONE);

                }
                if (user.getAsJsonObject().has("description")) {
                    new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().get("description").getAsString(), title, (Activity) inflater.getContext(), "");
                    if (title.getText().toString().isEmpty()) {
                        title.setVisibility(View.GONE);
                    }
                } else {
                    title.setVisibility(View.GONE);

                }


            }
            gif = rootView.findViewById(R.id.gif);


            gif.setVisibility(View.VISIBLE);
            final MediaVideoView v = (MediaVideoView) gif;
            v.clearFocus();



            String dat;
            if (gallery) {
                dat = ("https://imgur.com/" + images.get(i).getAsJsonObject().get("hash").getAsString() + ".png");

            } else {
                dat = (images.get(i).getAsJsonObject().getAsJsonObject("links").get("original").getAsString());

            }


            new GifUtils.AsyncLoadGif(AlbumPager.this, (MediaVideoView) rootView.findViewById(R.id.gif), loader, null, null, false).execute(dat);

            return rootView;
        }

        JsonElement user;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = this.getArguments();
            i = bundle.getInt("page", 0);
            user = images.get(bundle.getInt("page", 0));

        }

    }

    public class ImageFullNoSubmission extends Fragment {

        private int i = 0;
        private JsonElement user;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.submission_imagecard_album, container, false);

            String url;

            if (gallery) {
                url = ("https://imgur.com/" + user.getAsJsonObject().get("hash").getAsString() + ".png");

            } else {
                url = (user.getAsJsonObject().getAsJsonObject("links").get("original").getAsString());

            }

            final SubsamplingScaleImageView image = (SubsamplingScaleImageView) rootView.findViewById(R.id.image);
            TitleTextView title = (TitleTextView) rootView.findViewById(R.id.title);
            SpoilerRobotoTextView desc = (SpoilerRobotoTextView) rootView.findViewById(R.id.desc);
            ImageView fakeImage = new ImageView(getActivity());
            fakeImage.setLayoutParams(new LinearLayout.LayoutParams(image.getWidth(), image.getHeight()));
            fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);


            ((Reddit) getActivity().getApplication()).getImageLoader()
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
                            image.setImage(ImageSource.bitmap(loadedImage));
                            (rootView.findViewById(R.id.progress)).setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                            Log.v("Slide", "LOADING CANCELLED");

                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current, int total) {
                            ((ProgressBar) rootView.findViewById(R.id.progress)).setProgress(Math.round(100.0f * current / total));
                        }
                    });
            title.setVisibility(View.VISIBLE);
            desc.setVisibility(View.VISIBLE);
            if (user.getAsJsonObject().has("image")) {
                {
                    if (!user.getAsJsonObject().getAsJsonObject("image").get("title").isJsonNull()) {

                        new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().getAsJsonObject("image").get("title").getAsString(), desc, (Activity) inflater.getContext(), "");
                        if (desc.getText().toString().isEmpty()) {
                            desc.setVisibility(View.GONE);
                        }

                    } else {
                        desc.setVisibility(View.GONE);

                    }
                }
                {
                    if (!user.getAsJsonObject().getAsJsonObject("image").get("caption").isJsonNull()) {
                        title.setText(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString());
                        new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString(), title, (Activity) inflater.getContext(), "");

                        if (title.getText().toString().isEmpty()) {
                            title.setVisibility(View.GONE);
                        }
                    } else {
                        title.setVisibility(View.GONE);

                    }
                }
            } else {
                if (user.getAsJsonObject().has("title")) {
                    new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().get("title").getAsString(), desc, (Activity) inflater.getContext(), "");
                    if (desc.getText().toString().isEmpty()) {
                        desc.setVisibility(View.GONE);
                    }

                } else {

                    desc.setVisibility(View.GONE);

                }
                if (user.getAsJsonObject().has("description")) {
                    new MakeTextviewClickable().ParseTextWithLinksTextViewComment(user.getAsJsonObject().get("description").getAsString(), title, (Activity) inflater.getContext(), "");
                    if (title.getText().toString().isEmpty()) {
                        title.setVisibility(View.GONE);
                    }
                } else {
                    title.setVisibility(View.GONE);

                }


            }


            return rootView;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = this.getArguments();
            i = bundle.getInt("page", 0);
            user = images.get(i);

        }

    }




}