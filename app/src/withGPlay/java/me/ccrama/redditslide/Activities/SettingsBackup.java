package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.FileUtil;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsBackup extends BaseActivityAnim
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    MaterialDialog progress;
    DriveFolder    appFolder;
    String         title;
    final private ResultCallback<DriveApi.MetadataBufferResult> newCallback  =
            new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {

                    int i = 0;
                    for (Metadata a : result.getMetadataBuffer()) {
                        i++;
                        title = a.getTitle();
                        new RetrieveDriveFileContentsAsyncTask(title).execute(a.getDriveId());


                    }
                    progress = new MaterialDialog.Builder(SettingsBackup.this)
                            .cancelable(false)
                            .title(R.string.backup_restoring)
                            .progress(false, i)
                            .build();
                    progress.show();


                }
            };
    final private ResultCallback<DriveApi.MetadataBufferResult> newCallback2 =
            new ResultCallback<DriveApi.MetadataBufferResult>() {
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
                            if (!s.contains("com.google") && !s.contains("cache") && !s.contains(
                                    "STACKTRACE")) {
                                title = s;
                                Drive.DriveApi.newDriveContents(mGoogleApiClient)
                                        .setResultCallback(
                                                new ResultCallback<DriveApi.DriveContentsResult>() {
                                                    @Override
                                                    public void onResult(
                                                            DriveApi.DriveContentsResult result) {
                                                        final String copy =
                                                                getApplicationInfo().dataDir
                                                                        + File.separator
                                                                        + "shared_prefs"
                                                                        + File.separator
                                                                        + s;
                                                        Log.v(LogUtil.getTag(),
                                                                "LOCATION IS " + copy);
                                                        if (!result.getStatus().isSuccess()) {
                                                            return;
                                                        }
                                                        final DriveContents driveContents =
                                                                result.getDriveContents();

                                                        // Perform I/O off the UI thread.
                                                        new Thread() {
                                                            @Override
                                                            public void run() {
                                                                // write content to DriveContents
                                                                OutputStream outputStream =
                                                                        driveContents.getOutputStream();
                                                                Writer writer =
                                                                        new OutputStreamWriter(
                                                                                outputStream);
                                                                String content = null;
                                                                File file = new File(
                                                                        copy); //for ex foo.txt
                                                                FileReader reader = null;
                                                                try {
                                                                    try {
                                                                        reader = new FileReader(
                                                                                file);
                                                                        char[] chars =
                                                                                new char[(int) file.length()];
                                                                        reader.read(chars);
                                                                        content = new String(chars);
                                                                        Log.v(LogUtil.getTag(),
                                                                                content);

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
                                                                    Log.e(LogUtil.getTag(),
                                                                            e.getMessage());
                                                                }

                                                                MetadataChangeSet changeSet =
                                                                        new MetadataChangeSet.Builder()
                                                                                .setTitle(s)
                                                                                .setMimeType(
                                                                                        "text/xml")
                                                                                .build();

                                                                // create a file on root folder
                                                                appFolder.createFile(
                                                                        mGoogleApiClient, changeSet,
                                                                        driveContents)
                                                                        .setResultCallback(
                                                                                fileCallback);
                                                            }
                                                        }.start();
                                                    }
                                                });
                            } else {
                                progress.setProgress(progress.getCurrentProgress() + 1);
                                if (progress.getCurrentProgress() == progress.getMaxProgress()) {

                                    new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                                            R.string.backup_success)
                                            .setPositiveButton(R.string.btn_close,
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog,
                                                                int which) {
                                                            finish();
                                                        }
                                                    })
                                            .setCancelable(false)
                                            .show();
                                }
                            }
                        }
                    }


                }
            };

    int errors;
    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback =
            new ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    progress.setProgress(progress.getCurrentProgress() + 1);
                    if (!result.getStatus().isSuccess()) {
                        errors += 1;
                        return;
                    }

                    if (progress.getCurrentProgress() == progress.getMaxProgress()) {

                        new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                                R.string.backup_success)
                                .setPositiveButton(R.string.btn_close,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        })
                                .setCancelable(false)
                                .show();
                    }
                }
            };
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 24) {
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            }
        } else if (requestCode == 42) {
            progress =
                    new MaterialDialog.Builder(SettingsBackup.this).title(R.string.backup_restoring)
                            .content(R.string.misc_please_wait)
                            .cancelable(false)
                            .progress(true, 1)
                            .build();
            progress.show();


            if (data != null) {
                Uri fileUri = data.getData();
                Log.v(LogUtil.getTag(), "WORKED! " + fileUri.toString());

                StringWriter fw = new StringWriter();
                try {
                    InputStream is = getContentResolver().openInputStream(fileUri);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    int c = reader.read();
                    while (c != -1) {
                        fw.write(c);
                        c = reader.read();
                    }
                    String read = fw.toString();
                    if (read.contains("Slide_backupEND>")) {

                        String[] files = read.split("END>");
                        progress.dismiss();
                        progress = new MaterialDialog.Builder(SettingsBackup.this).title(
                                R.string.backup_restoring)
                                .progress(false, files.length - 1)
                                .cancelable(false)
                                .build();
                        progress.show();
                        for (int i = 1; i < files.length; i++) {
                            String innerFile = files[i];
                            String t = innerFile.substring(6, innerFile.indexOf(">"));
                            innerFile = innerFile.substring(innerFile.indexOf(">") + 1,
                                    innerFile.length());

                            File newF = new File(getApplicationInfo().dataDir
                                    + File.separator
                                    + "shared_prefs"
                                    + File.separator
                                    + t);
                            Log.v(LogUtil.getTag(), "WRITING TO " + newF.getAbsolutePath());
                            try {
                                FileWriter newfw = new FileWriter(newF);
                                BufferedWriter bw = new BufferedWriter(newfw);
                                bw.write(innerFile);
                                bw.close();
                                progress.setProgress(progress.getCurrentProgress() + 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        new AlertDialogWrapper.Builder(SettingsBackup.this)
                                .setCancelable(false)
                                .setTitle(R.string.backup_restore_settings)
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        ProcessPhoenix.triggerRebirth(SettingsBackup.this);

                                    }
                                })
                                .setMessage(R.string.backup_restarting)
                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        ProcessPhoenix.triggerRebirth(SettingsBackup.this);
                                    }
                                })
                                .setPositiveButton(R.string.btn_ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ProcessPhoenix.triggerRebirth(SettingsBackup.this);

                                            }
                                        })
                                .setCancelable(false)
                                .show();

                    } else {
                        progress.hide();
                        new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                                getString(R.string.err_not_valid_backup))
                                .setMessage(getString(
                                        R.string.err_not_valid_backup_msg))
                                .setPositiveButton(R.string.btn_ok, null)
                                .setCancelable(false)
                                .show();
                    }
                } catch (Exception e) {
                    progress.hide();
                    e.printStackTrace();
                    new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                            getString(R.string.err_file_not_found))
                            .setMessage(getString(
                                    R.string.err_file_not_found_msg))
                            .setPositiveButton(R.string.btn_ok, null)
                            .setCancelable(false)
                            .show();
                }
            } else {
                progress.dismiss();
                new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                        getString(R.string.err_file_not_found))
                        .setMessage(
                                getString(R.string.err_file_not_found_msg))
                        .setPositiveButton(R.string.btn_ok, null)
                        .setCancelable(false)
                        .show();
            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (SettingValues.isPro) mGoogleApiClient.connect();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_sync);
        setupAppBar(R.id.toolbar, R.string.settings_title_backup, true, true);

        if (SettingValues.isPro) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mGoogleApiClient.isConnected()) {
                        new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                                R.string.general_confirm)
                                .setMessage(R.string.backup_confirm)
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                    }
                                })
                                .setPositiveButton(R.string.btn_ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                                File prefsdir = new File(getApplicationInfo().dataDir, "shared_prefs");

                                                if (prefsdir.exists() && prefsdir.isDirectory()) {

                                                    String[] list = prefsdir.list();
                                                    progress = new MaterialDialog.Builder(
                                                            SettingsBackup.this).title(
                                                            R.string.backup_backing_up)
                                                            .progress(false, list.length)
                                                            .cancelable(false)
                                                            .build();
                                                    progress.show();
                                                    appFolder.listChildren(mGoogleApiClient)
                                                            .setResultCallback(newCallback2);

                                                }
                                            }
                                        })
                                .setNegativeButton(R.string.btn_no, null)
                                .setCancelable(false)
                                .show();
                    } else {
                        new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                                R.string.settings_google)
                                .setMessage(R.string.settings_google_msg)
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                    }
                                })
                                .setPositiveButton(R.string.btn_ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                            }
                                        })
                                .setCancelable(false)
                                .show();
                    }
                }
            });


            findViewById(R.id.restore).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mGoogleApiClient.isConnected()) {
                        new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                                R.string.general_confirm)
                                .setMessage(R.string.backup_restore_confirm)
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                    }
                                })
                                .setPositiveButton(R.string.btn_ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                                progress = new MaterialDialog.Builder(SettingsBackup.this).title(
                                                        R.string.backup_restoring)
                                                        .content(R.string.misc_please_wait)
                                                        .cancelable(false)
                                                        .progress(true, 1)
                                                        .build();
                                                progress.show();
                                                appFolder.listChildren(mGoogleApiClient).setResultCallback(newCallback);
                                            }
                                        })
                                .setNegativeButton(R.string.btn_no, null)
                                .setCancelable(false)
                                .show();
                    } else {
                        new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                                R.string.settings_google)
                                .setMessage(R.string.settings_google_msg)
                                //avoid that the dialog can be closed
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                    }
                                })
                                .setPositiveButton(R.string.btn_ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                            }
                                        })
                                .setCancelable(false)
                                .show();
                    }

                }
            });
            findViewById(R.id.backfile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                            getString(me.ccrama.redditslide.R.string.include_personal_info))
                            .setMessage(getString(
                                    me.ccrama.redditslide.R.string.include_personal_info_msg))
                            .setPositiveButton(R.string.btn_yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            backupToDir(false);
                                        }
                                    })
                            .setNegativeButton(R.string.btn_no,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            backupToDir(true);
                                        }
                                    })
                            .setNeutralButton(R.string.btn_cancel, null)
                            .setCancelable(false)
                            .show();
                }
            });


            findViewById(R.id.restorefile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    String[] mimeTypes = {"text/plain"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    startActivityForResult(intent, 42);
                }
            });
        } else {
            new AlertDialogWrapper.Builder(this).setTitle("Settings Backup is a Pro feature")
                    .setMessage(R.string.pro_upgrade_msg)
                    //avoid that the dialog can be closed
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .setPositiveButton(R.string.btn_yes_exclaim,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                                "market://details?id=" + getString(R.string.ui_unlock_package))));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                                "http://play.google.com/store/apps/details?id=" + getString(R.string.ui_unlock_package))));
                                    }
                                }
                            })
                    .setNegativeButton(R.string.btn_no_danks,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();
                                }
                            })
                    .setCancelable(false)
                    .show();
        }
    }

    File file;

    public void backupToDir(final boolean personal) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                progress = new MaterialDialog.Builder(SettingsBackup.this).cancelable(false)
                        .title(R.string.backup_backing_up)
                        .progress(false, 40)
                        .cancelable(false)
                        .build();

                progress.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                File prefsdir = new File(getApplicationInfo().dataDir, "shared_prefs");

                if (prefsdir.exists() && prefsdir.isDirectory()) {
                    String[] list = prefsdir.list();

                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .mkdirs();
                    File backedup = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS)
                            + File.separator
                            + "Slide"
                            + new SimpleDateFormat("-yyyy-MM-dd-HH-mm-ss").format(
                            Calendar.getInstance().getTime())
                            + (!personal ? "-personal" : "")
                            + ".txt");

                    file = backedup;
                    FileWriter fw = null;
                    try {
                        backedup.createNewFile();
                        fw = new FileWriter(backedup);
                        fw.write("Slide_backupEND>");
                        for (String s : list) {

                            if (!s.contains("cache") && !s.contains("ion-cookies") && !s.contains(
                                    "albums") && !s.contains("STACKTRACE") && !s.contains(
                                    "com.google") && (!personal ||
                                    (!s.contains("SUBSNEW")
                                    && !s.contains("appRestart")
                                    && !s.contains("AUTH")
                                    && !s.contains("TAGS")
                                    && !s.contains("SEEN")
                                    && !s.contains("HIDDEN")
                                    && !s.contains("HIDDEN_POSTS")))) {
                                FileReader fr = null;
                                try {
                                    fr = new FileReader(new File(prefsdir + File.separator + s));
                                    int c = fr.read();
                                    fw.write("<START" + new File(s).getName() + ">");
                                    while (c != -1) {
                                        fw.write(c);
                                        c = fr.read();
                                    }
                                    fw.write("END>");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    close(fr);
                                }
                            }

                        }
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        //todo error
                    } finally {
                        close(fw);
                    }

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progress.dismiss();
                new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                        getString(me.ccrama.redditslide.R.string.backup_complete))
                        .setMessage(
                                getString(me.ccrama.redditslide.R.string.backup_saved_downloads))
                        .setPositiveButton(R.string.btn_view,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = FileUtil.getFileIntent(file,
                                                new Intent(Intent.ACTION_VIEW),
                                                SettingsBackup.this);
                                        if (intent.resolveActivityInfo(getPackageManager(), 0)
                                                != null) {
                                            startActivity(
                                                    Intent.createChooser(intent, "View backup"));
                                        } else {
                                            Snackbar s =
                                                    Snackbar.make(findViewById(R.id.restorefile),
                                                            "No file explorer found, file located at "
                                                                    + file.getAbsolutePath(),
                                                            Snackbar.LENGTH_INDEFINITE);
                                            View view = s.getView();
                                            TextView tv = (TextView) view.findViewById(
                                                    com.google.android.material.R.id.snackbar_text);
                                            tv.setTextColor(Color.WHITE);
                                            s.show();
                                        }
                                    }
                                })
                        .setNegativeButton(R.string.btn_close, null)
                        .setCancelable(false)
                        .show();
            }
        }.execute();

    }

    public static void close(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
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
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            apiAvailability.getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
        }
    }


    final private class RetrieveDriveFileContentsAsyncTask
            extends AsyncTask<DriveId, Boolean, String> {


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
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(driveContents.getInputStream()));
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

            File newF = new File(getApplicationInfo().dataDir
                    + File.separator
                    + "shared_prefs"
                    + File.separator
                    + t);
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


                new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                        R.string.backup_restore_settings)
                        .setMessage(R.string.backup_restarting)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                ProcessPhoenix.triggerRebirth(SettingsBackup.this);
                            }
                        })
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ProcessPhoenix.triggerRebirth(SettingsBackup.this);
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
            if (result == null) {
                //showMessage("Error while reading from the file");

                return;
            }
            Log.v(LogUtil.getTag(), "File contents: " + result);
        }
    }

}