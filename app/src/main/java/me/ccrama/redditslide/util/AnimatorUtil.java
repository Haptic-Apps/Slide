package me.ccrama.redditslide.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

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
}
