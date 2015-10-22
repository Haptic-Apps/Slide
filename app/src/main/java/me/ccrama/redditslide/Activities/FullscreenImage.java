package me.ccrama.redditslide.Activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;


/**
 * Created by ccrama on 3/5/2015.
 */
public class FullscreenImage extends BaseActivity {


    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(""), true);

        setContentView(R.layout.activity_image);

        final SubsamplingScaleImageView i = (SubsamplingScaleImageView) findViewById(R.id.submission_image);

        final ProgressBar bar = (ProgressBar) findViewById(R.id.progress);
        bar.setIndeterminate(false);
        bar.setProgress(0);
        String url = getIntent().getExtras().getString("url");
        if (url != null && url.contains("imgur") && (!url.contains(".png") || !url.contains(".jpg") || !url.contains(".jpeg"))) {
            url = url + ".png";
        }

        ((Reddit) getApplication()).getImageLoader()
                .loadImage(url, new ImageSize(i.getWidth(), i.getHeight()), DisplayImageOptions.createSimple(),
                        new SimpleImageLoadingListener() {

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                i.setImage(ImageSource.bitmap(loadedImage));
                            }


                        }, new ImageLoadingProgressListener() {

                            @Override
                            public void onProgressUpdate(String imageUri, View view, final int current, final int total) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (bar.getVisibility() == View.GONE) {
                                            bar.setVisibility(View.VISIBLE);
                                        }
                                        bar.setProgress((current * 100) / total);
                                        if (current == total) {

                                            bar.setVisibility(View.GONE);
                                        }
                                    }
                                });


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
                    ((Reddit) getApplication()).getImageLoader()
                            .loadImage(finalUrl, new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    String localAbsoluteFilePath = saveImageLocally(loadedImage);

                                    if (localAbsoluteFilePath != "") {

                                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                        Uri phototUri = Uri.parse(localAbsoluteFilePath);

                                        File file = new File(phototUri.getPath());

                                        Log.d("Slide", "file path: " + file.getPath());

                                        if (file.exists()) {
                                            shareIntent.setData(phototUri);
                                            shareIntent.setType("image/png");
                                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

                                            FullscreenImage.this.startActivity(shareIntent);
                                        } else {
                                            // file create fail
                                        }


                                    }
                                }


                            });
                }
            });
            {
                findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {


                        try {
                            ((Reddit) getApplication()).getImageLoader()
                                    .loadImage(finalUrl, new SimpleImageLoadingListener() {
                                        @Override
                                        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                                            String localAbsoluteFilePath = saveImageGallery(loadedImage);

                                            MediaScannerConnection.scanFile(FullscreenImage.this, new String[]{localAbsoluteFilePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                                public void onScanCompleted(String path, Uri uri) {
                                                    Notification notif = new NotificationCompat.Builder(FullscreenImage.this)
                                                            .setContentTitle("Photo Saved")
                                                            .setSmallIcon(R.drawable.notif)
                                                            .setLargeIcon(loadedImage)
                                                            .setStyle(new NotificationCompat.BigPictureStyle()
                                                                    .bigPicture(loadedImage)).build();

                                                    NotificationManager mNotificationManager =
                                                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                    mNotificationManager.notify(1, notif);

                                                }
                                            });

                                        }

                                    });
                        } catch (Exception e) {
                            Log.v("RedditSlide", "COULDN'T DOWNLOAD!");
                        }

                    }

                });
            }


        }

    }


    private static String saveImageGallery(Bitmap _bitmap) {

        File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "Slide");
        File outputFile = null;
        try {
            outputFile = File.createTempFile("slide", ".png", outputDir);
        } catch (IOException e1) {
            // handle exception
        }

        try {
            FileOutputStream out = new FileOutputStream(outputFile);
            _bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();

        } catch (Exception e) {
            // handle exception
        }

        return outputFile.getAbsolutePath();
    }

    private static String saveImageLocally(Bitmap _bitmap) {

        File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outputFile = null;
        try {
            outputFile = File.createTempFile("tmp", ".png", outputDir);
        } catch (IOException e1) {
            // handle exception
        }

        try {
            FileOutputStream out = new FileOutputStream(outputFile);
            _bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

        } catch (Exception e) {
            // handle exception
        }

        return outputFile.getAbsolutePath();
    }
}