package me.ccrama.redditslide.ForceTouch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import me.ccrama.redditslide.R;


public class PeekViewActivity extends AppCompatActivity {

    private PeekView peekView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        removePeek(null);
    }

    float origY;


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (peekView != null && event.getAction() == MotionEvent.ACTION_UP) {

            // the user lifted their finger, so we are going to remove the peek view
            removePeek(event);

            return false;
        } else if (peekView != null) {
            peekView.doScroll(event);
               /* todo FrameLayout.LayoutParams params =
                        (FrameLayout.LayoutParams) peekView.content.findViewById(R.id.content_area).getLayoutParams();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        params.topMargin = (int) (origY - event.getRawY() / 10);
                        peekView.setLayoutParams(params);
                        break;
                }*/


            // we don't want to pass along the touch event or else it will just scroll under the PeekView

            return false;
        }

        return super.dispatchTouchEvent(event);
    }

    public void showPeek(final PeekView view, float origY) {
        peekView = view;
        peekView.show();
        this.origY = origY;
    }

    public void removePeek(MotionEvent event) {
        if (peekView != null) {
            if (event != null) peekView.checkButtons(event);
            peekView.hide();
            peekView = null;
        }
    }
}
