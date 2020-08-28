/*
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package me.ccrama.redditslide.ForceTouch.builder;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

import me.ccrama.redditslide.ForceTouch.PeekView;
import me.ccrama.redditslide.ForceTouch.PeekViewActivity;
import me.ccrama.redditslide.ForceTouch.callback.OnPeek;
import me.ccrama.redditslide.ForceTouch.util.GestureListener;

/**
 * This is a builder class to facilitate the creation of the PeekView.
 */
public class Peek {

    public static Peek into(@LayoutRes int layoutRes, @Nullable OnPeek onPeek) {
        return new Peek(layoutRes, onPeek);
    }

    public static Peek into(View layout, @Nullable OnPeek onPeek) {
        return new Peek(layout, onPeek);
    }

    /**
     * Used to clear the peeking ability. This could be useful for a RecyclerView/ListView, where a recycled item
     * shouldn't use the PeekView, but the original item did.
     *
     * @param view the view we want to stop peeking into
     */
    public static void clear(View view) {
        view.setOnTouchListener(null);
    }

    private int layoutRes = 0;
    private View layout = null;

    private PeekViewOptions options = new PeekViewOptions();
    private OnPeek callbacks;

    private Peek(@LayoutRes int layoutRes, @Nullable OnPeek callbacks) {
        this.layoutRes = layoutRes;
        this.callbacks = callbacks;

    }

    private Peek(View layout, @Nullable OnPeek callbacks) {
        this.layout = layout;
        this.callbacks = callbacks;
    }

    /**
     * Apply the options to the PeekView, when it is shown.
     *
     * @param options
     */
    public Peek with(PeekViewOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Finish the builder by selecting the base view that you want to show the PeekView from.
     *
     * @param activity the PeekViewActivity that you are on.
     * @param base the view that you want to touch to apply the peek to.
     */
    public void applyTo(final PeekViewActivity activity, final View base) {
        final GestureDetectorCompat detector =
                new GestureDetectorCompat(activity, new GestureListener(activity, base, this));

        base.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, final MotionEvent motionEvent) {
                // we use the detector for the long and short click motion events
                detector.onTouchEvent(motionEvent);

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    forceRippleAnimation(base, motionEvent);
                }

                return true;
            }
        });
    }

    /**
     * Show the PeekView
     *
     * @param activity
     * @param motionEvent
     */
    public void show(PeekViewActivity activity, MotionEvent motionEvent) {
        PeekView peek;

        if (layout == null) {
            peek = new PeekView(activity, options, layoutRes, callbacks);
        } else {
            peek = new PeekView(activity, options, layout, callbacks);
        }

        peek.setOffsetByMotionEvent(motionEvent);
        activity.showPeek(peek, motionEvent.getRawY());
    }

    private void forceRippleAnimation(View view, MotionEvent event) {
        Drawable background = view.getBackground();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && background instanceof RippleDrawable) {
            final RippleDrawable rippleDrawable = (RippleDrawable) background;

            rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});
            rippleDrawable.setHotspot(event.getX(), event.getY());

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    rippleDrawable.setState(new int[]{});
                }
            }, 300);
        }
    }
}
