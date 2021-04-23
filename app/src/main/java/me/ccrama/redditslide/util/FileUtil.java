package me.ccrama.redditslide.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.text.DecimalFormat;
import java.util.UUID;

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
        Uri selectedUri = getFileUri(file, context);

        intent.setDataAndType(selectedUri, context.getContentResolver().getType(selectedUri));
        intent.setFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        return intent;
    }

    /**
     * Gets a valid File Uri to a file in the system
     *
     * @param file    the {@code File} to open
     * @param context Current context
     * @return a File Uri to the given file
     */
    public static Uri getFileUri(File file, Context context) {
        String packageName = context.getApplicationContext().getPackageName() + ".provider";
        Uri selectedUri = FileProvider.getUriForFile(context, packageName, file);
        context.grantUriPermission(packageName, selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return selectedUri;
    }

    /**
     * Deletes all files in a folder
     *
     * @param dir to clear contents
     */
    public static void deleteFilesInDir(File dir) {
        for (File child : dir.listFiles()) {
            child.delete();
        }
    }

    /**
     * Convert a byte count into a human-readable size
     *
     * @param size Byte count
     * @return Human-readable size
     */
    public static String readableFileSize(final long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        final int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
                + " "
                + units[digitGroups];
    }

    /**
     * Returns a unique file that can be used without any collision.
     *
     * @param folderPath Base directory path for saving
     * @param subfolderPath Sub directory path with File separator, such as "/subreddit"
     * @param title Unsafe file name with any length or any characters
     * @param fileIndex File index for the file, or empty string if it is not required
     * @param extension File extension with period, such has ".png", ".mp4"
     * @return A safe file for writing
     */
    public static File getValidFile(String folderPath, String subfolderPath,
                                    String title, String fileIndex, String extension) {
        validateDirectory(folderPath);
        validateDirectory(folderPath + subfolderPath);

        File file;

        int tries = 0;
        do {
            String extras = tries == 0 ? fileIndex + extension : fileIndex + "_" + tries + extension;
            String sanitizedTitle = sanitizeFileName(title, extras);

            if (sanitizedTitle == null || sanitizedTitle.trim().isEmpty()) {
                sanitizedTitle = UUID.randomUUID().toString();
            }

            String fileName = sanitizedTitle + extras;
            file = new File(folderPath + subfolderPath + File.separator + fileName);

            tries++;
        } while (file.exists());

        return file;
    }

    /**
     * Checks for directory existence, if it does not exist, creates one.
     */
    private static void validateDirectory(String path) {
        File directory = new File(path);

        if (directory.exists()) return;

        directory.mkdirs();
    }

    /**
     * Truncates a UTF-8 unicode string to be used within the 255 Bytes limit of file name length on Linux,
     * as well as replaces reserved file name characters with underscore.
     *
     * Broken multi-byte unicode character will be ignored.
     * File name will have "…" added at the end if it gets truncated.
     *
     * @param fileName The string that will be saved as file name.
     * @param extras Additional string, such as index with file extension.
     * @return Sanitized file name (without the extras) that will be within the byte limit with the extras added later.
     */
    @Nullable
    private static String sanitizeFileName(String fileName, String extras) {
        if (fileName == null) return null;

        String sanitizedFileName = fileName.replaceAll("[/?<>\\\\:*|\"]", "_");

        Charset charset = Charset.defaultCharset();
        byte[] fileNameBytes = sanitizedFileName.getBytes(charset);

        int usableByteLimit = 255 - extras.getBytes().length;
        if (fileNameBytes.length <= usableByteLimit) return sanitizedFileName;

        String replacementChar = "…";
        int replacementCharLength = replacementChar.getBytes().length;

        ByteBuffer byteBuffer = ByteBuffer.wrap(fileNameBytes, 0, usableByteLimit - replacementCharLength);
        CharBuffer charBuffer = CharBuffer.allocate(usableByteLimit - replacementCharLength);

        // Replaces a broken / incomplete character because of truncation
        CharsetDecoder decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.IGNORE);
        decoder.decode(byteBuffer, charBuffer, true);
        decoder.flush(charBuffer);

        return new String(charBuffer.array(), 0, charBuffer.position()) + replacementChar;
    }

}
