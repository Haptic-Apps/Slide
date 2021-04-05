package me.ccrama.redditslide.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

import me.ccrama.redditslide.Adapters.ProfileCommentViewHolder;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;

/**
 * Created by TacoTheDank on 03/15/2021.
 * <p>
 * These functions wouldn't really make sense to be anywhere else, so...
 * MiscUtil is meant to be temporary; these functions will ideally eventually go into their own little places.
 */
public class MiscUtil {

    // Used in SubredditView, MainActivity, and CommentPage (ugly-af code moment)
    public static void doSubscribeButtonText(boolean currentlySubbed, TextView subscribe) {
        if (Authentication.didOnline) {
            if (currentlySubbed) {
                subscribe.setText(R.string.unsubscribe_caps);
            } else {
                subscribe.setText(R.string.subscribe_caps);
            }
        } else {
            if (currentlySubbed) {
                subscribe.setText(R.string.btn_remove_from_sublist);
            } else {
                subscribe.setText(R.string.btn_add_to_sublist);
            }
        }
    }

    public static void addAwards(final Context mContext, final int fontsize,
                                 final ProfileCommentViewHolder holder,
                                 final Integer awards, final int drawable) {
        if (awards > 0) {
            final String timesAwarded = awards == 1 ? "" : "\u200Ax" + awards;
            final SpannableStringBuilder awarded =
                    new SpannableStringBuilder("\u00A0★" + timesAwarded + "\u00A0");
            Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), drawable);
            final float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
            image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                    (int) Math.ceil(fontsize), true);
            awarded.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            awarded.setSpan(new RelativeSizeSpan(0.75f), 3, awarded.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) holder.gild).append(awarded);
        }
    }

    public static void addCommentAwards(final Context mContext, final int fontsize,
                                        final SpannableStringBuilder titleString, final Integer awards,
                                        Bitmap image) {
        if (awards > 0) {
            final String timesAwarded = awards == 1 ? "" : "\u200Ax" + awards;
            final SpannableStringBuilder awarded =
                    new SpannableStringBuilder("\u00A0★" + timesAwarded + "\u00A0");
            final float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
            image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                    (int) Math.ceil(fontsize), true);
            awarded.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            awarded.setSpan(new RelativeSizeSpan(0.75f), 3, awarded.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(awarded);
            titleString.append(" ");
        }
    }

    public static void addSubmissionAwards(final Context mContext, final int fontsize,
                                           final SpannableStringBuilder titleString,
                                           final Integer awards, final int drawable) {
        if (awards > 0) {
            final String timesAwarded = awards == 1 ? "" : "\u200Ax" + awards;
            final SpannableStringBuilder awarded =
                    new SpannableStringBuilder("\u00A0★" + timesAwarded + "\u00A0");
            Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), drawable);
            final float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
            image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                    (int) Math.ceil(fontsize), true);
            awarded.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            awarded.setSpan(new RelativeSizeSpan(0.75f), 3, awarded.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(awarded);
        }
    }

    public static void addSearchAwards(final Context mContext, final int fontsize,
                                       final SpannableStringBuilder titleString,
                                       final Integer awards, final int drawable) {
        if (awards > 0) {
            final String timesAwarded = awards == 1 ? "" : "\u200Ax" + awards;
            final SpannableStringBuilder awarded =
                    new SpannableStringBuilder("\u00A0★" + timesAwarded + "\u00A0");
            Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), drawable);
            final float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
            image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                    (int) Math.ceil(fontsize), true);
            awarded.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            awarded.setSpan(new RelativeSizeSpan(0.75f), 3, awarded.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(awarded);
            titleString.append(" ");
        }
    }
}
