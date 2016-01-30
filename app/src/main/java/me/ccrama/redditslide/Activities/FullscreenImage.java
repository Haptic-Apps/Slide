package me.ccrama.redditslide.Activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.ImageLoaderUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class FullscreenImage extends FullScreenActivity {


    public static final String EXTRA_URL = "url";
    String toReturn;

    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();

        super.onCreate(savedInstanceState);

        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(""), true);

        setContentView(R.layout.activity_image);

        if (SettingValues.imageViewerSolidBackground) {
            findViewById(R.id.root).setBackgroundColor(ContextCompat.getColor(this, R.color.darkbg));
        }

        final SubsamplingScaleImageView i = (SubsamplingScaleImageView) findViewById(R.id.submission_image);

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

        String url = getIntent().getExtras().getString(EXTRA_URL);
        if (url != null && ContentType.isImgurLink(url)) {
            url = url + ".png";
        }
        ImageView fakeImage = new ImageView(FullscreenImage.this);
        fakeImage.setLayoutParams(new LinearLayout.LayoutParams(i.getWidth(), i.getHeight()));
        fakeImage.setScaleType(ImageView.ScaleType.CENTER_CROP);


        ((Reddit) getApplication()).getImageLoader()
                .displayImage(url, new ImageViewAware(fakeImage), ImageLoaderUtils.options, new ImageLoadingListener() {
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
            final String finalUrl = url;
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

                                            if (localAbsoluteFilePath != null) {
                                                MediaScannerConnection.scanFile(FullscreenImage.this, new String[]{localAbsoluteFilePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                                    public void onScanCompleted(String path, Uri uri) {
                                                        Intent intent = new Intent();
                                                        intent.setAction(android.content.Intent.ACTION_VIEW);
                                                        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(".PNG");

                                                        intent.setDataAndType(Uri.parse(localAbsoluteFilePath), mime);
                                                        PendingIntent contentIntent = PendingIntent.getActivity(FullscreenImage.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


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

                                                    }
                                                });
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

    private String saveImageGallery(final Bitmap _bitmap, String URL) {

        return MediaStore.Images.Media.insertImage(getContentResolver(), _bitmap, URL, "");


    }

    private void shareImage(final Bitmap bitmap) {

        String pathofBmp = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Shared", null);
        Uri bmpUri = Uri.parse(pathofBmp);
        final Intent shareImageIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareImageIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareImageIntent.setType("image/png");
        startActivity(Intent.createChooser(shareImageIntent, getString(R.string.misc_img_share)));


    }
}