package me.ccrama.redditslide.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;

/**
 * Created by adrian on 8/24/16.
 */
public class ImageUtil {
    /* TODO: Implement tolerance */
    public static void drawWithTargetColor(Bitmap bm, Bitmap src, int targetcolor, int tolerance) {
        /*int r = (targetcolor >> 16) & 0xFF;
        int g = (targetcolor >>  8) & 0xFF;
        int b = (targetcolor >>  0) & 0xFF;*/

        int width = Math.max(bm.getWidth(), src.getWidth());
        int height = Math.max(bm.getHeight(), src.getHeight());

        int[] bmpixels = new int[width*height];
        bm.getPixels(bmpixels, 0, bm.getWidth(), 0, 0, width, height);

        int[] srcpixels = new int[width*height];
        src.getPixels(srcpixels, 0, src.getWidth(), 0, 0, width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bmpixels[x+width*y] == targetcolor) {
                    bmpixels[x+width*y] = srcpixels[x+width*y];
                }
            }
        }
        bm.setPixels(bmpixels, 0, bm.getWidth(), 0, 0, width, height);
    }

    public static Bitmap clipToCircle(final Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle((float) (width / 2), (float) (height / 2),
                (float) Math.min(width, height / 2), Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }
}
