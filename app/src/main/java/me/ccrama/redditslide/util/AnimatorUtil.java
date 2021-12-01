package me.ccrama.redditslide.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import io.codetail.animation.ViewAnimationUtils;
import me.ccrama.redditslide.R;

/**
 * Created by TacoTheDank on 03/15/2021.
 */
public class AnimatorUtil {
    public static ValueAnimator flipAnimatorIfNonNull(final boolean isFlipped, final View view) {
        if (view != null) {
            flipAnimator(isFlipped, view);
        }
        return null;
    }

    public static ValueAnimator flipAnimator(final boolean isFlipped, final View view) {
        final ValueAnimator animator = ValueAnimator.ofFloat(isFlipped ? -1f : 1f, isFlipped ? 1f : -1f);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        //Update height
        animator.addUpdateListener(valueAnimator ->
                view.setScaleY((Float) valueAnimator.getAnimatedValue()));
        return animator;
    }

    public static ValueAnimator slideAnimator(final int start, final int end, final View view) {
        final ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            //Update height
            final int value = (Integer) valueAnimator.getAnimatedValue();
            final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = value;
            view.setLayoutParams(layoutParams);
        });
        return animator;
    }

    public static void animateOut(final View view) {
        final ValueAnimator mAnimator = slideAnimator(DisplayUtil.dpToPxVertical(36), 0, view);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        mAnimator.start();
    }

    public static void animateIn(final View view, final int dp) {
        view.setVisibility(View.VISIBLE);
        final ValueAnimator mAnimator = slideAnimator(0, DisplayUtil.dpToPxVertical(dp), view);
        mAnimator.start();
    }

    public static void fadeIn(final View view) {
        final ValueAnimator mAnimator = fadeAnimator(0.66f, 1, view);
        mAnimator.start();
    }

    public static void fadeOut(final View view) {
        final ValueAnimator mAnimator = fadeAnimator(1, .66f, view);
        mAnimator.start();
    }

    public static ValueAnimator fadeAnimator(final float start, final float end, final View view) {
        final ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        //Update height
        animator.addUpdateListener(valueAnimator ->
                view.setAlpha((Float) valueAnimator.getAnimatedValue()));
        return animator;
    }

    public static void setFlashAnimation(final View vBig, final View from, final int color) {
        // get the center for the clipping circle
        final View v = vBig.findViewById(R.id.vote);
        v.post(() -> {
            v.setBackgroundColor(color);
            v.setVisibility(View.VISIBLE);
            v.setAlpha(1f);

            final int cx = (from.getLeft() + from.getRight()) / 2;
            final int cy = vBig.getHeight() - from.getHeight() / 2;
            //from.getRight() - ( from.getWidth()/ 2);

            // get the final radius for the clipping circle
            final int dx = Math.max(cx, vBig.getWidth() - cx);
            final int dy = Math.max(cy, vBig.getHeight() - cy);
            final float finalRadius = (float) Math.hypot(dx, dy);

            try {
                final Animator animator =
                        ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
                animator.setInterpolator(new FastOutSlowInInterpolator());
                animator.setDuration(250);
                animator.start();

                v.postDelayed(() -> {
                    final ObjectAnimator animator2 = ObjectAnimator.ofFloat(v, View.ALPHA, 1f, 0f);

                    animator2.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator2.setDuration(450);
                    animator2.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            v.setVisibility(View.GONE);
                        }
                    });
                    animator2.start();

                }, 450);
            } catch (Exception e) {
                v.setVisibility(View.GONE);
            }
        });
    }
}
