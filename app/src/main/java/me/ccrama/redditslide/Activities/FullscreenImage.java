package me.ccrama.redditslide.Activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;


/**
 * Created by ccrama on 3/5/2015.
 */
public class FullscreenImage extends FullScreenActivity {


    String toReturn;

    public void onCreate(Bundle savedInstanceState) {


        requestWindowFeature(Window.FEATURE_NO_TITLE);


        super.onCreate(savedInstanceState);
        if(Reddit.fullscreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
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
                .loadImage(url, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        i.setImage(ImageSource.bitmap(loadedImage));
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

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
                findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v2) {


                        try {
                            ((Reddit) getApplication()).getImageLoader()
                                    .loadImage(finalUrl, new SimpleImageLoadingListener() {
                                        @Override
                                        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                                            String localAbsoluteFilePath = saveImageGallery(loadedImage);

                                            if (localAbsoluteFilePath != null) {
                                                MediaScannerConnection.scanFile(FullscreenImage.this, new String[]{localAbsoluteFilePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                                    public void onScanCompleted(String path, Uri uri) {
                                                        Notification notif = new NotificationCompat.Builder(FullscreenImage.this)
                                                                .setContentTitle(getString(R.string.info_photo_saved))
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

    private void showShareDialog(final String url){
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
                        String localAbsoluteFilePath = saveImageLocally(loadedImage);

                        if (!localAbsoluteFilePath.isEmpty() && localAbsoluteFilePath != null) {

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

    private String saveImageGallery(final Bitmap _bitmap) {

        File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "Slide");
        File outputFile = null;
        try {
            outputFile = File.createTempFile("slide", ".png", outputDir);
            FileOutputStream out = new FileOutputStream(outputFile);
            _bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            return outputFile.getAbsolutePath();
        } catch (Exception e1) {

            new AlertDialogWrapper.Builder(FullscreenImage.this).setTitle(R.string.err_title)
                    .setMessage(R.string.misc_retry)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toReturn = saveImageGallery(_bitmap);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toReturn = null;
                            dialog.dismiss();
                        }
                    }).show();
            return toReturn;
        }

    }

    private String saveImageLocally(final Bitmap _bitmap) {

        File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outputFile = null;
        try {
            outputFile = File.createTempFile("tmp", ".png", outputDir);

            FileOutputStream out = new FileOutputStream(outputFile);
            _bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

            return outputFile.getAbsolutePath();

        } catch (Exception e) {
            new AlertDialogWrapper.Builder(FullscreenImage.this).setTitle(R.string.err_title)
                    .setMessage(R.string.misc_retry)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toReturn = saveImageLocally(_bitmap);
                        }
                    })
                    .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toReturn = null;
                        }
                    }).show();
            return toReturn;

        }

    }
}