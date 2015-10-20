package me.ccrama.redditslide.Visuals;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
public class Ripple extends LinearLayout {

    /**
     * User: eluleci
     * Date: 25.09.2014
     * Time: 17:23
     */
        private TouchEffectsAnimator touchEffectAnimator;

        public Ripple(Context context) {
            super(context);
            init();
        }

        public Ripple(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {

            // you should set a background to view for effect to be visible. in this sample, this
            // linear layout contains a transparent background which is set inside the XML

            // giving the view to animate on
            touchEffectAnimator = new TouchEffectsAnimator(this);

            // enabling ripple effect. it only performs ease effect without enabling ripple effect
            touchEffectAnimator.setHasRippleEffect(true);

            // setting the effect color
            touchEffectAnimator.setEffectColor(Color.LTGRAY);

            // setting the duration
            touchEffectAnimator.setAnimDuration(1000);

            // setting radius to clip the effect. use it if you have a rounded background
            touchEffectAnimator.setClipRadius(20);

            // the view must contain an onClickListener to receive UP touch events. touchEffectAnimator
            // doesn't return any value in onTouchEvent for flexibility. so it is developers
            // responsibility to add a listener
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
        }

        @Override
        public boolean onTouchEvent(final MotionEvent event) {
            // send the touch event to animator
            touchEffectAnimator.onTouchEvent(event);
            return super.onTouchEvent(event);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // let animator show the animation by applying changes to view's canvas
            touchEffectAnimator.onDraw(canvas);
            super.onDraw(canvas);
        }
    }

