package me.ccrama.redditslide.ForceTouch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;


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
    int twelve = Reddit.dpToPxVertical(12);

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_UP){
            Reddit.peek = false;
        }
        if (peekView != null && event.getAction() == MotionEvent.ACTION_UP) {

            // the user lifted their finger, so we are going to remove the peek view
            removePeek(event);

            return false;
        } else if (peekView != null) {
            // peekView.doScroll(event);
            peekView.highlightMenu(event);
            View peek = peekView.content.findViewById(R.id.peek);
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) peek.getLayoutParams();

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:

                    params.topMargin = (int) -((origY - event.getY()) / 5);
                    if( false && event.getY() < (2* origY) / 3) {
                        params.leftMargin = twelve - (int) ((origY - event.getY())) / 2;
                        params.rightMargin =  twelve -(int)((origY - event.getY()) )  / 2;
                    } else {
                        params.leftMargin = twelve;
                        params.rightMargin = twelve;
                    }

                    if (event.getY() < (1 * origY) / 2) {
                        Reddit.peek = true;
                        peekView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        peekView.pop();
                        removePeek(event);
                    }
                    peek.setLayoutParams(params);
                    break;
            }
            // we don't want to pass along the touch event or else it will just scroll under the PeekView
            return false;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && Reddit.peek) {
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
