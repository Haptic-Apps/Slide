package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.devspark.robototextview.util.RobotoTypefaceManager;

import java.util.List;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Visuals.FontPreferences;

/**
 * Class that provides methods to help bind submissions with
 * multiple blocks of text.
 */
public class CommentOverflow extends LinearLayout {
    private ColorPreferences colorPreferences;
    private Typeface typeface;

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
        typeface = RobotoTypefaceManager.obtainTypeface(
                context,
                new FontPreferences(context).getFontTypeComment().getTypeface());
    }

    /**
     * Set the text for the corresponding views.
     *
     * @param blocks list of all blocks to be set
     * @param subreddit
     */
    public void setViews(List<String> blocks, String subreddit) {
        removeAllViewsInLayout();

        Context context = getContext();

        if (!blocks.isEmpty()) {
            setVisibility(View.VISIBLE);
        }

        for (String block : blocks) {
            if (block.startsWith("<table>")) {
                HorizontalScrollView scrollView = new HorizontalScrollView(context);
                TableLayout table = formatTable(block, subreddit);
                scrollView.addView(table);
                scrollView.setPadding(0, 0, 8, 0);
                addView(scrollView);
            } else if (block.startsWith("<pre>")) {
                HorizontalScrollView scrollView = new HorizontalScrollView(context);
                SpoilerRobotoTextView newTextView = new SpoilerRobotoTextView(context);
                //textView.setMovementMethod(new MakeTextviewClickable.TextViewLinkHandler(c, subreddit, null));
                newTextView.setLinkTextColor(colorPreferences.getColor(subreddit));
                newTextView.setTypeface(typeface);
                newTextView.setText(block, TextView.BufferType.SPANNABLE);
                newTextView.setPadding(0, 0, 8, 0);
                scrollView.addView(newTextView);
                scrollView.setPadding(0, 0, 8, 0);
                addView(scrollView);
            } else {
                SpoilerRobotoTextView newTextView = new SpoilerRobotoTextView(context);
                //textView.setMovementMethod(new MakeTextviewClickable.TextViewLinkHandler(c, subreddit, null));
                newTextView.setLinkTextColor(colorPreferences.getColor(subreddit));
                newTextView.setTypeface(typeface);
                newTextView.setText(block, TextView.BufferType.SPANNABLE);
                newTextView.setPadding(0, 0, 8, 0);
                addView(newTextView);
            }
        }
    }

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
                //textView.setMovementMethod(new TextViewLinkHandler(context, subreddit, null));
                textView.setLinkTextColor(colorPreferences.getColor(subreddit));
                textView.setText(text.subSequence(columnStart, columnEnd), TextView.BufferType.SPANNABLE);
                textView.setPadding(3, 0 ,0 , 0);

                row.addView(textView);

                columnStart = 0;
                i += tableColumnEnd.length();
            } else {
                i += 1;
            }
        }

        return table;
    }
}
