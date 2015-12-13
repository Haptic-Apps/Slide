package me.ccrama.redditslide;

/**
 * Created by ccrama on 9/26/2015.
 */
/*
 * Copyright 2012 Laurence Dawson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.URI;

public class ActiveTextView extends TextView {

    private boolean mLinkSet;
    private String mUrl;
    private boolean spoilerClicked = false;
    private SpannableStringBuilder mSpannable;
    private ActiveTextView.OnLinkClickedListener mListener;
    private ActiveTextView.OnLongPressedLinkListener mLongPressedLinkListener;

    public ActiveTextView(final Context context) {
        super(context);
        setup();
    }

    /**
     * Returns whether the element that triggered the clicked event
     * was from a spoiler link.
     * @return
     */
    public boolean isSpoilerClicked() {
        return spoilerClicked;
    }

    public void resetSpoilerClicked() {
        spoilerClicked = false;
    }

    public ActiveTextView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ActiveTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    public static String getDomainName(String url) {
        URI uri;
        try {
            uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (Exception e) {
            e.printStackTrace();
            return url;
        }

    }

    private boolean isLinkPending() {
        return mLinkSet;
    }

    private void cancelLink() {
        mLinkSet = false;
    }

    // When a link is clicked, stop the view from drawing the touched drawable
    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] states;
        if (mLinkSet) {
            states = Button.EMPTY_STATE_SET;
            return states;
        } else {
            return super.onCreateDrawableState(extraSpace);
        }
    }

    // Implemented to stop the following bug with TextViews in Jelly Bean
    // http://code.google.com/p/android/issues/detail?id=34872
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (IndexOutOfBoundsException e) {
            if (getText() instanceof SpannedString) {
                SpannedString s = (SpannedString) getText();
                mSpannable.clear();
                mSpannable.append(s);
                StyleSpan[] a = s.getSpans(0, s.length(), StyleSpan.class);
                if (a.length > 0) {
                    mSpannable.removeSpan(a[0]);
                    setText(mSpannable);
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                } else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            } else if (getText() instanceof SpannableString) {
                SpannableString s = (SpannableString) getText();
                mSpannable.clear();
                mSpannable.append(s);
                StyleSpan[] a = s.getSpans(0, s.length(), StyleSpan.class);
                if (a.length > 0) {
                    mSpannable.removeSpan(a[0]);
                    setText(mSpannable);

                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                } else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        }
    }

    private void setup() {
        mSpannable = new SpannableStringBuilder();
        // Set the movement method
        setMovementMethod(new LinkMovementMethod() {
            @Override
            public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {

                int action = event.getAction();

                if (action == MotionEvent.ACTION_UP ||
                        action == MotionEvent.ACTION_DOWN) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    x -= widget.getTotalPaddingLeft();
                    y -= widget.getTotalPaddingTop();

                    x += widget.getScrollX();
                    y += widget.getScrollY();

                    Layout layout = widget.getLayout();
                    int line = layout.getLineForVertical(y);
                    int off = layout.getOffsetForHorizontal(line, x);
                    float maxLineRight = layout.getLineWidth(line);

                    // Stops the space after a link being clicked
                    if (x <= maxLineRight) {
                        ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);

                        if (links.length != 0) {
                            if (action == MotionEvent.ACTION_UP) {
                                // If a link click listener is set, call that
                                // Otherwise just open the link
                                if (mLinkSet) {
                                    if (mListener != null) {
                                        spoilerClicked = true;
                                        int urlEnd = buffer.getSpanEnd(links[0]);
                                        mListener.onClick(mUrl, urlEnd);
                                    } else {
                                        if (mUrl != null) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse(mUrl));
                                            getContext().startActivity(intent);
                                        }
                                    }
                                }
                                Selection.removeSelection(buffer);
                                return true;
                            } else if (action == MotionEvent.ACTION_DOWN) {
                                Selection.setSelection(buffer,
                                        buffer.getSpanStart(links[0]),
                                        buffer.getSpanEnd(links[0]));
                                URLSpan s = (URLSpan) links[0];
                                mUrl = s.getURL();
                                mLinkSet = true;
                                return true;
                            }
                        } else {
                            Selection.removeSelection(buffer);
                        }
                    }
                }
                return false;
            }
        });

        setLongClickable(true);
        setFocusable(false);
        setClickable(false);

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (mLongPressedLinkListener != null) {
                    if (isLinkPending()) {
                        // Create the dialog
                        /*todo
                        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getContext());
                        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
                        final View dialoglayout = inflater.inflate(R.layout.linkmenu, null);
                        ((TextView)dialoglayout.findViewById(R.id.title)).setText(getDomainName(mUrl));
                        ((TextView)dialoglayout.findViewById(R.id.subtitle)).setText(mUrl);

                        dialoglayout.findViewById(R.id.external).setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(mUrl));
                                getContext().startActivity(i);
                            }
                        });
                        dialoglayout.findViewById(R.id.internal).setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                callOnClick();
                            }
                        });
                        dialoglayout.findViewById(R.id.share).setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                                share.putExtra(android.content.Intent.EXTRA_SUBJECT, mUrl);
                                share.putExtra(android.content.Intent.EXTRA_TEXT, mUrl);
                                share.setType("text/plain");
                                getContext().startActivity(Intent.createChooser(share, "Share"));
                            }
                        });
                        builder.setView(dialoglayout);

                        Dialog alert = builder.create();
                        alert.setCanceledOnTouchOutside(true);
                        alert.show();*/
                    } else {
                        mLongPressedLinkListener.onLongPressed();
                    }

                    cancelLink();
                    return true;
                } else {
                    cancelLink();
                    return false;
                }
            }
        });

        // Provide an easier interface for the parent view
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLinkPending() && mListener != null) {
                    mListener.onClick(null, -1);
                }
                cancelLink();
            }
        });
    }

    /**
     * Set a link click listener, this is called when a user clicks on a link
     *
     * @param clickListener The click listener to call when a link is clicked
     */
    public void setLinkClickedListener(ActiveTextView.OnLinkClickedListener clickListener) {
        this.mListener = clickListener;
    }

    /**
     * Set a long press listener, this is called when a user long presses on a link
     * a small submenu with a few options is then displayed
     *
     * @param minDisplay Enable a smaller submenu when long pressed (removes the option Long press parent)
     */
    public void setLongPressedLinkListener(ActiveTextView.OnLongPressedLinkListener longPressedLinkListener, boolean minDisplay) {
        this.mLongPressedLinkListener = longPressedLinkListener;
        boolean mDisplayMinLongPress = minDisplay;
    }

    public interface OnLinkClickedListener {
        /**
         * Perform an action due to a short click event. The <code>xOffset</code>
         * parameter is help deal with spans associated with spoiler links.
         * @param url the url for a given link.
         * @param xOffset the last character index of the clicked link
         */
        void onClick(String url, int xOffset);
    }

    // Called when a link is long clicked
    public interface OnLongPressedLinkListener {
        void onLongPressed();
    }
}