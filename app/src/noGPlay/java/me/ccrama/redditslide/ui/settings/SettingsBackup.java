package me.ccrama.redditslide.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.afollestad.materialdialogs.MaterialDialog;
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
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import me.ccrama.redditslide.Activities.BaseActivityAnim;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.FileUtil;
import me.ccrama.redditslide.util.LayoutUtils;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.ProUtil;


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
        super.onActivityResult(requestCode, resultCode, data);
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

                        String[] files = read.split("END>\\s*");
                        progress.dismiss();
                        progress = new MaterialDialog.Builder(SettingsBackup.this).title(
                                R.string.backup_restoring)
                                .progress(false, files.length - 1)
                                .build();
                        progress.show();
                        for (int i = 1; i < files.length; i++) {
                            String innerFile = files[i];
                            String t = innerFile.substring(6, innerFile.indexOf(">"));
                            innerFile = innerFile.substring(innerFile.indexOf(">") + 1
                            );

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
                        new AlertDialog.Builder(SettingsBackup.this)
                                .setTitle(R.string.backup_restore_settings)
                                .setMessage(R.string.backup_restarting)
                                .setOnDismissListener(dialog ->
                                        ProcessPhoenix.triggerRebirth(SettingsBackup.this))
                                .setPositiveButton(R.string.btn_ok, (dialog, which) ->
                                        ProcessPhoenix.triggerRebirth(SettingsBackup.this))
                                .setCancelable(false)
                                .show();

                    } else {
                        progress.hide();
                        new AlertDialog.Builder(SettingsBackup.this)
                                .setTitle(R.string.err_not_valid_backup)
                                .setMessage(R.string.err_not_valid_backup_msg)
                                .setPositiveButton(R.string.btn_ok, null)
                                .setCancelable(false)
                                .show();
                    }
                } catch (Exception e) {
                    progress.hide();
                    e.printStackTrace();
                    new AlertDialog.Builder(SettingsBackup.this)
                            .setTitle(R.string.err_file_not_found)
                            .setMessage(R.string.err_file_not_found_msg)
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                }

            } else {
                progress.dismiss();
                new AlertDialog.Builder(SettingsBackup.this)
                        .setTitle(R.string.err_file_not_found)
                        .setMessage(R.string.err_file_not_found_msg)
                        .setPositiveButton(R.string.btn_ok, null)
                        .setCancelable(false)
                        .show();
            }

        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_sync);
        setupAppBar(R.id.toolbar, R.string.settings_title_backup, true, true);

        if (SettingValues.isPro) {

            findViewById(R.id.backfile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(SettingsBackup.this)
                            .setTitle(R.string.settings_backup_include_personal_title)
                            .setMessage(R.string.settings_backup_include_personal_text)
                            .setPositiveButton(R.string.btn_yes, (dialog, which) ->
                                    backupToDir(false))
                            .setNegativeButton(R.string.btn_no, (dialog, which) ->
                                    backupToDir(true))
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
            ProUtil.proUpgradeMsg(this, R.string.general_backup_ispro)
                    .setNegativeButton(R.string.btn_no_thanks, (dialog, whichButton) ->
                            finish())
                    .setCancelable(false)
                    .show();
        }
    }

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
                                    "albums") && !s.contains("com.google") && (!personal || (!s.contains("SUBSNEW")
                                    && !s.contains("appRestart")
                                    && !s.contains("STACKTRACE")
                                    && !s.contains("AUTH")
                                    && !s.contains("TAGS")
                                    && !s.contains("SEEN")
                                    && !s.contains("HIDDEN")
                                    && !s.contains("HIDDEN_POSTS")))) {
                                FileReader fr = null;
                                try {
                                    fr = new FileReader(prefsdir + File.separator + s);
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
                new AlertDialog.Builder(SettingsBackup.this)
                        .setTitle(R.string.backup_complete)
                        .setMessage(R.string.backup_saved_downloads)
                        .setPositiveButton(R.string.btn_view, (dialog, which) -> {
                            Intent intent = FileUtil.getFileIntent(file,
                                    new Intent(Intent.ACTION_VIEW),
                                    SettingsBackup.this);
                            if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
                                startActivity(
                                        Intent.createChooser(
                                                intent, getString(R.string.settings_backup_view)));
                            } else {
                                Snackbar s = Snackbar.make(findViewById(R.id.restorefile),
                                        getString(R.string.settings_backup_err_no_explorer,
                                                file.getAbsolutePath() + file),
                                        Snackbar.LENGTH_INDEFINITE);
                                LayoutUtils.showSnackbar(s);
                            }
                        })
                        .setNegativeButton(R.string.btn_close, null)
                        .setCancelable(false)
                        .show();
            }
        }.execute();

    }

}
