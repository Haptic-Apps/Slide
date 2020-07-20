package me.ccrama.redditslide.Views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import io.codetail.animation.ViewAnimationUtils;
import me.ccrama.redditslide.R;

/**
 * Created by carlo_000 on 2/24/2016.
 */
public class AnimateHelper {


    private AnimateHelper() {
    }

    public static void setFlashAnimation(final View vBig, final View from, final int color) {
        // get the center for the clipping circle
        final View v = vBig.findViewById(R.id.vote);
        v.post(new Runnable() {
            @Override
            public void run() {
        v.setBackgroundColor(color);
        v.setVisibility(View.VISIBLE);
        v.setAlpha(1f);

        final int cx = (from.getLeft() + from.getRight()) / 2;
        final int cy = vBig.getHeight() - (from.getHeight() / 2);//from.getRight() - ( from.getWidth()/ 2);

// get the final radius for the clipping circle
        int dx = Math.max(cx, vBig.getWidth() - cx);
        int dy = Math.max(cy, vBig.getHeight() - cy);
        final float finalRadius = (float) Math.hypot(dx, dy);


                try {
                    Animator animator =
                            ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
                    animator.setInterpolator(new FastOutSlowInInterpolator());
                    animator.setDuration(250);
                    animator.start();

                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ObjectAnimator animator2 = ObjectAnimator.ofFloat(v, View.ALPHA, 1f, 0f);

                            animator2.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator2.setDuration(450);
                            animator2.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    v.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            });
                            animator2.start();

                        }
                    }, 450);
                } catch(Exception e){
                    v.setVisibility(View.GONE);
                }

            }
        });

    }


}
