package me.ccrama.redditslide.SubmissionViews;

import android.app.Activity;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.TimeUtils;

/**
 * Created by carlo_000 on 2/27/2016.
 */
public class PopulateShadowboxInfo {
    public static void doActionbar(Submission s, View rootView, Activity c) {
        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView desc = (TextView) rootView.findViewById(R.id.desc);
        String distingush = "";
        if (s.getDistinguishedStatus() == DistinguishedStatus.MODERATOR)
            distingush = "[M]";
        else if (s.getDistinguishedStatus() == DistinguishedStatus.ADMIN)
            distingush = "[A]";

        title.setText(Html.fromHtml(s.getTitle()));

        String separator = c.getResources().getString(R.string.submission_properties_seperator);
        desc.setText(s.getSubredditName() + distingush + separator + TimeUtils.getTimeAgo(s.getCreated().getTime(), c));

        ((TextView) rootView.findViewById(R.id.comments)).setText("" + s.getCommentCount());
        ((TextView) rootView.findViewById(R.id.score)).setText("" + s.getScore());
    }
}
