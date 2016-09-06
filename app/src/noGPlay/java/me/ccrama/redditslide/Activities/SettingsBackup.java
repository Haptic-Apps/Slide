package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsBackup extends BaseActivityAnim {
    MaterialDialog progress;
    String         title;
    File           file;

    public static void close(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42) {
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

                File path = new File(fileUri.getPath());
                StringWriter fw = new StringWriter();
                try {
                    FileReader fr = new FileReader(path);
                    int c = fr.read();
                    while (c != -1) {
                        fw.write(c);
                        c = fr.read();
                    }
                    String read = fw.toString();
                    if (read.contains("Slide_backupEND>")) {

                        String[] files = read.split("END>");
                        progress.dismiss();
                        progress = new MaterialDialog.Builder(SettingsBackup.this).title(
                                R.string.backup_restoring)
                                .progress(false, files.length - 1)
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
                        new AlertDialogWrapper.Builder(SettingsBackup.this).setCancelable(false)
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
                                .show();

                    } else {
                        progress.hide();
                        new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                                R.string.err_not_valid_backup)
                                .setMessage(R.string.err_not_valid_backup_msg)
                                .setPositiveButton(R.string.btn_ok, null)
                                .show();
                    }
                } catch (Exception e) {
                    progress.hide();
                    e.printStackTrace();
                    new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                            R.string.err_file_not_found)
                            .setMessage(R.string.err_file_not_found_msg)
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                }

            } else {
                progress.dismiss();
                new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                        R.string.err_file_not_found)
                        .setMessage(R.string.err_file_not_found_msg)
                        .setPositiveButton(R.string.btn_ok, null)
                        .show();
            }

        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_sync);
        setupAppBar(R.id.toolbar, R.string.settings_title_backup, true, true);

        if (SettingValues.tabletUI) {

            findViewById(R.id.backfile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialogWrapper.Builder(SettingsBackup.this).setTitle(
                            R.string.settings_backup_include_personal_title)
                            .setMessage(
                                    R.string.settings_backup_include_personal_text)
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
                            .show();
                }
            });


            findViewById(R.id.restorefile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/txt");
                    startActivityForResult(intent, 42);

                }
            });
        } else {
            new AlertDialogWrapper.Builder(this).setTitle(
                    "Settings Backup is a Pro feature")
                    .setMessage(R.string.pro_upgrade_msg)
                    .setPositiveButton(R.string.btn_yes_exclaim,

                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                        "market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                        "http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                            }
                        }
                    })
                    .setNegativeButton(R.string.btn_no_danks,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();
                                }
                            })
                    .show();
        }
    }

    public void backupToDir(final boolean personal) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                progress = new MaterialDialog.Builder(SettingsBackup.this)
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

                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();

                    File backedup = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS)
                            + File.separator
                            + "Slide"
                            + new SimpleDateFormat("MMddyy-hh:mm").format(
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
                                    "albums") && !s.contains("com.google") && (((personal
                                    && !s.contains("SUBSNEW")
                                    && !s.contains("appRestart")
                                    && !s.contains("STACKTRACE")
                                    && !s.contains("AUTH")
                                    && !s.contains("TAGS")
                                    && !s.contains("SEEN")
                                    && !s.contains("HIDDEN")
                                    && !s.contains("HIDDEN_POSTS"))) || !personal)) {
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
                        R.string.backup_complete)
                        .setMessage(R.string.backup_saved_downloads)
                        .setPositiveButton(R.string.btn_view,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri selectedUri =
                                                Uri.parse("file://" + file.getAbsolutePath());
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(selectedUri);
                                        if (intent.resolveActivityInfo(getPackageManager(), 0)
                                                != null) {
                                            startActivity(
                                                    Intent.createChooser(intent, getString(R.string.settings_backup_view)));
                                        } else {
                                            Snackbar s =
                                                    Snackbar.make(findViewById(R.id.restorefile),
                                                            getString(R.string.settings_backup_err_no_explorer,
                                                                    file.getAbsolutePath() + file),
                                                            Snackbar.LENGTH_INDEFINITE);
                                            View view = s.getView();
                                            TextView tv = (TextView) view.findViewById(
                                                    android.support.design.R.id.snackbar_text);
                                            tv.setTextColor(Color.WHITE);
                                            s.show();
                                        }
                                    }
                                })
                        .setNegativeButton(R.string.btn_close, null)
                        .show();
            }
        }.execute();

    }

}