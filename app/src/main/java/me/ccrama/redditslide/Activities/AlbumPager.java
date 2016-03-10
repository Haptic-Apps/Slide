package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.google.gson.JsonElement;
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

import it.sephiroth.android.library.tooltip.Tooltip;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.ImageSource;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.Views.SubsamplingScaleImageView;
import me.ccrama.redditslide.Views.ToolbarColorizeHelper;
import me.ccrama.redditslide.util.AlbumUtils;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SubmissionParser;


/**
 * Created by ccrama on 1/25/2016.
 * <p/>
 * This is an extension of Album.java which utilizes a ViewPager for Imgur content
 * instead of a RecyclerView (horizontal vs vertical). It also supports gifs and progress
 * bars which Album.java doesn't.
 */
public class AlbumPager extends FullScreenActivity implements FolderChooserDialog.FolderCallback {
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
        b.setTitle(R.string.type_album);
        ToolbarColorizeHelper.colorizeToolbar(b, Color.WHITE, this);
        setSupportActionBar(b);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        new LoadIntoPager(getIntent().getExtras().getString("url", ""), this).execute();
        if (!Reddit.appRestart.contains("tutorialSwipeAlbum")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Tooltip.make(AlbumPager.this,
                            new Tooltip.Builder(106)
                                    .text("Drag from the very edge to exit")
                                    .maxWidth(500)
                                    .closePolicy(new Tooltip.ClosePolicy()
                                            .insidePolicy(true, false)
                                            .outsidePolicy(true, false), 3000)
                                    .anchor(findViewById(R.id.tutorial), Tooltip.Gravity.RIGHT)
                                    .activateDelay(800)
                                    .showDelay(300)
                                    .withArrow(true)
                                    .withOverlay(true)
                                    .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                    .build()
                    ).show();
                }
            }, 250);
        }

        findViewById(R.id.slider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingValues.albumSwipe = false;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ALBUM_SWIPE, false).apply();
                Intent i = new Intent(AlbumPager.this, Album.class);
                i.putExtra("url", getIntent().getExtras().getString("url", ""));
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!Reddit.appRestart.contains("tutorialSwipeAlbum")) {
            Reddit.appRestart.edit().putBoolean("tutorialSwipeAlbum", true).apply();
        }
    }

    public class LoadIntoPager extends AlbumUtils.GetAlbumJsonFromUrl {

        public LoadIntoPager(@NotNull String url, @NotNull Activity baseActivity) {
            super(url, baseActivity);
        }

        @Override
        public void doWithData(final ArrayList<JsonElement> jsonElements) {
            AlbumPager.this.gallery = LoadIntoPager.this.gallery;
            images = new ArrayList<>(jsonElements);

            ViewPager p = (ViewPager) findViewById(R.id.images_horizontal);

            getSupportActionBar().setSubtitle(1 + "/" + images.size());

            AlbumViewPager adapter = new AlbumViewPager(getSupportFragmentManager());
            p.setAdapter(adapter);
            {
                JsonElement user = jsonElements.get(0);


                if (user.getAsJsonObject().has("image")) {
                    if (!user.getAsJsonObject().getAsJsonObject("image").get("title").isJsonNull()) {
                        List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().getAsJsonObject("image").get("title").getAsString());
                        getSupportActionBar().setTitle(text.get(0));

                    }

                    if (!user.getAsJsonObject().getAsJsonObject("image").get("caption").isJsonNull()) {
                        List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString());
                        final String done = text.get(0);
                        findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialogWrapper.Builder(AlbumPager.this).setMessage(done).show();
                            }
                        });
                        if (done.isEmpty()) {
                            findViewById(R.id.text).setVisibility(View.GONE);
                        } else {
                            findViewById(R.id.text).setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    if (user.getAsJsonObject().has("title")) {
                        List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().get("title").getAsString());
                        getSupportActionBar().setTitle(text.get(0));

                    }

                    if (user.getAsJsonObject().has("description")) {
                        List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().get("description").getAsString());
                        final String done = text.get(0);
                        findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialogWrapper.Builder(AlbumPager.this).setMessage(done).show();
                            }
                        });
                        if (done.isEmpty()) {
                            findViewById(R.id.text).setVisibility(View.GONE);
                        } else {
                            findViewById(R.id.text).setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
            p.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    getSupportActionBar().setSubtitle((position + 1) + "/" + images.size());
                }

                @Override
                public void onPageSelected(int position) {
                    JsonElement user = jsonElements.get(position);


                    if (user.getAsJsonObject().has("image")) {
                        if (!user.getAsJsonObject().getAsJsonObject("image").get("title").isJsonNull()) {
                            List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().getAsJsonObject("image").get("title").getAsString());
                            getSupportActionBar().setTitle(text.get(0));

                        }

                        if (!user.getAsJsonObject().getAsJsonObject("image").get("caption").isJsonNull()) {
                            List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().getAsJsonObject("image").get("caption").getAsString());
                            final String done = text.get(0);
                            findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AlertDialogWrapper.Builder(AlbumPager.this).setMessage(done).show();
                                }
                            });
                            if (done.isEmpty()) {
                                findViewById(R.id.text).setVisibility(View.GONE);
                            } else {
                                findViewById(R.id.text).setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        if (user.getAsJsonObject().has("title")) {
                            List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().get("title").getAsString());
                            getSupportActionBar().setTitle(text.get(0));

                        }

                        if (user.getAsJsonObject().has("description")) {
                            List<String> text = SubmissionParser.getBlocks(user.getAsJsonObject().get("description").getAsString());
                            final String done = text.get(0);
                            findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AlertDialogWrapper.Builder(AlbumPager.this).setMessage(done).show();
                                }
                            });
                            if (done.isEmpty()) {
                                findViewById(R.id.text).setVisibility(View.GONE);
                            } else {
                                findViewById(R.id.text).setVisibility(View.VISIBLE);
                            }
                        }
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            adapter.notifyDataSetChanged();
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

            if (url.contains("gif") || (images.get(i).getAsJsonObject().has("ext") && images.get(i).getAsJsonObject().get("ext").getAsString().contains("gif"))) {
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

            String dat;
            if (gallery) {

                dat = ("https://imgur.com/" + images.get(i).getAsJsonObject().get("hash").getAsString() + ".gif");

            } else {
                dat = (images.get(i).getAsJsonObject().getAsJsonObject("links").get("original").getAsString());

            }

            new GifUtils.AsyncLoadGif(AlbumPager.this, (MediaVideoView) rootView.findViewById(R.id.gif), loader, null, null, false, true).execute(dat);

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
                    R.layout.album_image_pager, container, false);

            String url;

            if (gallery) {
                url = ("https://imgur.com/" + user.getAsJsonObject().get("hash").getAsString() + ".png");

            } else {
                url = (user.getAsJsonObject().getAsJsonObject("links").get("original").getAsString());

            }

            final String finalUrl  = url;
            {
                final ImageView iv = (ImageView) rootView.findViewById(R.id.share);
                rootView.findViewById(R.id.external).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Reddit.defaultShare(finalUrl, AlbumPager.this);

                    }
                });
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showShareDialog(finalUrl);
                    }
                });
                {
                    final String finalUrl1 = url;
                    rootView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v2) {


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

                        }

                    });
                }


            }
            final SubsamplingScaleImageView image = (SubsamplingScaleImageView) rootView.findViewById(R.id.image);
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
        new AlertDialogWrapper.Builder(this)
                .setTitle("Set image save location")
                .setMessage("Slide's image save location has not been set yet. Would you like to set this now?")
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialog.Builder(AlbumPager.this)
                                .chooseButton(R.string.btn_select)  // changes label of the choose button
                                .initialPath("/sdcard/")  // changes initial path, defaults to external storage directory
                                .show();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
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
                        if ( !f.getAbsolutePath().isEmpty()) {
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
        new AlertDialogWrapper.Builder(AlbumPager.this)
                .setTitle("Uh oh, something went wrong.")
                .setMessage("Slide couldn't save to the selected directory. Would you like to choose a new save location?")
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialog.Builder(AlbumPager.this)
                                .chooseButton(R.string.btn_select)  // changes label of the choose button
                                .initialPath("/sdcard/")  // changes initial path, defaults to external storage directory
                                .show();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
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
                Reddit.defaultShareText(url, AlbumPager.this);
            }
        });


        builder.setView(dialoglayout);
        builder.show();
    }

    @Override
    public void onFolderSelection(FolderChooserDialog dialog, File folder) {
        if (folder != null) {
            Reddit.appRestart.edit().putString("imagelocation", folder.getAbsolutePath().toString()).apply();
            Toast.makeText(this, "Images will be saved to " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }
}