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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.TouchImageView;


/**
 * Created by ccrama on 3/5/2015.
 */
public class FullscreenImage extends BaseActivity {



    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit("", true).getBaseId(), true);

        setContentView(R.layout.activity_image);

        final TouchImageView i = (TouchImageView) findViewById(R.id.submission_image);

        final ProgressBar bar = (ProgressBar) findViewById(R.id.progress);
        bar.setIndeterminate(false);
        bar.setProgress(0);
        String url = getIntent().getExtras().getString("url");
        if (url != null && url.contains("imgur") && (!url.contains(".png") || !url.contains(".jpg") || !url.contains(".jpeg"))) {
            url = url + ".png";
        }

        Ion.with(this).load(url).progress(new ProgressCallback() {


            @Override
            public void onProgress(final long downloaded, final long total) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bar.getVisibility() == View.GONE) {
                            bar.setVisibility(View.VISIBLE);
                        }
                        bar.setProgress((int) ((downloaded * 100) / total));
                        if (downloaded == total) {

                            bar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        }).intoImageView(i);


        i.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v2) {
                FullscreenImage.this.finish();
            }
        });


        {
            final ImageView iv = (ImageView) findViewById(R.id.share);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String localAbsoluteFilePath = saveImageLocally(Ion.with(i).getBitmap());

                    if ( localAbsoluteFilePath != "") {

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
        {
            findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v2) {


                  try{
                      final Bitmap b = Ion.with(i).getBitmap();

                      String localAbsoluteFilePath = saveImageGallery(b);

                      MediaScannerConnection.scanFile(FullscreenImage.this, new String[]{localAbsoluteFilePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Notification notif = new NotificationCompat.Builder(FullscreenImage.this)
                                        .setContentTitle("Photo Saved")
                                        .setSmallIcon(R.drawable.notif)
                                        .setLargeIcon(b)
                                        .setStyle(new NotificationCompat.BigPictureStyle()
                                                .bigPicture(b)).build();

                                NotificationManager mNotificationManager =
                                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                mNotificationManager.notify(1, notif);

                            }
                        });
                    } catch (Exception e) {
                        Log.v("RedditSlide", "COULDN'T DOWNLOAD!");
                    }
                }
            });
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            // when user first touches the screen we get x and y coordinate
            case MotionEvent.ACTION_DOWN: {
                x1 = touchevent.getX();
                y1 = touchevent.getY();
                break;
            }
            case MotionEvent.ACTION_UP: {
                x2 = touchevent.getX();
                y2 = touchevent.getY();

                //if left to right sweep event on screen
                if (x1 < x2) {
                    finish();
                }

                // if right to left sweep event on screen
                if (x1 > x2) {
                    finish();
                }


                break;
            }
        }
        return false;
    }

    float x1, x2;
    float y1, y2;

    private String saveImageGallery(Bitmap _bitmap) {

        File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+ File.separator + "Slide");
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

    private String saveImageLocally(Bitmap _bitmap) {

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