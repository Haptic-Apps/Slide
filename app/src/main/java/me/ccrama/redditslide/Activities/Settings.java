package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

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
import com.rey.material.widget.Slider;

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

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Settings extends BaseActivityNoAnim implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    MaterialDialog progress;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            Intent i = new Intent(Settings.this, Settings.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            overridePendingTransition(0, 0);

            finish();
            overridePendingTransition(0, 0);


        } else if (requestCode == 24) {
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    String title;


    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                    Log.v("Slide", "Created a file with content: " + result.getDriveFile().getDriveId());

                }
            };
    final private ResultCallback<DriveApi.MetadataBufferResult> newCallback = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(DriveApi.MetadataBufferResult result) {

            int i = 0;
            for (Metadata a : result.getMetadataBuffer()) {
                i++;
                title = a.getTitle();
                Log.v("Slide", title);
                new RetrieveDriveFileContentsAsyncTask(title).execute(a.getDriveId());


            }
            progress = new MaterialDialog.Builder(Settings.this).title("Writing Settings").progress(false, i).build();
            progress.show();


        }
    };

    final private class RetrieveDriveFileContentsAsyncTask extends AsyncTask<DriveId, Boolean, String> {


        String t;
        public RetrieveDriveFileContentsAsyncTask(String title){
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
                Log.e("Slide", "IOException while reading from the stream", e);
            }

            File newF = new File(getApplicationInfo().dataDir + File.separator + "shared_prefs" + File.separator + t);
            Log.v("Slide", "WRITING TO " + newF.getAbsolutePath());


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
            if(progress.getCurrentProgress() == progress.getMaxProgress()){
                progress.dismiss();
                new AlertDialogWrapper.Builder(Settings.this).setTitle("Setting Restore Successful!").setMessage("App will now restart.").setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Reddit.forceRestart(Settings.this);
                    }
                }).setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Reddit.forceRestart(Settings.this);

                    }
                }).show();
            }
            if (result == null) {
                //showMessage("Error while reading from the file");

                return;
            }
            Log.v("Slide", "File contents: " + result);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings);
        Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            Settings.this.setTaskDescription(new ActivityManager.TaskDescription("Settings", ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));


        }

        if (Reddit.tabletUI) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        findViewById(R.id.pro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                }
            }
        });
        findViewById(R.id.general).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsGeneral.class);
                startActivityForResult(i, 2);
            }
        });
        findViewById(R.id.subtheme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsSubreddit.class);
                startActivityForResult(i, 2);
            }
        });
        findViewById(R.id.notifications).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Settings.this);
                final Slider landscape = (Slider) dialoglayout.findViewById(R.id.landscape);

                final CheckBox checkBox = (CheckBox) dialoglayout.findViewById(R.id.load);
                if (Reddit.notificationTime == -1) {
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                    landscape.setValue(Reddit.notificationTime / 15, false);
                    checkBox.setText("Check for new messages every " + Inbox.getTime(Reddit.notificationTime));

                }
                landscape.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                    @Override
                    public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                        checkBox.setText("Check for new messages every " + Inbox.getTime(i1 * 15));
                    }
                });
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!isChecked) {
                            Reddit.notificationTime = -1;
                            Reddit.seen.edit().putInt("notificationOverride", -1).apply();
                            Reddit.notifications.cancel(getApplication());
                        } else {
                            Reddit.notificationTime = 15;
                            landscape.setValue(1, true);
                        }
                    }
                });
                dialoglayout.findViewById(R.id.title).setBackgroundColor(Pallete.getDefaultColor());
                //todo final Slider portrait = (Slider) dialoglayout.findViewById(R.id.portrait);

                //todo  portrait.setBackgroundColor(Pallete.getDefaultColor());
                landscape.setValue(Reddit.dpWidth, false);


                final Dialog dialog = builder.setView(dialoglayout).create();
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (checkBox.isChecked()) {
                            Reddit.notificationTime = landscape.getValue();
                            Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                            if (Reddit.notifications != null) {
                                Reddit.notifications.cancel(getApplication());
                                Reddit.notifications.start(getApplication());
                            } else {
                                Reddit.notifications = new NotificationJobScheduler(getApplication());
                            }
                        }
                    }
                });

            }
        });
        findViewById(R.id.theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Settings.this, SettingsTheme.class);
                startActivityForResult(i, 2);
            }
        });
        findViewById(R.id.layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, EditCardsLayout.class);
                startActivityForResult(i, 2);
            }
        });
        findViewById(R.id.preset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, EditCardsLayout.class);
                i.putExtra("secondary", "yes");
                startActivityForResult(i, 2);
            }
        });


        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File prefsdir = new File(getApplicationInfo().dataDir, "shared_prefs");

                if (prefsdir.exists() && prefsdir.isDirectory()) {

                    String[] list = prefsdir.list();
                    for (final String s : list) {
                        if(!s.contains("com.google")) {
                            Log.v("Slide", "NAME IS " + s);
                            title = s;
                            Drive.DriveApi.newDriveContents(mGoogleApiClient)
                                    .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                                        @Override
                                        public void onResult(DriveApi.DriveContentsResult result) {
                                            final String copy = getApplicationInfo().dataDir + File.separator + "shared_prefs" + File.separator + s;
                                            Log.v("Slide", "LOCATION IS " + copy);
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
                                                            Log.v("Slide", content);

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
                                                        Log.e("Slide", e.getMessage());
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
                        }
                    }
                }
            }
        });


        findViewById(R.id.restore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress = new MaterialDialog.Builder(Settings.this).title("Syincing your Settings").progress(true, 1).build();
                progress.show();
                appFolder.listChildren(mGoogleApiClient).setResultCallback(newCallback);
            }
        });
        findViewById(R.id.support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inte = new Intent(Settings.this, DonateView.class);
                Settings.this.startActivity(inte);
            }
        });

    }

    DriveFolder appFolder;

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
}