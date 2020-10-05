package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import java.io.File;

/**
 * Created by ccrama on 9/28/2015.
 */
public class DeleteFile extends Activity {

    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String PATH = "path";


    public static PendingIntent getDeleteIntent(int notificationId, Context context, String toDelete) {
        Intent intent = new Intent(context, DeleteFile.class);
        intent.putExtra(NOTIFICATION_ID , notificationId - 3);
        intent.putExtra(PATH, toDelete);
        return PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Intent intent = getIntent();

        NotificationManager manager = ContextCompat.getSystemService(this, NotificationManager.class);
        if (manager != null) {
            manager.cancel(intent.getIntExtra(NOTIFICATION_ID, -1));
        }

        Bundle extras = intent.getExtras();
        String image;

        if (extras != null) {
            image = getIntent().getStringExtra(PATH);
            image = image.replace("/external_files", Environment.getExternalStorageDirectory().toString());
            try {
                final String finalImage = image;
                MediaScannerConnection.scanFile(this, new String[] { image }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                if (uri != null) {
                                    getContentResolver().delete(uri, null,
                                            null);
                                }
                                new File(finalImage).delete();
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }        }
        finish();
    }
}
