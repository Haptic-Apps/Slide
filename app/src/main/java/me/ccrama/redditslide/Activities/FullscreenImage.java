package me.ccrama.redditslide.Activities;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import net.dean.jraw.models.Submission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.TouchImageView;


/**
 * Created by carlo_000 on 3/5/2015.
 */
public class FullscreenImage extends BaseActivity {

    /**
     * Called when the activity is first created.
     */
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                FullscreenImage.this.finish();
                return false; // Right to left
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                FullscreenImage.this.finish();

                return false; // Left to right
            }

            if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                FullscreenImage.this.finish();

                return false; // Bottom to top
            }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                FullscreenImage.this.finish();

                return false; // Top to bottom
            }
            return false;
        }
    }

    public void onCreate(Bundle savedInstanceState) {


// set an exit transition


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);




        final TouchImageView i = (TouchImageView) findViewById(R.id.submission_image);

       final ProgressBar bar = (ProgressBar) findViewById(R.id.progress);
        bar.setIndeterminate(false);
        bar.setProgress(0);
        String url = getIntent().getExtras().getString("url");
        if(url != null && url.contains("imgur") && (!url.contains(".png") || !url.contains(".jpg") || !url.contains(".jpeg"))){
            url = url + ".png";
        }
        Log.v("Slide", "URL IS " + url);
        Ion.with(this).load(url).progress(new ProgressCallback() {


            @Override
            public void onProgress(final long downloaded, final long total) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(bar.getVisibility() == View.GONE){
                            bar.setVisibility(View.VISIBLE);
                        }
                        bar.setProgress((int) ((downloaded * 100)/ total));
                        Log.v("Slide", "LOADING " + ( (downloaded / total) * 100) + "ALTERNATE " + ((int) ((downloaded * 100)/ total)));
                        if (downloaded == total) {

                            bar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        }).intoImageView(i);
        ImageView external = ((ImageView) findViewById(R.id.external));
        external.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO content open
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            i.setOnClickListener(new View.OnClickListener() {

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v2) {

                    FullscreenImage.this.finishAfterTransition();
                }
            });

        } else {


            i.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v2) {
                    FullscreenImage.this.finish();
                }
            });

        }

        {
            final ImageView iv = (ImageView)findViewById(R.id.share);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String localAbsoluteFilePath = saveImageLocally(Ion.with(i).getBitmap());

                    if (localAbsoluteFilePath != null && localAbsoluteFilePath != "") {

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
            final ImageView iv = (ImageView) findViewById(R.id.save);

            iv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v2) {
                    final Submission s = DataShare.sharedSubmission;
                    final String desc;
                    if(s == null){
                        desc = "Saved image";
                    } else {
                         desc = "Submitted by " + s.getAuthor() + " to /r/" + s.getSubredditName();
                    }
                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "Slide"); //Creates app specific folder
                    path.mkdirs();
                    File imageFile = new File(path, s.getId() + ".png"); // Imagename.png
                    try {
                        FileOutputStream out = new FileOutputStream(imageFile);
                        final Bitmap b = ((BitmapDrawable)i.getDrawable()).getBitmap();
                      b.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
                        out.flush();
                        out.close();

                        // Tell the media scanner about the new file so that it is
                        // immediately available to the user.
                        MediaScannerConnection.scanFile(FullscreenImage.this, new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("RedditSlide", "Scanned " + path + ":");
                                Log.i("RedditSlide", "-> uri=" + uri);
                                Notification.Builder notif = new Notification.Builder(FullscreenImage.this)
                                        .setContentTitle("Photo Saved")
                                        .setContentText(s.getTitle())
                                        .setSmallIcon(R.drawable.appicon)
                                        .setLargeIcon(b)
                                        .setStyle(new Notification.BigPictureStyle()
                                                .bigPicture(b));

                                NotificationManager mNotificationManager =
                                        (NotificationManager) getSystemService(FullscreenImage.this.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                                mNotificationManager.notify(1, notif.build());
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
    public boolean onTouchEvent(MotionEvent touchevent)
    {
        switch (touchevent.getAction())
        {
            // when user first touches the screen we get x and y coordinate
            case MotionEvent.ACTION_DOWN:
            {
                x1 = touchevent.getX();
                y1 = touchevent.getY();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                x2 = touchevent.getX();
                y2 = touchevent.getY();

                //if left to right sweep event on screen
                if (x1 < x2)
                {
                    finish();
                }

                // if right to left sweep event on screen
                if (x1 > x2)
                {
                    finish();
                }


                break;
            }
        }
        return false;
    }
    float x1,x2;
    float y1, y2;

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