package me.ccrama.redditslide.handler;

import android.os.Handler;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.BaseMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import me.ccrama.redditslide.ClickableText;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;

public class TextViewLinkHandler extends BaseMovementMethod {
    private final ClickableText clickableText;
    final         String        subreddit;
    SpoilerRobotoTextView comm;
    final Spannable sequence;
    float                 position;
    boolean               clickHandled;
    final Handler  handler;
    final Runnable longClicked;
    URLSpan[]             link;
    MotionEvent           event;

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
                if (link != null && link.length > 0 && link[0] != null) {
                    TextViewLinkHandler.this.clickableText.onLinkLongClick(link[0].getURL(), event);
                }

            }
        };
    }

    @Override
    public boolean canSelectArbitrarily() {
        return false;
    }

    @Override
    public boolean onTouchEvent(TextView widget, final Spannable buffer, MotionEvent event) {
        comm = (SpoilerRobotoTextView) widget;

        int x = (int) event.getX();
        int y = (int) event.getY();
        x -= widget.getTotalPaddingLeft();
        y -= widget.getTotalPaddingTop();
        x += widget.getScrollX();
        y += widget.getScrollY();

        Layout layout = widget.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        link = buffer.getSpans(off, off, URLSpan.class);
        if (link.length > 0) {
            comm.setLongClickable(false);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                position = event.getY(); //used to see if the user scrolled or not
            }
            if (!(event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_DOWN)) {
                if (Math.abs((position - event.getY())) > 25) {
                    handler.removeCallbacksAndMessages(null);
                }
                return super.onTouchEvent(widget, buffer, event);
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    clickHandled = false;
                    this.event = event;
                    if (SettingValues.peek) {
                        handler.postDelayed(longClicked,
                                android.view.ViewConfiguration.getTapTimeout() + 50);
                    } else {
                        handler.postDelayed(longClicked,
                                android.view.ViewConfiguration.getLongPressTimeout());
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    comm.setLongClickable(true);
                    handler.removeCallbacksAndMessages(null);

                    if (!clickHandled) {
                        // regular click
                        if (link.length != 0) {
                            int i = 0;
                            if (sequence != null) {
                                i = sequence.getSpanEnd(link[0]);
                            }
                            if (!link[0].getURL().isEmpty()) {
                                clickableText.onLinkClick(link[0].getURL(), i, subreddit, link[0]);
                            }
                        } else {
                            return false;
                        }
                    }
                    break;
            }
            return true;

        } else {
            Selection.removeSelection(buffer);
            return false;
        }
    }

}
