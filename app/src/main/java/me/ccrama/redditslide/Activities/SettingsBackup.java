package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsBackup extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    MaterialDialog progress;
    DriveFolder appFolder;
    String title;
    final private ResultCallback<DriveApi.MetadataBufferResult> newCallback = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(DriveApi.MetadataBufferResult result) {

            int i = 0;
            for (Metadata a : result.getMetadataBuffer()) {
                i++;
                title = a.getTitle();
                new RetrieveDriveFileContentsAsyncTask(title).execute(a.getDriveId());


            }
            progress = new MaterialDialog.Builder(SettingsBackup.this).title(R.string.backup_restoring).progress(false, i).build();
            progress.show();


        }
    };
    final private ResultCallback<DriveApi.MetadataBufferResult> newCallback2 = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(DriveApi.MetadataBufferResult result) {

            int i = 0;
            for (Metadata a : result.getMetadataBuffer()) {
                i++;
                title = a.getTitle();
                DriveFile file = a.getDriveId().asDriveFile();

                file.delete(mGoogleApiClient);

            }
            Drive.DriveApi.requestSync(mGoogleApiClient);

            File prefsdir = new File(getApplicationInfo().dataDir, "shared_prefs");

            if (prefsdir.exists() && prefsdir.isDirectory()) {

                String[] list = prefsdir.list();

                for (final String s : list) {
                    if (!s.contains("com.google")) {
                        title = s;
                        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                                    @Override
                                    public void onResult(DriveApi.DriveContentsResult result) {
                                        final String copy = getApplicationInfo().dataDir + File.separator + "shared_prefs" + File.separator + s;
                                        Log.v(LogUtil.getTag(), "LOCATION IS " + copy);
                                        if (!result.getStatus().isSuccess()) {
                                            return;
                                        }
                                        final DriveContents driveContents = result.getDriveContents();

                                        // Perform I/O off the UI thread.
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                // write content to DriveContents
                                                OutputStream outputStream = driveContents.getOutputStream();
                                                Writer writer = new OutputStreamWriter(outputStream);
                                                String content = null;
                                                File file = new File(copy); //for ex foo.txt
                                                FileReader reader = null;
                                                try {
                                                    try {
                                                        reader = new FileReader(file);
                                                        char[] chars = new char[(int) file.length()];
                                                        reader.read(chars);
                                                        content = new String(chars);
                                                        Log.v(LogUtil.getTag(), content);

                                                        reader.close();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    } finally {
                                                        if (reader != null) {
                                                            reader.close();
                                                        }
                                                    }

                                                    writer.write(content);
                                                    writer.close();
                                                } catch (Exception e) {
                                                    Log.e(LogUtil.getTag(), e.getMessage());
                                                }

                                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                                        .setTitle(s)
                                                        .setMimeType("text/xml")
                                                        .build();

                                                // create a file on root folder
                                                appFolder
                                                        .createFile(mGoogleApiClient, changeSet, driveContents)
                                                        .setResultCallback(fileCallback);
                                            }
                                        }.start();
                                    }
                                });
                    } else {
                        progress.setProgress(progress.getCurrentProgress() + 1);
                        if (progress.getCurrentProgress() == progress.getMaxProgress()) {
                            new AlertDialogWrapper.Builder(SettingsBackup.this)
                                    .setTitle(R.string.backup_success)
                                    .setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    }).show();
                        }
                    }
                }
            }



        }
    };

    int errors;
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    progress.setProgress(progress.getCurrentProgress() + 1);
                    if (!result.getStatus().isSuccess()) {
                        errors += 1;
                        return;
                    }

                    if (progress.getCurrentProgress() == progress.getMaxProgress()) {
                        new AlertDialogWrapper.Builder(SettingsBackup.this)
                                .setTitle(R.string.backup_success)
                                .setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }).show();
                    }
                }
            };
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 24) {
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (SettingValues.tabletUI)
            mGoogleApiClient.connect();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_sync);
        setupAppBar(R.id.toolbar, R.string.settings_title_backup, true, true);

        if (SettingValues.tabletUI) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mGoogleApiClient.isConnected()) {

                        File prefsdir = new File(getApplicationInfo().dataDir, "shared_prefs");

                        if (prefsdir.exists() && prefsdir.isDirectory()) {

                            String[] list = prefsdir.list();
                            progress = new MaterialDialog.Builder(SettingsBackup.this).title(R.string.backup_backing_up).progress(false, list.length).cancelable(false).build();
                            progress.show();
                            appFolder.listChildren(mGoogleApiClient).setResultCallback(newCallback2);

                        }

                    } else {
                        new AlertDialogWrapper.Builder(SettingsBackup.this)
                                .setTitle(R.string.settings_google)
                                .setMessage(R.string.settings_google_msg)
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                    }
                                })
                                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                    }
                                }).show();
                    }
                }
            });


            findViewById(R.id.restore).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mGoogleApiClient.isConnected()) {
                        progress = new MaterialDialog.Builder(SettingsBackup.this)
                                .title(R.string.backup_restoring)
                                .content(R.string.misc_please_wait)
                                .cancelable(false)
                                .progress(true, 1)
                                .build();
                        progress.show();
                        appFolder.listChildren(mGoogleApiClient).setResultCallback(newCallback);
                    } else {
                        new AlertDialogWrapper.Builder(SettingsBackup.this)
                                .setTitle(R.string.settings_google)
                                .setMessage(R.string.settings_google_msg)
                                        //avoid that the dialog can be closed
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                    }
                                })
                                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                    }
                                }).show();
                    }

                }
            });
        } else {
            new AlertDialogWrapper.Builder(SettingsBackup.this)
                    .setTitle(R.string.general_pro)
                    .setMessage(R.string.general_pro_msg)
                            //avoid that the dialog can be closed
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                            }
                        }
                    }).setNegativeButton(R.string.btn_no_danks, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            }).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        appFolder = Drive.DriveApi.getAppFolder(mGoogleApiClient);
        Drive.DriveApi.requestSync(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 24);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }



    final private class RetrieveDriveFileContentsAsyncTask extends AsyncTask<DriveId, Boolean, String> {


        String t;

        public RetrieveDriveFileContentsAsyncTask(String title) {
            t = title;
        }

        @Override
        protected String doInBackground(DriveId... params) {
            String contents = null;
            DriveFile file = params[0].asDriveFile();
            DriveApi.DriveContentsResult driveContentsResult =
                    file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return null;
            }


            DriveContents driveContents = driveContentsResult.getDriveContents();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(driveContents.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                contents = builder.toString();
            } catch (IOException e) {
                Log.e(LogUtil.getTag(), "IOException while reading from the stream", e);
            }

            File newF = new File(getApplicationInfo().dataDir + File.separator + "shared_prefs" + File.separator + t);
            Log.v(LogUtil.getTag(), "WRITING TO " + newF.getAbsolutePath());


            try {
                FileWriter fw = new FileWriter(newF);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(contents);
                bw.close();
                progress.setProgress(progress.getCurrentProgress() + 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            driveContents.discard(mGoogleApiClient);
            return contents;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (progress.getCurrentProgress() == progress.getMaxProgress()) {
                progress.dismiss();
                new AlertDialogWrapper.Builder(SettingsBackup.this)
                        .setTitle(R.string.backup_restore_settings)
                        .setMessage(R.string.backup_restarting).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Reddit.forceRestart(SettingsBackup.this, true);
                    }
                }).setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Reddit.forceRestart(SettingsBackup.this, true);

                    }
                }).show();
            }
            if (result == null) {
                //showMessage("Error while reading from the file");

                return;
            }
            Log.v(LogUtil.getTag(), "File contents: " + result);
        }
    }

}