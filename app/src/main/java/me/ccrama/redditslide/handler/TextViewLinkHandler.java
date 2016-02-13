package me.ccrama.redditslide.handler;

import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import me.ccrama.redditslide.ClickableText;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.util.LogUtil;

public class TextViewLinkHandler extends LinkMovementMethod {
    private final ClickableText clickableText;
    String subreddit;
    SpoilerRobotoTextView comm;
    Spannable sequence;
    float position;
    boolean clickHandled;
    Handler handler;
    Runnable longClicked;
    URLSpan[] link;

    public TextViewLinkHandler(ClickableText clickableText, String subreddit, Spannable sequence) {
        this.clickableText = clickableText;
        this.subreddit = subreddit;
        this.sequence = sequence;

        clickHandled = false;
        handler = new Handler();
        longClicked = new Runnable() {
            @Override
            public void run() {
                // long click
                clickHandled = true;
                handler.removeCallbacksAndMessages(null);
                TextViewLinkHandler.this.clickableText.onLinkLongClick(link[0].getURL());
            }
        };
    }

    @Override
    public boolean onTouchEvent(TextView widget, final Spannable buffer, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            position = event.getY(); //used to see if the user scrolled or not
        if (!(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_DOWN)) {
            if(Math.abs((position - event.getY())) > 10){
                handler.removeCallbacksAndMessages(null);
                Log.v(LogUtil.getTag(), "POSITION NOT CLICK IS " + event.getY());
            }
            return super.onTouchEvent(widget, buffer, event);
        }

        Log.v(LogUtil.getTag(), "POSITION IS " + position);

        comm = (SpoilerRobotoTextView) widget;

        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= widget.getTotalPaddingLeft();
        y -= widget.getTotalPaddingTop();

        x += widget.getScrollX();
        y += widget.getScrollY();

        Layout layout = widget.getLayout();
        int line = layout.getLineForVertical(y);
        final int off = layout.getOffsetForHorizontal(line, x);

        link = buffer.getSpans(off, off, URLSpan.class);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clickHandled = false;
                if (link.length != 0) {
                    handler.postDelayed(longClicked,
                            android.view.ViewConfiguration.getLongPressTimeout());
                }
                break;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacksAndMessages(null);

                if (!clickHandled ) {
                    // regular click
                    if (link.length != 0) {
                        int i = 0;
                        if (sequence != null) {
                            i = sequence.getSpanEnd(link[0]);
                        }
                        clickableText.onLinkClick(link[0].getURL(), i, subreddit);
                    }
                }
                break;
        }
        return true;
    }

}
