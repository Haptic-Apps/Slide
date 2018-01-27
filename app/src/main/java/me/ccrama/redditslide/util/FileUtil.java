package me.ccrama.redditslide.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

public class FileUtil {
    private FileUtil() {
    }

    /**
     * Modifies an {@code Intent} to open a file with the FileProvider
     *
     * @param file    the {@code File} to open
     * @param intent  the {@Intent} to modify
     * @param context Current context
     * @return a base {@code Intent} with read and write permissions granted to the receiving
     * application
     */
    public static Intent getFileIntent(File file, Intent intent, Context context) {
        String packageName = context.getApplicationContext().getPackageName() + ".provider";

        Uri selectedUri = FileProvider.getUriForFile(context, packageName, file);

        context.grantUriPermission(packageName, selectedUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        intent.setData(selectedUri);
        intent.setFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        return intent;
    }
}
