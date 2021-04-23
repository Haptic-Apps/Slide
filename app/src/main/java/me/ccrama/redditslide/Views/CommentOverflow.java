package me.ccrama.redditslide.Views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.devspark.robototextview.RobotoTypefaces;

import java.util.List;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Visuals.ColorPreferences;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.util.DisplayUtil;

/**
 * Class that provides methods to help bind submissions with
 * multiple blocks of text.
 */
public class CommentOverflow extends LinearLayout {
    private ColorPreferences colorPreferences;
    private Typeface typeface = null;
    private              int                textColor;
    private              int                fontSize;
    private static final MarginLayoutParams COLUMN_PARAMS;
    private static final MarginLayoutParams MARGIN_PARAMS;
    private static final MarginLayoutParams HR_PARAMS;

    static {
        COLUMN_PARAMS = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        COLUMN_PARAMS.setMargins(0, 0, 32, 0);

        MARGIN_PARAMS = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        MARGIN_PARAMS.setMargins(0, 16, 0, 16);

        HR_PARAMS = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayUtil.dpToPxVertical(2));
        HR_PARAMS.setMargins(0, 16, 0, 16);
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
        setViews(blocks, subreddit, null, null);
    }

    /**
     * Set the text for the corresponding views.
     *
     * @param blocks    list of all blocks to be set
     * @param subreddit
     */
    public void setViews(List<String> blocks, String subreddit, OnClickListener click,
            OnLongClickListener longClick) {
        Context context = getContext();
        int type = new FontPreferences(context).getFontTypeComment().getTypeface();
        if (type >= 0) {
            typeface = RobotoTypefaces.obtainTypeface(context, type);
        } else {
            typeface = Typeface.DEFAULT;
        }
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.fontColor, typedValue, true);
        textColor = typedValue.data;
        TypedValue fontSizeTypedValue = new TypedValue();
        theme.resolveAttribute(R.attr.font_commentbody, fontSizeTypedValue, true);
        TypedArray a = context.obtainStyledAttributes(null, new int[]{R.attr.font_commentbody},
                R.attr.font_commentbody,
                new FontPreferences(context).getCommentFontStyle().getResId());
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
                TableLayout table = formatTable(block, subreddit, click, longClick);
                scrollView.setLayoutParams(MARGIN_PARAMS);
                table.setPaddingRelative(0, 0, 0, DisplayUtil.dpToPxVertical(10));
                scrollView.addView(table);
                addView(scrollView);
            } else if (block.equals("<hr/>")) {
                View line = new View(context);
                line.setLayoutParams(HR_PARAMS);
                line.setBackgroundColor(textColor);
                line.setAlpha(0.6f);
                addView(line);
            } else if (block.startsWith("<pre>")) {
                HorizontalScrollView scrollView = new HorizontalScrollView(context);
                scrollView.setScrollbarFadingEnabled(false);
                SpoilerRobotoTextView newTextView = new SpoilerRobotoTextView(context);
                newTextView.setTextHtml(block, subreddit);
                setStyle(newTextView, subreddit);
                scrollView.setLayoutParams(MARGIN_PARAMS);
                newTextView.setPaddingRelative(0, 0, 0, DisplayUtil.dpToPxVertical(10));
                scrollView.addView(newTextView);
                if (click != null) newTextView.setOnClickListener(click);
                if (longClick != null) newTextView.setOnLongClickListener(longClick);
                addView(scrollView);

            } else {
                SpoilerRobotoTextView newTextView = new SpoilerRobotoTextView(context);
                newTextView.setTextHtml(block, subreddit);
                setStyle(newTextView, subreddit);
                newTextView.setLayoutParams(MARGIN_PARAMS);
                if (click != null) newTextView.setOnClickListener(click);
                if (longClick != null) newTextView.setOnLongClickListener(longClick);
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
        return formatTable(text, subreddit, null, null);
    }

    private TableLayout formatTable(String text, String subreddit, OnClickListener click,
            OnLongClickListener longClick) {
        TableRow.LayoutParams rowParams =
                new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT);

        Context context = getContext();
        TableLayout table = new TableLayout(context);
        TableLayout.LayoutParams params =
                new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);
        table.setLayoutParams(params);

        final String tableStart = "<table>";
        final String tableEnd = "</table>";
        final String tableHeadStart = "<thead>";
        final String tableHeadEnd = "</thead>";
        final String tableRowStart = "<tr>";
        final String tableRowEnd = "</tr>";
        final String tableColumnStart = "<td>";
        final String tableColumnEnd = "</td>";
        final String tableColumnStartLeft = "<td align=\"left\">";
        final String tableColumnStartRight = "<td align=\"right\">";
        final String tableColumnStartCenter = "<td align=\"center\">";
        final String tableHeaderStart = "<th>";
        final String tableHeaderStartLeft = "<th align=\"left\">";
        final String tableHeaderStartRight = "<th align=\"right\">";
        final String tableHeaderStartCenter = "<th align=\"center\">";
        final String tableHeaderEnd = "</th>";

        int i = 0;
        int columnStart = 0;
        int columnEnd;
        int gravity = Gravity.START;
        boolean columnStarted = false;

        TableRow row = null;

        while (i < text.length()) {
            if (text.charAt(i) != '<') { // quick check otherwise it falls through to else
                i += 1;
            } else if (text.subSequence(i, i + tableStart.length()).toString().equals(tableStart)) {
                i += tableStart.length();
            } else if (text.subSequence(i, i + tableHeadStart.length())
                    .toString()
                    .equals(tableHeadStart)) {
                i += tableHeadStart.length();
            } else if (text.subSequence(i, i + tableRowStart.length())
                    .toString()
                    .equals(tableRowStart)) {
                row = new TableRow(context);
                row.setLayoutParams(rowParams);
                i += tableRowStart.length();
            } else if (text.subSequence(i, i + tableRowEnd.length())
                    .toString()
                    .equals(tableRowEnd)) {
                table.addView(row);
                i += tableRowEnd.length();
            } else if (text.subSequence(i, i + tableEnd.length()).toString().equals(tableEnd)) {
                i += tableEnd.length();
            } else if (text.subSequence(i, i + tableHeadEnd.length())
                    .toString()
                    .equals(tableHeadEnd)) {
                i += tableHeadEnd.length();
            } else if (!columnStarted
                    && i + tableColumnStart.length() < text.length()
                    && (text.subSequence(i, i + tableColumnStart.length())
                    .toString()
                    .equals(tableColumnStart) || text.subSequence(i, i + tableHeaderStart.length())
                    .toString()
                    .equals(tableHeaderStart))) {
                columnStarted = true;
                gravity = Gravity.START;
                i += tableColumnStart.length();
                columnStart = i;
            } else if (!columnStarted && i + tableColumnStartRight.length() < text.length() && (text
                    .subSequence(i, i + tableColumnStartRight.length())
                    .toString()
                    .equals(tableColumnStartRight) || text.subSequence(i,
                    i + tableHeaderStartRight.length()).toString().equals(tableHeaderStartRight))) {
                columnStarted = true;
                gravity = Gravity.END;
                i += tableColumnStartRight.length();
                columnStart = i;
            } else if (!columnStarted && i + tableColumnStartCenter.length() < text.length() && (
                    text.subSequence(i, i + tableColumnStartCenter.length())
                            .toString()
                            .equals(tableColumnStartCenter)
                            || text.subSequence(i, i + tableHeaderStartCenter.length())
                            .toString()
                            .equals(tableHeaderStartCenter))) {
                columnStarted = true;
                gravity = Gravity.CENTER;
                i += tableColumnStartCenter.length();
                columnStart = i;
            } else if (!columnStarted
                    && i + tableColumnStartLeft.length() < text.length()
                    && (text.subSequence(i, i + tableColumnStartLeft.length())
                    .toString()
                    .equals(tableColumnStartLeft) || text.subSequence(i,
                    i + tableHeaderStartLeft.length()).toString().equals(tableHeaderStartLeft))) {
                columnStarted = true;
                gravity = Gravity.START;
                i += tableColumnStartLeft.length();
                columnStart = i;
            } else if (text.substring(i).startsWith("<td")) {
                // case for <td colspan="2"  align="left">
                // See last table in https://www.reddit.com/r/GlobalOffensive/comments/51s3r8/virtuspro_vs_vgcyberzen_sl_ileague_s2_finals/
                columnStarted = true;
                i += text.substring(i).indexOf(">") + 1;
                columnStart = i;
            } else if (text.subSequence(i, i + tableColumnEnd.length())
                    .toString()
                    .equals(tableColumnEnd) || text.subSequence(i, i + tableHeaderEnd.length())
                    .toString()
                    .equals(tableHeaderEnd)) {
                columnEnd = i;

                SpoilerRobotoTextView textView = new SpoilerRobotoTextView(context);
                textView.setTextHtml(text.subSequence(columnStart, columnEnd), subreddit);
                setStyle(textView, subreddit);
                textView.setLayoutParams(COLUMN_PARAMS);
                textView.setGravity(gravity);
                if (click != null) textView.setOnClickListener(click);
                if (longClick != null) textView.setOnLongClickListener(longClick);
                if (text.subSequence(i, i + tableHeaderEnd.length())
                        .toString()
                        .equals(tableHeaderEnd)) {
                    textView.setTypeface(null, Typeface.BOLD);
                }
                if (row != null) {
                    row.addView(textView);
                }

                columnStart = 0;
                columnStarted = false;
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
        if (typeface != null) textView.setTypeface(typeface);
        textView.setLinkTextColor(colorPreferences.getColor(subreddit));
    }
}
