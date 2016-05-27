package me.ccrama.redditslide.Views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.devspark.robototextview.util.RobotoTypefaceManager;

import java.util.List;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Visuals.FontPreferences;

/**
 * Class that provides methods to help bind submissions with
 * multiple blocks of text.
 */
public class CommentOverflow extends LinearLayout {
    private ColorPreferences colorPreferences;
    private Typeface typeface = null;
    private int textColor;
    private int fontSize;
    private static final MarginLayoutParams COLUMN_PARAMS;
    private static final MarginLayoutParams MARGIN_PARAMS;

    static {
        COLUMN_PARAMS = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        COLUMN_PARAMS.setMargins(0, 0, 32, 0);

        MARGIN_PARAMS = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        MARGIN_PARAMS.setMargins(0, 16, 0, 16);
    }

    public CommentOverflow(Context context) {
        super(context);
        init(context);
    }

    public CommentOverflow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CommentOverflow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        colorPreferences = new ColorPreferences(context);
    }

    /**
     * Set the text for the corresponding views.
     *
     * @param blocks    list of all blocks to be set
     * @param subreddit
     */
    public void setViews(List<String> blocks, String subreddit) {
        Context context = getContext();
        int type = new FontPreferences(context).getFontTypeComment().getTypeface();
        if (type >= 0) {
            typeface = RobotoTypefaceManager.obtainTypeface(
                    context,
                    type);
        }
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.font, typedValue, true);
        textColor = typedValue.data;
        TypedValue fontSizeTypedValue = new TypedValue();
        theme.resolveAttribute(R.attr.font_commentbody, fontSizeTypedValue, true);
        TypedArray a = context.obtainStyledAttributes(null, new int[]{R.attr.font_commentbody}, R.attr.font_commentbody, new FontPreferences(context).getCommentFontStyle().getResId());
        fontSize = a.getDimensionPixelSize(0, -1);
        a.recycle();
        removeAllViews();

        if (!blocks.isEmpty()) {
            setVisibility(View.VISIBLE);
        }


        for (String block : blocks) {
            if (block.startsWith("<table>")) {
                HorizontalScrollView scrollView = new HorizontalScrollView(context);
                scrollView.setScrollbarFadingEnabled(false);
                TableLayout table = formatTable(block, subreddit);
                scrollView.setLayoutParams(MARGIN_PARAMS);
                table.setPaddingRelative(0, 0, 0, Reddit.dpToPxVertical(10));
                scrollView.addView(table);
                addView(scrollView);

            } else if (block.startsWith("<pre>")) {
                HorizontalScrollView scrollView = new HorizontalScrollView(context);
                scrollView.setScrollbarFadingEnabled(false);
                SpoilerRobotoTextView newTextView = new SpoilerRobotoTextView(context);
                newTextView.setTextHtml(block, subreddit);
                setStyle(newTextView, subreddit);
                scrollView.setLayoutParams(MARGIN_PARAMS);
                newTextView.setPaddingRelative(0, 0, 0, Reddit.dpToPxVertical(10));
                scrollView.addView(newTextView);
                addView(scrollView);

            } else {
                SpoilerRobotoTextView newTextView = new SpoilerRobotoTextView(context);
                newTextView.setTextHtml(block, subreddit);
                setStyle(newTextView, subreddit);
                newTextView.setLayoutParams(MARGIN_PARAMS);
                addView(newTextView);
            }
        }
    }

    /*todo: possibly fix tapping issues, better method required (this disables scrolling the HorizontalScrollView)
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return false;
    }*/
    private TableLayout formatTable(String text, String subreddit) {
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        Context context = getContext();
        TableLayout table = new TableLayout(context);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        table.setLayoutParams(params);

        final String tableStart = "<table>";
        final String tableEnd = "</table>";
        final String tableHeadStart = "<thead>";
        final String tableHeadEnd = "</thead>";
        final String tableRowStart = "<tr>";
        final String tableRowEnd = "</tr>";
        final String tableColumnStart = "<td>";
        final String tableColumnEnd = "</td>";
        final String tableHeaderStart = "<th>";
        final String tableHeaderEnd = "</th>";

        int i = 0;
        int columnStart = 0;
        int columnEnd;
        TableRow row = null;
        while (i < text.length()) {
            if (text.charAt(i) != '<') { // quick check otherwise it falls through to else
                i += 1;
            } else if (text.subSequence(i, i + tableStart.length()).toString().equals(tableStart)) {
                i += tableStart.length();
            } else if (text.subSequence(i, i + tableHeadStart.length()).toString().equals(tableHeadStart)) {
                i += tableHeadStart.length();
            } else if (text.subSequence(i, i + tableRowStart.length()).toString().equals(tableRowStart)) {
                row = new TableRow(context);
                row.setLayoutParams(rowParams);
                i += tableRowStart.length();
            } else if (text.subSequence(i, i + tableRowEnd.length()).toString().equals(tableRowEnd)) {
                table.addView(row);
                i += tableRowEnd.length();
            } else if (text.subSequence(i, i + tableEnd.length()).toString().equals(tableEnd)) {
                i += tableEnd.length();
            } else if (text.subSequence(i, i + tableHeadEnd.length()).toString().equals(tableHeadEnd)) {
                i += tableHeadEnd.length();
            } else if (text.subSequence(i, i + tableColumnStart.length()).toString().equals(tableColumnStart)
                    || text.subSequence(i, i + tableHeaderStart.length()).toString().equals(tableHeaderStart)) {
                i += tableColumnStart.length();
                columnStart = i;
            } else if (text.subSequence(i, i + tableColumnEnd.length()).toString().equals(tableColumnEnd)
                    || text.subSequence(i, i + tableHeaderEnd.length()).toString().equals(tableHeaderEnd)) {
                columnEnd = i;

                SpoilerRobotoTextView textView = new SpoilerRobotoTextView(context);
                textView.setTextHtml(text.subSequence(columnStart, columnEnd), subreddit);
                setStyle(textView, subreddit);
                textView.setLayoutParams(COLUMN_PARAMS);

                row.addView(textView);

                columnStart = 0;
                i += tableColumnEnd.length();
            } else {
                i += 1;
            }
        }

        return table;
    }

    private void setStyle(SpoilerRobotoTextView textView, String subreddit) {
        textView.setTextColor(textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        if (typeface != null)
            textView.setTypeface(typeface);
        textView.setLinkTextColor(colorPreferences.getColor(subreddit));
    }
}
