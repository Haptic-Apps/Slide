package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.cocosw.bottomsheet.BottomSheet;
import com.devspark.robototextview.RobotoTypefaces;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Adapters.ImageGridAdapterTumblr;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.Fragments.BlankFragment;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.Notifications.ImageDownloadNotificationService;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Tumblr.Photo;
import me.ccrama.redditslide.Tumblr.TumblrUtils;
import me.ccrama.redditslide.Views.ExoVideoView;
import me.ccrama.redditslide.Views.ImageSource;
import me.ccrama.redditslide.Views.SubsamplingScaleImageView;
import me.ccrama.redditslide.Views.ToolbarColorizeHelper;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.ShareUtil;
import me.ccrama.redditslide.util.SubmissionParser;


/**
 * Created by ccrama on 1/25/2016. <p/> This is an extension of Album.java which utilizes a
 * ViewPager for Imgur content instead of a RecyclerView (horizontal vs vertical). It also supports
 * gifs and progress bars which Album.java doesn't.
 */
public class TumblrPager extends FullScreenActivity
        implements FolderChooserDialogCreate.FolderCallback {

    private static int adapterPosition;
    public static final String SUBREDDIT = "subreddit";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        if (id == R.id.vertical) {
            SettingValues.albumSwipe = false;
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ALBUM_SWIPE, false).apply();
            Intent i = new Intent(TumblrPager.this, Tumblr.class);
            if (getIntent().hasExtra(MediaView.SUBMISSION_URL)) {
                i.putExtra(MediaView.SUBMISSION_URL,
                        getIntent().getStringExtra(MediaView.SUBMISSION_URL));
            }
            if (getIntent().hasExtra(SUBREDDIT)) {
                i.putExtra(SUBREDDIT, getIntent().getStringExtra(SUBREDDIT));
            }
            i.putExtras(getIntent());
            startActivity(i);
            finish();
        }
        if (id == R.id.grid) {
            mToolbar.findViewById(R.id.grid).callOnClick();
        }
        if (id == R.id.external) {
            LinkUtil.openExternally(getIntent().getExtras().getString("url", ""));
        }

        if (id == R.id.comments) {
            int adapterPosition = getIntent().getIntExtra(MediaView.ADAPTER_POSITION, -1);
            finish();
            SubmissionsView.datachanged(adapterPosition);
            //getIntent().getStringExtra(MediaView.SUBMISSION_SUBREDDIT));
            //SubmissionAdapter.setOpen(this, getIntent().getStringExtra(MediaView.SUBMISSION_URL));
        }

        if (id == R.id.download) {
            int index = 0;
            for (final Photo elem : images) {
                doImageSave(false, elem.getOriginalSize().getUrl(), index);
                index++;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3) {
            Reddit.appRestart.edit().putBoolean("tutorialSwipe", true).apply();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(
                new ColorPreferences(this).getDarkThemeSubreddit(ColorPreferences.FONT_STYLE),
                true);
        setContentView(R.layout.album_pager);

        //Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.type_album);
        ToolbarColorizeHelper.colorizeToolbar(mToolbar, Color.WHITE, this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(getIntent().hasExtra(SUBREDDIT)){
            this.subreddit = getIntent().getStringExtra(SUBREDDIT);
        }

        mToolbar.setPopupTheme(
                new ColorPreferences(this).getDarkThemeSubreddit(ColorPreferences.FONT_STYLE));

        adapterPosition = getIntent().getIntExtra(MediaView.ADAPTER_POSITION, -1);

        String url = getIntent().getExtras().getString("url", "");
        setShareUrl(url);
        new LoadIntoPager(url, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if (!Reddit.appRestart.contains("tutorialSwipe")) {
            startActivityForResult(new Intent(this, SwipeTutorial.class), 3);
        }

    }

    public class LoadIntoPager extends TumblrUtils.GetTumblrPostWithCallback {

        String url;

        public LoadIntoPager(@NotNull String url, @NotNull Activity baseActivity) {
            super(url, baseActivity);
            this.url = url;
        }

        @Override
        public void onError() {
            Intent i =
                    new Intent(TumblrPager.this, Website.class);
            i.putExtra(LinkUtil.EXTRA_URL, url);
            startActivity(i);
            finish();
        }

        @Override
        public void doWithData(final List<Photo> jsonElements) {
            super.doWithData(jsonElements);
            findViewById(R.id.progress).setVisibility(View.GONE);
            images = new ArrayList<>(jsonElements);

            p = (ViewPager) findViewById(R.id.images_horizontal);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setSubtitle(1 + "/" + images.size());
            }

            AlbumViewPager adapter = new AlbumViewPager(getSupportFragmentManager());
            p.setAdapter(adapter);
            p.setCurrentItem(1);
            findViewById(R.id.grid).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater l = getLayoutInflater();
                    View body = l.inflate(R.layout.album_grid_dialog, null, false);
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(TumblrPager.this);
                    GridView gridview = body.findViewById(R.id.images);
                    gridview.setAdapter(new ImageGridAdapterTumblr(TumblrPager.this, images));


                    b.setView(body);
                    final Dialog d = b.create();
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
                            p.setCurrentItem(position + 1);
                            d.dismiss();
                        }
                    });
                    d.show();
                }
            });
            p.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                        int positionOffsetPixels) {
                    if (position != 0) {
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setSubtitle((position) + "/" + images.size());
                        }
                    }
                    if (position == 0 && positionOffset < 0.2) {
                        finish();
                    }
                }

                @Override
                public void onPageSelected(int position) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            adapter.notifyDataSetChanged();

        }
    }

    ViewPager p;

    public List<Photo> images;
    public String subreddit;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.album_pager, menu);
        adapterPosition = getIntent().getIntExtra(MediaView.ADAPTER_POSITION, -1);
        if (adapterPosition < 0) {
            menu.findItem(R.id.comments).setVisible(false);
        }
        return true;
    }

    public class AlbumViewPager extends FragmentStatePagerAdapter {
        public AlbumViewPager(FragmentManager m) {
            super(m, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int i) {

            if (i == 0) {
                return new BlankFragment();
            }

            i--;
            Photo current = images.get(i);

            try {
                if (ContentType.isGif(new URI(current.getOriginalSize().getUrl()))) {
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
            } catch (URISyntaxException e) {
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
            return images.size() + 1;
        }
    }

    public static class Gif extends Fragment {

        private int i = 0;
        private View gif;
        ViewGroup   rootView;
        ProgressBar loader;

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (this.isVisible()) {
                if (!isVisibleToUser)   // If we are becoming invisible, then...
                {
                    ((ExoVideoView) gif).pause();
                    gif.setVisibility(View.GONE);
                }

                if (isVisibleToUser) // If we are becoming visible, then...
                {
                    ((ExoVideoView) gif).play();
                    gif.setVisibility(View.VISIBLE);

                }
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            rootView = (ViewGroup) inflater.inflate(R.layout.submission_gifcard_album, container,
                    false);
            loader = rootView.findViewById(R.id.gifprogress);


            gif = rootView.findViewById(R.id.gif);

            gif.setVisibility(View.VISIBLE);
            final ExoVideoView v = (ExoVideoView) gif;
            v.clearFocus();

            final String url = ((TumblrPager) getActivity()).images.get(i).getOriginalSize().getUrl();

            new GifUtils.AsyncLoadGif(getActivity(), rootView.findViewById(R.id.gif), loader, null, new Runnable() {
                @Override
                public void run() {

                }
            }, false, true, (TextView) rootView.findViewById(R.id.size),  ((TumblrPager) getActivity()).subreddit).execute(url);
            rootView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((TumblrPager) getActivity()).showBottomSheetImage(url, true, i);
                }
            });
            rootView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaView.doOnClick.run();
                }
            });
            return rootView;
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = this.getArguments();
            i = bundle.getInt("page", 0);

        }

    }

    public void showBottomSheetImage(final String contentUrl, final boolean isGif,
            final int index) {

        int[] attrs = new int[]{R.attr.tintColor};
        TypedArray ta = obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable external = getResources().getDrawable(R.drawable.openexternal);
        Drawable share = getResources().getDrawable(R.drawable.share);
        Drawable image = getResources().getDrawable(R.drawable.image);
        Drawable save = getResources().getDrawable(R.drawable.save);

        external.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        image.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        save.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        ta.recycle();
        BottomSheet.Builder b = new BottomSheet.Builder(this).title(contentUrl);

        b.sheet(2, external, getString(R.string.submission_link_extern));
        b.sheet(5, share, getString(R.string.submission_link_share));
        if (!isGif) b.sheet(3, image, getString(R.string.share_image));
        b.sheet(4, save, getString(R.string.submission_save_image));
        b.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case (2): {
                        LinkUtil.openExternally(contentUrl);
                    }
                    break;
                    case (3): {
                        ShareUtil.shareImage(contentUrl, TumblrPager.this);
                    }
                    break;
                    case (5): {
                        Reddit.defaultShareText("", contentUrl, TumblrPager.this);
                    }
                    break;
                    case (4): {
                        doImageSave(isGif, contentUrl, index);
                    }
                    break;
                }
            }
        });

        b.show();

    }

    public void doImageSave(boolean isGif, String contentUrl, int index) {
        if (!isGif) {
            if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
                showFirstDialog();
            } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
                showErrorDialog();
            } else {
                Intent i = new Intent(this, ImageDownloadNotificationService.class);
                i.putExtra("actuallyLoaded", contentUrl);
                if (subreddit != null && !subreddit.isEmpty()) i.putExtra("subreddit", subreddit);
                i.putExtra("index", index);
                startService(i);
            }
        } else {
            MediaView.doOnClick.run();
        }
    }

    public static class ImageFullNoSubmission extends Fragment {

        private int i = 0;

        public ImageFullNoSubmission() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final ViewGroup rootView =
                    (ViewGroup) inflater.inflate(R.layout.album_image_pager, container, false);

            final Photo current = ((TumblrPager) getActivity()).images.get(i);
            final String url = current.getOriginalSize().getUrl();
            boolean lq = false;
            if (SettingValues.loadImageLq && (SettingValues.lowResAlways
                    || (!NetworkUtil.isConnectedWifi(getActivity())
                    && SettingValues.lowResMobile)) && current.getAltSizes()!= null&&! current.getAltSizes().isEmpty()) {
                String lqurl = current.getAltSizes().get(current.getAltSizes().size()/2).getUrl();
                loadImage(rootView, this, lqurl);
                lq = true;
            } else {
                loadImage(rootView, this, url);
            }

            {
                rootView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((TumblrPager) getActivity()).showBottomSheetImage(url, false, i);
                    }
                });
                {
                    rootView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v2) {
                            ((TumblrPager) getActivity()).doImageSave(false, url, i);
                        }

                    });
                    if (!SettingValues.imageDownloadButton) {
                        rootView.findViewById(R.id.save).setVisibility(View.INVISIBLE);
                    }
                }


            }
            {
                String title = "";
                String description = "";

                if (current.getCaption() != null) {
                    List<String> text = SubmissionParser.getBlocks(current.getCaption());
                    description = text.get(0).trim();
                }
                if (title.isEmpty() && description.isEmpty()) {
                    rootView.findViewById(R.id.panel).setVisibility(View.GONE);
                    (rootView.findViewById(R.id.margin)).setPadding(0, 0, 0, 0);
                } else if (title.isEmpty()) {
                    setTextWithLinks(description,
                            ((SpoilerRobotoTextView) rootView.findViewById(R.id.title)));
                } else {
                    setTextWithLinks(title,
                            ((SpoilerRobotoTextView) rootView.findViewById(R.id.title)));
                    setTextWithLinks(description,
                            ((SpoilerRobotoTextView) rootView.findViewById(R.id.body)));
                }
                {
                    int type = new FontPreferences(getContext()).getFontTypeComment().getTypeface();
                    Typeface typeface;
                    if (type >= 0) {
                        typeface = RobotoTypefaces.obtainTypeface(getContext(), type);
                    } else {
                        typeface = Typeface.DEFAULT;
                    }
                    ((SpoilerRobotoTextView) rootView.findViewById(R.id.body)).setTypeface(typeface);
                }
                {
                    int type = new FontPreferences(getContext()).getFontTypeTitle().getTypeface();
                    Typeface typeface;
                    if (type >= 0) {
                        typeface = RobotoTypefaces.obtainTypeface(getContext(), type);
                    } else {
                        typeface = Typeface.DEFAULT;
                    }
                    ((SpoilerRobotoTextView) rootView.findViewById(R.id.title)).setTypeface(typeface);
                }
                final SlidingUpPanelLayout l = rootView.findViewById(R.id.sliding_layout);
                rootView.findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        l.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    }
                });
                rootView.findViewById(R.id.body).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        l.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    }
                });
            }
            if (lq) {
                rootView.findViewById(R.id.hq).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadImage(rootView, ImageFullNoSubmission.this, url);
                        rootView.findViewById(R.id.hq).setVisibility(View.GONE);
                    }
                });
            } else {
                rootView.findViewById(R.id.hq).setVisibility(View.GONE);
            }

            if (getActivity().getIntent().hasExtra(MediaView.SUBMISSION_URL)) {
                rootView.findViewById(R.id.comments).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().finish();
                        SubmissionsView.datachanged(adapterPosition);
                    }
                });
            } else {
                rootView.findViewById(R.id.comments).setVisibility(View.GONE);
            }
            return rootView;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = this.getArguments();
            i = bundle.getInt("page", 0);
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

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
                + " "
                + units[digitGroups];
    }

    private static void loadImage(final View rootView, Fragment f, String url) {
        final SubsamplingScaleImageView image = rootView.findViewById(R.id.image);
        image.setMinimumDpi(70);
        image.setMinimumTileDpi(240);
        ImageView fakeImage = new ImageView(f.getActivity());
        final TextView size = rootView.findViewById(R.id.size);
        fakeImage.setLayoutParams(
                new LinearLayout.LayoutParams(image.getWidth(), image.getHeight()));
        fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ((Reddit) f.getActivity().getApplication()).getImageLoader()
                .displayImage(url, new ImageViewAware(fakeImage),
                        new DisplayImageOptions.Builder().resetViewBeforeLoading(true)
                                .cacheOnDisk(true)
                                .imageScaleType(ImageScaleType.NONE)
                                .cacheInMemory(false)
                                .build(), new ImageLoadingListener() {
                            private View mView;

                            @Override
                            public void onLoadingStarted(String imageUri, View view) {
                                mView = view;
                                size.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view,
                                    FailReason failReason) {
                                Log.v("Slide", "LOADING FAILED");

                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view,
                                    Bitmap loadedImage) {
                                size.setVisibility(View.GONE);
                                image.setImage(ImageSource.bitmap(loadedImage));
                                (rootView.findViewById(R.id.progress)).setVisibility(View.GONE);
                            }

                            @Override
                            public void onLoadingCancelled(String imageUri, View view) {
                                Log.v("Slide", "LOADING CANCELLED");

                            }
                        }, new ImageLoadingProgressListener() {
                            @Override
                            public void onProgressUpdate(String imageUri, View view, int current,
                                    int total) {
                                size.setText(readableFileSize(total));

                                ((ProgressBar) rootView.findViewById(R.id.progress)).setProgress(
                                        Math.round(100.0f * current / total));
                            }
                        });
    }

    public void showFirstDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialogWrapper.Builder(TumblrPager.this).setTitle(R.string.set_save_location)
                        .setMessage(R.string.set_save_location_msg)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new FolderChooserDialogCreate.Builder(TumblrPager.this).chooseButton(
                                        R.string.btn_select)  // changes label of the choose button
                                        .initialPath(Environment.getExternalStorageDirectory()
                                                .getPath())  // changes initial path, defaults to external storage directory
                                        .show();
                            }
                        })
                        .setNegativeButton(R.string.btn_no, null)
                        .show();
            }
        });

    }

    public void showErrorDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialogWrapper.Builder(TumblrPager.this).setTitle(
                        R.string.err_something_wrong)
                        .setMessage(R.string.err_couldnt_save_choose_new)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new FolderChooserDialogCreate.Builder(TumblrPager.this).chooseButton(
                                        R.string.btn_select)  // changes label of the choose button
                                        .initialPath(Environment.getExternalStorageDirectory()
                                                .getPath())  // changes initial path, defaults to external storage directory
                                        .show();
                            }
                        })
                        .setNegativeButton(R.string.btn_no, null)
                        .show();
            }
        });

    }

    @Override
    public void onFolderSelection(FolderChooserDialogCreate dialog, File folder, boolean isSaveToLocation) {
        if (folder != null) {
            Reddit.appRestart.edit().putString("imagelocation", folder.getAbsolutePath()).apply();
            Toast.makeText(this,
                    getString(R.string.settings_set_image_location, folder.getAbsolutePath())
                            + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();

        }
    }
}
