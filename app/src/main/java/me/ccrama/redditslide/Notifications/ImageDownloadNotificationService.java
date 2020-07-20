package me.ccrama.redditslide.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.common.io.Files;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import me.ccrama.redditslide.Activities.DeleteFile;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.FileUtil;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by Carlos on 7/15/2016.
 */
public class ImageDownloadNotificationService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {
        String actuallyLoaded = intent.getStringExtra("actuallyLoaded");
        if (actuallyLoaded.contains("imgur.com") && (!actuallyLoaded.contains(".png")
                && !actuallyLoaded.contains(".jpg"))) {
            actuallyLoaded = actuallyLoaded + ".png";
        }
        String subreddit = "";
        if (intent.hasExtra("subreddit")) {
            subreddit = intent.getStringExtra("subreddit");
        }
        new PollTask(actuallyLoaded,
                intent.getIntExtra("index", -1),
                subreddit,
                intent.getStringExtra("saveToLocation")).executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class PollTask extends AsyncTask<Void, Void, Void> {

        public  int                        id;
        private NotificationManager        mNotifyManager;
        private NotificationCompat.Builder mBuilder;
        public  String                     actuallyLoaded;
        private int                        index;
        private String                     subreddit;
        public  String                     saveToLocation;


        public PollTask(String actuallyLoaded, int index, String subreddit, String saveToLocation) {
            this.actuallyLoaded = actuallyLoaded;
            this.index = index;
            this.subreddit = subreddit;
            this.saveToLocation = saveToLocation;
        }

        public void startNotification() {
            id = (int) (System.currentTimeMillis() / 1000);
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(getApplicationContext(), Reddit.CHANNEL_IMG);
            mBuilder.setContentTitle(getString(R.string.mediaview_notif_title))
                    .setContentText(getString(R.string.mediaview_notif_text))
                    .setSmallIcon(R.drawable.save_png);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                Toast.makeText(ImageDownloadNotificationService.this, "Downloading image...",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {

            }
            startNotification();
            mBuilder.setProgress(100, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
        }

        int percentDone, latestPercentDone;

        @Override
        protected Void doInBackground(Void... params) {
            final String finalUrl = actuallyLoaded;
            try {
                ((Reddit) getApplication()).getImageLoader()
                        .loadImage(finalUrl, null, new DisplayImageOptions.Builder().imageScaleType(
                                ImageScaleType.NONE).cacheInMemory(false).cacheOnDisk(true).build(),
                                new SimpleImageLoadingListener() {

                                    @Override
                                    public void onLoadingComplete(String imageUri, View view,
                                            final Bitmap loadedImage) {
                                        File f = ((Reddit) getApplicationContext()).getImageLoader()
                                                .getDiskCache()
                                                .get(finalUrl);
                                        if (f != null && f.exists()) {
                                            File f_out = null;
                                            try {
                                                if(SettingValues.imageSubfolders && !subreddit.isEmpty()){
                                                    File directory;
                                                    if (saveToLocation != null) {
                                                        directory = new File(new File(saveToLocation,
                                                                "")
                                                                + (SettingValues.imageSubfolders && !subreddit.isEmpty() ? File.separator + subreddit : ""));
                                                    } else {
                                                        directory = new File( Reddit.appRestart.getString("imagelocation",
                                                                "")
                                                                + (SettingValues.imageSubfolders && !subreddit.isEmpty() ?File.separator + subreddit : ""));
                                                    }
                                                    directory.mkdirs();
                                                }
                                                if (saveToLocation != null) {
                                                    f_out = new File(
                                                            saveToLocation
                                                                    + (SettingValues.imageSubfolders && !subreddit.isEmpty() ?File.separator + subreddit : "")
                                                                    + File.separator
                                                                    + (index > -1 ? String.format(
                                                                    "%03d_", index) : "")
                                                                    + getFileName(new URL(finalUrl)));
                                                } else {
                                                    f_out = new File(
                                                            Reddit.appRestart.getString("imagelocation",
                                                                    "")
                                                                    + (SettingValues.imageSubfolders && !subreddit.isEmpty() ?File.separator + subreddit : "")
                                                                    + File.separator
                                                                    + (index > -1 ? String.format(
                                                                    "%03d_", index) : "")
                                                                    + getFileName(new URL(finalUrl)));
                                                }
                                            } catch (MalformedURLException e) {
                                                if (saveToLocation != null) {
                                                    f_out = new File(
                                                            saveToLocation
                                                                    + (SettingValues.imageSubfolders && !subreddit.isEmpty() ?File.separator + subreddit : "")
                                                                    + File.separator
                                                                    + (index > -1 ? String.format(
                                                                    "%03d_", index) : "")
                                                                    + UUID.randomUUID().toString()
                                                                    + ".png");
                                                } else {
                                                    f_out = new File(
                                                            Reddit.appRestart.getString("imagelocation",
                                                                    "")
                                                                    + (SettingValues.imageSubfolders && !subreddit.isEmpty() ?File.separator + subreddit : "")
                                                                    + File.separator
                                                                    + (index > -1 ? String.format(
                                                                    "%03d_", index) : "")
                                                                    + UUID.randomUUID().toString()
                                                                    + ".png");
                                                }
                                            }
                                            LogUtil.v("F out is " + f_out.toString());
                                            try {
                                                Files.copy(f, f_out);
                                                showNotifPhoto(f_out, loadedImage);
                                            } catch (IOException e) {
                                                try {
                                                    saveImageGallery(loadedImage, finalUrl);
                                                } catch (IOException ignored) {
                                                    onError(e);
                                                }
                                            }
                                        } else {
                                            try {
                                                saveImageGallery(loadedImage, finalUrl);
                                            } catch (IOException e) {
                                                onError(e);
                                            }
                                        }
                                    }
                                }, new ImageLoadingProgressListener() {
                                    @Override
                                    public void onProgressUpdate(String imageUri, View view,
                                            int current, int total) {
                                        latestPercentDone = (int) ((current / (float) total) * 100);
                                        if (percentDone <= latestPercentDone + 30
                                                || latestPercentDone == 100) { //Do every 10 percent
                                            percentDone = latestPercentDone;
                                            mBuilder.setProgress(100, percentDone, false);
                                            mNotifyManager.notify(id, mBuilder.build());
                                        }


                                    }
                                });
            } catch (Exception e) {
                onError(e);
                Log.v(LogUtil.getTag(), "COULDN'T DOWNLOAD!");
            }
            return null;
        }

        public void onError(Exception e){
            e.printStackTrace();
            mNotifyManager.cancel(id);
            stopSelf();
            try {
                Toast.makeText(getBaseContext(), "Error saving image", Toast.LENGTH_LONG).show();
            } catch (Exception ignored) {}
        }

        private String getFileName(URL url) {
            if (url == null) return null;
            String path = url.getPath();
            String end = path.substring(path.lastIndexOf("/") + 1);
            if (!end.endsWith(".png") && !end.endsWith(".jpg") && !end.endsWith(".jpeg")) {
                end = end + ".png";
            }
            return end;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mNotifyManager.cancel(id);
        }

        public void showNotifPhoto(final File localAbsoluteFilePath, final Bitmap loadedImage) {
            MediaScannerConnection.scanFile(getApplicationContext(),
                    new String[]{localAbsoluteFilePath.getAbsolutePath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            PendingIntent pContentIntent, pShareIntent, pDeleteIntent, pEditIntent;
                            Uri photoURI = FileUtil.getFileUri(localAbsoluteFilePath,
                                    ImageDownloadNotificationService.this);

                            {
                                final Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                                shareIntent.setDataAndType(photoURI, "image/*");
                                List<ResolveInfo> resInfoList =
                                        getPackageManager().queryIntentActivities(shareIntent,
                                                PackageManager.MATCH_DEFAULT_ONLY);
                                for (ResolveInfo resolveInfo : resInfoList) {
                                    String packageName = resolveInfo.activityInfo.packageName;
                                    grantUriPermission(packageName, photoURI,
                                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                                    | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }

                                pContentIntent =
                                        PendingIntent.getActivity(getApplicationContext(), id,
                                                shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                            }

                            {
                                final Intent shareIntent = new Intent(Intent.ACTION_EDIT);
                                shareIntent.setDataAndType(photoURI, "image/*");
                                List<ResolveInfo> resInfoList =
                                        getPackageManager().queryIntentActivities(shareIntent,
                                                PackageManager.MATCH_DEFAULT_ONLY);
                                for (ResolveInfo resolveInfo : resInfoList) {
                                    String packageName = resolveInfo.activityInfo.packageName;
                                    grantUriPermission(packageName, photoURI,
                                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                                    | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }

                                pEditIntent =
                                        PendingIntent.getActivity(getApplicationContext(), id + 1,
                                                shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                            }

                            {
                                final Intent shareIntent = new Intent(Intent.ACTION_SEND);

                                List<ResolveInfo> resInfoList =
                                        getPackageManager().queryIntentActivities(shareIntent,
                                                PackageManager.MATCH_DEFAULT_ONLY);
                                for (ResolveInfo resolveInfo : resInfoList) {
                                    String packageName = resolveInfo.activityInfo.packageName;
                                    grantUriPermission(packageName, photoURI,
                                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                                    | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }

                                shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
                                shareIntent.setDataAndType(photoURI,
                                        getContentResolver().getType(photoURI));

                                pShareIntent =
                                        PendingIntent.getActivity(getApplicationContext(), id + 2,
                                                Intent.createChooser(shareIntent,
                                                        getString(R.string.misc_img_share)),
                                                PendingIntent.FLAG_CANCEL_CURRENT);
                            }

                            {
                                pDeleteIntent =
                                        DeleteFile.getDeleteIntent(id + 3, getApplicationContext(),
                                                photoURI.getPath());
                            }


                            Notification notif = new NotificationCompat.Builder(
                                    getApplicationContext(), Reddit.CHANNEL_IMG).setContentTitle(
                                    getString(R.string.info_photo_saved))
                                    .setSmallIcon(R.drawable.save_png)
                                    .setLargeIcon(loadedImage)
                                    .setContentIntent(pContentIntent)
                                    .addAction(R.drawable.ic_share, getString(R.string.share_image),
                                            pShareIntent)
                                    //maybe add this in later .addAction(R.drawable.edit, "EDIT", pEditIntent)
                                    .addAction(R.drawable.ic_delete, getString(R.string.btn_delete),
                                            pDeleteIntent)
                                    .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(
                                            Bitmap.createScaledBitmap(loadedImage, 400, 400,
                                                    false)))
                                    .build();

                            NotificationManager mNotificationManager =
                                    (NotificationManager) getApplicationContext().getSystemService(
                                            NOTIFICATION_SERVICE);
                            notif.flags |= Notification.FLAG_AUTO_CANCEL;
                            mNotificationManager.notify(id, notif);
                            loadedImage.recycle();
                            stopSelf();
                        }
                    });
        }


        private void saveImageGallery(final Bitmap bitmap, String URL) throws IOException {

            File f = new File(
                    Reddit.appRestart.getString("imagelocation", "") + File.separator + (index > -1
                            ? String.format("%03d_", index) : "") + UUID.randomUUID().toString() + (
                            URL.endsWith("png") ? ".png" : ".jpg"));

            FileOutputStream out = null;
            f.createNewFile();
            out = new FileOutputStream(f);
            bitmap.compress(
                    URL.endsWith("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG,
                    100, out);
            {
                try {
                    if (out != null) {
                        out.close();
                        showNotifPhoto(f, bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    onError(e);
                }
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }
}