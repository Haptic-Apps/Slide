package me.ccrama.redditslide.ForceTouch;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;
import me.ccrama.redditslide.ForceTouch.builder.PeekViewOptions;
import me.ccrama.redditslide.ForceTouch.callback.OnButtonUp;
import me.ccrama.redditslide.ForceTouch.callback.OnPeek;
import me.ccrama.redditslide.ForceTouch.callback.OnPop;
import me.ccrama.redditslide.ForceTouch.callback.OnRemove;
import me.ccrama.redditslide.ForceTouch.util.DensityUtils;
import me.ccrama.redditslide.ForceTouch.util.NavigationUtils;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.PeekMediaView;

public class PeekView extends FrameLayout {

    private static final int          ANIMATION_TIME = 300;
    private static final Interpolator INTERPOLATOR   = new DecelerateInterpolator();
    private static final int          FINGER_SIZE_DP = 40;

    private int FINGER_SIZE;

    public  View                   content;
    private ViewGroup.LayoutParams contentParams;

    private PeekViewOptions options;
    private int             distanceFromTop;
    private int             distanceFromLeft;
    private int             screenWidth;
    private int             screenHeight;
    private ViewGroup androidContentView = null;
    private OnPeek   callbacks;
    private OnRemove remove;

    public PeekView(Activity context, PeekViewOptions options, @LayoutRes int layoutRes,
            @Nullable OnPeek callbacks) {
        super(context);
        init(context, options, LayoutInflater.from(context).inflate(layoutRes, this, false),
                callbacks);
    }

    public void addButton(@IdRes Integer i, OnButtonUp onButtonUp) {
        buttons.put(i, onButtonUp);
    }

    private OnPop mOnPop;

    int currentHighlight;
    static int eight = Reddit.dpToPxVertical(8);

    public void highlightMenu(MotionEvent event) {
        if(currentHighlight != 0){
            final View v = content.findViewById(currentHighlight);
            Rect outRect = new Rect();
            v.getGlobalVisibleRect(outRect);
            if(!outRect.contains((int) event.getX(), (int) event.getY())){
                currentHighlight = 0;
                ValueAnimator animator = ValueAnimator.ofInt(eight, eight * 2);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator){
                        v.setPadding(0,  (Integer) valueAnimator.getAnimatedValue(), 0, (Integer) valueAnimator.getAnimatedValue());
                    }
                });
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(150);
                animator.start();
            } else {
                return;
            }
        }
        for (Integer i : buttons.keySet()) {
            final View v = content.findViewById(i);
            Rect outRect = new Rect();
            v.getGlobalVisibleRect(outRect);
            if(outRect.contains((int) event.getX(), (int) event.getY()) && i != currentHighlight){
                currentHighlight = i;
                ValueAnimator animator = ValueAnimator.ofInt(eight * 2, eight);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator){
                        v.setPadding(0,  (Integer) valueAnimator.getAnimatedValue(), 0, (Integer) valueAnimator.getAnimatedValue());
                    }
                });
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(150);
                animator.start();

                break;
            } else if(outRect.contains((int) event.getX(), (int) event.getY())){
                break;
            }
        }
    }

    public void pop(){
        if(mOnPop != null)
        mOnPop.onPop();
    }

    public void setOnPop(OnPop mOnPop){
        this.mOnPop = mOnPop;
    }

    public PeekView(Activity context, PeekViewOptions options, @NonNull View content,
            @Nullable OnPeek callbacks) {
        super(context);
        init(context, options, content, callbacks);
    }

    HashMap<Integer, OnButtonUp> buttons = new HashMap<>();

    public void checkButtons(MotionEvent event) {
        for (Map.Entry<Integer, OnButtonUp> entry : buttons.entrySet()) {
            View v = content.findViewById(entry.getKey());
            Rect outRect = new Rect();
            v.getGlobalVisibleRect(outRect);
            if(outRect.contains((int) event.getX(), (int) event.getY())){
                entry.getValue().onButtonUp();
            }
        }
    }

    public void doScroll(MotionEvent event) {
        ((PeekMediaView)content.findViewById(R.id.peek)).doScroll(event);
    }

    private void init(Activity context, PeekViewOptions options, @NonNull View content,
            @Nullable OnPeek callbacks) {
        this.options = options;
        this.callbacks = callbacks;

        FINGER_SIZE = DensityUtils.toPx(context, FINGER_SIZE_DP);

        // get the main content view of the display
        androidContentView = (FrameLayout) context.findViewById(android.R.id.content).getRootView();

        // initialize the display size
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenHeight = size.y;
        screenWidth = size.x;

        // set up the content we want to show
        this.content = content;
        contentParams = content.getLayoutParams();

        if (options.getAbsoluteHeight() != 0) {
            setHeight(DensityUtils.toPx(context, options.getAbsoluteHeight()));
        } else {
            setHeightByPercent(options.getHeightPercent());
        }

        if (options.getAbsoluteWidth() != 0) {
            setWidth(DensityUtils.toPx(context, options.getAbsoluteWidth()));
        } else {
            setWidthByPercent(options.getWidthPercent());
        }

        // tell the code that the view has been onInflated and let them use it to
        // set up the layout.
        if (callbacks != null) {
            callbacks.onInflated(this, content);
        }

        // add the background dim to the frame
        View dim = new View(context);
        dim.setBackgroundColor(Color.BLACK);
        dim.setAlpha(options.getBackgroundDim());

        LayoutParams dimParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        dim.setLayoutParams(dimParams);

        if (options.shouldBlurBackground()) {
            try {
                Blurry.with(context)
                        .radius(2)
                        .sampling(5)
                        .animate()
                        .color(options.getBlurOverlayColor())
                        .onto((ViewGroup) androidContentView.getRootView());

                dim.setAlpha(0f);
            } catch(Exception ignored){

            }
        }

        // add the dim and the content view to the upper level frame layout
        addView(dim);
        addView(content);
    }

    /**
     * Sets how far away from the top of the screen the view should be displayed. Distance should be
     * the value in PX.
     *
     * @param distance the distance from the top in px.
     */
    private void setDistanceFromTop(int distance) {
        this.distanceFromTop = options.fullScreenPeek() ? 0 : distance;
    }

    /**
     * Sets how far away from the left side of the screen the view should be displayed. Distance
     * should be the value in PX.
     *
     * @param distance the distance from the left in px.
     */
    private void setDistanceFromLeft(int distance) {
        this.distanceFromLeft = options.fullScreenPeek() ? 0 : distance;
    }

    /**
     * Sets the width of the view in PX.
     *
     * @param width the width of the circle in px
     */
    private void setWidth(int width) {
        contentParams.width = options.fullScreenPeek() ? screenWidth : width;
        content.setLayoutParams(contentParams);
    }

    /**
     * Sets the height of the view in PX.
     *
     * @param height the height of the circle in px
     */
    private void setHeight(int height) {
        contentParams.height = options.fullScreenPeek() ? screenHeight : height;
        content.setLayoutParams(contentParams);
    }

    /**
     * Sets the width of the window according to the screen width.
     *
     * @param percent of screen width
     */
    public void setWidthByPercent(@FloatRange(from = 0, to = 1) float percent) {
        setWidth((int) (screenWidth * percent));
    }

    /**
     * Sets the height of the window according to the screen height.
     *
     * @param percent of screen height
     */
    public void setHeightByPercent(@FloatRange(from = 0, to = 1) float percent) {
        setHeight((int) (screenHeight * percent));
    }

    /**
     * Places the peek view over the top of a motion event. This will translate the motion event's
     * start points so that the PeekView isn't covered by the finger.
     *
     * @param event event that activates the peek view
     */
    public void setOffsetByMotionEvent(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        if (x + contentParams.width + FINGER_SIZE < screenWidth) {
            setContentOffset(x, y, Translation.HORIZONTAL, FINGER_SIZE);
        } else if (x - FINGER_SIZE - contentParams.width > 0) {
            setContentOffset(x, y, Translation.HORIZONTAL, -1 * FINGER_SIZE);
        } else if (y + contentParams.height + FINGER_SIZE < screenHeight) {
            setContentOffset(x, y, Translation.VERTICAL, FINGER_SIZE);
        } else if (y - FINGER_SIZE - contentParams.height > 0) {
            setContentOffset(x, y, Translation.VERTICAL, -1 * FINGER_SIZE);
        } else {
            // it won't fit anywhere
            if (x < screenWidth / 2) {
                setContentOffset(x, y, Translation.HORIZONTAL, FINGER_SIZE);
            } else {
                setContentOffset(x, y, Translation.HORIZONTAL, -1 * FINGER_SIZE);
            }
        }
    }

    /**
     * Show the PeekView over the point of motion
     *
     * @param startX
     * @param startY
     */
    private void setContentOffset(int startX, int startY, Translation translation,
            int movementAmount) {

        if (translation == Translation.VERTICAL) {

            // center the X around the start point
            int originalStartX = startX;
            startX -= contentParams.width / 2;

            // if Y is in the lower half, we want it to go up, otherwise, leave it the same
            boolean moveDown = true;
            if (startY + contentParams.height + FINGER_SIZE > screenHeight) {
                startY -= contentParams.height;
                moveDown = false;

                if (movementAmount > 0) {
                    movementAmount *= -1;
                }
            }

            // when moving the peek view below the finger location, we want to offset it a bit to the right
            // or left as well, just so the hand doesn't cover it up.
            int extraXOffset = 0;
            if (moveDown) {
                extraXOffset = DensityUtils.toPx(getContext(), 200);
                if (originalStartX > screenWidth / 2) {
                    extraXOffset = extraXOffset * -1; // move it a bit to the left
                }
            }

            // make sure they aren't outside of the layout bounds and move them with the movementAmount
            // I move the x just a bit to the right or left here as well, because it just makes things look better
            startX = ensureWithinBounds(startX + extraXOffset, screenWidth, contentParams.width);
            startY =
                    ensureWithinBounds(startY + movementAmount, screenHeight, contentParams.height);

        } else {

            // center the Y around the start point
            startY -= contentParams.height / 2;

            // if X is in the right half, we want it to go left
            if (startX + contentParams.width + FINGER_SIZE > screenWidth) {
                startX -= contentParams.width;
                if (movementAmount > 0) {
                    movementAmount *= -1;
                }
            }

            // make sure they aren't outside of the layout bounds and move them with the movementAmount
            startX = ensureWithinBounds(startX + movementAmount, screenWidth, contentParams.width);
            startY = ensureWithinBounds(startY, screenHeight, contentParams.height);
        }

        // check to see if the system bars are covering anything

        int statusBar = NavigationUtils.getStatusBarHeight(getContext());
        if (startY < statusBar) { // if it is above the status bar and action bar
            startY = statusBar + 10;
        } else if (NavigationUtils.hasNavBar(getContext())
                && startY + contentParams.height > screenHeight - NavigationUtils.getNavBarHeight(
                getContext())) {
            // if there is a nav bar and the popup is underneath it
            startY = screenHeight - contentParams.height - NavigationUtils.getNavBarHeight(
                    getContext()) - DensityUtils.toDp(getContext(), 10);
        } else if (!NavigationUtils.hasNavBar(getContext())
                && startY + contentParams.height > screenHeight) {
            startY = screenHeight - contentParams.height - DensityUtils.toDp(getContext(), 10);
        }

        // set the newly computed distances from the start and top sides
        setDistanceFromLeft(startX);
        setDistanceFromTop(startY);
    }

    private int ensureWithinBounds(int value, int screenSize, int contentSize) {
        // check these against the layout bounds
        if (value < 0) {
            // if it is off the left side
            value = 10;
        } else if (value > screenSize - contentSize) {
            // if it is off the right side
            value = screenSize - contentSize - 10;
        }

        return value;
    }

    /**
     * Show the content of the PeekView by adding it to the android.R.id.content FrameLayout.
     */
    public void show() {
        androidContentView.addView(this);

        // set the translations for the content view
        content.setTranslationX(distanceFromLeft);
        content.setTranslationY(distanceFromTop);

        // animate the alpha of the PeekView
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, View.ALPHA, 0.0f, 1.0f);
        animator.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                if (callbacks != null) {
                    callbacks.shown();
                }
            }
        });
        animator.setDuration(options.useFadeAnimation() ? ANIMATION_TIME : 0);
        animator.setInterpolator(INTERPOLATOR);
        animator.start();
    }

    /**
     * Hide the PeekView and remove it from the android.R.id.content FrameLayout.
     */
    public void hide() {

        // animate with a fade
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, View.ALPHA, 1.0f, 0.0f);
        animator.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                // remove the view from the screen
                androidContentView.removeView(PeekView.this);

                if (callbacks != null) {
                    callbacks.dismissed();
                }
            }
        });
        animator.setDuration(options.useFadeAnimation() ? ANIMATION_TIME : 0);
        animator.setInterpolator(INTERPOLATOR);
        animator.start();

        Blurry.delete((ViewGroup) androidContentView.getRootView());

        if(remove != null)
            remove.onRemove();
    }

    public void setOnRemoveListener(OnRemove onRemove){
        this.remove = onRemove;
    }

    /**
     * Wrapper class so we only have to implement the onAnimationEnd method.
     */
    private abstract static class AnimatorEndListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {
        }

        @Override
        public void onAnimationCancel(Animator animator) {
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
        }
    }

    private enum Translation {HORIZONTAL, VERTICAL}
}
