package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.bottomsheet.BottomSheet;
import com.devspark.robototextview.util.RobotoTypefaceManager;
import com.mikepenz.itemanimators.AlphaInAnimator;
import com.mikepenz.itemanimators.SlideRightAlphaAnimator;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.Drafts;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.UserTags;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Views.PreCachingLayoutManagerComments;
import me.ccrama.redditslide.Views.RoundedBackgroundSpan;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.Vote;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;
import me.ccrama.redditslide.util.SubmissionParser;


public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final static int HEADER = 1;
    private final int SPACER = 6;
    public Context mContext;
    public SubmissionComments dataSet;
    public Submission submission;
    public CommentViewHolder currentlySelected;
    public String currentSelectedItem = "";
    public int shiftFrom;
    public FragmentManager fm;
    public int clickpos;
    public int currentPos;
    public CommentViewHolder isHolder;
    public boolean isClicking;
    public HashMap<String, Integer> keys = new HashMap<>();
    public ArrayList<CommentObject> users;
    public ArrayList<String> deleted = new ArrayList<>();
    RecyclerView listView;
    ArrayList<String> up;
    ArrayList<String> down;
    CommentPage mPage;
    boolean isSame;
    int shifted;
    int toShiftTo;
    ArrayList<String> hidden;
    ArrayList<String> hiddenPersons;
    ArrayList<String> toCollapse;
    ArrayList<String> replie;
    private String backedText = "";
    private String currentlyEditingId = "";

    public <T extends Contribution> void showModBottomSheet(final Context mContext, final CommentNode baseNode, final Comment comment, final CommentViewHolder holder, final Map<String, Integer> reports, final Map<String, String> reports2) {

        int[] attrs = new int[]{R.attr.tint};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
        final Drawable report = mContext.getResources().getDrawable(R.drawable.report);
        final Drawable approve = mContext.getResources().getDrawable(R.drawable.support);
        final Drawable nsfw = mContext.getResources().getDrawable(R.drawable.hide);
        final Drawable pin = mContext.getResources().getDrawable(R.drawable.lock);
        final Drawable distinguish = mContext.getResources().getDrawable(R.drawable.iconstarfilled);
        final Drawable remove = mContext.getResources().getDrawable(R.drawable.close);

        profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        report.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        approve.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        nsfw.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        distinguish.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        remove.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        pin.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        ta.recycle();

        BottomSheet.Builder b = new BottomSheet.Builder((Activity) mContext)
                .title(Html.fromHtml(comment.getBody()));

        int reportCount = reports.size() + reports2.size();

        if (reportCount == 0) {
            b.sheet(0, report, "No reports");
        } else {
            b.sheet(0, report, "View " + reportCount + " reports");
        }

        boolean approved = false;
        String whoApproved = "";
        if (comment.getDataNode().get("approved_by").asText().equals("null")) {
            b.sheet(1, approve, "Approve comment");
        } else {
            approved = true;
            whoApproved = comment.getDataNode().get("approved_by").asText();
            b.sheet(1, approve, "Approved by /u/" + whoApproved);
        }

        // b.sheet(2, spam, mContext.getString(R.string.mod_btn_spam)) todo this


        final boolean stickied = comment.getDataNode().has("stickied") && comment.getDataNode().get("stickied").asBoolean();
        if (baseNode.isTopLevel())
            if (!stickied) {
                b.sheet(4, pin, "Sticky comment");
            } else {
                b.sheet(4, pin, "Un-sticky comment");
            }

        final boolean distinguished = !comment.getDataNode().get("distinguished").isNull();
        if (comment.getAuthor().equalsIgnoreCase(Authentication.name)) {
            if (!distinguished) {
                b.sheet(9, distinguish, "Distinguish comment");
            } else {
                b.sheet(9, distinguish, "Un-distinguish comment");
            }
        }


        final String finalWhoApproved = whoApproved;
        final boolean finalApproved = approved;
        b.sheet(6, remove, mContext.getString(R.string.btn_remove))
                .sheet(8, profile, "Author profile")
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                new AsyncTask<Void, Void, ArrayList<String>>() {
                                    @Override
                                    protected ArrayList<String> doInBackground(Void... params) {

                                        ArrayList<String> finalReports = new ArrayList<>();
                                        for (Map.Entry<String, Integer> entry : reports.entrySet()) {
                                            finalReports.add("x" + entry.getValue() + " " + entry.getKey());
                                        }
                                        for (Map.Entry<String, String> entry : reports2.entrySet()) {
                                            finalReports.add(entry.getKey() + ": " + entry.getValue());
                                        }
                                        if (finalReports.isEmpty()) {
                                            finalReports.add(mContext.getString(R.string.mod_no_reports));
                                        }
                                        return finalReports;
                                    }

                                    @Override
                                    public void onPostExecute(ArrayList<String> data) {
                                        new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_reports).setItems(data.toArray(new CharSequence[data.size()]),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                }).show();
                                    }
                                }.execute();

                                break;
                            case 1:
                                if (finalApproved) {
                                    Intent i = new Intent(mContext, Profile.class);
                                    i.putExtra(Profile.EXTRA_PROFILE, finalWhoApproved);
                                    mContext.startActivity(i);
                                } else {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_approve)
                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {

                                                    new AsyncTask<Void, Void, Boolean>() {

                                                        @Override
                                                        public void onPostExecute(Boolean b) {
                                                            if (b) {
                                                                dialog.dismiss();
                                                                Snackbar.make(holder.itemView, R.string.mod_approved, Snackbar.LENGTH_LONG).show();

                                                            } else {
                                                                new AlertDialogWrapper.Builder(mContext)
                                                                        .setTitle(R.string.err_general)
                                                                        .setMessage(R.string.err_retry_later).show();
                                                            }
                                                        }

                                                        @Override
                                                        protected Boolean doInBackground(Void... params) {
                                                            try {
                                                                new ModerationManager(Authentication.reddit).approve(comment);
                                                            } catch (ApiException e) {
                                                                e.printStackTrace();
                                                                return false;

                                                            }
                                                            return true;
                                                        }
                                                    }.execute();

                                                }
                                            }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).show();
                                }
                                break;
                            case 2:
                                //todo this
                                break;

                            case 4:
                                if (stickied) {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.unsticky_comment)
                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {

                                                    new AsyncTask<Void, Void, Boolean>() {

                                                        @Override
                                                        public void onPostExecute(Boolean b) {

                                                            if (b) {
                                                                dialog.dismiss();

                                                                Snackbar s = Snackbar.make(holder.itemView, R.string.comment_unstickied, Snackbar.LENGTH_LONG);
                                                                View view = s.getView();
                                                                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                tv.setTextColor(Color.WHITE);
                                                                s.show();


                                                            } else {
                                                                new AlertDialogWrapper.Builder(mContext)
                                                                        .setTitle(R.string.err_general)
                                                                        .setMessage(R.string.err_retry_later).show();
                                                            }
                                                        }

                                                        @Override
                                                        protected Boolean doInBackground(Void... params) {
                                                            try {
                                                                new ModerationManager(Authentication.reddit).setSticky(comment, false);
                                                            } catch (ApiException e) {
                                                                e.printStackTrace();
                                                                return false;

                                                            }
                                                            return true;
                                                        }
                                                    }.execute();

                                                }
                                            }).setNegativeButton(R.string.btn_no, null).show();
                                } else {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.sticky_comment)
                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {

                                                    new AsyncTask<Void, Void, Boolean>() {

                                                        @Override
                                                        public void onPostExecute(Boolean b) {
                                                            if (b) {
                                                                dialog.dismiss();
                                                                Snackbar s = Snackbar.make(holder.itemView, R.string.comment_stickied, Snackbar.LENGTH_LONG);
                                                                View view = s.getView();
                                                                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                tv.setTextColor(Color.WHITE);
                                                                s.show();


                                                            } else {
                                                                new AlertDialogWrapper.Builder(mContext)
                                                                        .setTitle(R.string.err_general)
                                                                        .setMessage(R.string.err_retry_later).show();
                                                            }
                                                        }

                                                        @Override
                                                        protected Boolean doInBackground(Void... params) {
                                                            try {
                                                                new ModerationManager(Authentication.reddit).setSticky(comment, true);
                                                            } catch (ApiException e) {
                                                                e.printStackTrace();
                                                                return false;

                                                            }
                                                            return true;
                                                        }
                                                    }.execute();

                                                }
                                            }).setNegativeButton(R.string.btn_no, null).show();
                                }

                                break;
                            case 9:
                                if (distinguished) {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.undistinguish_comment)
                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {

                                                    new AsyncTask<Void, Void, Boolean>() {

                                                        @Override
                                                        public void onPostExecute(Boolean b) {
                                                            if (b) {
                                                                dialog.dismiss();

                                                                Snackbar s = Snackbar.make(holder.itemView, R.string.comment_undistinguished, Snackbar.LENGTH_LONG);
                                                                View view = s.getView();
                                                                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                tv.setTextColor(Color.WHITE);
                                                                s.show();

                                                            } else {
                                                                new AlertDialogWrapper.Builder(mContext)
                                                                        .setTitle(R.string.err_general)
                                                                        .setMessage(R.string.err_retry_later).show();
                                                            }
                                                        }

                                                        @Override
                                                        protected Boolean doInBackground(Void... params) {
                                                            try {
                                                                new ModerationManager(Authentication.reddit).setDistinguishedStatus(comment, DistinguishedStatus.NORMAL);
                                                            } catch (ApiException e) {
                                                                e.printStackTrace();
                                                                return false;

                                                            }
                                                            return true;
                                                        }
                                                    }.execute();

                                                }
                                            }).setNegativeButton(R.string.btn_no, null).show();
                                } else {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.distinguish_comment)
                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {

                                                    new AsyncTask<Void, Void, Boolean>() {

                                                        @Override
                                                        public void onPostExecute(Boolean b) {
                                                            if (b) {
                                                                dialog.dismiss();
                                                                Snackbar s = Snackbar.make(holder.itemView, R.string.comment_distinguished, Snackbar.LENGTH_LONG);
                                                                View view = s.getView();
                                                                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                tv.setTextColor(Color.WHITE);
                                                                s.show();


                                                            } else {
                                                                new AlertDialogWrapper.Builder(mContext)
                                                                        .setTitle(R.string.err_general)
                                                                        .setMessage(R.string.err_retry_later).show();
                                                            }
                                                        }

                                                        @Override
                                                        protected Boolean doInBackground(Void... params) {
                                                            try {
                                                                new ModerationManager(Authentication.reddit).setDistinguishedStatus(comment, DistinguishedStatus.MODERATOR);
                                                            } catch (ApiException e) {
                                                                e.printStackTrace();
                                                                return false;

                                                            }
                                                            return true;
                                                        }
                                                    }.execute();

                                                }
                                            }).setNegativeButton(R.string.btn_no, null).show();
                                }

                                break;
                            case 6:

                                new AlertDialogWrapper.Builder(mContext).setTitle(R.string.remove_comment)
                                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, int which) {

                                                new AsyncTask<Void, Void, Boolean>() {

                                                    @Override
                                                    public void onPostExecute(Boolean b) {
                                                        if (b) {
                                                            dialog.dismiss();
                                                            Snackbar s = Snackbar.make(holder.itemView, R.string.comment_removed, Snackbar.LENGTH_LONG);
                                                            View view = s.getView();
                                                            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                            tv.setTextColor(Color.WHITE);
                                                            s.show();

                                                            deleted.add(comment.getFullName());
                                                            holder.firstTextView.setTextHtml(mContext.getString(R.string.content_removed));
                                                            holder.content.setText(R.string.content_removed);
                                                        } else {
                                                            new AlertDialogWrapper.Builder(mContext)
                                                                    .setTitle(R.string.err_general)
                                                                    .setMessage(R.string.err_retry_later).show();
                                                        }
                                                    }

                                                    @Override
                                                    protected Boolean doInBackground(Void... params) {
                                                        try {
                                                            new ModerationManager(Authentication.reddit).remove(comment, false);
                                                        } catch (ApiException e) {
                                                            e.printStackTrace();
                                                            return false;

                                                        }
                                                        return true;
                                                    }
                                                }.execute();

                                            }
                                        }).setNegativeButton(R.string.btn_no, null).show();
                                break;
                            case 8:
                                Intent i = new Intent(mContext, Profile.class);
                                i.putExtra(Profile.EXTRA_PROFILE, comment.getAuthor());
                                mContext.startActivity(i);
                                break;

                        }
                    }
                });


        b.show();
    }

    public CommentAdapter(CommentPage mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission, FragmentManager fm) {
        this.mContext = mContext.getContext();
        mPage = mContext;
        this.listView = listView;
        this.dataSet = dataSet;
        this.fm = fm;

        this.submission = submission;
        hidden = new ArrayList<>();
        users = dataSet.comments;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                keys.put(users.get(i).getName(), i);
            }
        }
        hiddenPersons = new ArrayList<>();
        toCollapse = new ArrayList<>();
        replie = new ArrayList<>();
        up = new ArrayList<>();
        down = new ArrayList<>();

        shifted = 0;

        isSame = false;

    }

    public void reset(Context mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission, int oldSize) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        this.submission = submission;
        hidden = new ArrayList<>();
        users = dataSet.comments;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                keys.put(users.get(i).getName(), i);
            }
        }
        hiddenPersons = new ArrayList<>();
        toCollapse = new ArrayList<>();

        replie = new ArrayList<>();


        isSame = false;
        notifyDataSetChanged();
        if (currentSelectedItem != null && !currentSelectedItem.isEmpty()) {
            int i = 1;

            for (CommentObject n : users) {

                if (n.getName().contains(currentSelectedItem) && !(n instanceof MoreChildItem)) {
                    CommentPage.TopSnappedSmoothScroller scroller = new CommentPage.TopSnappedSmoothScroller(mContext, (PreCachingLayoutManagerComments) listView.getLayoutManager());
                    scroller.setTargetPosition(i);
                    (listView.getLayoutManager()).startSmoothScroll(scroller);
                    break;
                }
                i++;
            }
        }
        mPage.resetScroll(true);
        if (mContext instanceof BaseActivity) {
            ((BaseActivity) mContext).setShareUrl("https://reddit.com" + submission.getPermalink());
        }
    }

    public void reset(Context mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission, boolean reset) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        this.submission = submission;
        hidden = new ArrayList<>();
        users = dataSet.comments;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                keys.put(users.get(i).getName(), i);
            }
        }

        hiddenPersons = new ArrayList<>();
        toCollapse = new ArrayList<>();
        replie = new ArrayList<>();


        if (currentSelectedItem != null && !currentSelectedItem.isEmpty() && !reset) {
            notifyDataSetChanged();
        } else {
            if (users != null && !reset) {
                notifyItemRangeChanged(2, users.size() + 1);
            } else if (users == null) {
                users = new ArrayList<>();
                notifyDataSetChanged();
            } else {
                notifyDataSetChanged();
            }
        }
        isSame = false;


        if (currentSelectedItem != null && !currentSelectedItem.isEmpty() && users != null && !users.isEmpty()) {
            int i = 2;
            for (CommentObject n : users) {
                if (n instanceof CommentItem && n.comment.getComment().getFullName().contains(currentSelectedItem)) {
                    ((PreCachingLayoutManagerComments) listView.getLayoutManager()).scrollToPositionWithOffset(i, mPage.headerHeight);
                    break;
                }
                i++;
            }
        }
        mPage.resetScroll(true);
        if (mContext instanceof BaseActivity) {
            ((BaseActivity) mContext).setShareUrl("https://reddit.com" + submission.getPermalink());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        switch (i) {
            case SPACER: {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spacer_post, viewGroup, false);
                RecyclerView.ViewHolder v2 = new SpacerViewHolder(v);
                return v2;
            }
            case HEADER: {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_fullscreen, viewGroup, false);
                RecyclerView.ViewHolder v2 = new SubmissionViewHolder(v);
                return v2;
            }
            case 2: {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment, viewGroup, false);
                RecyclerView.ViewHolder v2 = new CommentViewHolder(v);
                return v2;
            }
            default: {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.morecomment, viewGroup, false);
                RecyclerView.ViewHolder v2 = new MoreCommentViewHolder(v);
                return v2;
            }
        }


    }

    public void collapseAll() {
        if (users == null) return;
        for (CommentObject o : users) {
            if (o.comment.isTopLevel()) {
                if (!hiddenPersons.contains(o.comment.getComment().getFullName()))
                    hiddenPersons.add(o.comment.getComment().getFullName());
                hideAll(o.comment);
            }
        }
        notifyItemChanged(2);
    }

    public class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }


    public void setError(boolean b) {
        listView.setAdapter(new ErrorAdapter());
    }


    public void doScoreText(CommentViewHolder holder, Comment comment, int offset) {
        final String spacer = " " + mContext.getString(R.string.submission_properties_seperator_comments) + " ";
        SpannableStringBuilder titleString = new SpannableStringBuilder();
        SpannableStringBuilder author = new SpannableStringBuilder(comment.getAuthor());
        final int authorcolor = Palette.getFontColorUser(comment.getAuthor());

        author.setSpan(new TypefaceSpan("sans-serif-condensed"), 0, author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        author.setSpan(new StyleSpan(Typeface.BOLD), 0, author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (comment.getDistinguishedStatus() == DistinguishedStatus.MODERATOR || comment.getDistinguishedStatus() == DistinguishedStatus.ADMIN) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (Authentication.name != null && comment.getAuthor().toLowerCase().equals(Authentication.name.toLowerCase())) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_300, false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (submission != null && comment.getAuthor().toLowerCase().equals(submission.getAuthor().toLowerCase())) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_blue_300, false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (authorcolor != 0) {
            author.setSpan(new ForegroundColorSpan(authorcolor), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        titleString.append(author);
        titleString.append(spacer);

        int scoreColor;
        switch (ActionStates.getVoteDirection(comment)) {
            case UPVOTE:
                scoreColor = (holder.textColorUp);
                break;
            case DOWNVOTE:
                scoreColor = (holder.textColorDown);
                break;
            case NO_VOTE:
                scoreColor = (holder.textColorRegular);
                break;
        }

        //Whether or not this comment was made by this user
        final boolean ownComment = comment.getAuthor().equals(Authentication.name);

        if (up.contains(comment.getFullName())) {
            scoreColor = (holder.textColorUp);

            //User upvoted their own comment--don't mess with what the API returns
            if (ownComment) {
                offset = 0;
            }
        } else if (down.contains(comment.getFullName())) {
            scoreColor = (holder.textColorDown);

            //User downvoted their own comment--offset it by an additional -1
            if (ownComment) {
                --offset;
            }
        } else {
            scoreColor = (holder.textColorRegular);

            //User un-voted their own comment--offset it by an additional -1
            if (ownComment) {
                --offset;
            }
        }

        String scoreText;
        if (comment.isScoreHidden()) {
            scoreText = "[" + mContext.getString(R.string.misc_score_hidden).toUpperCase() + "]";
        } else {
            scoreText = String.format(Locale.getDefault(), "%d", comment.getScore() + offset);
        }

        SpannableStringBuilder score = new SpannableStringBuilder(scoreText);

        if (score == null || score.toString().isEmpty()) {
            score = new SpannableStringBuilder("0");
        }
        if (!scoreText.contains("[")) {
            score.append(String.format(Locale.getDefault(), " %s", mContext.getResources().getQuantityString(R.plurals.points, comment.getScore() + offset)));
        }
        score.setSpan(new ForegroundColorSpan(scoreColor), 0, score.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        titleString.append(score);
        titleString.append((comment.isControversial() ? " †" : ""));

        titleString.append(spacer);
        String timeAgo = TimeUtils.getTimeAgo(comment.getCreated().getTime(), mContext);
        titleString.append((timeAgo == null || timeAgo.isEmpty()) ? "just now" : timeAgo); //some users were crashing here

        titleString.append(((comment.getEditDate() != null) ? " (edit " + TimeUtils.getTimeAgo(comment.getEditDate().getTime(), mContext) + ")" : ""));
        titleString.append("  ");

        if (comment.getDataNode().get("stickied").asBoolean()) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0" + mContext.getString(R.string.submission_stickied).toUpperCase() + "\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (comment.getTimesGilded() > 0) {
            //if the comment has only been gilded once, don't show a number
            final String timesGilded = (comment.getTimesGilded() == 1) ? "" : "\u200A" + Integer.toString(comment.getTimesGilded());
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0★" + timesGilded + "\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_orange_500, false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (UserTags.isUserTagged(comment.getAuthor())) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0" + UserTags.getUserTag(comment.getAuthor()) + "\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_blue_500, false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (UserSubscriptions.friends.contains(comment.getAuthor())) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0" + mContext.getString(R.string.profile_friend) + "\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_500, false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (comment.getAuthorFlair() != null && comment.getAuthorFlair().getText() != null && !comment.getAuthorFlair().getText().isEmpty()) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.activity_background, typedValue, true);
            int color = typedValue.data;
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0" + Html.fromHtml(comment.getAuthorFlair().getText()) + "\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(holder.firstTextView.getCurrentTextColor(), color, false, mContext), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        } else if (comment.getAuthorFlair() != null) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.activity_background, typedValue, true);
            int color = typedValue.data;
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0" + Html.fromHtml(comment.getAuthorFlair().getCssClass()) + "\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(holder.firstTextView.getCurrentTextColor(), color, false, mContext), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        holder.content.setText(titleString);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHolder, int old) {
        int pos = old != 0 ? old - 1 : old;

        if (firstHolder instanceof CommentViewHolder) {
            final CommentViewHolder holder = (CommentViewHolder) firstHolder;
            int nextPos = pos - 1;

            nextPos = getRealPosition(nextPos);


            if (pos > toShiftTo) {
                shifted = 0;
            }
            if (pos < shiftFrom) {
                shifted = 0;
            }

            final CommentNode baseNode = users.get(nextPos).comment;
            final Comment comment = baseNode.getComment();


            if (pos == getItemCount() - 1) {
                holder.itemView.setPadding(0, 0, 0, (int) mContext.getResources().getDimension(R.dimen.overview_top_padding_single));
            } else {
                holder.itemView.setPadding(0, 0, 0, 0);
            }

            if (comment.getVote() == VoteDirection.UPVOTE) {
                if (!up.contains(comment.getFullName())) {
                    up.add(comment.getFullName());
                }
            } else if (comment.getVote() == VoteDirection.DOWNVOTE && !down.contains(comment.getFullName())) {
                down.add(comment.getFullName());
            }

            switch (comment.getVote()) {

                case UPVOTE:
                    doScoreText(holder, comment, 1);
                    break;
                case DOWNVOTE:
                    doScoreText(holder, comment, -1);
                    break;
                case NO_VOTE:
                    doScoreText(holder, comment, 0);
                    break;
            }

            View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (SettingValues.swap) {
                        doOnClick(holder, comment, baseNode);
                    } else {
                        doLongClick(holder, comment, baseNode);
                    }
                    return true;
                }
            };

            holder.firstTextView.setOnLongClickListener(onLongClickListener);
            holder.commentOverflow.setOnLongClickListener(onLongClickListener);

            int type = new FontPreferences(mContext).getFontTypeComment().getTypeface();
            if (type >= 0) {
                Typeface typeface = RobotoTypefaceManager.obtainTypeface(
                        mContext, type
                );
                holder.firstTextView.setTypeface(typeface);
            }
            if (!toCollapse.contains(comment.getFullName()) && SettingValues.collapseComments || !SettingValues.collapseComments)
                setViews(comment.getDataNode().get("body_html").asText(), submission.getSubredditName(), holder);

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!currentlyEditingId.equals(comment.getFullName()))
                        if (SettingValues.swap) {
                            doOnClick(holder, comment, baseNode);
                        } else {
                            doLongClick(holder, comment, baseNode);
                        }
                    return true;
                }
            });

            if (baseNode.isTopLevel()) {
                holder.itemView.findViewById(R.id.next).setVisibility(View.VISIBLE);
            } else if (holder.itemView.findViewById(R.id.next).getVisibility() == View.VISIBLE) {
                holder.itemView.findViewById(R.id.next).setVisibility(View.GONE);
            }


            if (hiddenPersons.contains(comment.getFullName()) || toCollapse.contains(comment.getFullName())) {
                int childnumber = getChildNumber(baseNode);
                if (hiddenPersons.contains(comment.getFullName()) && childnumber > 0) {
                    holder.childrenNumber.setVisibility(View.VISIBLE);
                    holder.childrenNumber.setText("+" + childnumber);
                } else {
                    holder.childrenNumber.setVisibility(View.GONE);
                }
                if (SettingValues.collapseComments && toCollapse.contains(comment.getFullName())) {
                    holder.firstTextView.setVisibility(View.GONE);
                    holder.commentOverflow.setVisibility(View.GONE);
                }
            } else {
                holder.childrenNumber.setVisibility(View.GONE);
                if (!holder.firstTextView.getText().toString().isEmpty())
                    holder.firstTextView.setVisibility(View.VISIBLE);
                else
                    holder.firstTextView.setVisibility(View.GONE);
                holder.commentOverflow.setVisibility(View.VISIBLE);
            }
            OnSingleClickListener singleClick = new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if (!currentlyEditingId.equals(comment.getFullName()))
                        if (SettingValues.swap) {
                            doLongClick(holder, comment, baseNode);
                        } else {
                            doOnClick(holder, comment, baseNode);
                        }
                }
            };
            holder.itemView.setOnClickListener(singleClick);
            holder.commentOverflow.setOnClickListener(singleClick);

            holder.firstTextView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    SpoilerRobotoTextView SpoilerRobotoTextView = (SpoilerRobotoTextView) v;
                    if (SettingValues.swap) {
                        doLongClick(holder, comment, baseNode);
                    } else if (!SpoilerRobotoTextView.isSpoilerClicked()) {
                        doOnClick(holder, comment, baseNode);
                    } else if (SpoilerRobotoTextView.isSpoilerClicked()) {
                        SpoilerRobotoTextView.resetSpoilerClicked();
                    }
                }
            });
            {
                holder.dot.setVisibility(View.VISIBLE);

                int dwidth = (int) (3 * Resources.getSystem().getDisplayMetrics().density);
                int width = 0;

                //Padding on the left, starting with the third comment
                for (int i = 2; i < baseNode.getDepth(); i++) {
                    width += dwidth;
                }
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                params.setMargins(width, 0, 0, 0);
                holder.itemView.setLayoutParams(params);

                if (baseNode.getDepth() - 1 > 0) {
                    int i22 = baseNode.getDepth() - 2;
                    if (i22 % 5 == 0) {
                        holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, !SettingValues.colorCommentDepth ? R.color.md_grey_700 : R.color.md_blue_500));
                    } else if (i22 % 4 == 0) {
                        holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, !SettingValues.colorCommentDepth ? R.color.md_grey_600 : R.color.md_green_500));
                    } else if (i22 % 3 == 0) {
                        holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, !SettingValues.colorCommentDepth ? R.color.md_grey_500 : R.color.md_yellow_500));
                    } else if (i22 % 2 == 0) {
                        holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, !SettingValues.colorCommentDepth ? R.color.md_grey_400 : R.color.md_orange_500));
                    } else {
                        holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, !SettingValues.colorCommentDepth ? R.color.md_grey_300 : R.color.md_red_500));
                    }
                } else {
                    holder.dot.setVisibility(View.GONE);
                }
            }


            if (currentSelectedItem != null && comment.getFullName().contains(currentSelectedItem) && !currentSelectedItem.isEmpty() && !currentlyEditingId.equals(comment.getFullName())) {
                doHighlighted(holder, comment, baseNode, false);
            } else if (!currentlyEditingId.equals(comment.getFullName())) {
                doUnHighlighted(holder, baseNode, false);
            }

            if (deleted.contains(comment.getFullName())) {
                holder.firstTextView.setText(R.string.comment_deleted);
                holder.content.setText(R.string.comment_deleted);
            }

            if (currentlyEditingId.equals(comment.getFullName())) {
                doUnHighlighted(holder, baseNode, false);
                doHighlightedStuff(holder, comment, baseNode, true, false);
            }

        } else if (firstHolder instanceof SubmissionViewHolder && submission != null) {
            new PopulateSubmissionViewHolder().populateSubmissionViewHolder((SubmissionViewHolder) firstHolder, submission, (Activity) mContext, true, true, null, null, false, false, null, this);
            if (Authentication.isLoggedIn && Authentication.didOnline) {
                if (submission.isArchived() || submission.isLocked())
                    firstHolder.itemView.findViewById(R.id.reply).setVisibility(View.GONE);
                else {
                    firstHolder.itemView.findViewById(R.id.reply).setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            final View replyArea = firstHolder.itemView.findViewById(R.id.innerSend);
                            if (replyArea.getVisibility() == View.GONE) {
                                expand(replyArea, true, true);
                                DoEditorActions.doActions(((EditText) firstHolder.itemView.findViewById(R.id.replyLine)), firstHolder.itemView, fm, (Activity) mContext);

                                currentlyEditing = ((EditText) firstHolder.itemView.findViewById(R.id.replyLine));
                                currentlyEditing.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                    @Override
                                    public void onFocusChange(View v, boolean hasFocus) {
                                        if (hasFocus) {
                                            mPage.fastScroll.setVisibility(View.GONE);
                                            if (mPage.fab != null)
                                                mPage.fab.setVisibility(View.GONE);
                                            mPage.overrideFab = true;
                                        } else if (SettingValues.fastscroll) {
                                            mPage.fastScroll.setVisibility(View.VISIBLE);
                                            if (mPage.fab != null)
                                                mPage.fab.setVisibility(View.VISIBLE);
                                            mPage.overrideFab = false;

                                        }
                                    }
                                });
                                editingPosition = firstHolder.getAdapterPosition();

                                firstHolder.itemView.findViewById(R.id.send).setOnClickListener(new OnSingleClickListener() {
                                    @Override
                                    public void onSingleClick(View v) {
                                        dataSet.refreshLayout.setRefreshing(true);

                                        if (SettingValues.fastscroll) {
                                            mPage.fastScroll.setVisibility(View.VISIBLE);
                                        }
                                        if (mPage.fab != null)
                                            mPage.fab.setVisibility(View.VISIBLE);
                                        mPage.overrideFab = false;
                                        String text = currentlyEditing.getText().toString();
                                        new ReplyTaskComment(submission).execute(text);
                                        replyArea.setVisibility(View.GONE);
                                        currentlyEditing = null;
                                        editingPosition = -1;
                                        //Hide soft keyboard
                                        View view = ((Activity) mContext).getCurrentFocus();
                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        }
                                    }
                                });
                            } else {
                                collapse(replyArea, true);
                            }
                        }
                    });
                    firstHolder.itemView.findViewById(R.id.discard).setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.GONE);
                            currentlyEditing = null;
                            editingPosition = -1;
                            if (SettingValues.fastscroll) {
                                mPage.fastScroll.setVisibility(View.VISIBLE);
                            }
                            if (mPage.fab != null)
                                mPage.fab.setVisibility(View.VISIBLE);
                            mPage.overrideFab = false;
                            currentlyEditingId = "";
                            backedText = "";
                            View view = ((Activity) mContext).getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
                    });
                }
            } else {
                firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.GONE);
                firstHolder.itemView.findViewById(R.id.reply).setVisibility(View.GONE);
            }

            firstHolder.itemView.findViewById(R.id.more).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    firstHolder.itemView.findViewById(R.id.menu).callOnClick();
                }
            });


        } else if (firstHolder instanceof MoreCommentViewHolder) {
            final MoreCommentViewHolder holder = (MoreCommentViewHolder) firstHolder;
            int nextPos = pos - 1;

            nextPos = getRealPosition(nextPos);

            final MoreChildItem baseNode = (MoreChildItem) users.get(nextPos);
            if (baseNode.children.getCount() > 0) {
                holder.content.setText(mContext.getString(R.string.comment_load_more_string, baseNode.children.getLocalizedCount()));
            } else if (!baseNode.children.getChildrenIds().isEmpty()) {
                holder.content.setText(R.string.comment_load_more_number_unknown);
            } else {
                holder.content.setText(R.string.thread_continue);
            }

            int dwidth = (int) (3 * Resources.getSystem().getDisplayMetrics().density);
            int width = 0;
            for (int i = 1; i < baseNode.comment.getDepth(); i++) {
                width += dwidth;
            }

            final View progress = holder.loading;
            progress.setVisibility(View.GONE);
            final int finalNextPos = nextPos;
            holder.content.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if (baseNode.children.getChildrenIds().isEmpty()) {
                        String toGoTo = "https://reddit.com" + submission.getPermalink() + baseNode.comment.getComment().getId();
                        new OpenRedditLink(mContext, toGoTo, true);
                    } else if (progress.getVisibility() == View.GONE) {
                        progress.setVisibility(View.VISIBLE);
                        holder.content.setText(R.string.comment_loading_more);
                        currentLoading = new AsyncLoadMore(getRealPosition(holder.getAdapterPosition() - 2), holder.getAdapterPosition(), holder, finalNextPos, baseNode.comment.getComment().getFullName());
                        currentLoading.execute(baseNode);
                    }
                }
            });

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(width, 0, 0, 0);
            holder.itemView.setLayoutParams(params);
        }
        if (firstHolder instanceof SpacerViewHolder) {
            //Make a space the size of the toolbar minus 1 so there isn't a gap
            firstHolder.itemView.findViewById(R.id.height)
                    .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            (Constants.SINGLE_HEADER_VIEW_OFFSET - Reddit.dpToPxVertical(1) + mPage.shownHeaders)));
        }
    }

    AsyncLoadMore currentLoading;

    public void setViews(String rawHTML, String subredditName, final SpoilerRobotoTextView firstTextView, CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0) + " ", subredditName);
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
            commentOverflow.setLongClickable(false);
        } else {
            commentOverflow.removeAllViews();
        }

    }

    private void setViews(String rawHTML, String subredditName, CommentViewHolder holder) {
        setViews(rawHTML, subredditName, holder.firstTextView, holder.commentOverflow);
    }

    int editingPosition;

    private ValueAnimator slideAnimator(int start, int end, final View v) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setInterpolator(new FastOutSlowInInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = value;
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    private void collapse(final View v, boolean full) {
        int finalHeight = v.getHeight();

        mAnimator = slideAnimator(finalHeight, 0, v);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                v.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private void collapse(final View v) {
        int finalHeight = v.getHeight();

        mAnimator = slideAnimator(finalHeight, 0, v);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                ((LinearLayout) v).removeAllViews();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                ((LinearLayout) v).removeAllViews();

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private void doShowMenu(final View l) {
        l.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l.measure(widthSpec, heightSpec);


        final View l2 = l.findViewById(R.id.menu);
        final int widthSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l2.measure(widthSpec2, heightSpec2);
        ValueAnimator mAnimator = slideAnimator(l.getMeasuredHeight(), l2.getMeasuredHeight(), l);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                l2.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                l2.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    ValueAnimator mAnimator;

    private void expand(final View l) {
        l.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l.measure(widthSpec, heightSpec);

        View l2 = l.findViewById(R.id.replyArea) == null ? l.findViewById(R.id.innerSend) : l.findViewById(R.id.replyArea);
        final int widthSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l2.measure(widthSpec2, heightSpec2);

        mAnimator = slideAnimator(0, l.getMeasuredHeight() - l2.getMeasuredHeight(), l);

        mAnimator.start();
    }

    private void expand(final View l, boolean b) {
        l.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l.measure(widthSpec, heightSpec);

        View l2 = l.findViewById(R.id.replyArea) == null ? l.findViewById(R.id.innerSend) : l.findViewById(R.id.replyArea);
        final int widthSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l2.measure(widthSpec2, heightSpec2);

        mAnimator = slideAnimator((l.getMeasuredHeight() - l2.getMeasuredHeight()), l.getMeasuredHeight() - (l.getMeasuredHeight() - l2.getMeasuredHeight()), l);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) l.getLayoutParams();
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                params.addRule(RelativeLayout.BELOW, R.id.commentOverflow);
                l.setLayoutParams(params);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) l.getLayoutParams();
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                params.addRule(RelativeLayout.BELOW, R.id.commentOverflow);
                l.setLayoutParams(params);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private void expand(final View l, boolean b, boolean full) {
        l.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l.measure(widthSpec, heightSpec);

        mAnimator = slideAnimator(0, l.getMeasuredHeight(), l);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) l.getLayoutParams();
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                l.setLayoutParams(params);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) l.getLayoutParams();
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                l.setLayoutParams(params);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    CommentNode currentBaseNode;

    public void doHighlightedStuff(final CommentViewHolder holder, final Comment n, final CommentNode baseNode, boolean isReplying, boolean animate) {
        if (currentlySelected != null && currentlySelected != holder) {
            doUnHighlighted(currentlySelected, currentBaseNode, true);
        }

        if (mContext instanceof BaseActivity) {
            ((BaseActivity) mContext).setShareUrl("https://reddit.com" + submission.getPermalink() + n.getFullName() + "?context=3");
        }

        // If a comment is hidden and (Swap long press == true), then a single click will un-hide the comment
        // and expand to show all children comments
        if (SettingValues.swap && holder.firstTextView.getVisibility() == View.GONE && !isReplying) {
            hiddenPersons.remove(n.getFullName());
            unhideAll(baseNode, holder.getAdapterPosition() + 1);


            if (toCollapse.contains(n.getFullName()) && SettingValues.collapseComments)
                setViews(n.getDataNode().get("body_html").asText(), submission.getSubredditName(), holder);


            hideChildrenObject(holder.childrenNumber);
            if (!holder.firstTextView.getText().toString().isEmpty())
                holder.firstTextView.setVisibility(View.VISIBLE);
            else
                holder.firstTextView.setVisibility(View.GONE);
            holder.commentOverflow.setVisibility(View.VISIBLE);


            toCollapse.remove(n.getFullName());

        } else {
            currentlySelected = holder;
            currentBaseNode = baseNode;
            int color = Palette.getColor(n.getSubredditName());
            currentSelectedItem = n.getFullName();

            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            resetMenu(holder.menuArea, false);
            final View baseView = (SettingValues.rightHandedCommentMenu)
                    ? inflater.inflate(R.layout.comment_menu_right_handed, holder.menuArea)
                    : inflater.inflate(R.layout.comment_menu, holder.menuArea);

            if (!isReplying) {
                baseView.setVisibility(View.GONE);
                if (animate) {
                    expand(baseView);
                } else {
                    baseView.setVisibility(View.VISIBLE);
                    final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    baseView.measure(widthSpec, heightSpec);
                    View l2 = baseView.findViewById(R.id.replyArea) == null ? baseView.findViewById(R.id.innerSend) : baseView.findViewById(R.id.replyArea);
                    final int widthSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    final int heightSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    l2.measure(widthSpec2, heightSpec2);
                    ViewGroup.LayoutParams layoutParams = baseView.getLayoutParams();
                    layoutParams.height = baseView.getMeasuredHeight() - l2.getMeasuredHeight();
                    baseView.setLayoutParams(layoutParams);
                }
            }


            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            holder.itemView.setLayoutParams(params);

            View reply = baseView.findViewById(R.id.reply);
            View send = baseView.findViewById(R.id.send);

            final View menu = baseView.findViewById(R.id.menu);
            final View replyArea = baseView.findViewById(R.id.replyArea);

            final View more = baseView.findViewById(R.id.more);
            final ImageView upvote = (ImageView) baseView.findViewById(R.id.upvote);
            final ImageView downvote = (ImageView) baseView.findViewById(R.id.downvote);
            View discard = baseView.findViewById(R.id.discard);
            final EditText replyLine = (EditText) baseView.findViewById(R.id.replyLine);


            if (up.contains(n.getFullName())) {
                upvote.setColorFilter(holder.textColorUp, PorterDuff.Mode.MULTIPLY);
            } else if (down.contains(n.getFullName())) {
                downvote.setColorFilter(holder.textColorDown, PorterDuff.Mode.MULTIPLY);
            } else {
                downvote.clearColorFilter();
                upvote.clearColorFilter();
            }
            {
                final ImageView mod = (ImageView) baseView.findViewById(R.id.mod);
                try {
                    if (UserSubscriptions.modOf.contains(submission.getSubredditName())) {
                        //todo
                        mod.setVisibility(View.GONE);

                    } else {
                        mod.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.d(LogUtil.getTag(), "Error loading mod " + e.toString());
                }
            }
            {
                if (UserSubscriptions.modOf != null && UserSubscriptions.modOf.contains(submission.getSubredditName().toLowerCase())) {
                    baseView.findViewById(R.id.mod).setVisibility(View.VISIBLE);
                    final Map<String, Integer> reports = baseNode.getComment().getUserReports();
                    final Map<String, String> reports2 = baseNode.getComment().getModeratorReports();
                    if (reports.size() + reports2.size() > 0) {
                        ((ImageView) baseView.findViewById(R.id.mod)).setColorFilter(ContextCompat.getColor(mContext, R.color.md_red_300), PorterDuff.Mode.SRC_ATOP);
                    } else {
                        ((ImageView) baseView.findViewById(R.id.mod)).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

                    }

                    baseView.findViewById(R.id.mod).setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            showModBottomSheet(mContext, baseNode, baseNode.getComment(), holder, reports, reports2);
                        }
                    });
                } else {
                    baseView.findViewById(R.id.mod).setVisibility(View.GONE);
                }
            }
            {
                final ImageView edit = (ImageView) baseView.findViewById(R.id.edit);
                if (Authentication.name != null && Authentication.name.toLowerCase().equals(baseNode.getComment().getAuthor().toLowerCase()) && Authentication.didOnline) {
                    edit.setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

                            final View dialoglayout = inflater.inflate(R.layout.edit_comment, null);
                            final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);

                            final EditText e = (EditText) dialoglayout.findViewById(R.id.entry);
                            e.setText(StringEscapeUtils.unescapeHtml4(baseNode.getComment().getBody()));

                            DoEditorActions.doActions(e, dialoglayout, fm, (Activity) mContext);

                            builder.setView(dialoglayout);
                            final Dialog d = builder.create();
                            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                            d.show();
                            dialoglayout.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    d.dismiss();
                                }
                            });
                            dialoglayout.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final String text = e.getText().toString();
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            try {
                                                new AccountManager(Authentication.reddit).updateContribution(baseNode.getComment(), text);
                                                currentSelectedItem = baseNode.getComment().getFullName();
                                                dataSet.loadMoreReply(CommentAdapter.this);
                                                d.dismiss();
                                            } catch (Exception e) {
                                                ((Activity) mContext).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        new AlertDialogWrapper.Builder(mContext)
                                                                .setTitle(R.string.comment_delete_err)
                                                                .setMessage(R.string.comment_delete_err_msg)
                                                                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                        doInBackground();
                                                                    }
                                                                }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        }).show();
                                                    }
                                                });
                                            }
                                            return null;
                                        }
                                    }.execute();
                                }
                            });


                        }
                    });
                } else {
                    edit.setVisibility(View.GONE);
                }
            }
            {
                final ImageView delete = (ImageView) baseView.findViewById(R.id.delete);
                if (Authentication.name != null && Authentication.name.toLowerCase().equals(baseNode.getComment().getAuthor().toLowerCase()) && Authentication.didOnline) {
                    delete.setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {

                            new AlertDialogWrapper.Builder(mContext)
                                    .setTitle(R.string.comment_delete)
                                    .setMessage(R.string.comment_delete_msg)
                                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new AsyncTask<Void, Void, Void>() {

                                                @Override
                                                protected Void doInBackground(Void... params) {
                                                    try {
                                                        new ModerationManager(Authentication.reddit).delete(baseNode.getComment());
                                                        deleted.add(baseNode.getComment().getFullName());

                                                        ((Activity) mContext).runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                holder.firstTextView.setTextHtml(mContext.getString(R.string.content_deleted));
                                                                holder.content.setText(R.string.content_deleted);
                                                            }
                                                        });

                                                    } catch (ApiException e) {
                                                        ((Activity) mContext).runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                new AlertDialogWrapper.Builder(mContext)
                                                                        .setTitle(R.string.comment_delete_err)
                                                                        .setMessage(R.string.comment_delete_err_msg)
                                                                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                dialog.dismiss();
                                                                                doInBackground();
                                                                            }
                                                                        }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                    }
                                                                }).show();
                                                            }
                                                        });

                                                        e.printStackTrace();
                                                    }

                                                    return null;
                                                }
                                            }.execute();

                                        }
                                    })
                                    .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();

                            //todo delete
                        }
                    });
                } else {
                    delete.setVisibility(View.GONE);
                }
            }
            if (Authentication.isLoggedIn && !submission.isArchived() && !submission.isLocked() && Authentication.didOnline) {
                if (isReplying) {
                    baseView.setVisibility(View.VISIBLE);

                    final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    baseView.measure(widthSpec, heightSpec);

                    View l2 = baseView.findViewById(R.id.replyArea) == null ? baseView.findViewById(R.id.innerSend) : baseView.findViewById(R.id.replyArea);
                    final int widthSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    final int heightSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    l2.measure(widthSpec2, heightSpec2);
                    RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) baseView.getLayoutParams();
                    params2.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                    params2.addRule(RelativeLayout.BELOW, R.id.commentOverflow);
                    baseView.setLayoutParams(params2);
                    replyArea.setVisibility(View.VISIBLE);
                    menu.setVisibility(View.GONE);
                    DoEditorActions.doActions(replyLine, replyArea, fm, (Activity) mContext);
                    currentlyEditing = replyLine;
                    currentlyEditing.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (hasFocus) {
                                mPage.fastScroll.setVisibility(View.GONE);
                                if (mPage.fab != null) {
                                    mPage.fab.setVisibility(View.GONE);
                                }
                                mPage.overrideFab = true;
                            } else if (SettingValues.fastscroll) {
                                mPage.fastScroll.setVisibility(View.VISIBLE);
                                if (mPage.fab != null) {
                                    mPage.fab.setVisibility(View.VISIBLE);
                                }
                                mPage.overrideFab = false;
                            }
                        }
                    });
                    currentlyEditingId = n.getFullName();
                    replyLine.setText(backedText);
                    replyLine.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            backedText = s.toString();
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                    editingPosition = holder.getAdapterPosition();
                }
                reply.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        expand(baseView, true);

                        //If the base theme is Light or Sepia, tint the Editor actions to be white
                        if (SettingValues.currentTheme == 1 || SettingValues.currentTheme == 5) {
                            ((ImageView) replyArea.findViewById(R.id.savedraft))
                                    .setColorFilter(Color.WHITE);
                            ((ImageView) replyArea.findViewById(R.id.draft))
                                    .setColorFilter(Color.WHITE);
                            ((ImageView) replyArea.findViewById(R.id.imagerep))
                                    .setColorFilter(Color.WHITE);
                            ((ImageView) replyArea.findViewById(R.id.link))
                                    .setColorFilter(Color.WHITE);
                            ((ImageView) replyArea.findViewById(R.id.bold))
                                    .setColorFilter(Color.WHITE);
                            ((ImageView) replyArea.findViewById(R.id.italics))
                                    .setColorFilter(Color.WHITE);
                            ((ImageView) replyArea.findViewById(R.id.bulletlist))
                                    .setColorFilter(Color.WHITE);
                            ((ImageView) replyArea.findViewById(R.id.numlist))
                                    .setColorFilter(Color.WHITE);
                            ((ImageView) replyArea.findViewById(R.id.quote))
                                    .setColorFilter(Color.WHITE);
                            ((ImageView) replyArea.findViewById(R.id.size))
                                    .setColorFilter(Color.WHITE);
                            ((ImageView) replyArea.findViewById(R.id.strike))
                                    .setColorFilter(Color.WHITE);

                            replyLine.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                        }

                        replyArea.setVisibility(View.VISIBLE);
                        menu.setVisibility(View.GONE);
                        DoEditorActions.doActions(replyLine, replyArea, fm, (Activity) mContext);
                        currentlyEditing = replyLine;
                        currentlyEditing.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (hasFocus) {
                                    mPage.fastScroll.setVisibility(View.GONE);
                                    if (mPage.fab != null)
                                        mPage.fab.setVisibility(View.GONE);
                                    mPage.overrideFab = true;
                                } else if (SettingValues.fastscroll) {
                                    mPage.fastScroll.setVisibility(View.VISIBLE);
                                    if (mPage.fab != null)
                                        mPage.fab.setVisibility(View.VISIBLE);
                                    mPage.overrideFab = false;
                                }
                            }
                        });
                        currentlyEditingId = n.getFullName();
                        replyLine.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                backedText = s.toString();
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                        editingPosition = holder.getAdapterPosition();
                    }
                });
                send.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        currentlyEditingId = "";
                        backedText = "";

                        doShowMenu(baseView);
                        if (SettingValues.fastscroll) {
                            mPage.fastScroll.setVisibility(View.VISIBLE);
                            if (mPage.fab != null)
                                mPage.fab.setVisibility(View.VISIBLE);
                            mPage.overrideFab = false;
                        }
                        dataSet.refreshLayout.setRefreshing(true);
                        String text = currentlyEditing.getText().toString();
                        new ReplyTaskComment(n, baseNode, holder).execute(text);
                        currentlyEditing = null;
                        editingPosition = -1;

                        //Hide soft keyboard
                        View view = ((Activity) mContext).getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                });
                discard.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        currentlyEditing = null;
                        editingPosition = -1;
                        currentlyEditingId = "";
                        backedText = "";
                        mPage.overrideFab = false;
                        View view = ((Activity) mContext).getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        doShowMenu(baseView);
                    }
                });
            } else {
                if (reply.getVisibility() == View.VISIBLE) {
                    reply.setVisibility(View.GONE);
                }
                if (upvote.getVisibility() == View.VISIBLE) {
                    upvote.setVisibility(View.GONE);
                }
                if (downvote.getVisibility() == View.VISIBLE) {
                    downvote.setVisibility(View.GONE);
                }
            }

            more.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    showBottomSheet(mContext, holder, baseNode);
                }
            });
            upvote.setOnClickListener(new OnSingleClickListener() {

                @Override
                public void onSingleClick(View v) {
                    doUnHighlighted(holder, baseNode.getComment(), baseNode, true);
                    if (up.contains(n.getFullName())) {
                        new Vote(v, mContext).execute(n);
                        up.remove(n.getFullName());
                        doScoreText(holder, n, 0);
                        upvote.clearColorFilter();

                    } else if (down.contains(n.getFullName())) {
                        new Vote(true, v, mContext).execute(n);
                        up.add(n.getFullName());
                        down.remove(n.getFullName());
                        downvote.clearColorFilter(); // reset colour
                        doScoreText(holder, n, 1);
                        upvote.setColorFilter(holder.textColorUp, PorterDuff.Mode.MULTIPLY);
                    } else {
                        new Vote(true, v, mContext).execute(n);
                        up.add(n.getFullName());
                        doScoreText(holder, n, 1);
                        upvote.setColorFilter(holder.textColorUp, PorterDuff.Mode.MULTIPLY);
                    }
                }
            });
            downvote.setOnClickListener(new OnSingleClickListener() {

                @Override
                public void onSingleClick(View v) {

                    doUnHighlighted(holder, baseNode.getComment(), baseNode, true);

                    if (down.contains(n.getFullName())) {
                        new Vote(v, mContext).execute(n);
                        down.remove(n.getFullName());
                        doScoreText(holder, n, 0);
                        downvote.clearColorFilter();

                    } else if (up.contains(n.getFullName())) {
                        new Vote(false, v, mContext).execute(n);
                        down.add(n.getFullName());
                        up.remove(n.getFullName());

                        upvote.clearColorFilter(); // reset colour
                        doScoreText(holder, n, -1);
                        downvote.setColorFilter(holder.textColorDown);

                    } else {
                        new Vote(false, v, mContext).execute(n);
                        down.add(n.getFullName());
                        doScoreText(holder, n, -1);
                        downvote.setColorFilter(holder.textColorDown);
                    }
                }
            });
            menu.setBackgroundColor(color);
            replyArea.setBackgroundColor(color);

            if (!isReplying) {
                menu.setVisibility(View.VISIBLE);
                replyArea.setVisibility(View.GONE);
            }

            holder.itemView.findViewById(R.id.background).setBackgroundColor(Color.argb(50, Color.red(color), Color.green(color), Color.blue(color)));
        }
    }

    public void doHighlighted(final CommentViewHolder holder, final Comment n, final CommentNode baseNode, boolean animate) {
        if (mAnimator != null && mAnimator.isRunning()) {
            holder.itemView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doHighlightedStuff(holder, n, baseNode, false, true);
                }
            }, mAnimator.getDuration());
        } else {
            doHighlightedStuff(holder, n, baseNode, false, animate);
        }
    }

    public EditText currentlyEditing;

    public void resetMenu(LinearLayout v, boolean collapsed) {
        if (collapsed) {
            v.removeAllViews();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
            params.height = 0;
            v.setLayoutParams(params);
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            v.setLayoutParams(params);
        }
    }

    public void doUnHighlighted(final CommentViewHolder holder, final CommentNode baseNode, boolean animate) {
        if (animate) {
            collapse(holder.menuArea);
        } else {
            resetMenu(holder.menuArea, true);
        }

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = mContext.getTheme();
        theme.resolveAttribute(R.attr.card_background, typedValue, true);
        int color = typedValue.data;
        int dwidth = (int) (3 * Resources.getSystem().getDisplayMetrics().density);
        int width = 0;

        //Padding on the left, starting with the third comment
        for (int i = 2; i < baseNode.getDepth(); i++) {
            width += dwidth;
        }
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        params.setMargins(width, 0, 0, 0);
        holder.itemView.setLayoutParams(params);

        holder.itemView.findViewById(R.id.background).setBackgroundColor(color);
    }

    public void doUnHighlighted(final CommentViewHolder holder, final Comment comment, final CommentNode baseNode, boolean animate) {
        if (currentlyEditing != null && !currentlyEditing.getText().toString().isEmpty() && holder.getAdapterPosition() <= editingPosition) {
            new AlertDialogWrapper.Builder(mContext)
                    .setTitle(R.string.discard_comment_title)
                    .setMessage(R.string.comment_discard_msg)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            doUnHighlighted(holder, comment, baseNode, true);
                            currentlyEditing = null;
                            editingPosition = -1;
                            if (SettingValues.fastscroll) {
                                mPage.fastScroll.setVisibility(View.VISIBLE);
                            }
                            if (mPage.fab != null)
                                mPage.fab.setVisibility(View.VISIBLE);
                            mPage.overrideFab = false;
                            currentlyEditingId = "";
                            backedText = "";
                            View view = ((Activity) mContext).getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                            if (mContext instanceof BaseActivity) {
                                ((BaseActivity) mContext).setShareUrl("https://reddit.com" + submission.getPermalink());
                            }
                        }
                    }).setNegativeButton(R.string.btn_no, null)
                    .show();
        } else {
            if (mContext instanceof BaseActivity) {
                ((BaseActivity) mContext).setShareUrl("https://freddit.com" + submission.getPermalink());
            }
            currentlySelected = null;
            currentSelectedItem = "";
            if (animate) {
                collapse(holder.menuArea);
            } else {
                resetMenu(holder.menuArea, true);
            }
            int dwidth = (int) (3 * Resources.getSystem().getDisplayMetrics().density);
            int width = 0;

            //Padding on the left, starting with the third comment
            for (int i = 2; i < baseNode.getDepth(); i++) {
                width += dwidth;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(width, 0, 0, 0);
            holder.itemView.setLayoutParams(params);

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.card_background, typedValue, true);
            int color = typedValue.data;
            holder.itemView.findViewById(R.id.background).setBackgroundColor(color);
        }
    }

    public void doLongClick(final CommentViewHolder holder, final Comment comment, final CommentNode baseNode) {
        if (currentlyEditing != null && !currentlyEditing.getText().toString().isEmpty()) {
            new AlertDialogWrapper.Builder(mContext)
                    .setTitle(R.string.discard_comment_title)
                    .setMessage(R.string.comment_discard_msg)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            doLongClick(holder, comment, baseNode);
                            currentlyEditing = null;
                            editingPosition = -1;
                            if (SettingValues.fastscroll) {
                                mPage.fastScroll.setVisibility(View.VISIBLE);
                            }
                            if (mPage.fab != null)
                                mPage.fab.setVisibility(View.VISIBLE);
                            mPage.overrideFab = false;
                            currentlyEditingId = "";
                            backedText = "";
                            View view = ((Activity) mContext).getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
                    }).setNegativeButton(R.string.btn_no, null)
                    .show();

        } else {
            if (currentSelectedItem != null && currentSelectedItem.contains(comment.getFullName())) {
                doUnHighlighted(holder, comment, baseNode, true);
            } else {
                doHighlighted(holder, comment, baseNode, true);
            }
        }
    }

    public void doOnClick(CommentViewHolder holder, Comment comment, CommentNode baseNode) {
        if (currentSelectedItem != null && currentSelectedItem.contains(comment.getFullName())) {
            if (SettingValues.swap) {
                //If the comment is highlighted and the user is long pressing the comment,
                //hide the comment.
                doOnClick(holder, baseNode, comment);
            }
            doUnHighlighted(holder, comment, baseNode, true);
        } else {
            doOnClick(holder, baseNode, comment);
        }
    }

    public void showChildrenObject(final View v) {
        v.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.setDuration(250);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) (animation.getAnimatedValue())).floatValue();
                v.setAlpha(value);
                v.setScaleX(value);
                v.setScaleY(value);
            }
        });

        animator.start();
    }

    public void hideChildrenObject(final View v) {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0);
        animator.setDuration(250);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {


            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) (animation.getAnimatedValue())).floatValue();
                v.setAlpha(value);
                v.setScaleX(value);
                v.setScaleY(value);

            }
        });

        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {

                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator arg0) {
                v.setVisibility(View.GONE);

            }
        });

        animator.start();
    }

    public void doOnClick(final CommentViewHolder holder, final CommentNode baseNode, final Comment comment) {
        if (currentlyEditing != null && !currentlyEditing.getText().toString().isEmpty() && holder.getAdapterPosition() <= editingPosition) {
            new AlertDialogWrapper.Builder(mContext)
                    .setTitle(R.string.discard_comment_title)
                    .setMessage(R.string.comment_discard_msg)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            doOnClick(holder, baseNode, comment);
                            currentlyEditing = null;
                            editingPosition = -1;
                            if (SettingValues.fastscroll) {
                                mPage.fastScroll.setVisibility(View.VISIBLE);
                            }
                            if (mPage.fab != null)
                                mPage.fab.setVisibility(View.VISIBLE);
                            mPage.overrideFab = false;
                            currentlyEditingId = "";
                            backedText = "";
                            View view = ((Activity) mContext).getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                        }
                    }).setNegativeButton(R.string.btn_no, null)
                    .show();

        } else {
            if (isClicking) {
                isClicking = false;
                resetMenu(holder.menuArea, true);
                isHolder.itemView.findViewById(R.id.menu).setVisibility(View.GONE);
            } else {
                if (hiddenPersons.contains(comment.getFullName())) {
                    hiddenPersons.remove(comment.getFullName());
                    unhideAll(baseNode, holder.getAdapterPosition() + 1);

                    if (toCollapse.contains(comment.getFullName()) && SettingValues.collapseComments)
                        setViews(comment.getDataNode().get("body_html").asText(), submission.getSubredditName(), holder);

                    hideChildrenObject(holder.childrenNumber);
                    if (!holder.firstTextView.getText().toString().isEmpty())
                        holder.firstTextView.setVisibility(View.VISIBLE);
                    else
                        holder.firstTextView.setVisibility(View.GONE);
                    holder.commentOverflow.setVisibility(View.VISIBLE);


                    toCollapse.remove(comment.getFullName());

                } else {
                    int childNumber = getChildNumber(baseNode);
                    if (childNumber > 0) {
                        hideAll(baseNode, holder.getAdapterPosition() + 1);
                        if (!hiddenPersons.contains(comment.getFullName()))
                            hiddenPersons.add(comment.getFullName());
                        if (childNumber > 0) {
                            showChildrenObject(holder.childrenNumber);
                            holder.childrenNumber.setText("+" + childNumber);
                        }
                    }
                    toCollapse.add(comment.getFullName());
                    if ((holder.firstTextView.getVisibility() == View.VISIBLE || holder.commentOverflow.getVisibility() == View.VISIBLE) && SettingValues.collapseComments) {
                        holder.firstTextView.setVisibility(View.GONE);
                        holder.commentOverflow.setVisibility(View.GONE);
                    } else if (SettingValues.collapseComments) {
                        if (!holder.firstTextView.getText().toString().isEmpty())
                            holder.firstTextView.setVisibility(View.VISIBLE);
                        else
                            holder.firstTextView.setVisibility(View.GONE);
                        holder.commentOverflow.setVisibility(View.VISIBLE);
                    }
                }
                clickpos = holder.getAdapterPosition() + 1;
            }
        }
    }

    private int getChildNumber(CommentNode user) {
        int i = 0;
        for (CommentNode ignored : user.walkTree()) {
            i++;
            if (ignored.hasMoreComments()) {
                i++;
            }
        }

        return i - 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || (users != null && !users.isEmpty() && position == (users.size() - hidden.size()) + 2) || (users != null && users.isEmpty() && position == 2)) {
            return SPACER;
        } else {
            position -= 1;
        }
        if (position == 0) {
            return HEADER;
        }

        return (users.get(getRealPosition(position - 1)) instanceof CommentItem ? 2 : 3);
    }

    @Override
    public int getItemCount() {
        if (users == null) {
            return 2;
        } else {
            return 3 + (users.size() - hidden.size());
        }
    }

    public void unhideAll(CommentNode n, int i) {
        try {
            int counter = unhideNumber(n, 0);
            if (SettingValues.collapseComments) {
                listView.setItemAnimator(null);
                notifyItemRangeInserted(i, counter);
            } else {
                try {
                    listView.setItemAnimator(new AlphaInAnimator());
                } catch (Exception e) {
                }
                notifyItemRangeInserted(i, counter);
            }
        } catch (Exception e) {

        }
    }

    public void hideAll(CommentNode n) {

        int counter = hideNumber(n, 0);
        if (SettingValues.collapseComments) {
            listView.setItemAnimator(null);
            notifyDataSetChanged();
        } else {
            listView.setItemAnimator(new AlphaInAnimator());
            notifyDataSetChanged();
        }

    }

    public void hideAll(CommentNode n, int i) {

        int counter = hideNumber(n, 0);
        if (SettingValues.collapseComments) {
            listView.setItemAnimator(null);
            notifyItemRangeRemoved(i, counter);
        } else {
            listView.setItemAnimator(new AlphaInAnimator());
            notifyItemRangeRemoved(i, counter);
        }

    }

    public boolean parentHidden(CommentNode n) {
        n = n.getParent();
        while (n != null && n.getDepth() > 0) {
            String name = n.getComment().getFullName();
            if (hiddenPersons.contains(name) || hidden.contains(name)) {
                return true;
            }
            n = n.getParent();
        }
        return false;
    }

    public int unhideNumber(CommentNode n, int i) {
        for (CommentNode ignored : n.getChildren()) {

            if (!ignored.getComment().getFullName().equals(n.getComment().getFullName())) {
                boolean parentHidden = parentHidden(ignored);

                if (parentHidden) {
                    continue;
                }

                String name = ignored.getComment().getFullName();

                if (hidden.contains(name) || hiddenPersons.contains(name)) {
                    hidden.remove(name);
                    i++;

                    if (ignored.hasMoreComments() && !hiddenPersons.contains(name)) {
                        name = name + "more";
                        if (hidden.contains(name)) {
                            hidden.remove(name);
                            toCollapse.remove(name);
                            i++;
                        }
                    }
                }
                i += unhideNumber(ignored, 0);
            }
        }
        if (n.hasMoreComments() && !parentHidden(n) && !hiddenPersons.contains(n.getComment().getFullName())) {
            String fullname = n.getComment().getFullName() + "more";

            if (hidden.contains(fullname)) {
                i++;
                hidden.remove(fullname);
            }
        }
        return i;
    }

    public int hideNumber(CommentNode n, int i) {
        for (CommentNode ignored : n.getChildren()) {
            if (!ignored.getComment().getFullName().equals(n.getComment().getFullName())) {
                String fullname = ignored.getComment().getFullName();

                if (!hidden.contains(fullname)) {
                    i++;
                    hidden.add(fullname);
                }
                if (ignored.hasMoreComments()) {
                    if (currentLoading != null && currentLoading.fullname.equals(fullname)) {
                        currentLoading.cancel(true);
                    }
                    fullname = fullname + "more";

                    if (!hidden.contains(fullname)) {
                        i++;
                        hidden.add(fullname);
                    }
                }
                i += hideNumber(ignored, 0);
            }

        }
        if (n.hasMoreComments()) {
            String fullname = n.getComment().getFullName() + "more";
            if (!hidden.contains(fullname)) {
                i++;
                hidden.add(fullname);
            }
        }
        return i;
    }

    public int getRealPosition(int position) {
        int hElements = getHiddenCountUpTo(position);
        int diff = 0;
        for (int i = 0; i < hElements; i++) {
            diff++;
            if (hidden.contains(users.get(position + diff).getName())) {
                i--;
            }
        }
        return (position + diff);
    }

    private int getHiddenCountUpTo(int location) {
        int count = 0;
        for (int i = 0; (i <= location && i < users.size()); i++) {
            if (users.size() > i && hidden.contains(users.get(i).getName())) {
                count++;
            }
        }
        return count;
    }


    public class AsyncLoadMore extends AsyncTask<MoreChildItem, Void, Integer> {
        public MoreCommentViewHolder holder;
        public int holderPos;
        public int position;
        public int dataPos;
        public String fullname;

        public AsyncLoadMore(int position, int holderPos, MoreCommentViewHolder holder, int dataPos, String fullname) {
            this.holderPos = holderPos;
            this.holder = holder;
            this.position = position;
            this.dataPos = dataPos;
            this.fullname = fullname;
        }

        @Override
        public void onPostExecute(Integer data) {
            currentLoading = null;
            if (data != null) {
                listView.setItemAnimator(new SlideRightAlphaAnimator());
                notifyItemRangeInserted(holderPos, data);
                currentPos = holderPos;
                toShiftTo = ((LinearLayoutManager) listView.getLayoutManager()).findLastVisibleItemPosition();
                shiftFrom = ((LinearLayoutManager) listView.getLayoutManager()).findFirstVisibleItemPosition();
            } else if (users.get(dataPos) instanceof MoreChildItem) {
                final MoreChildItem baseNode = (MoreChildItem) users.get(dataPos);
                if (baseNode.children.getCount() > 0) {
                    holder.content.setText(mContext.getString(R.string.comment_load_more, baseNode.children.getCount()));
                } else if (!baseNode.children.getChildrenIds().isEmpty()) {
                    holder.content.setText(R.string.comment_load_more_number_unknown);
                } else {
                    holder.content.setText(R.string.thread_continue);
                }
                holder.loading.setVisibility(View.GONE);
            }
        }

        @Override
        protected Integer doInBackground(MoreChildItem... params) {
            ArrayList<CommentObject> finalData = new ArrayList<>();
            int i = 0;

            if (params.length > 0) {
                try {
                    CommentNode node = params[0].comment;
                    node.loadMoreComments(Authentication.reddit);
                    HashMap<Integer, MoreChildItem> waiting = new HashMap<>();

                    for (CommentNode n : node.walkTree()) {
                        if (!keys.containsKey(n.getComment().getFullName())) {

                            CommentObject obj = new CommentItem(n);
                            ArrayList<Integer> removed = new ArrayList<>();
                            Map<Integer, MoreChildItem> map = new TreeMap<>(Collections.reverseOrder());
                            map.putAll(waiting);

                            for (Integer i2 : map.keySet()) {
                                if (i2 >= n.getDepth()) {
                                    finalData.add(waiting.get(i2));
                                    removed.add(i2);
                                    waiting.remove(i2);
                                    i++;

                                }
                            }

                            finalData.add(obj);
                            i++;

                            if (n.hasMoreComments()) {
                                waiting.put(n.getDepth(), new MoreChildItem(n, n.getMoreChildren()));
                            }
                        }
                    }
                    if (node.hasMoreComments()) {
                        finalData.add(new MoreChildItem(node, node.getMoreChildren()));
                        i++;
                    }
                } catch (Exception e) {
                    Log.w(LogUtil.getTag(), "Cannot load more comments " + e);
                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    e.printStackTrace(printWriter);
                    String stacktrace = writer.toString().replace(";", ",");
                    if (stacktrace.contains("UnknownHostException") || stacktrace.contains("SocketTimeoutException") || stacktrace.contains("ConnectException")) {
                        //is offline
                        final Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                                          @Override
                                          public void run() {
                                              try {
                                                  new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_title)
                                                          .setMessage(R.string.err_connection_failed_msg)
                                                          .setNegativeButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                              @Override
                                                              public void onClick(DialogInterface dialog, int which) {

                                                              }
                                                          }).show();
                                              } catch (Exception ignored) {

                                              }
                                          }
                                      }

                        );
                    } else if (stacktrace.contains("403 Forbidden") || stacktrace.contains("401 Unauthorized")) {
                        //Un-authenticated
                        final Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_title)
                                            .setMessage(R.string.err_refused_request_msg)
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Reddit.authentication.updateToken((mContext));
                                        }
                                    }).show();
                                } catch (Exception ignored) {

                                }
                            }
                        });

                    } else if (stacktrace.contains("404 Not Found") || stacktrace.contains("400 Bad Request")) {
                        final Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_title)
                                            .setMessage(R.string.err_could_not_find_content_msg)
                                            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }

                                            }).show();
                                } catch (Exception ignored) {

                                }
                            }
                        });
                    }
                    return null;
                }

                shifted += i;
                users.remove(position);
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemRemoved(holderPos);
                    }
                });
                int oldSize = users.size();
                users.addAll(position, finalData);
                int newSize = users.size();

                for (int i2 = 0; i2 < users.size(); i2++) {
                    keys.put(users.get(i2).getName(), i2);
                }
                i = newSize - oldSize;
            }
            return i;
        }
    }

    public class AsyncForceLoadChild extends AsyncTask<String, Void, Integer> {
        CommentNode node;
        public int holderPos;
        public int position;


        public AsyncForceLoadChild(int position, int holderPos, CommentNode baseNode) {
            this.holderPos = holderPos;
            this.node = baseNode;
            this.position = position;
        }

        @Override
        public void onPostExecute(Integer data) {
            if (data != -1) {
                listView.setItemAnimator(new SlideRightAlphaAnimator());

                notifyItemInserted(holderPos + 1);

                currentPos = holderPos + 1;
                toShiftTo = ((LinearLayoutManager) listView.getLayoutManager()).findLastVisibleItemPosition();
                shiftFrom = ((LinearLayoutManager) listView.getLayoutManager()).findFirstVisibleItemPosition();

                dataSet.refreshLayout.setRefreshing(false);
            } else {
                //Comment could not be found, force a reload
                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    public void run() {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dataSet.refreshLayout.setRefreshing(false);
                                dataSet.loadMoreReply(CommentAdapter.this);
                            }
                        });
                    }
                }, 2000);
            }
        }

        @Override
        protected Integer doInBackground(String... params) {

            int i = 0;

            if (params.length > 0) {
                try {
                    node.insertComment(Authentication.reddit, "t1_" + params[0]);
                    for (CommentNode n : node.walkTree()) {
                        if (n.getComment().getFullName().contains(params[0])) {
                            users.add(position, new CommentItem(n));
                            i++;
                        }
                    }

                } catch (Exception e) {
                    Log.w(LogUtil.getTag(), "Cannot load more comments " + e);
                    i = -1;
                }

                shifted += i;

                for (int i2 = 0; i2 < users.size(); i2++) {
                    keys.put(users.get(i2).getName(), i2);
                }
            }
            return i;
        }
    }

    public class ReplyTaskComment extends AsyncTask<String, Void, String> {
        public Contribution sub;
        CommentNode node;
        CommentViewHolder holder;
        boolean isSubmission;

        public ReplyTaskComment(Contribution n, CommentNode node, CommentViewHolder holder) {
            sub = n;
            this.holder = holder;
            this.node = node;
        }

        public ReplyTaskComment(Contribution n) {
            sub = n;
            isSubmission = true;
        }

        @Override
        public void onPostExecute(final String s) {
            if (s == null || s.isEmpty()) {

                if (commentBack != null && !commentBack.isEmpty()) {
                    Drafts.addDraft(commentBack);
                    new AlertDialogWrapper.Builder(mContext)
                            .setTitle(R.string.err_comment_post)
                            .setMessage(((why == null) ? "" : mContext.getString(R.string.err_comment_post_reason) + why) + mContext.getString(R.string.err_comment_post_message))
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                } else {
                    new AlertDialogWrapper.Builder(mContext)
                            .setTitle(R.string.err_comment_post)
                            .setMessage(((why == null) ? "" : mContext.getString(R.string.err_comment_post_reason) + why) + mContext.getString(R.string.err_comment_post_nosave_message))
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                }
            } else {
                if (isSubmission) {
                    new AsyncForceLoadChild(0, 0, submission.getComments()).execute(s);
                } else {
                    new AsyncForceLoadChild(getRealPosition(holder.getAdapterPosition() - 1), holder.getAdapterPosition(), node).execute(s);
                }
            }
        }

        String why;
        String commentBack;

        @Override
        protected String doInBackground(String... comment) {
            if (Authentication.me != null) {
                try {
                    commentBack = comment[0];
                    return new AccountManager(Authentication.reddit).reply(sub, comment[0]);
                } catch (Exception e) {
                    if (e instanceof ApiException) {
                        why = ((ApiException) e).getExplanation();
                    }
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    public String reportReason;

    public void showBottomSheet(final Context mContext, final CommentViewHolder holder, final CommentNode n2) {

        int[] attrs = new int[]{R.attr.tint};
        final Comment n = n2.getComment();
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
        Drawable saved = mContext.getResources().getDrawable(R.drawable.iconstarfilled);
        Drawable gild = mContext.getResources().getDrawable(R.drawable.gild);
        Drawable copy = mContext.getResources().getDrawable(R.drawable.ic_content_copy);
        Drawable share = mContext.getResources().getDrawable(R.drawable.share);
        Drawable parent = mContext.getResources().getDrawable(R.drawable.commentchange);
        Drawable permalink = mContext.getResources().getDrawable(R.drawable.link);
        Drawable report = mContext.getResources().getDrawable(R.drawable.report);

        profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        saved.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        gild.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        report.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        copy.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        parent.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        permalink.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        ta.recycle();

        BottomSheet.Builder b = new BottomSheet.Builder((Activity) mContext)
                .title(Html.fromHtml(n.getBody()));


        if (Authentication.didOnline) {
            b.sheet(1, profile, "/u/" + n.getAuthor());
            String save = mContext.getString(R.string.btn_save);
            if (ActionStates.isSaved(n)) {
                save = mContext.getString(R.string.comment_unsave);
            }
            if (Authentication.isLoggedIn) {
                b.sheet(3, saved, save);
                b.sheet(16, report, mContext.getString(R.string.btn_report));

            }
        }
        b.sheet(5, gild, mContext.getString(R.string.comment_gild))
                .sheet(7, copy, mContext.getString(R.string.misc_copy_text))
                .sheet(23, permalink, mContext.getString(R.string.comment_permalink))
                .sheet(4, share, mContext.getString(R.string.comment_share));
        if (!currentBaseNode.isTopLevel()) {
            b.sheet(10, parent, mContext.getString(R.string.comment_parent));
        }
        b.listener(new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           switch (which) {
                               case 1: {
                                   Intent i = new Intent(mContext, Profile.class);
                                   i.putExtra(Profile.EXTRA_PROFILE, n.getAuthor());
                                   mContext.startActivity(i);
                               }
                               break;

                               case 3:
                                   saveComment(n, mContext, holder);
                                   break;
                               case 23: {
                                   String s = "https://reddit.com" + submission.getPermalink() +
                                           n.getFullName().substring(3, n.getFullName().length()) + "?context=3";
                                   new OpenRedditLink(mContext, s);

                               }
                               break;
                               case 5: {
                                   Intent i = new Intent(mContext, Website.class);
                                   i.putExtra(Website.EXTRA_URL, "https://reddit.com" + submission.getPermalink() +
                                           n.getFullName().substring(3, n.getFullName().length()) + "?context=3");
                                   i.putExtra(Website.EXTRA_COLOR, Palette.getColor(n.getSubredditName()));
                                   mContext.startActivity(i);
                               }
                               break;
                               case 16:
                                   reportReason = "";
                                   new MaterialDialog.Builder(mContext).input(mContext.getString(R.string.input_reason_for_report), null, true, new MaterialDialog.InputCallback() {
                                       @Override
                                       public void onInput(MaterialDialog dialog, CharSequence input) {
                                           reportReason = input.toString();
                                       }
                                   }).alwaysCallInputCallback()
                                           .positiveText(R.string.btn_report)
                                           .negativeText(R.string.btn_cancel)
                                           .onNegative(null)
                                           .onPositive(new MaterialDialog.SingleButtonCallback() {
                                               @Override
                                               public void onClick(MaterialDialog dialog, DialogAction which) {
                                                   new AsyncTask<Void, Void, Void>() {
                                                       @Override
                                                       protected Void doInBackground(Void... params) {
                                                           try {
                                                               new AccountManager(Authentication.reddit).report(currentBaseNode.getComment(), reportReason);
                                                           } catch (ApiException e) {
                                                               e.printStackTrace();
                                                           }
                                                           return null;
                                                       }

                                                       @Override
                                                       protected void onPostExecute(Void aVoid) {

                                                           Snackbar s = Snackbar.make(listView, R.string.msg_report_sent, Snackbar.LENGTH_SHORT);

                                                           View view = s.getView();
                                                           TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                           tv.setTextColor(Color.WHITE);
                                                           s.show();

                                                       }
                                                   }.execute();
                                               }
                                           })
                                           .show();
                                   break;
                               case 10:
                                   int old = holder.getAdapterPosition();
                                   int pos = (old < 2) ? 0 : old - 1;
                                   for (int i = pos - 1; i >= 0; i--) {
                                       CommentObject o = users.get(getRealPosition(i));
                                       if (o instanceof CommentItem && pos - 1 != i && o.comment.getDepth() < n2.getDepth()) {
                                           LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                                           final View dialoglayout = inflater.inflate(R.layout.parent_comment_dialog, null);
                                           final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                                           Comment parent = o.comment.getComment();
                                           setViews(parent.getDataNode().get("body_html").asText(), submission.getSubredditName(), (SpoilerRobotoTextView) dialoglayout.findViewById(R.id.firstTextView), (CommentOverflow) dialoglayout.findViewById(R.id.commentOverflow));
                                           builder.setView(dialoglayout);
                                           builder.show();
                                           break;
                                       }
                                   }
                                   break;
                               case 7:
                                   ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                   ClipData clip = ClipData.newPlainText("Comment text", n.getBody());
                                   clipboard.setPrimaryClip(clip);

                                   Toast.makeText(mContext, "Comment text copied", Toast.LENGTH_SHORT).show();
                                   break;
                               case 4:
                                   Reddit.defaultShareText(submission.getTitle(), "https://reddit.com" + submission.getPermalink() +
                                                   n.getFullName().substring(3, n.getFullName().length()) + "?context=3"
                                           , mContext);
                                   break;
                           }
                       }
                   }
        );
        b.show();
    }

    private void saveComment(final Comment comment, final Context mContext, final CommentViewHolder holder) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (ActionStates.isSaved(comment)) {
                        new AccountManager(Authentication.reddit).unsave(comment);
                        ActionStates.setSaved(comment, false);
                    } else {
                        new AccountManager(Authentication.reddit).save(comment);
                        ActionStates.setSaved(comment, true);
                    }

                } catch (ApiException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Snackbar s;
                if (ActionStates.isSaved(comment)) {
                    s = Snackbar.make(holder.itemView, "Comment saved", Snackbar.LENGTH_LONG);
                    if (Authentication.me.hasGold()) {
                        s.setAction("CATEGORIZE", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                categorizeComment(comment, mContext);
                            }
                        });
                    }
                } else {
                    s = Snackbar.make(holder.itemView, "Comment un-saved", Snackbar.LENGTH_SHORT);
                }
                View view = s.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                s.show();
            }
        }.execute();
    }

    private void categorizeComment(final Comment comment, final Context mContext) {
        new AsyncTask<Void, Void, List<String>>() {

            Dialog d;

            @Override
            public void onPreExecute() {
                d = new MaterialDialog.Builder(mContext).progress(true, 100).title("Loading categories").show();
            }

            @Override
            protected List<String> doInBackground(Void... params) {
                try {
                    List<String> categories = new ArrayList<String>(new AccountManager(Authentication.reddit).getSavedCategories());
                    categories.add("New category");
                    return categories;
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<String>() {{
                        add("New category");
                    }};
                }
            }

            @Override
            public void onPostExecute(final List<String> data) {
                try {
                    new MaterialDialog.Builder(mContext).items(data)
                            .title("Select flair")
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, final View itemView, int which, CharSequence text) {
                                    final String t = data.get(which);
                                    if (which == data.size() - 1) {
                                        new MaterialDialog.Builder(mContext).title("Set category name")
                                                .input("Category name", null, false, new MaterialDialog.InputCallback() {
                                                    @Override
                                                    public void onInput(MaterialDialog dialog, CharSequence input) {

                                                    }
                                                }).positiveText("Set")
                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                    @Override
                                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                                        final String flair = dialog.getInputEditText().getText().toString();
                                                        new AsyncTask<Void, Void, Boolean>() {
                                                            @Override
                                                            protected Boolean doInBackground(Void... params) {
                                                                try {
                                                                    new AccountManager(Authentication.reddit).save(comment, flair);
                                                                    return true;
                                                                } catch (ApiException e) {
                                                                    e.printStackTrace();
                                                                    return false;
                                                                }
                                                            }

                                                            @Override
                                                            protected void onPostExecute(Boolean done) {
                                                                Snackbar s;
                                                                if (done) {
                                                                    if (itemView != null) {
                                                                        s = Snackbar.make(itemView, R.string.submission_info_saved, Snackbar.LENGTH_SHORT);
                                                                        View view = s.getView();
                                                                        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                        tv.setTextColor(Color.WHITE);
                                                                        s.show();
                                                                    }
                                                                } else {
                                                                    if (itemView != null) {
                                                                        s = Snackbar.make(itemView, "Error setting category", Snackbar.LENGTH_SHORT);
                                                                        View view = s.getView();
                                                                        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                        tv.setTextColor(Color.WHITE);
                                                                        s.show();
                                                                    }
                                                                }

                                                            }
                                                        }.execute();
                                                    }
                                                }).negativeText(R.string.btn_cancel)
                                                .show();
                                    } else {
                                        new AsyncTask<Void, Void, Boolean>() {
                                            @Override
                                            protected Boolean doInBackground(Void... params) {
                                                try {
                                                    new AccountManager(Authentication.reddit).save(comment, t);
                                                    return true;
                                                } catch (ApiException e) {
                                                    e.printStackTrace();
                                                    return false;
                                                }
                                            }

                                            @Override
                                            protected void onPostExecute(Boolean done) {
                                                Snackbar s;
                                                if (done) {
                                                    if (itemView != null) {
                                                        s = Snackbar.make(itemView, R.string.submission_info_saved, Snackbar.LENGTH_SHORT);
                                                        View view = s.getView();
                                                        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                        tv.setTextColor(Color.WHITE);
                                                        s.show();
                                                    }
                                                } else {
                                                    if (itemView != null) {
                                                        s = Snackbar.make(itemView, "Error setting category", Snackbar.LENGTH_SHORT);
                                                        View view = s.getView();
                                                        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                        tv.setTextColor(Color.WHITE);
                                                        s.show();
                                                    }
                                                }
                                            }
                                        }.execute();
                                    }
                                }
                            }).show();
                    if (d != null) {
                        d.dismiss();
                    }
                } catch (Exception ignored) {

                }
            }
        }.execute();
    }
}