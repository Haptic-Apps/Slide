package me.ccrama.redditslide.util;

import android.os.Environment;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.R;

public class DialogUtil {
    public static void showErrorDialog(final AppCompatActivity activity) {
        showBaseChooserDialog(activity,
                R.string.err_something_wrong, R.string.err_couldnt_save_choose_new);
    }

    public static void showFirstDialog(final AppCompatActivity activity) {
        showBaseChooserDialog(activity,
                R.string.set_save_location, R.string.set_save_location_msg);
    }

    private static void showBaseChooserDialog(
            final AppCompatActivity activity,
            final @StringRes int titleId,
            final @StringRes int messageId
    ) {
        new AlertDialog.Builder(activity)
                .setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(android.R.string.ok, (dialog, which) ->
                        showFolderChooserDialog(activity)
                )
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static void showFolderChooserDialog(final AppCompatActivity activity) {
        new FolderChooserDialogCreate.Builder(activity)
                // changes label of the choose button
                .chooseButton(R.string.btn_select)
                .initialPath(Environment.getExternalStorageDirectory()
                        // changes initial path, defaults to external storage directory
                        .getPath())
                .allowNewFolder(true, 0)
                .show(activity);
    }
}
