package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
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
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.bottomsheet.BottomSheet;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.ccrama.redditslide.Adapters.ImageGridAdapter;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.ImgurAlbum.AlbumUtils;
import me.ccrama.redditslide.ImgurAlbum.Image;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.ImageSource;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.Views.SubsamplingScaleImageView;
import me.ccrama.redditslide.Views.ToolbarColorizeHelper;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.SubmissionParser;


/**
 * Created by ccrama on 1/25/2016.
 * <p/>
 * This is an extension of Album.java which utilizes a ViewPager for Imgur content
 * instead of a RecyclerView (horizontal vs vertical). It also supports gifs and progress
 * bars which Album.java doesn't.
 */
public class AlbumPager extends FullScreenActivity implements FolderChooserDialogCreate.FolderCallback {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        if (id == R.id.vertical) {
            SettingValues.albumSwipe = false;
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ALBUM_SWIPE, false).apply();
            Intent i = new Intent(AlbumPager.this, Album.class);
            i.putExtra("url", getIntent().getExtras().getString("url", ""));
            startActivity(i);
            finish();
        }
        if (id == R.id.grid) {
            mToolbar.findViewById(R.id.grid).callOnClick();
        }
        if (id == R.id.external) {
            Reddit.defaultShare(getIntent().getExtras().getString("url", ""), this);
        }
        if (id == R.id.download) {
            final MaterialDialog d = new MaterialDialog.Builder(AlbumPager.this)
                    .title("Saving album")
                    .progress(false, images.size())
                    .show();
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    if (images != null && !images.isEmpty()) {
                        for (final Image elem : images) {
                            saveImageGallery(((Reddit) getApplicationContext()).getImageLoader().loadImageSync(elem.getImageUrl()), elem.getImageUrl());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    d.setProgress(d.getCurrentProgress() + 1);

                                }
                            });
                        }
                        d.dismiss();
                    }
                    return null;
                }
            }.execute();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            Reddit.appRestart.edit().putBoolean("tutorialSwipe", true).apply();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getDarkThemeSubreddit(ColorPreferences.FONT_STYLE), true);
        setContentView(R.layout.album_pager);

        //Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.type_album);
        ToolbarColorizeHelper.colorizeToolbar(mToolbar, Color.WHITE, this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setPopupTheme(new ColorPreferences(this).getDarkThemeSubreddit(ColorPreferences.FONT_STYLE));

        String url = getIntent().getExtras().getString("url", "");
        setShareUrl(url);
        new LoadIntoPager(url, this).execute();

        if (!Reddit.appRestart.contains("tutorialSwipe")) {
            startActivityForResult(new Intent(this, SwipeTutorial.class), 3);
        }

    }

    public class LoadIntoPager extends AlbumUtils.GetAlbumWithCallback {

        String url;

        public LoadIntoPager(@NotNull String url, @NotNull Activity baseActivity) {
            super(url, baseActivity);
            this.url = url;
        }

        @Override
        public void onError() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new AlertDialogWrapper.Builder(AlbumPager.this)
                                .setTitle("Album not found")
                                .setMessage("Would you like to open the link in browser?")
                                .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }).setCancelable(false)
                                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(AlbumPager.this, Website.class);
                                        i.putExtra(Website.EXTRA_URL, url);
                                        startActivity(i);
                                        finish();
                                    }
                                }).show();
                    } catch (Exception e) {

                    }
                }
            });

        }

        @Override
        public void doWithData(final List<Image> jsonElements) {
            super.doWithData(jsonElements);
            findViewById(R.id.progress).setVisibility(View.GONE);
            images = new ArrayList<>(jsonElements);

            final ViewPager p = (ViewPager) findViewById(R.id.images_horizontal);

            if (getSupportActionBar() != null)
                getSupportActionBar().setSubtitle(1 + "/" + images.size());

            AlbumViewPager adapter = new AlbumViewPager(getSupportFragmentManager());
            p.setAdapter(adapter);

            findViewById(R.id.grid).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater l = getLayoutInflater();
                    View body = l.inflate(R.layout.album_grid_dialog, null, false);
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(AlbumPager.this);
                    GridView gridview = (GridView) body.findViewById(R.id.images);
                    gridview.setAdapter(new ImageGridAdapter(AlbumPager.this, images));


                    b.setView(body);
                    final Dialog d = b.create();
                    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v,
                                                int position, long id) {
                            p.setCurrentItem(position);
                            d.dismiss();
                        }
                    });
                    d.show();
                }
            });
            p.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (getSupportActionBar() != null)

                        getSupportActionBar().setSubtitle((position + 1) + "/" + images.size());
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

    public List<Image> images;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.album_pager, menu);

        //   if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        //   else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }

    public class AlbumViewPager extends FragmentStatePagerAdapter {

        public AlbumViewPager(FragmentManager m) {
            super(m);
        }

        @Override
        public Fragment getItem(int i) {


            Image current = images.get(i);

            if (current.isAnimated()) {
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

    public static class Gif extends Fragment {

        private int i = 0;
        private View gif;
        ViewGroup rootView;
        ProgressBar loader;

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = (ViewGroup) inflater.inflate(
                    R.layout.submission_gifcard_album, container, false);
            loader = (ProgressBar) rootView.findViewById(R.id.gifprogress);


            gif = rootView.findViewById(R.id.gif);

            gif.setVisibility(View.VISIBLE);
            final MediaVideoView v = (MediaVideoView) gif;
            v.clearFocus();

            final String url = ((AlbumPager) getActivity()).images.get(i).getImageUrl();

            new GifUtils.AsyncLoadGif(getActivity(), (MediaVideoView) rootView.findViewById(R.id.gif), loader, null, new Runnable() {
                @Override
                public void run() {

                }
            }, false, true, i == 0).execute(url);
            ((MediaVideoView) rootView.findViewById(R.id.gif)).setZOrderOnTop(true);
            rootView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AlbumPager) getActivity()).showBottomSheetImage(url, true);
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

    public void showBottomSheetImage(final String contentUrl, final boolean isGif) {

        int[] attrs = new int[]{R.attr.tint};
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
        BottomSheet.Builder b = new BottomSheet.Builder(this)
                .title(contentUrl);

        b.sheet(2, external, "Open externally");
        b.sheet(5, share, "Share link");
        if (!isGif)
            b.sheet(3, image, "Share image");
        b.sheet(4, save, "Save image");
        b.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case (2): {
                        Reddit.defaultShare(contentUrl, AlbumPager.this);
                    }
                    break;
                    case (3): {
                        shareImage(contentUrl);
                    }
                    break;
                    case (5): {
                        Reddit.defaultShareText("", contentUrl, AlbumPager.this);
                    }
                    break;
                    case (4): {
                        if (!isGif) {
                            String url = contentUrl;
                            final String finalUrl1 = url;
                            final String finalUrl = contentUrl;
                            try {
                                ((Reddit) getApplication()).getImageLoader()
                                        .loadImage(finalUrl, new SimpleImageLoadingListener() {
                                            @Override
                                            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                                                saveImageGallery(loadedImage, finalUrl1);
                                            }

                                        });

                            } catch (Exception e) {
                                Log.v(LogUtil.getTag(), "COULDN'T DOWNLOAD!");
                            }
                        } else {
                            MediaView.doOnClick.run();
                        }
                    }
                    break;
                }
            }
        });

        b.show();

    }

    public static class ImageFullNoSubmission extends Fragment {

        private int i = 0;

        public ImageFullNoSubmission() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.album_image_pager, container, false);

            final Image current = ((AlbumPager) getActivity()).images.get(i);
            final String url = current.getImageUrl();
            boolean lq = false;
            if (SettingValues.loadImageLq && (SettingValues.lowResAlways || (!NetworkUtil.isConnectedWifi(getActivity()) && SettingValues.lowResMobile))) {
                String lqurl = url.substring(0, url.lastIndexOf(".")) + (SettingValues.imgurLq ? "m" : "h") + url.substring(url.lastIndexOf("."), url.length());
                loadImage(rootView, this, lqurl);
                lq = true;
            } else {
                loadImage(rootView, this, url);
            }

            final String finalUrl = url;
            {
                rootView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((AlbumPager) getActivity()).showBottomSheetImage(url, false);
                    }
                });
                {
                    final String finalUrl1 = url;
                    rootView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v2) {
                            try {
                                ((Reddit) (getActivity()).getApplication()).getImageLoader()
                                        .loadImage(finalUrl, new SimpleImageLoadingListener() {
                                            @Override
                                            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                                                ((AlbumPager) getActivity()).saveImageGallery(loadedImage, finalUrl1);
                                            }

                                        });
                            } catch (Exception e) {
                                Log.v(LogUtil.getTag(), "COULDN'T DOWNLOAD!");
                            }

                        }

                    });
                }


            }
            {
                String title = "";
                String description = "";
                if (current.getTitle() != null) {
                    List<String> text = SubmissionParser.getBlocks(current.getTitle());
                    title = text.get(0).trim();
                }

                if (current.getDescription() != null) {
                    List<String> text = SubmissionParser.getBlocks(current.getDescription());
                    description = text.get(0).trim();
                }
                if (title.isEmpty() && description.isEmpty()) {
                    rootView.findViewById(R.id.panel).setVisibility(View.GONE);
                    (rootView.findViewById(R.id.margin)).setPadding(0, 0, 0, 0);
                } else if (title.isEmpty()) {
                    ((SpoilerRobotoTextView) rootView.findViewById(R.id.title)).setTextHtml(description);
                } else {
                    ((SpoilerRobotoTextView) rootView.findViewById(R.id.title)).setTextHtml(title);
                    ((SpoilerRobotoTextView) rootView.findViewById(R.id.body)).setTextHtml(description);
                }
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

            return rootView;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = this.getArguments();
            i = bundle.getInt("page", 0);
        }
    }

    private static void loadImage(final View rootView, Fragment f, String url) {
        final SubsamplingScaleImageView image = (SubsamplingScaleImageView) rootView.findViewById(R.id.image);
        image.setMinimumDpi(70);
        image.setMinimumTileDpi(240);
        ImageView fakeImage = new ImageView(f.getActivity());
        fakeImage.setLayoutParams(new LinearLayout.LayoutParams(image.getWidth(), image.getHeight()));
        fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ((Reddit) f.getActivity().getApplication()).getImageLoader()
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
    }

    private void shareImage(String finalUrl) {
        ((Reddit) getApplication()).getImageLoader()
                .loadImage(finalUrl, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        shareImage(loadedImage);
                    }
                });
    }

    private void saveImageGallery(final Bitmap bitmap, String URL) {
        if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
            showFirstDialog();
        } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
            showErrorDialog();
        } else {
            File f = new File(Reddit.appRestart.getString("imagelocation", "") + File.separator + UUID.randomUUID().toString() + ".png");


            FileOutputStream out = null;
            try {
                f.createNewFile();
                out = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        showNotifPhoto(f, bitmap);


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog();
                }
            }
        }

    }

    public void showFirstDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialogWrapper.Builder(AlbumPager.this)
                        .setTitle(R.string.set_save_location)
                        .setMessage(R.string.set_save_location_msg)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new FolderChooserDialogCreate.Builder(AlbumPager.this)
                                        .chooseButton(R.string.btn_select)  // changes label of the choose button
                                        .initialPath(Environment.getExternalStorageDirectory().getPath())  // changes initial path, defaults to external storage directory
                                        .show();
                            }
                        })
                        .setNegativeButton(R.string.btn_no, null)
                        .show();
            }
        });

    }

    public void showNotifPhoto(final File localAbsoluteFilePath, final Bitmap loadedImage) {
        MediaScannerConnection.scanFile(AlbumPager.this, new String[]{localAbsoluteFilePath.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {

                final Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                shareIntent.setDataAndType(Uri.fromFile(localAbsoluteFilePath), "image/*");
                PendingIntent contentIntent = PendingIntent.getActivity(AlbumPager.this, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);


                Notification notif = new NotificationCompat.Builder(AlbumPager.this)
                        .setContentTitle(getString(R.string.info_photo_saved))
                        .setSmallIcon(R.drawable.notif)
                        .setLargeIcon(loadedImage)
                        .setContentIntent(contentIntent)
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .bigPicture(loadedImage)).build();


                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotificationManager.notify(1, notif);
                loadedImage.recycle();
            }

        });
    }

    private void shareImage(final Bitmap bitmap) {

        if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
            showFirstDialog();
        } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
            showErrorDialog();
        } else {
            File f = new File(Reddit.appRestart.getString("imagelocation", "") + File.separator + UUID.randomUUID().toString() + ".png");


            FileOutputStream out = null;
            try {
                f.createNewFile();
                out = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        if (!f.getAbsolutePath().isEmpty()) {
                            Uri bmpUri = Uri.parse(f.getAbsolutePath());
                            final Intent shareImageIntent = new Intent(android.content.Intent.ACTION_SEND);
                            shareImageIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                            shareImageIntent.setType("image/png");
                            startActivity(Intent.createChooser(shareImageIntent, getString(R.string.misc_img_share)));
                        } else {
                            showErrorDialog();
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog();
                }
            }
        }


    }

    public void showErrorDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialogWrapper.Builder(AlbumPager.this)
                        .setTitle(R.string.err_something_wrong)
                        .setMessage(R.string.err_couldnt_save_choose_new)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new FolderChooserDialogCreate.Builder(AlbumPager.this)
                                        .chooseButton(R.string.btn_select)  // changes label of the choose button
                                        .initialPath(Environment.getExternalStorageDirectory().getPath())  // changes initial path, defaults to external storage directory
                                        .show();
                            }
                        })
                        .setNegativeButton(R.string.btn_no, null)
                        .show();
            }
        });

    }

    private void showShareDialog(final String url) {
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.sharemenu, null);

        dialoglayout.findViewById(R.id.share_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage(url);
            }
        });

        dialoglayout.findViewById(R.id.share_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reddit.defaultShareText("", url, AlbumPager.this);
            }
        });


        builder.setView(dialoglayout);
        builder.show();
    }

    @Override
    public void onFolderSelection(FolderChooserDialogCreate dialog, File folder) {
        if (folder != null) {
            Reddit.appRestart.edit().putString("imagelocation", folder.getAbsolutePath().toString()).apply();
            Toast.makeText(this, "Images will be saved to " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();

        }
    }
}
