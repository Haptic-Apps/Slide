package me.ccrama.redditslide.Activities;

import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import it.sephiroth.android.library.tooltip.Tooltip;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.ImageSource;
import me.ccrama.redditslide.Views.SubsamplingScaleImageView;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.CustomTabUtil;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class FullscreenImage extends FullScreenActivity implements FolderChooserDialog.FolderCallback {


    public float previous;
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_SHARE_URL = "urlShare";
    public boolean hidden;

    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();

        super.onCreate(savedInstanceState);

        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(""), true);

        setContentView(R.layout.activity_image);


        String url = getIntent().getExtras().getString(EXTRA_URL);
        if (url != null && ContentType.isImgurLink(url)) {
            url = url + ".png";
        }
        LogUtil.v(url);
        if (url != null && !url.startsWith("https://i.redditmedia.com") && !url.contains("imgur.com")) { //we can assume redditmedia and imgur links are to direct images and not websites
            final String finalUrl2 = url;
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        URL obj = new URL(finalUrl2);
                        URLConnection conn = obj.openConnection();
                        final String type = conn.getHeaderField("Content-Type");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (type.startsWith("image/")) {
                                    //is image
                                    loadImage(finalUrl2);
                                } else {
                                    CustomTabUtil.openUrl(finalUrl2, Palette.getDefaultColor(), FullscreenImage.this);
                                    finish();
                                }
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //get all headers

                    return null;
                }
            }.execute();

        } else if (url != null) {
            loadImage(url);
        } else {
            finish();
            //todo maybe something better
        }

        if (!Reddit.appRestart.contains("tutorialSwipeImage")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    Tooltip.make(FullscreenImage.this,
                            new Tooltip.Builder(106)
                                    .text("Drag from the very edge to exit")
                                    .closePolicy(new Tooltip.ClosePolicy()
                                            .insidePolicy(true, false)
                                            .outsidePolicy(true, false), 3000)
                                    .maxWidth(500)
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
    }

    public void loadImage(String url) {
        final SubsamplingScaleImageView i = (SubsamplingScaleImageView) findViewById(R.id.submission_image);

        i.setMinimumDpi(10);
        final ProgressBar bar = (ProgressBar) findViewById(R.id.progress);
        bar.setIndeterminate(false);
        bar.setProgress(0);

        final Handler handler = new Handler();
        final Runnable progressBarDelayRunner = new Runnable() {
            public void run() {
                bar.setVisibility(View.VISIBLE);
            }
        };
        handler.postDelayed(progressBarDelayRunner, 500);

        ImageView fakeImage = new ImageView(FullscreenImage.this);
        fakeImage.setLayoutParams(new LinearLayout.LayoutParams(i.getWidth(), i.getHeight()));
        fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);


        ((Reddit) getApplication()).getImageLoader()
                .displayImage(url, new ImageViewAware(fakeImage), new DisplayImageOptions.Builder()
                        .resetViewBeforeLoading(true)
                        .cacheOnDisk(true)
                        .imageScaleType(ImageScaleType.NONE)
                        .cacheInMemory(false)
                        .displayer(new FadeInBitmapDisplayer(250))
                        .build(), new ImageLoadingListener() {
                    private View mView;

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        mView = view;
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        Log.v(LogUtil.getTag(), "LOADING FAILED");

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        i.setImage(ImageSource.bitmap(loadedImage));

                        (findViewById(R.id.progress)).setVisibility(View.GONE);
                        handler.removeCallbacks(progressBarDelayRunner);

                        previous = i.scale;
                        final float base = i.scale;
                        i.setOnZoomChangedListener(new me.ccrama.redditslide.Views.SubsamplingScaleImageView.OnZoomChangedListener() {
                            @Override
                            public void onZoomLevelChanged(float zoom) {
                                if (zoom > previous && !hidden && zoom > base) {
                                    hidden = true;
                                    final View base = findViewById(R.id.gifheader);

                                    ValueAnimator va = ValueAnimator.ofFloat(1.0f, 0.2f);
                                    int mDuration = 250; //in millis
                                    va.setDuration(mDuration);
                                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        public void onAnimationUpdate(ValueAnimator animation) {
                                            Float value = (Float) animation.getAnimatedValue();
                                            base.setAlpha(value);
                                        }
                                    });

                                    va.start();

                                    //hide
                                } else if (zoom <= previous && hidden) {
                                    hidden = false;
                                    final View base = findViewById(R.id.gifheader);

                                    ValueAnimator va = ValueAnimator.ofFloat(0.2f, 1.0f);
                                    int mDuration = 250; //in millis
                                    va.setDuration(mDuration);
                                    va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        public void onAnimationUpdate(ValueAnimator animation) {
                                            Float value = (Float) animation.getAnimatedValue();
                                            base.setAlpha(value);
                                        }
                                    });

                                    va.start();

                                    //unhide
                                }
                                previous = zoom;

                            }
                        });
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        Log.v(LogUtil.getTag(), "LOADING CANCELLED");

                    }
                }, new ImageLoadingProgressListener() {
                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {
                        ((ProgressBar) findViewById(R.id.progress)).setProgress(Math.round(100.0f * current / total));
                    }
                });

        i.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v2) {
                FullscreenImage.this.finish();
            }
        });


        {
            final ImageView iv = (ImageView) findViewById(R.id.share);
            final String finalUrl = getIntent().getExtras().getString(EXTRA_SHARE_URL, url);
            findViewById(R.id.external).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Reddit.defaultShare(finalUrl, FullscreenImage.this);

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
                findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {


                        try {
                            ((Reddit) getApplication()).getImageLoader()
                                    .loadImage(finalUrl, new SimpleImageLoadingListener() {
                                        @Override
                                        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {

                                            final String localAbsoluteFilePath = saveImageGallery(loadedImage, finalUrl1);
                                            if (localAbsoluteFilePath != null && ! localAbsoluteFilePath.isEmpty() && !localAbsoluteFilePath.equals("choosing")) {
                                                showNotifPhoto(localAbsoluteFilePath, loadedImage);
                                            } else if(localAbsoluteFilePath == null || localAbsoluteFilePath.isEmpty()) {
                                               showErrorDialog();
                                            }

                                        }

                                    });
                        } catch (Exception e) {
                            Log.v(LogUtil.getTag(), "COULDN'T DOWNLOAD!");
                        }

                    }

                });
            }


        }
    }

    public void showNotifPhoto(final String localAbsoluteFilePath, final Bitmap loadedImage) {
        LogUtil.v(localAbsoluteFilePath);
        MediaScannerConnection.scanFile(FullscreenImage.this, new String[]{localAbsoluteFilePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {

                final Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                shareIntent.setDataAndType(Uri.parse(localAbsoluteFilePath), "image/*");
                PendingIntent contentIntent = PendingIntent.getActivity(FullscreenImage.this, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);


                Notification notif = new NotificationCompat.Builder(FullscreenImage.this)
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

    public void showErrorDialog(){
        new AlertDialogWrapper.Builder(FullscreenImage.this)
                .setTitle("Uh oh, something went wrong.")
                .setMessage("Slide couldn't save to the selected directory. Would you like to choose a new save location?")
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialog.Builder(FullscreenImage.this)
                                .chooseButton(R.string.btn_select)  // changes label of the choose button
                                .initialPath("/sdcard/")  // changes initial path, defaults to external storage directory
                                .show();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!Reddit.appRestart.contains("tutorialSwipeImage")) {
            Reddit.appRestart.edit().putBoolean("tutorialSwipeImage", true).apply();
        }
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
                Reddit.defaultShareText(url, FullscreenImage.this);
            }
        });


        builder.setView(dialoglayout);
        builder.show();
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


    private String saveImageGallery(final Bitmap bitmap, String URL) {
        if (Reddit.appRestart.getString("imagelocation", "").isEmpty() || !new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
            if( !new File(Reddit.appRestart.getString("imagelocation", "")).exists()){
                showErrorDialog();
            } else {
                new FolderChooserDialog.Builder(this)
                        .chooseButton(R.string.btn_select)  // changes label of the choose button
                        .initialPath("/sdcard/")  // changes initial path, defaults to external storage directory
                        .show();
            }
            return "choosing";

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
                        return f.getAbsolutePath();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    private void shareImage(final Bitmap bitmap) {

        String loc = saveImageGallery(bitmap, "");
        if (loc != null && !loc.isEmpty() && !loc.equals("choosing")) {
            Uri bmpUri = Uri.parse(loc);
            final Intent shareImageIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareImageIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareImageIntent.setType("image/png");
            startActivity(Intent.createChooser(shareImageIntent, getString(R.string.misc_img_share)));
        } else if(loc == null || loc.isEmpty()  ){
           showErrorDialog();
        }

    }

    @Override
    public void onFolderSelection(FolderChooserDialog dialog, File folder) {
        if (folder != null) {
            Reddit.appRestart.edit().putString("imagelocation", folder.getAbsolutePath().toString()).apply();
            Toast.makeText(this, "Images will be saved to " + folder.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }
}