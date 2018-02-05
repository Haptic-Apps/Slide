package me.ccrama.redditslide.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;

public class ShareUtil {
    private ShareUtil() {
    }

    public static void shareImage(final String finalUrl, final Context context) {
        ((Reddit) context.getApplicationContext()).getImageLoader()
                .loadImage(finalUrl, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        shareImage(loadedImage, context);
                    }
                });
    }

    /**
     * Converts an image to a PNG, stores it to the cache, then shares it. Saves the image to
     * /cache/shared_image for easy deletion. If the /cache/shared_image folder already exists, we
     * clear it's contents as to avoid increasing the cache size unnecessarily.
     *
     * @param bitmap image to share
     */
    public static void shareImage(final Bitmap bitmap, Context context) {
        File image; //image to share

        //check to see if the cache/shared_images directory is present
        final File imagesDir =
                new File(context.getCacheDir().toString() + File.separator + "shared_image");
        if (!imagesDir.exists()) {
            imagesDir.mkdir(); //create the folder if it doesn't exist
        } else {
            FileUtil.deleteFilesInDir(imagesDir);
        }

        try {
            //creates a file in the cache; filename will be prefixed with "img" and end with ".png"
            image = File.createTempFile("img", ".png", imagesDir);
            FileOutputStream out = null;

            try {
                //convert image to png
                out = new FileOutputStream(image);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } finally {
                if (out != null) {
                    out.close();

                    final Uri contentUri = FileUtil.getFileUri(image, context);
                    if (contentUri != null) {
                        final Intent shareImageIntent =
                                FileUtil.getFileIntent(image, new Intent(Intent.ACTION_SEND),
                                        context);
                        shareImageIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

                        //Select a share option
                        context.startActivity(Intent.createChooser(shareImageIntent,
                                context.getString(R.string.misc_img_share)));
                    } else {
                        Toast.makeText(context, context.getString(R.string.err_share_image),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(context, context.getString(R.string.err_share_image), Toast.LENGTH_LONG)
                    .show();
        }
    }
}
