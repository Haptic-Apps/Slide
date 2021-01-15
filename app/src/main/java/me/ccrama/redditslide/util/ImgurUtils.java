package me.ccrama.redditslide.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import me.ccrama.redditslide.Reddit;

//Following methods sourced from https://github.com/Kennyc1012/Opengur, Code by Kenny Campagna
public class ImgurUtils {
    public static File createFile(Uri uri, @NonNull Context context) {
        InputStream in;
        ContentResolver resolver = context.getContentResolver();
        String type = resolver.getType(uri);
        String extension;

        if ("image/png".equals(type)) {
            extension = ".gif";
        } else if ("image/png".equals(type)) {
            extension = ".png";
        } else {
            extension = ".jpg";
        }

        try {
            in = resolver.openInputStream(uri);
        } catch (FileNotFoundException e) {
            return null;
        }

        // Create files from a uri in our cache directory so they eventually get deleted
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File cacheDir = ((Reddit) context.getApplicationContext()).getImageLoader()
                .getDiskCache()
                .getDirectory();
        File tempFile = new File(cacheDir, timeStamp + extension);

        if (writeInputStreamToFile(in, tempFile)) {
            return tempFile;
        } else {
            // If writeInputStreamToFile fails, delete the excess file
            tempFile.delete();
        }

        return null;
    }

    public static boolean writeInputStreamToFile(@NonNull InputStream in, @NonNull File file) {
        BufferedOutputStream buffer = null;
        boolean didFinish = false;

        try {
            buffer = new BufferedOutputStream(new FileOutputStream(file));
            byte[] byt = new byte[1024];
            int i;

            for (long l = 0L; (i = in.read(byt)) != -1; l += i) {
                buffer.write(byt, 0, i);
            }

            buffer.flush();
            didFinish = true;
        } catch (IOException e) {
            didFinish = false;
        } finally {
            closeStream(in);
            closeStream(buffer);
        }

        return didFinish;
    }

    public static void closeStream(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ex) {
            }
        }
    }
}
