package me.ccrama.redditslide.Adapters;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.bottomsheet.BottomSheet;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.oauth.InvalidScopeException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.Reauthenticate;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.UserTags;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Views.RoundedBackgroundSpan;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by Carlos on 8/4/2016.
 */
public class CommentAdapterHelper {
    public static String reportReason;

    public static void showOverflowBottomSheet(final CommentAdapter adapter, final Context mContext,
            final CommentViewHolder holder, final CommentNode baseNode) {

        int[] attrs = new int[]{R.attr.tintColor};
        final Comment n = baseNode.getComment();
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
        Drawable saved = mContext.getResources().getDrawable(R.drawable.iconstarfilled);
        Drawable gild = mContext.getResources().getDrawable(R.drawable.gild);
        Drawable copy = mContext.getResources().getDrawable(R.drawable.ic_content_copy);
        Drawable share = mContext.getResources().getDrawable(R.drawable.share);
        Drawable parent = mContext.getResources().getDrawable(R.drawable.commentchange);
        Drawable replies = mContext.getResources().getDrawable(R.drawable.notifs);
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
        replies.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        ta.recycle();

        BottomSheet.Builder b =
                new BottomSheet.Builder((Activity) mContext).title(Html.fromHtml(n.getBody()));

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
            if (Authentication.name.equalsIgnoreCase(baseNode.getComment().getAuthor())) {
                b.sheet(50, replies, mContext.getString(R.string.disable_replies_comment));
            }
        }
        b.sheet(5, gild, mContext.getString(R.string.comment_gild))
                .sheet(7, copy, mContext.getString(R.string.misc_copy_text))
                .sheet(23, permalink, mContext.getString(R.string.comment_permalink))
                .sheet(4, share, mContext.getString(R.string.comment_share));
        if (!adapter.currentBaseNode.isTopLevel()) {
            b.sheet(10, parent, mContext.getString(R.string.comment_parent));
        }
        b.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 1: {
                        //Go to author
                        Intent i = new Intent(mContext, Profile.class);
                        i.putExtra(Profile.EXTRA_PROFILE, n.getAuthor());
                        mContext.startActivity(i);
                    }
                    break;
                    case 3:
                        //Save comment
                        saveComment(n, mContext, holder);
                        break;
                    case 23: {
                        //Go to comment permalink
                        String s = "https://reddit.com"
                                + adapter.submission.getPermalink()
                                + n.getFullName().substring(3, n.getFullName().length())
                                + "?context=3";
                        new OpenRedditLink(mContext, s);
                    }
                    break;
                    case 50: {
                        setReplies(baseNode.getComment(), holder, !baseNode.getComment()
                                .getDataNode()
                                .get("send_replies")
                                .asBoolean());
                    }
                    break;
                    case 5: {
                        //Gild comment
                        Intent i = new Intent(mContext, Website.class);
                        i.putExtra(Website.EXTRA_URL, "https://reddit.com"
                                + adapter.submission.getPermalink()
                                + n.getFullName().substring(3, n.getFullName().length())
                                + "?context=3&inapp=false");
                        i.putExtra(Website.EXTRA_COLOR, Palette.getColor(n.getSubredditName()));
                        mContext.startActivity(i);
                    }
                    break;
                    case 16:
                        //report
                        reportReason = "";
                        new MaterialDialog.Builder(mContext).input(
                                mContext.getString(R.string.input_reason_for_report), null, true,
                                new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        reportReason = input.toString();
                                    }
                                })
                                .title(R.string.report_comment)
                                .alwaysCallInputCallback()
                                .inputType(InputType.TYPE_CLASS_TEXT
                                        | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
                                        | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                                .positiveText(R.string.btn_report)
                                .negativeText(R.string.btn_cancel)
                                .onNegative(null)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                        new AsyncReportTask(adapter.currentBaseNode,
                                                adapter.listView).execute();
                                    }
                                })
                                .show();
                        break;
                    case 10:
                        //View comment parent
                        viewCommentParent(adapter, holder, mContext, baseNode);
                        break;
                    case 7:
                        //Show select and copy text to clipboard
                        final TextView showText = new TextView(mContext);
                        showText.setText(StringEscapeUtils.unescapeHtml4(n.getBody()));
                        showText.setTextIsSelectable(true);
                        int sixteen = Reddit.dpToPxVertical(24);
                        showText.setPadding(sixteen, 0, sixteen, 0);
                        AlertDialogWrapper.Builder builder =
                                new AlertDialogWrapper.Builder(mContext);
                        builder.setView(showText)
                                .setTitle("Select text to copy")
                                .setCancelable(true)
                                .setPositiveButton("COPY", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String selected = showText.getText()
                                                .toString()
                                                .substring(showText.getSelectionStart(),
                                                        showText.getSelectionEnd());
                                        ClipboardManager clipboard =
                                                (ClipboardManager) mContext.getSystemService(
                                                        Context.CLIPBOARD_SERVICE);
                                        ClipData clip =
                                                ClipData.newPlainText("Comment text", selected);
                                        clipboard.setPrimaryClip(clip);

                                        Toast.makeText(mContext, R.string.submission_comment_copied,
                                                Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .setNegativeButton(R.string.btn_cancel, null)
                                .setNeutralButton("COPY ALL",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ClipboardManager clipboard =
                                                        (ClipboardManager) mContext.getSystemService(
                                                                Context.CLIPBOARD_SERVICE);
                                                ClipData clip =
                                                        ClipData.newPlainText("Comment text",
                                                                Html.fromHtml(n.getBody()));
                                                clipboard.setPrimaryClip(clip);

                                                Toast.makeText(mContext,
                                                        R.string.submission_comment_copied,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                .show();
                        break;
                    case 4:
                        //Share comment
                        Reddit.defaultShareText(adapter.submission.getTitle(), "https://reddit.com"
                                + adapter.submission.getPermalink()
                                + n.getFullName().substring(3, n.getFullName().length())
                                + "?context=3", mContext);
                        break;
                }
            }
        });
        b.show();
    }

    private static void setReplies(final Comment comment, final CommentViewHolder holder,
            final boolean showReplies) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    new AccountManager(Authentication.reddit).sendRepliesToInbox(comment,
                            showReplies);

                } catch (ApiException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Snackbar s;
                try {
                    if (holder.itemView != null) {
                        if (!showReplies) {
                            s = Snackbar.make(holder.itemView, R.string.replies_disabled_comment,
                                    Snackbar.LENGTH_LONG);
                        } else {
                            s = Snackbar.make(holder.itemView, R.string.replies_enabled_comment,
                                    Snackbar.LENGTH_SHORT);
                        }
                        View view = s.getView();
                        TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        s.show();
                    }
                } catch (Exception ignored) {

                }
            }
        }.execute();
    }

    private static void viewCommentParent(CommentAdapter adapter, CommentViewHolder holder,
            Context mContext, CommentNode baseNode) {
        int old = holder.getAdapterPosition();
        int pos = (old < 2) ? 0 : old - 1;
        for (int i = pos - 1; i >= 0; i--) {
            CommentObject o = adapter.currentComments.get(adapter.getRealPosition(i));
            if (o instanceof CommentItem
                    && pos - 1 != i
                    && o.comment.getDepth() < baseNode.getDepth()) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.parent_comment_dialog, null);
                final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                Comment parent = o.comment.getComment();
                adapter.setViews(parent.getDataNode().get("body_html").asText(),
                        adapter.submission.getSubredditName(),
                        (SpoilerRobotoTextView) dialoglayout.findViewById(R.id.firstTextView),
                        (CommentOverflow) dialoglayout.findViewById(R.id.commentOverflow));
                builder.setView(dialoglayout);
                builder.show();
                break;
            }
        }
    }

    private static void saveComment(final Comment comment, final Context mContext,
            final CommentViewHolder holder) {
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
                try {
                    if (holder.itemView != null) {
                        if (ActionStates.isSaved(comment)) {
                            s = Snackbar.make(holder.itemView, R.string.submission_comment_saved,
                                    Snackbar.LENGTH_LONG);
                            if (Authentication.me != null && Authentication.me.hasGold()) {
                                s.setAction(R.string.category_categorize,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                categorizeComment(comment, mContext);
                                            }
                                        });
                            }
                        } else {
                            s = Snackbar.make(holder.itemView, R.string.submission_comment_unsaved,
                                    Snackbar.LENGTH_SHORT);
                        }
                        View view = s.getView();
                        TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        s.show();
                    }
                } catch (Exception ignored) {

                }
            }
        }.execute();
    }

    private static void categorizeComment(final Comment comment, final Context mContext) {
        new AsyncTask<Void, Void, List<String>>() {

            Dialog d;

            @Override
            public void onPreExecute() {
                d = new MaterialDialog.Builder(mContext).progress(true, 100)
                        .content(R.string.misc_please_wait)
                        .title(R.string.profile_category_loading)
                        .show();
            }

            @Override
            protected List<String> doInBackground(Void... params) {
                try {
                    List<String> categories = new ArrayList<String>(
                            new AccountManager(Authentication.reddit).getSavedCategories());
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
                            .title(R.string.sidebar_select_flair)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, final View itemView,
                                        int which, CharSequence text) {
                                    final String t = data.get(which);
                                    if (which == data.size() - 1) {
                                        new MaterialDialog.Builder(mContext).title(
                                                R.string.category_set_name)
                                                .input(mContext.getString(
                                                        R.string.category_set_name_hint), null,
                                                        false, new MaterialDialog.InputCallback() {
                                                            @Override
                                                            public void onInput(
                                                                    MaterialDialog dialog,
                                                                    CharSequence input) {

                                                            }
                                                        })
                                                .positiveText(R.string.btn_set)
                                                .onPositive(
                                                        new MaterialDialog.SingleButtonCallback() {
                                                            @Override
                                                            public void onClick(
                                                                    MaterialDialog dialog,
                                                                    DialogAction which) {
                                                                final String flair =
                                                                        dialog.getInputEditText()
                                                                                .getText()
                                                                                .toString();
                                                                new AsyncTask<Void, Void, Boolean>() {
                                                                    @Override
                                                                    protected Boolean doInBackground(
                                                                            Void... params) {
                                                                        try {
                                                                            new AccountManager(
                                                                                    Authentication.reddit)
                                                                                    .save(comment,
                                                                                            flair);
                                                                            return true;
                                                                        } catch (ApiException e) {
                                                                            e.printStackTrace();
                                                                            return false;
                                                                        }
                                                                    }

                                                                    @Override
                                                                    protected void onPostExecute(
                                                                            Boolean done) {
                                                                        Snackbar s;
                                                                        if (done) {
                                                                            if (itemView != null) {
                                                                                s = Snackbar.make(
                                                                                        itemView,
                                                                                        R.string.submission_info_saved,
                                                                                        Snackbar.LENGTH_SHORT);
                                                                                View view =
                                                                                        s.getView();
                                                                                TextView tv =
                                                                                        view.findViewById(
                                                                                                android.support.design.R.id.snackbar_text);
                                                                                tv.setTextColor(
                                                                                        Color.WHITE);
                                                                                s.show();
                                                                            }
                                                                        } else {
                                                                            if (itemView != null) {
                                                                                s = Snackbar.make(
                                                                                        itemView,
                                                                                        R.string.category_set_error,
                                                                                        Snackbar.LENGTH_SHORT);
                                                                                View view =
                                                                                        s.getView();
                                                                                TextView tv =
                                                                                        view.findViewById(
                                                                                                android.support.design.R.id.snackbar_text);
                                                                                tv.setTextColor(
                                                                                        Color.WHITE);
                                                                                s.show();
                                                                            }
                                                                        }

                                                                    }
                                                                }.execute();
                                                            }
                                                        })
                                                .negativeText(R.string.btn_cancel)
                                                .show();
                                    } else {
                                        new AsyncTask<Void, Void, Boolean>() {
                                            @Override
                                            protected Boolean doInBackground(Void... params) {
                                                try {
                                                    new AccountManager(Authentication.reddit).save(
                                                            comment, t);
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
                                                        s = Snackbar.make(itemView,
                                                                R.string.submission_info_saved,
                                                                Snackbar.LENGTH_SHORT);
                                                        View view = s.getView();
                                                        TextView tv = view.findViewById(
                                                                android.support.design.R.id.snackbar_text);
                                                        tv.setTextColor(Color.WHITE);
                                                        s.show();
                                                    }
                                                } else {
                                                    if (itemView != null) {
                                                        s = Snackbar.make(itemView,
                                                                R.string.category_set_error,
                                                                Snackbar.LENGTH_SHORT);
                                                        View view = s.getView();
                                                        TextView tv = view.findViewById(
                                                                android.support.design.R.id.snackbar_text);
                                                        tv.setTextColor(Color.WHITE);
                                                        s.show();
                                                    }
                                                }
                                            }
                                        }.execute();
                                    }
                                }
                            })
                            .show();
                    if (d != null) {
                        d.dismiss();
                    }
                } catch (Exception ignored) {

                }
            }
        }.execute();
    }

    public static void showModBottomSheet(final CommentAdapter adapter, final Context mContext,
            final CommentNode baseNode, final Comment comment, final CommentViewHolder holder,
            final Map<String, Integer> reports, final Map<String, String> reports2) {

        int[] attrs = new int[]{R.attr.tintColor};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        //Initialize drawables
        int color = ta.getColor(0, Color.WHITE);
        Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
        final Drawable report = mContext.getResources().getDrawable(R.drawable.report);
        final Drawable approve = mContext.getResources().getDrawable(R.drawable.support);
        final Drawable nsfw = mContext.getResources().getDrawable(R.drawable.hide);
        final Drawable pin = mContext.getResources().getDrawable(R.drawable.sub);
        final Drawable distinguish = mContext.getResources().getDrawable(R.drawable.iconstarfilled);
        final Drawable remove = mContext.getResources().getDrawable(R.drawable.close);
        final Drawable ban = mContext.getResources().getDrawable(R.drawable.ban);
        final Drawable spam = mContext.getResources().getDrawable(R.drawable.spam);

        //Tint drawables
        profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        report.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        approve.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        nsfw.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        distinguish.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        remove.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        pin.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        ban.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        spam.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        ta.recycle();

        //Bottom sheet builder
        BottomSheet.Builder b = new BottomSheet.Builder((Activity) mContext).title(
                Html.fromHtml(comment.getBody()));

        int reportCount = reports.size() + reports2.size();

        if (reportCount == 0) {
            b.sheet(0, report, mContext.getString(R.string.mod_no_reports));
        } else {
            b.sheet(0, report, mContext.getResources()
                    .getQuantityString(R.plurals.mod_btn_reports, reportCount, reportCount));
        }

        b.sheet(1, approve, mContext.getString(R.string.mod_btn_approve));
        // b.sheet(2, spam, mContext.getString(R.string.mod_btn_spam)) todo this


        final boolean stickied = comment.getDataNode().has("stickied") && comment.getDataNode()
                .get("stickied")
                .asBoolean();
        if (baseNode.isTopLevel()) {
            if (!stickied) {
                b.sheet(4, pin, mContext.getString(R.string.mod_sticky));
            } else {
                b.sheet(4, pin, mContext.getString(R.string.mod_unsticky));
            }
        }

        final boolean distinguished = !comment.getDataNode().get("distinguished").isNull();
        if (comment.getAuthor().equalsIgnoreCase(Authentication.name)) {
            if (!distinguished) {
                b.sheet(9, distinguish, mContext.getString(R.string.mod_distinguish));
            } else {
                b.sheet(9, distinguish, mContext.getString(R.string.mod_undistinguish));
            }
        }

        b.sheet(23, ban, mContext.getString(R.string.mod_ban_user));

        b.sheet(6, remove, mContext.getString(R.string.btn_remove))
                .sheet(10, spam, "Mark as spam")
                .sheet(8, profile, mContext.getString(R.string.mod_btn_author))
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                viewReports(mContext, reports, reports2);
                                break;
                            case 1:
                                doApproval(mContext, holder, comment, adapter);
                                break;
                            case 4:
                                if (stickied) {
                                    unStickyComment(mContext, holder, comment);
                                } else {
                                    stickyComment(mContext, holder, comment);
                                }
                                break;
                            case 9:
                                if (distinguished) {
                                    unDistinguishComment(mContext, holder, comment);
                                } else {
                                    distinguishComment(mContext, holder, comment);
                                }
                                break;
                            case 6:
                                removeComment(mContext, holder, comment, adapter, false);
                                break;
                            case 10:
                                removeComment(mContext, holder, comment, adapter, true);
                                break;
                            case 8:
                                Intent i = new Intent(mContext, Profile.class);
                                i.putExtra(Profile.EXTRA_PROFILE, comment.getAuthor());
                                mContext.startActivity(i);
                                break;
                            case 23:
                                showBan(mContext, adapter.listView, comment, "", "", "", "");
                                break;

                        }
                    }
                });
        b.show();
    }

    public static void showBan(final Context mContext, final View mToolbar,
            final Comment submission, String rs, String nt, String msg, String t) {
        LinearLayout l = new LinearLayout(mContext);
        l.setOrientation(LinearLayout.VERTICAL);
        int sixteen = Reddit.dpToPxVertical(16);
        l.setPadding(sixteen, 0, sixteen, 0);

        final EditText reason = new EditText(mContext);
        reason.setHint(R.string.mod_ban_reason);
        reason.setText(rs);
        reason.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        l.addView(reason);


        final EditText note = new EditText(mContext);
        note.setHint(R.string.mod_ban_note_mod);
        note.setText(nt);
        note.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        l.addView(note);

        final EditText message = new EditText(mContext);
        message.setHint(R.string.mod_ban_note_user);
        message.setText(msg);
        message.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        l.addView(message);

        final EditText time = new EditText(mContext);
        time.setHint(R.string.mod_ban_time);
        time.setText(t);
        time.setInputType(InputType.TYPE_CLASS_NUMBER);
        l.addView(time);

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
        builder.setView(l)
                .setTitle(mContext.getString(R.string.mod_ban_title, submission.getAuthor()))
                .setCancelable(true)
                .setPositiveButton(R.string.mod_btn_ban, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //to ban
                                if (reason.getText().toString().isEmpty() || time.getText()
                                        .toString()
                                        .isEmpty()) {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(
                                            R.string.mod_ban_requirements)
                                            .setMessage(R.string.misc_please_try_again)
                                            .setPositiveButton(R.string.btn_ok,
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog,
                                                                int which) {
                                                            showBan(mContext, mToolbar, submission,
                                                                    reason.getText().toString(),
                                                                    note.getText().toString(),
                                                                    message.getText().toString(),
                                                                    time.getText().toString());
                                                        }
                                                    })
                                            .setCancelable(false)
                                            .show();
                                } else {
                                    new AsyncTask<Void, Void, Boolean>() {
                                        @Override
                                        protected Boolean doInBackground(Void... params) {
                                            try {
                                                String n = note.getText().toString();
                                                String m = message.getText().toString();

                                                if (n.isEmpty()) {
                                                    n = null;
                                                }
                                                if (m.isEmpty()) {
                                                    m = null;
                                                }
                                                new ModerationManager(Authentication.reddit).banUser(
                                                        submission.getSubredditName(),
                                                        submission.getAuthor(), reason.getText().toString(),
                                                        n, m, Integer.valueOf(time.getText().toString()));
                                                return true;
                                            } catch (Exception e) {
                                                if (e instanceof InvalidScopeException) {
                                                    scope = true;
                                                }
                                                e.printStackTrace();
                                                return false;
                                            }
                                        }

                                        boolean scope;

                                        @Override
                                        protected void onPostExecute(Boolean done) {
                                            Snackbar s;
                                            if (done) {
                                                s = Snackbar.make(mToolbar, R.string.mod_ban_success,
                                                        Snackbar.LENGTH_SHORT);
                                            } else {
                                                if (scope) {
                                                    new AlertDialogWrapper.Builder(mContext).setTitle(
                                                            R.string.mod_ban_reauth)
                                                            .setMessage(R.string.mod_ban_reauth_question)
                                                            .setPositiveButton(R.string.btn_ok,
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(
                                                                                DialogInterface dialog,
                                                                                int which) {
                                                                            Intent i = new Intent(mContext,
                                                                                    Reauthenticate.class);
                                                                            mContext.startActivity(i);
                                                                        }
                                                                    })
                                                            .setNegativeButton(R.string.misc_maybe_later,
                                                                    null)
                                                            .setCancelable(false)
                                                            .show();
                                                }
                                                s = Snackbar.make(mToolbar, R.string.mod_ban_fail,
                                                        Snackbar.LENGTH_INDEFINITE)
                                                        .setAction(R.string.misc_try_again,
                                                                new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        showBan(mContext, mToolbar,
                                                                                submission,
                                                                                reason.getText().toString(),
                                                                                note.getText().toString(),
                                                                                message.getText()
                                                                                        .toString(),
                                                                                time.getText().toString());
                                                                    }
                                                                });

                                            }

                                            if (s != null)

                                            {
                                                View view = s.getView();
                                                TextView tv = view.findViewById(
                                                        android.support.design.R.id.snackbar_text);
                                                tv.setTextColor(Color.WHITE);
                                                s.show();
                                            }
                                        }
                                    }.execute();
                                }
                            }
                        }

                )
                .setNegativeButton(R.string.btn_cancel, null)
                .show();

    }

    public static void distinguishComment(final Context mContext, final CommentViewHolder holder,
            final Comment comment) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, R.string.comment_distinguished,
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setDistinguishedStatus(comment,
                            DistinguishedStatus.MODERATOR);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.execute();
    }

    public static void unDistinguishComment(final Context mContext, final CommentViewHolder holder,
            final Comment comment) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, R.string.comment_undistinguished,
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setDistinguishedStatus(comment,
                            DistinguishedStatus.NORMAL);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.execute();
    }

    public static void stickyComment(final Context mContext, final CommentViewHolder holder,
            final Comment comment) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, R.string.comment_stickied,
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
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

    public static void viewReports(final Context mContext, final Map<String, Integer> reports,
            final Map<String, String> reports2) {
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
                new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_reports)
                        .setItems(data.toArray(new CharSequence[data.size()]),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                        .show();
            }
        }.execute();
    }

    public static void doApproval(final Context mContext, final CommentViewHolder holder,
            final Comment comment, final CommentAdapter adapter) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    adapter.approved.add(comment.getFullName());
                    adapter.removed.remove(comment.getFullName());
                    holder.content.setText(
                            CommentAdapterHelper.getScoreString(comment, mContext, holder,
                                    adapter.submission, adapter));
                    Snackbar.make(holder.itemView, R.string.mod_approved, Snackbar.LENGTH_LONG)
                            .show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
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

    public static void unStickyComment(final Context mContext, final CommentViewHolder holder,
            final Comment comment) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, R.string.comment_unstickied,
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
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

    public static void removeComment(final Context mContext, final CommentViewHolder holder,
            final Comment comment, final CommentAdapter adapter, final boolean spam) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, R.string.comment_removed,
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                    adapter.removed.add(comment.getFullName());
                    adapter.approved.remove(comment.getFullName());
                    holder.content.setText(
                            CommentAdapterHelper.getScoreString(comment, mContext, holder,
                                    adapter.submission, adapter));
                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).remove(comment, spam);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.execute();
    }

    public static SpannableStringBuilder createApprovedLine(String approvedBy, Context c) {
        SpannableStringBuilder removedString = new SpannableStringBuilder("\n");
        SpannableStringBuilder mod = new SpannableStringBuilder("Approved by ");
        mod.append(approvedBy);
        mod.setSpan(new StyleSpan(Typeface.BOLD), 0, mod.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod.setSpan(new RelativeSizeSpan(0.8f), 0, mod.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod.setSpan(new ForegroundColorSpan(c.getResources().getColor(R.color.md_green_300)), 0,
                mod.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        removedString.append(mod);
        return removedString;
    }

    public static SpannableStringBuilder createRemovedLine(String removedBy, Context c) {
        SpannableStringBuilder removedString = new SpannableStringBuilder("\n");
        SpannableStringBuilder mod = new SpannableStringBuilder("Removed by ");
        if (removedBy.equalsIgnoreCase(
                "true")) {//Probably shadowbanned or removed not by mod action
            mod = new SpannableStringBuilder("Removed by Reddit");
        } else {
            mod.append(removedBy);
        }
        mod.setSpan(new StyleSpan(Typeface.BOLD), 0, mod.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod.setSpan(new RelativeSizeSpan(0.8f), 0, mod.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod.setSpan(new ForegroundColorSpan(c.getResources().getColor(R.color.md_red_300)), 0,
                mod.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        removedString.append(mod);
        return removedString;
    }

    public static Spannable getScoreString(Comment comment, Context mContext,
            CommentViewHolder holder, Submission submission, CommentAdapter adapter) {
        final String spacer =
                " " + mContext.getString(R.string.submission_properties_seperator_comments) + " ";
        SpannableStringBuilder titleString =
                new SpannableStringBuilder("\u200B");//zero width space to fix first span height
        SpannableStringBuilder author = new SpannableStringBuilder(comment.getAuthor());
        final int authorcolor = Palette.getFontColorUser(comment.getAuthor());

        author.setSpan(new TypefaceSpan("sans-serif-condensed"), 0, author.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        author.setSpan(new StyleSpan(Typeface.BOLD), 0, author.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (comment.getDistinguishedStatus() == DistinguishedStatus.ADMIN) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_red_300, false),
                    0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (comment.getDistinguishedStatus() == DistinguishedStatus.SPECIAL) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_red_500, false),
                    0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (comment.getDistinguishedStatus() == DistinguishedStatus.MODERATOR) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, false),
                    0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (Authentication.name != null && comment.getAuthor()
                .toLowerCase(Locale.ENGLISH)
                .equals(Authentication.name.toLowerCase(Locale.ENGLISH))) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_300,
                            false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (submission != null && comment.getAuthor()
                .toLowerCase(Locale.ENGLISH)
                .equals(submission.getAuthor().toLowerCase(Locale.ENGLISH)) && !comment.getAuthor()
                .equals("[deleted]")) {
            author.replace(0, author.length(), " " + comment.getAuthor() + " ");
            author.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_blue_300, false),
                    0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (authorcolor != 0) {
            author.setSpan(new ForegroundColorSpan(authorcolor), 0, author.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            default:
                scoreColor = (holder.textColorRegular);
                break;
        }

        String scoreText;
        if (comment.isScoreHidden()) {
            scoreText = "[" + mContext.getString(R.string.misc_score_hidden).toUpperCase() + "]";
        } else {
            scoreText = String.format(Locale.getDefault(), "%d", getScoreText(comment));
        }

        SpannableStringBuilder score = new SpannableStringBuilder(scoreText);

        if (score == null || score.toString().isEmpty()) {
            score = new SpannableStringBuilder("0");
        }
        if (!scoreText.contains("[")) {
            score.append(String.format(Locale.getDefault(), " %s", mContext.getResources()
                    .getQuantityString(R.plurals.points, comment.getScore())));
        }
        score.setSpan(new ForegroundColorSpan(scoreColor), 0, score.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        titleString.append(score);
        titleString.append((comment.isControversial() ? " †" : ""));

        titleString.append(spacer);

        Long time = comment.getCreated().getTime();
        String timeAgo = TimeUtils.getTimeAgo(time, mContext);

        SpannableStringBuilder timeSpan = new SpannableStringBuilder().append(
                (timeAgo == null || timeAgo.isEmpty()) ? "just now" : timeAgo);

        if (SettingValues.highlightTime
                && adapter.lastSeen != 0
                && adapter.lastSeen < time
                && !adapter.dataSet.single
                && SettingValues.commentLastVisit) {
            timeSpan.setSpan(new RoundedBackgroundSpan(Color.WHITE,
                            Palette.getColor(comment.getSubredditName()), false, mContext), 0,
                    timeSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        titleString.append(timeSpan);

        titleString.append(((comment.getEditDate() != null) ? " (edit " + TimeUtils.getTimeAgo(
                comment.getEditDate().getTime(), mContext) + ")" : ""));
        titleString.append("  ");

        if (comment.getDataNode().get("stickied").asBoolean()) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0"
                    + mContext.getString(R.string.submission_stickied).toUpperCase()
                    + "\u00A0");
            pinned.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, false),
                    0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (comment.getTimesGilded() > 0) {
            //if the comment has only been gilded once, don't show a number
            final String timesGilded = (comment.getTimesGilded() == 1) ? ""
                    : "\u200Ax" + Integer.toString(comment.getTimesGilded());
            SpannableStringBuilder gilded =
                    new SpannableStringBuilder("\u00A0★" + timesGilded + "\u00A0");
            TypedArray a = mContext.obtainStyledAttributes(
                    new FontPreferences(mContext).getPostFontStyle().getResId(),
                    R.styleable.FontStyle);
            int fontsize =
                    (int) (a.getDimensionPixelSize(R.styleable.FontStyle_font_cardtitle, -1) * .75);
            a.recycle();
            Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.gold);
            float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
            image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                    (int) Math.ceil(fontsize), true);
            gilded.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            gilded.setSpan(new RelativeSizeSpan(0.75f), 3, gilded.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(gilded);
            titleString.append(" ");
        }
        if (UserTags.isUserTagged(comment.getAuthor())) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(
                    "\u00A0" + UserTags.getUserTag(comment.getAuthor()) + "\u00A0");
            pinned.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_blue_500, false),
                    0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (UserSubscriptions.friends.contains(comment.getAuthor())) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(
                    "\u00A0" + mContext.getString(R.string.profile_friend) + "\u00A0");
            pinned.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_500,
                            false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (comment.getAuthorFlair() != null && (comment.getAuthorFlair().getText() != null
                || comment.getAuthorFlair().getCssClass() != null)) {
            String flairText = null;
            if (comment.getAuthorFlair() != null
                    && comment.getAuthorFlair().getText() != null
                    && !comment.getAuthorFlair().getText().isEmpty()) {
                flairText = comment.getAuthorFlair().getText();
            } else if (!comment.getAuthorFlair().getCssClass().isEmpty()) {
                flairText = comment.getAuthorFlair().getCssClass();
            }

            if (flairText != null) {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = mContext.getTheme();
                theme.resolveAttribute(R.attr.activity_background, typedValue, true);
                int color = typedValue.data;
                SpannableStringBuilder pinned =
                        new SpannableStringBuilder("\u00A0" + Html.fromHtml(flairText) + "\u00A0");
                pinned.setSpan(
                        new RoundedBackgroundSpan(holder.firstTextView.getCurrentTextColor(), color,
                                false, mContext), 0, pinned.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                titleString.append(pinned);
                titleString.append(" ");
            }
        }

        if (adapter.removed.contains(comment.getFullName()) || (comment.getBannedBy() != null
                && !adapter.approved.contains(comment.getFullName()))) {
            titleString.append(CommentAdapterHelper.createRemovedLine(
                    (comment.getBannedBy() == null) ? Authentication.name : comment.getBannedBy(),
                    mContext));
        } else if (adapter.approved.contains(comment.getFullName()) || (comment.getApprovedBy()
                != null && !adapter.removed.contains(comment.getFullName()))) {
            titleString.append(CommentAdapterHelper.createApprovedLine(
                    (comment.getApprovedBy() == null) ? Authentication.name
                            : comment.getApprovedBy(), mContext));
        }
        return titleString;
    }

    public static int getScoreText(Comment comment) {
        int submissionScore = comment.getScore();
        switch (ActionStates.getVoteDirection(comment)) {
            case UPVOTE: {
                if (comment.getVote() != VoteDirection.UPVOTE) {
                    if (comment.getVote() == VoteDirection.DOWNVOTE) ++submissionScore;
                    ++submissionScore; //offset the score by +1
                }
                break;
            }
            case DOWNVOTE: {
                if (comment.getVote() != VoteDirection.DOWNVOTE) {
                    if (comment.getVote() == VoteDirection.UPVOTE) --submissionScore;
                    --submissionScore; //offset the score by +1
                }
                break;
            }
            case NO_VOTE:
                if (comment.getVote() == VoteDirection.UPVOTE && comment.getAuthor()
                        .equalsIgnoreCase(Authentication.name)) {
                    submissionScore--;
                }
                break;
        }
        return submissionScore;
    }

    public static void doCommentEdit(final CommentAdapter adapter, final Context mContext,
            FragmentManager fm, final CommentNode baseNode, String replyText,
            final CommentViewHolder holder) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

        final View dialoglayout = inflater.inflate(R.layout.edit_comment, null);
        final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);

        final EditText e = dialoglayout.findViewById(R.id.entry);
        e.setText(StringEscapeUtils.unescapeHtml4(baseNode.getComment().getBody()));

        DoEditorActions.doActions(e, dialoglayout, fm, (Activity) mContext,
                StringEscapeUtils.unescapeHtml4(replyText), null);

        builder.setCancelable(false).setView(dialoglayout);
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
                new AsyncEditTask(adapter, baseNode, text, mContext, d, holder).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

    }

    public static void deleteComment(final CommentAdapter adapter, final Context mContext,
            final CommentNode baseNode, final CommentViewHolder holder) {
        new AlertDialogWrapper.Builder(mContext).setTitle(R.string.comment_delete)
                .setMessage(R.string.comment_delete_msg)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AsyncDeleteTask(adapter, baseNode, holder, mContext).executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                })
                .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static class AsyncEditTask extends AsyncTask<Void, Void, Void> {
        CommentAdapter    adapter;
        CommentNode       baseNode;
        String            text;
        Context           mContext;
        Dialog            dialog;
        CommentViewHolder holder;

        public AsyncEditTask(CommentAdapter adapter, CommentNode baseNode, String text,
                Context mContext, Dialog dialog, CommentViewHolder holder) {
            this.adapter = adapter;
            this.baseNode = baseNode;
            this.text = text;
            this.mContext = mContext;
            this.dialog = dialog;
            this.holder = holder;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                new AccountManager(Authentication.reddit).updateContribution(baseNode.getComment(),
                        text);
                adapter.currentSelectedItem = baseNode.getComment().getFullName();
                CommentNode n = baseNode.notifyCommentChanged(Authentication.reddit);
                adapter.editComment(n, holder);
                dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(mContext).setTitle(
                                R.string.comment_delete_err)
                                .setMessage(R.string.comment_delete_err_msg)
                                .setPositiveButton(R.string.btn_yes,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                new AsyncEditTask(adapter, baseNode, text, mContext,
                                                        AsyncEditTask.this.dialog,
                                                        holder).execute();
                                            }
                                        })
                                .setNegativeButton(R.string.btn_no,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                .show();
                    }
                });
            }
            return null;
        }
    }

    public static class AsyncDeleteTask extends AsyncTask<Void, Void, Boolean> {
        CommentAdapter    adapter;
        CommentNode       baseNode;
        CommentViewHolder holder;
        Context           mContext;

        public AsyncDeleteTask(CommentAdapter adapter, CommentNode baseNode,
                CommentViewHolder holder, Context mContext) {
            this.adapter = adapter;
            this.baseNode = baseNode;
            this.holder = holder;
            this.mContext = mContext;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                holder.firstTextView.setTextHtml(mContext.getString(R.string.content_deleted));
                holder.content.setText(R.string.content_deleted);
            } else {
                new AlertDialogWrapper.Builder(mContext).setTitle(R.string.comment_delete_err)
                        .setMessage(R.string.comment_delete_err_msg)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                doInBackground();
                            }
                        })
                        .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                new ModerationManager(Authentication.reddit).delete(baseNode.getComment());
                adapter.deleted.add(baseNode.getComment().getFullName());
                return true;
            } catch (ApiException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public static class AsyncReportTask extends AsyncTask<Void, Void, Void> {
        private CommentNode baseNode;
        private View        contextView;

        public AsyncReportTask(CommentNode baseNode, View contextView) {
            this.baseNode = baseNode;
            this.contextView = contextView;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                new AccountManager(Authentication.reddit).report(baseNode.getComment(),
                        reportReason);
            } catch (ApiException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Snackbar s =
                    Snackbar.make(contextView, R.string.msg_report_sent, Snackbar.LENGTH_SHORT);
            View view = s.getView();
            TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
            s.show();
        }
    }

    public static void showChildrenObject(final View v) {
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

    public static void hideChildrenObject(final View v) {
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

}
