package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.cocosw.bottomsheet.BottomSheet;
import com.devspark.robototextview.RobotoTypefaces;
import com.google.android.material.snackbar.Snackbar;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.InboxManager;
import net.dean.jraw.models.Captcha;
import net.dean.jraw.models.Message;
import net.dean.jraw.models.PrivateMessage;

import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Activities.Inbox;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SendMessage;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Drafts;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.UserTags;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Views.RoundedBackgroundSpan;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.SubmissionParser;


public class InboxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements BaseAdapter {

    private static final int TOP_LEVEL = 1;
    private final        int SPACER    = 6;
    public final  Context       mContext;
    private final RecyclerView  listView;
    public        InboxMessages dataSet;

    public InboxAdapter(Context mContext, InboxMessages dataSet, RecyclerView listView) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        boolean isSame = false;

    }

    @Override
    public void setError(Boolean b) {
        listView.setAdapter(new ErrorAdapter());
    }

    @Override
    public void undoSetError() {
        listView.setAdapter(this);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && !dataSet.posts.isEmpty()
                || position == dataSet.posts.size() + 1
                && dataSet.nomore
                && !dataSet.where.equalsIgnoreCase("where")) {
            return SPACER;
        } else {
            position -= 1;
        }
        if (position == dataSet.posts.size() && !dataSet.posts.isEmpty() && !dataSet.nomore) {
            return 5;
        }
        if (dataSet.posts.get(position).getSubject().toLowerCase(Locale.ENGLISH).contains("re:")
                && dataSet.where.equalsIgnoreCase("messages"))//IS COMMENT IN MESSAGES
        {
            return 2;
        }

        return TOP_LEVEL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == SPACER) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.spacer, viewGroup, false);
            return new SpacerViewHolder(v);
        } else if (i == TOP_LEVEL) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.top_level_message, viewGroup, false);
            return new MessageViewHolder(v);
        } else if (i == 5) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.loadingmore, viewGroup, false);
            return new ContributionAdapter.EmptyViewHolder(v);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.message_reply, viewGroup, false);
            return new MessageViewHolder(v);

        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int pos) {
        int i = pos != 0 ? pos - 1 : pos;

        if (!(viewHolder instanceof ContributionAdapter.EmptyViewHolder)
                && !(viewHolder instanceof SpacerViewHolder)) {
            final MessageViewHolder messageViewHolder = (MessageViewHolder) viewHolder;

            final Message comment = dataSet.posts.get(i);
            messageViewHolder.time.setText(
                    TimeUtils.getTimeAgo(comment.getCreated().getTime(), mContext));

            SpannableStringBuilder titleString = new SpannableStringBuilder();
            String author = comment.getAuthor();
            String direction = "from ";
            if (!dataSet.where.contains("mod")
                    && comment.getDataNode().has("dest")
                    && !Authentication.name.equalsIgnoreCase(
                    comment.getDataNode().get("dest").asText())
                    && !comment.getDataNode().get("dest").asText().equals("reddit")) {
                author = comment.getDataNode().get("dest").asText().replace("#", "/r/");
                direction = "to ";
            }
            if (comment.getDataNode().has("subreddit") && author == null || author.isEmpty()) {
                direction = "via /r/" + comment.getSubreddit();
            }
            titleString.append(direction);
            if (author != null) {
                titleString.append(author);
                titleString.append(" ");
                if (UserTags.isUserTagged(author)) {
                    SpannableStringBuilder pinned =
                            new SpannableStringBuilder(" " + UserTags.getUserTag(author) + " ");
                    pinned.setSpan(
                            new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_blue_500,
                                    false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    titleString.append(pinned);
                    titleString.append(" ");
                }

                if (UserSubscriptions.friends.contains(author)) {
                    SpannableStringBuilder pinned = new SpannableStringBuilder(
                            " " + mContext.getString(R.string.profile_friend) + " ");
                    pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white,
                                    R.color.md_deep_orange_500, false), 0, pinned.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    titleString.append(pinned);
                    titleString.append(" ");
                }
            }
            String spacer = mContext.getString(R.string.submission_properties_seperator);
            if (comment.getDataNode().has("subreddit") && !comment.getDataNode()
                    .get("subreddit")
                    .isNull()) {
                titleString.append(spacer);
                String subname = comment.getDataNode().get("subreddit").asText();
                SpannableStringBuilder subreddit = new SpannableStringBuilder("/r/" + subname);
                if ((SettingValues.colorSubName
                        && Palette.getColor(subname) != Palette.getDefaultColor())) {
                    subreddit.setSpan(new ForegroundColorSpan(Palette.getColor(subname)), 0,
                            subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subreddit.setSpan(new StyleSpan(Typeface.BOLD), 0, subreddit.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                titleString.append(subreddit);
            }

            messageViewHolder.user.setText(titleString);
            SpannableStringBuilder b = new SpannableStringBuilder();
            if (mContext instanceof Inbox
                    && comment.getCreated().getTime() > ((Inbox) mContext).last
                    && !comment.isRead()) {
                SpannableStringBuilder tagNew = new SpannableStringBuilder("\u00A0NEW\u00A0");
                tagNew.setSpan(new RoundedBackgroundSpan(Color.WHITE,
                                mContext.getResources().getColor(R.color.md_green_400), true, mContext), 0,
                        tagNew.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                b.append(tagNew);
                b.append(" ");
            }

            b.append(comment.getSubject());

            if (comment.getDataNode().has("link_title")) {
                SpannableStringBuilder link = new SpannableStringBuilder(" "
                        + Html.fromHtml(comment.getDataNode().get("link_title").asText())
                        + " ");
                link.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, link.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                link.setSpan(new RelativeSizeSpan(0.8f), 0, link.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                b.append(link);
            }
            messageViewHolder.title.setText(b);


            if (comment.isRead()) {
                messageViewHolder.title.setTextColor(
                        messageViewHolder.content.getCurrentTextColor());
            } else {
                messageViewHolder.title.setTextColor(
                        ContextCompat.getColor(mContext, R.color.md_red_400));
            }


            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int[] attrs = new int[]{R.attr.tintColor};
                    TypedArray ta = mContext.obtainStyledAttributes(attrs);

                    final int color = ta.getColor(0, Color.WHITE);
                    Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
                    final Drawable reply = mContext.getResources().getDrawable(R.drawable.reply);
                    Drawable unhide = mContext.getResources().getDrawable(R.drawable.ic_visibility);
                    Drawable hide = mContext.getResources().getDrawable(R.drawable.hide);
                    Drawable copy = mContext.getResources().getDrawable(R.drawable.ic_content_copy);
                    Drawable reddit = mContext.getResources().getDrawable(R.drawable.commentchange);

                    profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    hide.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    copy.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    reddit.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    reply.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    unhide.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

                    ta.recycle();

                    BottomSheet.Builder b = new BottomSheet.Builder((Activity) mContext).title(
                            Html.fromHtml(comment.getSubject()));

                    String author = comment.getAuthor();
                    if (!dataSet.where.contains("mod")
                            && comment.getDataNode().has("dest")
                            && !Authentication.name.equalsIgnoreCase(
                            comment.getDataNode().get("dest").asText())
                            && !comment.getDataNode().get("dest").asText().equals("reddit")) {
                        author = comment.getDataNode().get("dest").asText().replace("#", "/r/");
                    }
                    if (comment.getAuthor() != null) {
                        b.sheet(1, profile, "/u/" + author);
                    }

                    String read = mContext.getString(R.string.mail_mark_read);
                    Drawable rDrawable = hide;
                    if (comment.isRead()) {
                        read = mContext.getString(R.string.mail_mark_unread);
                        rDrawable = unhide;
                    }
                    b.sheet(2, rDrawable, read);
                    b.sheet(3, reply, mContext.getString(R.string.btn_reply));
                    b.sheet(25, copy, mContext.getString(R.string.misc_copy_text));
                    if (comment.isComment()) {
                        b.sheet(30, reddit, mContext.getString(R.string.mail_view_full_thread));
                    }
                    final String finalAuthor = author;
                    b.listener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 1: {
                                    Intent i = new Intent(mContext, Profile.class);
                                    i.putExtra(Profile.EXTRA_PROFILE, finalAuthor);
                                    mContext.startActivity(i);
                                }
                                break;
                                case 2: {
                                    if (comment.isRead()) {
                                        comment.read = false;
                                        new AsyncSetRead(false).execute(comment);
                                        messageViewHolder.title.setTextColor(
                                                ContextCompat.getColor(mContext,
                                                        R.color.md_red_400));
                                    } else {
                                        comment.read = true;
                                        new AsyncSetRead(true).execute(comment);
                                        messageViewHolder.title.setTextColor(
                                                messageViewHolder.content.getCurrentTextColor());
                                    }
                                }
                                break;
                                case 3: {
                                    doInboxReply(comment);
                                }
                                break;
                                case 25: {
                                    ClipboardManager clipboard =
                                            (ClipboardManager) mContext.getSystemService(
                                                    Context.CLIPBOARD_SERVICE);
                                    ClipData clip =
                                            ClipData.newPlainText("Message", comment.getBody());
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(mContext,
                                            mContext.getString(R.string.mail_message_copied),
                                            Toast.LENGTH_SHORT).show();
                                }
                                break;
                                case 30: {
                                    String context = comment.getDataNode().get("context").asText();
                                    new OpenRedditLink(mContext,
                                            "https://reddit.com" + context.substring(0,
                                                    context.lastIndexOf("/")));
                                }
                                break;
                            }
                        }
                    }).show();
                    return true;
                }
            });

            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.isRead()) {
                        if (comment instanceof PrivateMessage) {
                            DataShare.sharedMessage = (PrivateMessage) comment;
                            Intent i = new Intent(mContext, SendMessage.class);
                            i.putExtra(SendMessage.EXTRA_NAME, comment.getAuthor());
                            i.putExtra(SendMessage.EXTRA_REPLY, true);
                            mContext.startActivity(i);
                        } else {
                            new OpenRedditLink(mContext,
                                    comment.getDataNode().get("context").asText());
                        }
                    } else {
                        comment.read = true;
                        new AsyncSetRead(true).execute(comment);

                        messageViewHolder.title.setTextColor(
                                messageViewHolder.content.getCurrentTextColor());
                        {
                            SpannableStringBuilder b =
                                    new SpannableStringBuilder(comment.getSubject());

                            if (comment.getDataNode().has("link_title")) {
                                SpannableStringBuilder link = new SpannableStringBuilder(
                                        " "
                                                + Html.fromHtml(
                                                comment.getDataNode().get("link_title").asText())
                                                + " ");
                                link.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, link.length(),
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                link.setSpan(new RelativeSizeSpan(0.8f), 0, link.length(),
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                b.append(link);
                            }
                            messageViewHolder.title.setText(b);
                        }
                    }
                }
            });            //Set typeface for body


            int type = new FontPreferences(mContext).getFontTypeComment().getTypeface();
            Typeface typeface;
            if (type >= 0) {
                typeface = RobotoTypefaces.obtainTypeface(mContext, type);
            } else {
                typeface = Typeface.DEFAULT;
            }
            messageViewHolder.content.setTypeface(typeface);


            setViews(comment.getDataNode().get("body_html").asText(), "FORCE_LINK_CLICK",
                    messageViewHolder);
        }

        if (viewHolder instanceof SpacerViewHolder) {
            viewHolder.itemView.findViewById(R.id.height)
                    .setLayoutParams(new LinearLayout.LayoutParams(viewHolder.itemView.getWidth(),
                            ((Activity) mContext).findViewById(R.id.header).getHeight()));
        }
    }

    private void doInboxReply(final Message replyTo) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

        final View dialoglayout = inflater.inflate(R.layout.edit_comment, null);
        final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);

        final EditText e = (EditText) dialoglayout.findViewById(R.id.entry);

        DoEditorActions.doActions(e, dialoglayout,
                ((AppCompatActivity) mContext).getSupportFragmentManager(), (Activity) mContext,
                replyTo.getBody(), null);

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
                new AsyncReplyTask(replyTo, text).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                d.dismiss();
            }
        });


    }

    private class AsyncReplyTask extends AsyncTask<Void, Void, Void> {
        String trying;

        Message replyTo;
        String  text;

        public AsyncReplyTask(Message replyTo, String text) {
            this.replyTo = replyTo;
            this.text = text;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            sendMessage(null, null);

            return null;
        }

        boolean sent;

        public void sendMessage(Captcha captcha, String captchaAttempt) {
            try {
                new net.dean.jraw.managers.AccountManager(Authentication.reddit).reply(replyTo,
                        text);
                sent = true;
            } catch (ApiException e) {
                sent = false;
                e.printStackTrace();
            }
        }

        @Override
        public void onPostExecute(Void voids) {
            if (sent) {
                Snackbar s = Snackbar.make(listView, "Reply sent!", Snackbar.LENGTH_LONG);
                View view = s.getView();
                TextView tv =
                        (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                s.show();
            } else {
                Snackbar s = Snackbar.make(listView, "Sending failed! Reply saved as a draft.",
                        Snackbar.LENGTH_LONG);
                View view = s.getView();
                TextView tv =
                        (TextView) view.findViewById(com.google.android.material.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                s.show();
                Drafts.addDraft(text);
                sent = true;
            }
        }
    }


    public static class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    private void setViews(String rawHTML, String subredditName, MessageViewHolder holder) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            holder.content.setVisibility(View.VISIBLE);
            holder.content.setTextHtml(blocks.get(0), subredditName);
            startIndex = 1;
        } else {
            holder.content.setText("");
            holder.content.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                holder.commentOverflow.setViews(blocks, subredditName);
            } else {
                holder.commentOverflow.setViews(blocks.subList(startIndex, blocks.size()),
                        subredditName);
            }
        } else {
            holder.commentOverflow.removeAllViews();
        }
    }


    @Override
    public int getItemCount() {
        if (dataSet.posts == null || dataSet.posts.isEmpty()) {
            return 0;
        } else {
            return dataSet.posts.size() + 2;

        }
    }

    private static class AsyncSetRead extends AsyncTask<Message, Void, Void> {

        Boolean b;

        public AsyncSetRead(Boolean b) {
            this.b = b;
        }

        @Override
        protected Void doInBackground(Message... params) {
            new InboxManager(Authentication.reddit).setRead(b, params[0]);
            return null;
        }
    }


}