package me.ccrama.redditslide.ui.settings.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.jakewharton.processphoenix.ProcessPhoenix;

import org.apache.commons.lang3.StringUtils;

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

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.ui.settings.SettingsActivity;
import me.ccrama.redditslide.util.FileUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.preference.PrefKeys;


/**
 * Created by ccrama on 3/5/2015.
 * <p>
 * Rewritten into AndroidX Preference by TacoTheDank on 08/03/2021.
 */
public class SettingsBackupFragment extends PreferenceFragmentCompat {
    public final ActivityResultLauncher<Intent> restoreFromFileLauncher =
            registerForActivityResult(new StartActivityForResult(), this::restoreFromFileResult);
    private File file;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.preferences_backup);

        final Preference backupToFile = findPreference(getString(PrefKeys.PREF_BACKUP_TO_FILE));
        final Preference restoreFromFile = findPreference(getString(PrefKeys.PREF_RESTORE_FROM_FILE));

        if (backupToFile != null && restoreFromFile != null) {
            backupToFile.setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.settings_backup_include_personal_title)
                        .setMessage(R.string.settings_backup_include_personal_text)
                        .setPositiveButton(R.string.btn_yes, (dialog, which) ->
                                backupToDir(false))
                        .setNegativeButton(R.string.btn_no, (dialog, which) ->
                                backupToDir(true))
                        .setNeutralButton(R.string.btn_cancel, null)
                        .setCancelable(false)
                        .show();
                return true;
            });

            restoreFromFile.setOnPreferenceClickListener(preference -> {
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                final String[] mimeTypes = {"text/plain"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                restoreFromFileLauncher.launch(intent);
                return true;
            });
        }
    }

    private void restoreFromFileResult(final ActivityResult result) {
        if (result.getData() != null) {
            final Uri fileUri = result.getData().getData();
            Log.v(LogUtil.getTag(), "WORKED! " + fileUri.toString());

            final StringWriter fw = new StringWriter();
            try {
                final InputStream is = getActivity().getContentResolver().openInputStream(fileUri);
                final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                int c = reader.read();
                while (c != -1) {
                    fw.write(c);
                    c = reader.read();
                }
                final String read = fw.toString();
                writeToSharedPrefs(read);

            } catch (final Exception e) {
                e.printStackTrace();
                showFileNotFoundDialog();
            }

        } else {
            showFileNotFoundDialog();
        }
    }

    private void showFileNotFoundDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.err_file_not_found)
                .setMessage(R.string.err_file_not_found_msg)
                .setPositiveButton(R.string.btn_ok, null)
                .setCancelable(false)
                .show();
    }

    private void writeToSharedPrefs(final String read) {
        if (read.contains("Slide_backupEND>")) {

            final String[] files = read.split("END>\\s*");
            for (int i = 1; i < files.length; i++) {
                String innerFile = files[i];
                final String t = innerFile.substring(6, innerFile.indexOf(">"));
                innerFile = innerFile.substring(innerFile.indexOf(">") + 1);

                final File newF = new File(getActivity().getApplicationInfo().dataDir
                        + File.separator
                        + "shared_prefs"
                        + File.separator
                        + t);
                Log.v(LogUtil.getTag(), "WRITING TO " + newF.getAbsolutePath());
                try {
                    final FileWriter newfw = new FileWriter(newF);
                    final BufferedWriter bw = new BufferedWriter(newfw);
                    bw.write(innerFile);
                    bw.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.backup_restore_settings)
                    .setMessage(R.string.backup_restarting)
                    .setOnDismissListener(dialog ->
                            ProcessPhoenix.triggerRebirth(getActivity()))
                    .setPositiveButton(R.string.btn_ok, (dialog, which) ->
                            ProcessPhoenix.triggerRebirth(getActivity()))
                    .setCancelable(false)
                    .show();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.err_not_valid_backup)
                    .setMessage(R.string.err_not_valid_backup_msg)
                    .setPositiveButton(R.string.btn_ok, null)
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_title_backup);
    }

    private void backupToDir(final boolean personal) {
        new BackupTask(personal).execute();
    }

    private void close(final Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (final IOException ignored) {
        }
    }

    private class BackupTask extends AsyncTask<Void, Void, Void> {
        private final boolean personal;

        private BackupTask(final boolean personal) {
            this.personal = personal;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            final File prefsdir = new File(getActivity().getApplicationInfo().dataDir, "shared_prefs");

            if (prefsdir.exists() && prefsdir.isDirectory()) {
                final String[] list = prefsdir.list();

                final File getExtDir
                        = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                getExtDir.mkdirs();

                final File backedup = new File(getExtDir
                        + File.separator
                        + "Slide"
                        + new SimpleDateFormat("-yyyy-MM-dd-HH-mm-ss")
                        .format(Calendar.getInstance().getTime())
                        + (!personal ? "-personal" : "")
                        + ".txt");
                file = backedup;
                FileWriter fw = null;
                try {
                    backedup.createNewFile();
                    fw = new FileWriter(backedup);
                    fw.write("Slide_backupEND>");
                    for (final String s : list) {

                        final boolean other = !StringUtils.containsAny(s, "cache",
                                "ion-cookies", "albums", "com.google", "STACKTRACE");
                        final boolean personal1 = !StringUtils.containsAny(s, "SUBSNEW",
                                "appRestart", "STACKTRACE", "AUTH", "TAGS", "SEEN", "HIDDEN", "HIDDEN_POSTS");
                        if (other && (!personal || personal1)) {
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
                } catch (final Exception e) {
                    e.printStackTrace();
                } finally {
                    close(fw);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.backup_complete)
                    .setMessage(R.string.backup_saved_downloads)
                    .setPositiveButton(R.string.btn_view, (dialog, which) -> {
                        final Intent intent = FileUtil.getFileIntent(file,
                                new Intent(Intent.ACTION_VIEW),
                                getActivity());
                        if (intent.resolveActivityInfo(getActivity().getPackageManager(), 0) != null) {
                            startActivity(Intent.createChooser(
                                    intent, getString(R.string.settings_backup_view)));
                        } else {
                            Toast.makeText(getActivity(),
                                    getString(R.string.settings_backup_err_no_explorer,
                                            file.getAbsolutePath() + file),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.btn_close, null)
                    .setCancelable(false)
                    .show();
        }
    }
}
