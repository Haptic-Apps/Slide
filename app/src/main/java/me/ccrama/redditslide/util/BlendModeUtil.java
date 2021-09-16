package me.ccrama.redditslide.util;

import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;

import java.util.List;

public class BlendModeUtil {
    private static void tintDrawable(@NonNull final Drawable drawable,
                                     @ColorInt final int color,
                                     final BlendModeCompat mode) {
        final ColorFilter filter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                color, mode);
        drawable.setColorFilter(filter);
    }

    private static void tintDrawables(@NonNull final List<Drawable> drawableSet,
                                      @ColorInt final int color,
                                      final BlendModeCompat mode) {
        for (final Drawable drawable : drawableSet) {
            tintDrawable(drawable, color, mode);
        }
    }

    public static void tintDrawableAsModulate(@NonNull final Drawable drawable,
                                              @ColorInt final int color) {
        tintDrawable(drawable, color, BlendModeCompat.MODULATE);//BlendMode.MODULATE = PorterDuff.Mode.MULTIPLY
    }

    public static void tintDrawableAsSrcIn(@NonNull final Drawable drawable,
                                           @ColorInt final int color) {
        tintDrawable(drawable, color, BlendModeCompat.SRC_IN);
    }

    public static void tintDrawablesAsSrcAtop(@NonNull final List<Drawable> drawableSet,
                                              @ColorInt final int color) {
        tintDrawables(drawableSet, color, BlendModeCompat.SRC_ATOP);
    }

    private static void tintImageView(@NonNull final ImageView imageView,
                                      @ColorInt final int color,
                                      final BlendModeCompat mode) {
        final ColorFilter filter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                color, mode);
        imageView.setColorFilter(filter);
    }

    private static void tintImageViews(@NonNull final List<ImageView> imageViewSet,
                                       @ColorInt final int color,
                                       final BlendModeCompat mode) {
        for (final ImageView imageView : imageViewSet) {
            tintImageView(imageView, color, mode);
        }
    }

    public static void tintImageViewAsModulate(@NonNull final ImageView imageView,
                                               @ColorInt final int color) {
        tintImageView(imageView, color, BlendModeCompat.MODULATE);//BlendMode.MODULATE = PorterDuff.Mode.MULTIPLY
    }

    public static void tintImageViewAsSrcAtop(@NonNull final ImageView imageView,
                                              @ColorInt final int color) {
        tintImageView(imageView, color, BlendModeCompat.SRC_ATOP);
    }

    public static void tintImageViewsAsSrcAtop(@NonNull final List<ImageView> imageViewSet,
                                               @ColorInt final int color) {
        tintImageViews(imageViewSet, color, BlendModeCompat.SRC_ATOP);
    }

    private static void tintPaint(@NonNull final Paint paint,
                                  @ColorInt final int color,
                                  final BlendModeCompat mode) {
        final ColorFilter filter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                color, mode);
        paint.setColorFilter(filter);
    }

    public static void tintPaintAsSrcAtop(@NonNull final Paint paint,
                                          @ColorInt final int color) {
        tintPaint(paint, color, BlendModeCompat.SRC_ATOP);
    }

    public static void tintPaintAsOverlay(@NonNull final Paint paint,
                                          @ColorInt final int color) {
        tintPaint(paint, color, BlendModeCompat.OVERLAY);
    }
}
