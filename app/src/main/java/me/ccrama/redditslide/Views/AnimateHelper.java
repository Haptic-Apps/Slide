package me.ccrama.redditslide.Views;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by carlo_000 on 2/24/2016.
 */
public class AnimateHelper {

   public static void setFlashAnimation(final View v, int colorTo, int colorFrom){
       if(v instanceof CardView) {
           ((CardView)v).setCardBackgroundColor(colorTo);
       } else {
           v.setBackgroundColor(colorTo);
       }

       ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
       colorAnimation.setDuration(500); // milliseconds
       colorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
       colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

           @Override
           public void onAnimationUpdate(ValueAnimator animator) {
               if(v instanceof CardView) {
                   ((CardView)v).setCardBackgroundColor((int) animator.getAnimatedValue());
               } else {
                   v.setBackgroundColor((int) animator.getAnimatedValue());
               }
           }

       });
       colorAnimation.start();

   }

}
