package me.ccrama.redditslide.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import androidx.core.content.ContextCompat;

import com.google.common.io.Files;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
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

    public static final String EXTRA_SUBMISSION_TITLE = "submissionTitle";

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
                intent.getStringExtra(EXTRA_SUBMISSION_TITLE),
                intent.getStringExtra("saveToLocation")).executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class PollTask extends AsyncTask<Void, Void, Void> {

        public int id;
        private NotificationManager mNotifyManager;
        private NotificationCompat.Builder mBuilder;
        public String actuallyLoaded;
        private final int index;
        private final String subreddit;
        private final String submissionTitle;
        public String saveToLocation;
        private static final String RESERVED_CHARS = "[|\\\\?*<\":>+\\[\\]/']";


        public PollTask(String actuallyLoaded, int index, String subreddit, String submissionTitle, String saveToLocation) {
            this.actuallyLoaded = actuallyLoaded;
            this.index = index;
            this.subreddit = subreddit;
            this.submissionTitle = submissionTitle;
            this.saveToLocation = saveToLocation;
        }

        public void startNotification() {
            id = (int) (System.currentTimeMillis() / 1000);
            mNotifyManager = ContextCompat.getSystemService(
                    ImageDownloadNotificationService.this, NotificationManager.class);
            mBuilder = new NotificationCompat.Builder(getApplicationContext(), Reddit.CHANNEL_IMG);
            mBuilder.setContentTitle(getString(R.string.mediaview_notif_title))
                    .setContentText(getString(R.string.mediaview_notif_text))
                    .setSmallIcon(R.drawable.save_content);
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
                                            if (SettingValues.imageSubfolders && !subreddit.isEmpty()) {
                                                File directory = new File(getFolderPath() + getSubfolderPath());
                                                directory.mkdirs();
                                            }

                                            File f_out = new File(getFolderPath()
                                                    + getSubfolderPath()
                                                    + File.separator
                                                    + getFileName(finalUrl));

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
                                }, (imageUri, view, current, total) -> {
                                    latestPercentDone = (int) ((current / (float) total) * 100);
                                    if (percentDone <= latestPercentDone + 30
                                            || latestPercentDone == 100) { //Do every 10 percent
                                        percentDone = latestPercentDone;
                                        mBuilder.setProgress(100, percentDone, false);
                                        mNotifyManager.notify(id, mBuilder.build());
                                    }
                                });
            } catch (Exception e) {
                onError(e);
                Log.v(LogUtil.getTag(), "COULDN'T DOWNLOAD!");
            }
            return null;
        }

        private String getFolderPath() {
            if (saveToLocation != null) return saveToLocation;
            else return Reddit.appRestart.getString("imagelocation", "");
        }

        @NotNull
        private String getSubfolderPath() {
            return SettingValues.imageSubfolders && !subreddit.isEmpty() ? File.separator + subreddit : "";
        }

        public void onError(Exception e) {
            e.printStackTrace();
            mNotifyManager.cancel(id);
            stopSelf();
            try {
                Toast.makeText(getBaseContext(), "Error saving image", Toast.LENGTH_LONG).show();
            } catch (Exception ignored) {
            }
        }

        private String getFileName(String url) {
            String extension;
            try {
                URL parsedUrl = new URL(url);
                String path = parsedUrl.getPath();
                if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                    extension = path.substring(path.lastIndexOf("."));
                } else {
                    throw new MalformedURLException();
                }
            } catch (MalformedURLException e) {
                extension = ".png";
            }
            String fileIndex = index > -1 ? String.format(Locale.ENGLISH, "_%03d", index) : "";
            String title = submissionTitle != null && !submissionTitle.replaceAll("\\W+", "").trim().isEmpty() //Replace all non-alphanumeric characters to ensure a valid File URL
                    ? submissionTitle.replaceAll("\\W+", "") : UUID.randomUUID().toString();
            String finalURL = (title + fileIndex + extension)
                    .replaceAll(RESERVED_CHARS, "")
                    .trim();

            File file = new File(getFolderPath()
                    + getSubfolderPath()
                    + File.separator
                    + finalURL);
            int tries = 0;
            while(file.exists()) {
                tries += 1;
                finalURL = (title + fileIndex + "_" + tries + extension )
                        .replaceAll(RESERVED_CHARS, "")
                        .trim();
                file = new File(getFolderPath()
                        + getSubfolderPath()
                        + File.separator
                        + finalURL);
            }
            return finalURL;
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
                                    .setSmallIcon(R.drawable.save_content)
                                    .setLargeIcon(loadedImage)
                                    .setContentIntent(pContentIntent)
                                    .addAction(R.drawable.share, getString(R.string.share_image),
                                            pShareIntent)
                                    //maybe add this in later .addAction(R.drawable.edit, "EDIT", pEditIntent)
                                    .addAction(R.drawable.delete, getString(R.string.btn_delete),
                                            pDeleteIntent)
                                    .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(
                                            Bitmap.createScaledBitmap(loadedImage, 400, 400,
                                                    false)))
                                    .build();

                            NotificationManager mNotificationManager =
                                    ContextCompat.getSystemService(getApplicationContext(),
                                            NotificationManager.class);
                            notif.flags |= Notification.FLAG_AUTO_CANCEL;
                            if (mNotificationManager != null) {
                                mNotificationManager.cancel(id);
                                mNotificationManager.notify(id, notif);
                            }
                            loadedImage.recycle();
                            stopSelf();
                        }
                    });
        }


        private void saveImageGallery(final Bitmap bitmap, String URL) throws IOException {

            File f = new File(getFolderPath()
                    + File.separator
                    + getFileName(""));
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(
                    URL.endsWith("png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG,
                    100, out);
            {
                try {
                    out.close();
                    showNotifPhoto(f, bitmap);
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
