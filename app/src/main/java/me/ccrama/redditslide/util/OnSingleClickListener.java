package me.ccrama.redditslide.util;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;

/**
 * Implementation of {@link View.OnClickListener} that ignores subsequent clicks that happen too quickly after the first one.<br/>
 * To use this class, implement {@link #onSingleClick(View)} instead of {@link View.OnClickListener#onClick(View)}.
 */
public abstract class OnSingleClickListener implements View.OnClickListener {
    
    private static final long MIN_DELAY_MS = 300;
    private static final String TAG = OnSingleClickListener.class.getSimpleName();
    private static long mLastClickTime;
    public static boolean override;

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        final long lastClickTime = mLastClickTime;
        final long now = SystemClock.uptimeMillis(); //guaranteed 100% monotonic

        if (now - lastClickTime < MIN_DELAY_MS && !override) {
            // Too fast: ignore
            Log.d(TAG, "onClick Clicked too quickly: ignored");
        } else {
            override = false;
            // Update mLastClickTime and register the click
            mLastClickTime = now;
            onSingleClick(v);
        }
    }

//    /**
//     * Wraps an {@link View.OnClickListener} into an {@link OnSingleClickListener}.
//     * The argument's {@link View.OnClickListener#onClick(View)} method will be called when a single click is registered.
//     *
//     * @param onClickListener The listener to wrap.
//     * @return the wrapped listener.
//     */
//    public static View.OnClickListener wrap(final View.OnClickListener onClickListener) {
//        return new OnSingleClickListener() {
//            @Override
//            public void onSingleClick(View v) {
//                onClickListener.onClick(v);
//            }
//        };
//    }
}
