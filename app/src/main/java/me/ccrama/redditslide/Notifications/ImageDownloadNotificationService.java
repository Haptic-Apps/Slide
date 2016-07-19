package me.ccrama.redditslide.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by Carlos on 7/15/2016.
 */
public class ImageDownloadNotificationService extends Service {

    String actuallyLoaded;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {
        actuallyLoaded = intent.getStringExtra("actuallyLoaded");
        if (actuallyLoaded.contains("imgur.com") && (!actuallyLoaded.contains(".png") || !actuallyLoaded.contains(".jpg"))) {
            actuallyLoaded = actuallyLoaded + ".png";
        }
        new PollTask().execute();
    }


    private class PollTask extends AsyncTask<Void, Void, Void> {

        public int id;
        private NotificationManager mNotifyManager;
        private NotificationCompat.Builder mBuilder;

        public void startNotification() {
            id = new Random(System.currentTimeMillis()).nextInt();
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(getApplicationContext());
            mBuilder.setContentTitle("Downloading image...")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.save);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startNotification();
            mBuilder.setProgress(100, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = actuallyLoaded;
            final String finalUrl1 = url;
            final String finalUrl = actuallyLoaded;
            try {
                ((Reddit) getApplication()).getImageLoader()
                        .loadImage(finalUrl, null, null, new SimpleImageLoadingListener() {

                            @Override
                            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                                try {
                                    saveImageGallery(loadedImage, finalUrl1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new ImageLoadingProgressListener() {
                            @Override
                            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                                if ((current / total * 100)%10 == 0) {
                                    mBuilder.setProgress(total, current, false);
                                    mNotifyManager.notify(id, mBuilder.build());
                                }
                            }
                        });
            } catch (Exception e) {
                Log.v(LogUtil.getTag(), "COULDN'T DOWNLOAD!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // handle your data
            super.onPostExecute(result);
            stopSelf();
        }

        public void showNotifPhoto(final File localAbsoluteFilePath, final Bitmap loadedImage) {
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{localAbsoluteFilePath.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {

                    final Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                    shareIntent.setDataAndType(Uri.fromFile(localAbsoluteFilePath), "image/*");
                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), id, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                    Notification notif = new NotificationCompat.Builder(getApplicationContext())
                            .setContentTitle(getString(R.string.info_photo_saved))
                            .setSmallIcon(R.drawable.savecontent)
                            .setLargeIcon(loadedImage)
                            .setContentIntent(contentIntent)
                            .setStyle(new NotificationCompat.BigPictureStyle()
                                    .bigPicture(loadedImage)).build();

                    NotificationManager mNotificationManager =
                            (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                    mNotificationManager.notify(id, notif);
                    loadedImage.recycle();
                }
            });
        }


        private void saveImageGallery(final Bitmap bitmap, String URL) throws IOException {

            File f = new File(Reddit.appRestart.getString("imagelocation", "") + File.separator + UUID.randomUUID().toString() + ".png");

            FileOutputStream out = null;
            f.createNewFile();
            out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            {
                try {
                    if (out != null) {
                        out.close();
                        showNotifPhoto(f, bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @Override
    public void onStart(Intent intent, int startId) {
        handleIntent(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }
}