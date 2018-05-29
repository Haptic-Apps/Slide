package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.HorizontalScrollView;

import java.util.List;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.SidebarLayout;
import me.ccrama.redditslide.Views.TitleTextView;
import me.ccrama.redditslide.util.SubmissionParser;

public class Announcement extends BaseActivity {

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);

    }

    @Override
    public void onCreate(Bundle savedInstance) {

        overridePendingTransition(R.anim.fade_in_real, 0);
        disableSwipeBackLayout();
        applyColorTheme();
        setTheme(R.style.popup);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        super.onCreate(savedInstance);
        setContentView(R.layout.submission_dialog);

        setViews(Reddit.appRestart.getString("page", ""), "NO SUB",
                (SpoilerRobotoTextView) findViewById(R.id.firstTextView),
                (CommentOverflow) findViewById(R.id.commentOverflow));
        ((TitleTextView) findViewById(R.id.title)).setText(
                Reddit.appRestart.getString("title", ""));

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.comments).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new OpenRedditLink(Announcement.this, Reddit.appRestart.getString("url", ""));
                finish();
            }
        });
    }

    private void setViews(String rawHTML, String subredditName, SpoilerRobotoTextView firstTextView,
            CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0), subredditName);
            firstTextView.setLinkTextColor(new ColorPreferences(this).getColor(subredditName));
            startIndex = 1;
        } else {
            firstTextView.setText("");
            firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                commentOverflow.setViews(blocks, subredditName);
            } else {
                commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName);
            }
            SidebarLayout sidebar = (SidebarLayout) findViewById(R.id.drawer_layout);
            for (int i = 0; i < commentOverflow.getChildCount(); i++) {
                View maybeScrollable = commentOverflow.getChildAt(i);
                if (maybeScrollable instanceof HorizontalScrollView) {
                    sidebar.addScrollable(maybeScrollable);
                }
            }
        } else {
            commentOverflow.removeAllViews();
        }
    }

}
