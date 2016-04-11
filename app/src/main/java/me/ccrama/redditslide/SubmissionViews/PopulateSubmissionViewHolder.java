
package me.ccrama.redditslide.SubmissionViews;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.bottomsheet.BottomSheet;

import net.dean.jraw.ApiException;
import net.dean.jraw.fluent.FlairReference;
import net.dean.jraw.fluent.FluentRedditClient;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.FlairTemplate;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.ModQueue;
import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Adapters.SubmissionViewHolder;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.LastComments;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.UserTags;
import me.ccrama.redditslide.Views.AnimateHelper;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.RoundedBackgroundSpan;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.Vote;
import me.ccrama.redditslide.util.CustomTabUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.SubmissionParser;

/**
 * Created by ccrama on 9/19/2015.
 */
public class PopulateSubmissionViewHolder {

    public static int getStyleAttribColorValue(final Context context, final int attribResId, final int defaultValue) {
        final TypedValue tv = new TypedValue();
        final boolean found = context.getTheme().resolveAttribute(attribResId, tv, true);
        return found ? tv.data : defaultValue;
    }

    private static void addClickFunctions(final View base, final ContentType.ImageType type, final Activity contextActivity, final Submission submission, final SubmissionViewHolder holder, final boolean full) {
        base.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SettingValues.storeHistory && !full) {
                    if ((type != ContentType.ImageType.NSFW_LINK && type != ContentType.ImageType.NSFW_IMAGE
                            && type != ContentType.ImageType.NSFW_GFY && type != ContentType.ImageType.NSFW_GIF) || SettingValues.storeNSFWHistory) {
                        HasSeen.addSeen(submission.getFullName());
                        if (contextActivity instanceof MainActivity || contextActivity instanceof MultiredditOverview || contextActivity instanceof SubredditView) {
                            holder.title.setAlpha(0.54f);
                        }
                    }
                }

                if (!PostMatch.openExternal(submission.getUrl())) {

                    switch (type) {
                        case VID_ME:
                        case STREAMABLE:
                            if (SettingValues.video) {
                                Intent myIntent = new Intent(contextActivity, GifView.class);
                                myIntent.putExtra(GifView.EXTRA_STREAMABLE, submission.getUrl());
                                contextActivity.startActivity(myIntent);
                            } else {
                                Reddit.defaultShare(submission.getUrl(), contextActivity);
                            }
                            break;
                        case NSFW_IMAGE:
                            openImage(contextActivity, submission);
                            break;
                        case IMGUR:
                            openImage(contextActivity, submission);
                            break;
                        case EMBEDDED:
                            if (SettingValues.video) {
                                String data = submission.getDataNode().get("media_embed").get("content").asText();
                                {
                                    Intent i = new Intent(contextActivity, FullscreenVideo.class);
                                    i.putExtra(FullscreenVideo.EXTRA_HTML, data);
                                    contextActivity.startActivity(i);
                                }
                            } else {
                                Reddit.defaultShare(submission.getUrl(), contextActivity);
                            }
                            break;
                        case NSFW_GIF:
                            openGif(false, contextActivity, submission);
                            break;
                        case NSFW_GFY:
                            openGif(true, contextActivity, submission);
                            break;
                        case REDDIT:
                            openRedditContent(submission.getUrl(), contextActivity);
                            break;
                        case LINK:
                        case IMAGE_LINK:
                        case NSFW_LINK:
                            CustomTabUtil.openUrl(submission.getUrl(), Palette.getColor(submission.getSubredditName()), contextActivity);
                            break;
                        case SELF:
                            if (holder != null) {
                                holder.itemView.performClick();
                            }
                            break;
                        case GFY:
                            openGif(true, contextActivity, submission);
                            break;
                        case ALBUM:
                            if (SettingValues.album) {
                                if (SettingValues.albumSwipe) {
                                    Intent i = new Intent(contextActivity, AlbumPager.class);
                                    i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                    contextActivity.startActivity(i);
                                    contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
                                } else {
                                    Intent i = new Intent(contextActivity, Album.class);
                                    i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                    contextActivity.startActivity(i);
                                    contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
                                }
                            } else {
                                Reddit.defaultShare(submission.getUrl(), contextActivity);

                            }
                            break;
                        case DEVIANTART:
                        case IMAGE:
                            openImage(contextActivity, submission);
                            break;
                        case GIF:
                            openGif(false, contextActivity, submission);
                            break;
                        case NONE_GFY:
                            openGif(true, contextActivity, submission);
                            break;
                        case NONE_GIF:
                            openGif(false, contextActivity, submission);
                            break;
                        case NONE:
                            if (holder != null) {
                                holder.itemView.performClick();
                            }
                            break;
                        case NONE_IMAGE:
                            openImage(contextActivity, submission);
                            break;
                        case NONE_URL:
                            CustomTabUtil.openUrl(submission.getUrl(), Palette.getColor(submission.getSubredditName()), contextActivity);
                            break;
                        case VIDEO:
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                            break;
                    }
                } else {
                    Reddit.defaultShare(submission.getUrl(), contextActivity);
                }
            }
        });
    }


    public static void openRedditContent(String url, Context c) {
        new OpenRedditLink(c, url);
    }


    public static void openImage(Activity contextActivity, Submission submission) {
        if (SettingValues.image) {
            Intent myIntent = new Intent(contextActivity, MediaView.class);
            String url;
            String previewUrl;
            url = submission.getUrl();

            if (SettingValues.loadImageLq && ((!NetworkUtil.isConnectedWifi(contextActivity) && SettingValues.lowResMobile) || SettingValues.lowResAlways) && submission.getThumbnails() != null && submission.getThumbnails().getVariations() != null) {
                int length = submission.getThumbnails().getVariations().length;
                previewUrl = Html.fromHtml(submission.getThumbnails().getVariations()[length / 2].getUrl()).toString(); //unescape url characters
                myIntent.putExtra(MediaView.EXTRA_LQ, true);
                myIntent.putExtra(MediaView.EXTRA_DISPLAY_URL, previewUrl);
            } else if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                previewUrl = submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
                myIntent.putExtra(MediaView.EXTRA_DISPLAY_URL, previewUrl);
            }
            myIntent.putExtra(MediaView.EXTRA_URL, url);
            myIntent.putExtra(MediaView.EXTRA_SHARE_URL, submission.getUrl());

            contextActivity.startActivity(myIntent);
        } else {
            Reddit.defaultShare(submission.getUrl(), contextActivity);
        }

    }

    public static void openGif(final boolean gfy, Activity contextActivity, Submission submission) {
        if (SettingValues.gif) {
            DataShare.sharedSubmission = submission;

            Intent myIntent = new Intent(contextActivity, MediaView.class);
            if (gfy) {
                myIntent.putExtra(MediaView.EXTRA_URL, "gfy" + submission.getUrl());
            } else {
                myIntent.putExtra(MediaView.EXTRA_URL, "" + submission.getUrl());
            }
            if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                String previewUrl = submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
                myIntent.putExtra(MediaView.EXTRA_DISPLAY_URL, previewUrl);
            }
            contextActivity.startActivity(myIntent);
        } else {
            Reddit.defaultShare(submission.getUrl(), contextActivity);
        }

    }

    public static int getCurrentTintColor(Context v) {
        return getStyleAttribColorValue(v, R.attr.tint, Color.WHITE);

    }

    public String reason;

    public String reportReason;

    boolean[] chosen = new boolean[]{false, false, false};

    public static int getWhiteTintColor() {
        return Palette.ThemeEnum.DARK.getTint();
    }

    public <T extends Contribution> void showBottomSheet(final Activity mContext, final Submission submission, final SubmissionViewHolder holder, final List<T> posts, final String baseSub, final RecyclerView recyclerview, final boolean full) {

        int[] attrs = new int[]{R.attr.tint};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
        final Drawable sub = mContext.getResources().getDrawable(R.drawable.sub);
        Drawable saved = mContext.getResources().getDrawable(R.drawable.iconstarfilled);
        Drawable hide = mContext.getResources().getDrawable(R.drawable.hide);
        final Drawable report = mContext.getResources().getDrawable(R.drawable.report);
        Drawable copy = mContext.getResources().getDrawable(R.drawable.ic_content_copy);
        Drawable open = mContext.getResources().getDrawable(R.drawable.openexternal);
        Drawable share = mContext.getResources().getDrawable(R.drawable.share);
        Drawable reddit = mContext.getResources().getDrawable(R.drawable.commentchange);
        Drawable filter = mContext.getResources().getDrawable(R.drawable.filter);

        profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        sub.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        saved.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        hide.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        report.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        copy.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        open.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        reddit.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        filter.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        BottomSheet.Builder b = new BottomSheet.Builder(mContext)
                .title(Html.fromHtml(submission.getTitle()));


        if (Authentication.didOnline) {
            b.sheet(1, profile, "/u/" + submission.getAuthor())
                    .sheet(2, sub, "/r/" + submission.getSubredditName());
            String save = mContext.getString(R.string.btn_save);
            if (ActionStates.isSaved(submission)) {
                save = mContext.getString(R.string.comment_unsave);
            }
            if (Authentication.isLoggedIn) {
                b.sheet(3, saved, save);
                b.sheet(12, report, mContext.getString(R.string.btn_report));
            }
        }
        if (submission.getSelftext() != null && !submission.getSelftext().isEmpty()) {
            b.sheet(25, copy, "Copy selftext");
        }
        boolean hidden = submission.isHidden();
        if (!full && Authentication.didOnline) {
            if (!hidden)
                b.sheet(5, hide, mContext.getString(R.string.submission_hide));
            else
                b.sheet(5, hide, mContext.getString(R.string.submission_unhide));

        }
        b.sheet(7, open, mContext.getString(R.string.submission_link_extern))
                .sheet(4, share, mContext.getString(R.string.submission_share_permalink))
                .sheet(8, reddit, mContext.getString(R.string.submission_share_reddit_url))
                .sheet(10, filter, mContext.getString(R.string.filter_content))
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 1: {
                                Intent i = new Intent(mContext, Profile.class);
                                i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                                mContext.startActivity(i);
                            }
                            break;
                            case 2: {
                                Intent i = new Intent(mContext, SubredditView.class);
                                i.putExtra(SubredditView.EXTRA_SUBREDDIT, submission.getSubredditName());
                                mContext.startActivityForResult(i, 14);
                            }
                            break;
                            case 10:

                                chosen = new boolean[]{SettingValues.subredditFilters.toLowerCase().contains(submission.getSubredditName().toLowerCase()), SettingValues.domainFilters.toLowerCase().contains(submission.getDomain().toLowerCase()), SettingValues.alwaysExternal.toLowerCase().contains(submission.getDomain().toLowerCase())};

                                final boolean[] oldChosen = new boolean[]{SettingValues.subredditFilters.toLowerCase().contains(submission.getSubredditName().toLowerCase()), SettingValues.domainFilters.toLowerCase().contains(submission.getDomain().toLowerCase()), SettingValues.alwaysExternal.toLowerCase().contains(submission.getDomain().toLowerCase())};

                                new AlertDialogWrapper.Builder(mContext).setTitle("What would you like to filter?")
                                        .alwaysCallMultiChoiceCallback()
                                        .setMultiChoiceItems(new String[]{"Posts from /r/" + submission.getSubredditName(), "Posts linking to " + submission.getDomain(), "Open " + submission.getDomain() + " urls externally"}, chosen, new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                chosen[which] = isChecked;
                                            }
                                        })
                                        .setPositiveButton("Filter", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                boolean filtered = false;
                                                SharedPreferences.Editor e = SettingValues.prefs.edit();
                                                if (chosen[0] && chosen[0] != oldChosen[0]) {
                                                    SettingValues.subredditFilters = SettingValues.subredditFilters + ((SettingValues.subredditFilters.isEmpty() || SettingValues.subredditFilters.endsWith(",")) ? "" : ",") + submission.getSubredditName();
                                                    filtered = true;
                                                    e.putString(SettingValues.PREF_SUBREDDIT_FILTERS, SettingValues.subredditFilters);
                                                } else if (!chosen[0] && chosen[0] != oldChosen[0]) {
                                                    SettingValues.subredditFilters = SettingValues.subredditFilters.replace(submission.getSubredditName(), "");
                                                    filtered = false;
                                                    e.putString(SettingValues.PREF_SUBREDDIT_FILTERS, SettingValues.subredditFilters);
                                                    e.apply();
                                                }
                                                if (chosen[1] && chosen[1] != oldChosen[1]) {
                                                    SettingValues.domainFilters = SettingValues.domainFilters + ((SettingValues.domainFilters.isEmpty() || SettingValues.domainFilters.endsWith(",")) ? "" : ",") + submission.getDomain();
                                                    filtered = true;
                                                    e.putString(SettingValues.PREF_DOMAIN_FILTERS, SettingValues.domainFilters);
                                                } else if (!chosen[1] && chosen[1] != oldChosen[1]) {
                                                    SettingValues.domainFilters = SettingValues.domainFilters.replace(submission.getDomain(), "");
                                                    filtered = false;
                                                    e.putString(SettingValues.PREF_DOMAIN_FILTERS, SettingValues.domainFilters);
                                                    e.apply();
                                                }
                                                if (chosen[2] && chosen[2] != oldChosen[2]) {
                                                    SettingValues.alwaysExternal = SettingValues.alwaysExternal + ((SettingValues.alwaysExternal.isEmpty() || SettingValues.alwaysExternal.endsWith(",")) ? "" : ",") + submission.getDomain();
                                                    e.putString(SettingValues.PREF_ALWAYS_EXTERNAL, SettingValues.alwaysExternal);
                                                    e.apply();
                                                } else if (!chosen[2] && chosen[2] != oldChosen[2]) {
                                                    SettingValues.alwaysExternal = SettingValues.alwaysExternal.replace(submission.getDomain(), "");
                                                    e.putString(SettingValues.PREF_ALWAYS_EXTERNAL, SettingValues.alwaysExternal);
                                                    e.apply();
                                                }

                                                if (filtered) {
                                                    e.apply();
                                                    final int pos = posts.indexOf(submission);
                                                    final T t = posts.get(pos);
                                                    posts.remove(pos);
                                                    Hidden.setHidden(t);
                                                    final OfflineSubreddit s;
                                                    if (baseSub != null) {
                                                        s = OfflineSubreddit.getSubreddit(baseSub);
                                                        s.hide(pos);
                                                    }
                                                    recyclerview.getAdapter().notifyItemRemoved(pos + 1);


                                                }
                                            }
                                        }).setNegativeButton("Cancel", null)
                                        .show();
                                break;

                            case 3:
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        try {
                                            if (ActionStates.isSaved(submission)) {
                                                new AccountManager(Authentication.reddit).unsave(submission);
                                                ActionStates.setSaved(submission, false);
                                            } else {
                                                new AccountManager(Authentication.reddit).save(submission);
                                                ActionStates.setSaved(submission, true);
                                            }

                                        } catch (ApiException e) {
                                            e.printStackTrace();
                                        }

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        Snackbar s;
                                        if (ActionStates.isSaved(submission)) {
                                            ((ImageView) holder.save).setColorFilter(ContextCompat.getColor(mContext, R.color.md_amber_500), PorterDuff.Mode.SRC_ATOP);
                                            s = Snackbar.make(holder.itemView, R.string.submission_info_saved, Snackbar.LENGTH_SHORT);
                                            AnimateHelper.setFlashAnimation(holder.itemView, holder.save, ContextCompat.getColor(mContext, R.color.md_amber_500));
                                        } else {
                                            s = Snackbar.make(holder.itemView, R.string.submission_info_unsaved, Snackbar.LENGTH_SHORT);
                                            ((ImageView) holder.save).setColorFilter(((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none"))) || full) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                                        }
                                        View view = s.getView();
                                        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                        tv.setTextColor(Color.WHITE);
                                        s.show();

                                    }
                                }.execute();
                                break;
                            case 5: {
                                hideSubmission(submission, posts, baseSub, recyclerview);
                            }
                            break;
                            case 7:
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(submission.getUrl()));
                                mContext.startActivity(browserIntent);
                                break;
                            case 4:
                                Reddit.defaultShareText(submission.getTitle() + "\n" + submission.getUrl(), mContext);
                                break;
                            case 12:
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
                                                            new AccountManager(Authentication.reddit).report(submission, reportReason);
                                                        } catch (ApiException e) {
                                                            e.printStackTrace();
                                                        }
                                                        return null;
                                                    }

                                                    @Override
                                                    protected void onPostExecute(Void aVoid) {
                                                        Snackbar s = Snackbar.make(holder.itemView, R.string.msg_report_sent, Snackbar.LENGTH_SHORT);
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
                            case 8:
                                Reddit.defaultShareText(submission.getTitle() + " \n" + "https://reddit.com" + submission.getPermalink(), mContext);
                                break;
                            case 6: {
                                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Link", submission.getUrl());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(mContext, "Link copied", Toast.LENGTH_SHORT).show();
                            }
                            break;
                            case 25:
                                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Selftext", submission.getSelftext());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(mContext, "Selftext copied", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });


        b.show();
    }

    public <T extends Contribution> void hideSubmission(final Submission submission, final List<T> posts, final String baseSub, final RecyclerView recyclerview) {
        final int pos = posts.indexOf(submission);
        if (pos != -1) {
            if(submission.isHidden()){
                posts.remove(pos);
                Hidden.undoHidden(submission);
                recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                Snackbar snack = Snackbar.make(recyclerview, R.string.submission_info_unhidden, Snackbar.LENGTH_LONG);
                View view = snack.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.show();
            } else {
                final T t = posts.get(pos);
                posts.remove(pos);
                Hidden.setHidden(t);
                final OfflineSubreddit s;
                if (baseSub != null) {
                    s = OfflineSubreddit.getSubreddit(baseSub);
                    s.hide(pos);
                } else {
                    s = null;
                }
                recyclerview.getAdapter().notifyItemRemoved(pos + 1);


                Snackbar snack = Snackbar.make(recyclerview, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (baseSub != null && s != null) {
                            s.unhideLast();
                        }
                        posts.add(pos, t);
                        recyclerview.getAdapter().notifyItemInserted(pos + 1);
                        Hidden.undoHidden(t);

                    }
                });
                View view = snack.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.show();
            }

        }
    }

    public <T extends Contribution> void showModBottomSheet(final Activity mContext, final Submission submission, final List<T> posts, final SubmissionViewHolder holder, final RecyclerView recyclerview, final Map<String, Integer> reports, final Map<String, String> reports2) {

        final Resources res = mContext.getResources();
        int[] attrs = new int[]{R.attr.tint};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
        final Drawable report = mContext.getResources().getDrawable(R.drawable.report);
        final Drawable approve = mContext.getResources().getDrawable(R.drawable.support);
        final Drawable spam = mContext.getResources().getDrawable(R.drawable.fontsizedarker);
        final Drawable nsfw = mContext.getResources().getDrawable(R.drawable.hide);
        final Drawable pin = mContext.getResources().getDrawable(R.drawable.lock);
        final Drawable flair = mContext.getResources().getDrawable(R.drawable.ic_format_quote_white_48dp);
        final Drawable remove = mContext.getResources().getDrawable(R.drawable.close);
        final Drawable remove_reason = mContext.getResources().getDrawable(R.drawable.reportreason);


        profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        report.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        approve.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        spam.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        nsfw.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        pin.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        flair.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        remove.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        remove_reason.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        BottomSheet.Builder b = new BottomSheet.Builder(mContext)
                .title(Html.fromHtml(submission.getTitle()));

        int reportCount = reports.size() + reports2.size();

        b.sheet(0, report, res.getQuantityString(R.plurals.mod_btn_reports, reportCount, reportCount));

        boolean approved = false;
        String whoApproved = "";
        if (submission.getDataNode().get("approved_by").asText().equals("null")) {
            b.sheet(1, approve, res.getString(R.string.mod_btn_approve));
        } else {
            approved = true;
            whoApproved = submission.getDataNode().get("approved_by").asText();
            b.sheet(1, approve, String.format(res.getString(R.string.mod_btn_approved), whoApproved));
        }

        // b.sheet(2, spam, mContext.getString(R.string.mod_btn_spam)) todo this
        b.sheet(20, flair, res.getString(R.string.mod_btn_submission_flair));

        final boolean isNsfw = submission.isNsfw();
        if (isNsfw) {
            b.sheet(3, nsfw, res.getString(R.string.mod_btn_unmark_nsfw));
        } else {
            b.sheet(3, nsfw, res.getString(R.string.mod_btn_mark_nsfw));
        }

        final boolean stickied = submission.isStickied();
        if (stickied) {
            b.sheet(4, pin, res.getString(R.string.mod_btn_unsticky));
        } else {
            b.sheet(4, pin, res.getString(R.string.mod_btn_sticky));
        }

        final String finalWhoApproved = whoApproved;
        final boolean finalApproved = approved;
        b.sheet(6, remove, mContext.getString(R.string.mod_btn_remove))
                .sheet(7, remove_reason, res.getString(R.string.mod_btn_remove_reason))
                .sheet(8, profile, res.getString(R.string.mod_btn_author))
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                new AsyncTask<Void, Void, ArrayList<String>>() {
                                    @Override
                                    protected ArrayList<String> doInBackground(Void... params) {

                                        ArrayList<String> finalReports = new ArrayList<>();
                                        for (String s : reports.keySet()) {
                                            finalReports.add( reports.get(s) + "× " + s);
                                        }
                                        for (String s : reports2.keySet()) {
                                            finalReports.add(s + ": " + reports2.get(s));
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
                                                                if (mContext instanceof ModQueue) {
                                                                    final int pos = posts.indexOf(submission);
                                                                    posts.remove(submission);

                                                                    if (pos == 0) {
                                                                        recyclerview.getAdapter().notifyDataSetChanged();
                                                                    } else {
                                                                        recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                                                                    }
                                                                    dialog.dismiss();
                                                                }

                                                                Snackbar s = Snackbar.make(holder.itemView, R.string.mod_approved, Snackbar.LENGTH_LONG);
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
                                                                new ModerationManager(Authentication.reddit).approve(submission);
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
                            case 3:
                                if (isNsfw) {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_remove_nsfw)
                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {
                                                    new AsyncTask<Void, Void, Boolean>() {

                                                        @Override
                                                        public void onPostExecute(Boolean b) {
                                                            if (b) {
                                                                dialog.dismiss();
                                                            } else {
                                                                new AlertDialogWrapper.Builder(mContext)
                                                                        .setTitle(R.string.err_general)
                                                                        .setMessage(R.string.err_retry_later).show();
                                                            }
                                                        }

                                                        @Override
                                                        protected Boolean doInBackground(Void... params) {
                                                            try {
                                                                new ModerationManager(Authentication.reddit).setNsfw(submission, false);
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

                                } else {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_mark_nsfw)
                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {
                                                    new AsyncTask<Void, Void, Boolean>() {

                                                        @Override
                                                        public void onPostExecute(Boolean b) {
                                                            if (b) {
                                                                dialog.dismiss();

                                                            } else {
                                                                new AlertDialogWrapper.Builder(mContext)
                                                                        .setTitle(R.string.err_general)
                                                                        .setMessage(R.string.err_retry_later).show();
                                                            }
                                                        }

                                                        @Override
                                                        protected Boolean doInBackground(Void... params) {
                                                            try {
                                                                new ModerationManager(Authentication.reddit).setNsfw(submission, true);
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
                            case 4:
                                if (stickied) {
                                    new AlertDialogWrapper.Builder(mContext).setTitle("Really un-pin this submission?")
                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {

                                                    new AsyncTask<Void, Void, Boolean>() {

                                                        @Override
                                                        public void onPostExecute(Boolean b) {
                                                            if (b) {
                                                                dialog.dismiss();
                                                                if (mContext instanceof ModQueue) {
                                                                    final int pos = posts.indexOf(submission);
                                                                    posts.remove(submission);

                                                                    if (pos == 0) {
                                                                        recyclerview.getAdapter().notifyDataSetChanged();
                                                                    } else {
                                                                        recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                                                                    }

                                                                    dialog.dismiss();
                                                                }
                                                                Snackbar s = Snackbar.make(holder.itemView, "Submission un-pinned", Snackbar.LENGTH_LONG);
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
                                                                new ModerationManager(Authentication.reddit).setSticky(submission, false);
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
                                    new AlertDialogWrapper.Builder(mContext).setTitle("Really pin this submission?")
                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {

                                                    new AsyncTask<Void, Void, Boolean>() {

                                                        @Override
                                                        public void onPostExecute(Boolean b) {
                                                            if (b) {
                                                                dialog.dismiss();
                                                                if (mContext instanceof ModQueue) {
                                                                    final int pos = posts.indexOf(submission);
                                                                    posts.remove(submission);

                                                                    if (pos == 0) {
                                                                        recyclerview.getAdapter().notifyDataSetChanged();
                                                                    } else {
                                                                        recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                                                                    }

                                                                    dialog.dismiss();
                                                                }
                                                                Snackbar s = Snackbar.make(holder.itemView, "Submission pinned", Snackbar.LENGTH_LONG);
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
                                                                new ModerationManager(Authentication.reddit).setSticky(submission, true);
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
                            case 5:
                                new AsyncTask<Void, Void, ArrayList<String>>() {
                                    String currentFlair;
                                    List<FlairTemplate> flair;
                                    String input;

                                    @Override
                                    protected ArrayList<String> doInBackground(Void... params) {
                                        FlairReference allFlairs = new FluentRedditClient(Authentication.reddit).subreddit(submission.getSubredditName()).flair();


                                        try {
                                            flair = allFlairs.options();
                                            currentFlair = allFlairs.current().getText();
                                            final ArrayList<String> finalFlairs = new ArrayList<>();
                                            for (FlairTemplate temp : flair) {
                                                finalFlairs.add(temp.getText());
                                            }
                                            mContext.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    new MaterialDialog.Builder(mContext).title(R.string.mod_flair_post).inputType(InputType.TYPE_CLASS_TEXT)
                                                            .input(mContext.getString(R.string.mod_flair_hint), "", new MaterialDialog.InputCallback() {
                                                                @Override
                                                                public void onInput(MaterialDialog dialog, CharSequence out) {
                                                                    input = out.toString();
                                                                }
                                                            }).items(finalFlairs.toArray(new String[finalFlairs.size()])).itemsCallback(new MaterialDialog.ListCallback() {
                                                        @Override
                                                        public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                                            materialDialog.dismiss();
                                                            try {
                                                                new ModerationManager(Authentication.reddit).setFlair(submission.getSubredditName(), flair.get(finalFlairs.indexOf(currentFlair)), input, submission);
                                                            } catch (ApiException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }).show();
                                                }
                                            });
                                            return finalFlairs;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            //sub probably has no flairs?
                                        }


                                        return null;
                                    }

                                    @Override
                                    public void onPostExecute(final ArrayList<String> data) {

                                    }
                                }.execute();


                                break;
                            case 6:

                                new AlertDialogWrapper.Builder(mContext).setTitle("Really remove this submission?")
                                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, int which) {

                                                new AsyncTask<Void, Void, Boolean>() {

                                                    @Override
                                                    public void onPostExecute(Boolean b) {
                                                        if (b) {
                                                            dialog.dismiss();
                                                            if (mContext instanceof ModQueue) {
                                                                final int pos = posts.indexOf(submission);
                                                                posts.remove(submission);

                                                                if (pos == 0) {
                                                                    recyclerview.getAdapter().notifyDataSetChanged();
                                                                } else {
                                                                    recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                                                                }
                                                                dialog.dismiss();
                                                            }
                                                            Snackbar s = Snackbar.make(holder.itemView, "Submission removed", Snackbar.LENGTH_LONG);
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
                                                            new ModerationManager(Authentication.reddit).remove(submission, false);
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
                            case 7:
                                reason = "";
                                new MaterialDialog.Builder(mContext).title("What is the reason for removing this submission?")
                                        .positiveText(R.string.btn_remove)
                                        .alwaysCallInputCallback()
                                        .input("Removal reason", "Removed for ", false, new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                                reason = input.toString();
                                            }
                                        })
                                        .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(final MaterialDialog dialog, DialogAction which) {

                                                new AsyncTask<Void, Void, Boolean>() {

                                                    @Override
                                                    public void onPostExecute(Boolean b) {
                                                        if (b) {
                                                            dialog.dismiss();
                                                            if (mContext instanceof ModQueue) {
                                                                final int pos = posts.indexOf(submission);
                                                                posts.remove(submission);

                                                                if (pos == 0) {
                                                                    recyclerview.getAdapter().notifyDataSetChanged();
                                                                } else {
                                                                    recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                                                                }

                                                                dialog.dismiss();
                                                            }
                                                            Snackbar s = Snackbar.make(holder.itemView, "Submission removed", Snackbar.LENGTH_LONG);
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
                                                            new ModerationManager(Authentication.reddit).remove(submission, false);
                                                            new AccountManager(Authentication.reddit).reply(submission, reason);
                                                        } catch (ApiException e) {
                                                            e.printStackTrace();
                                                            return false;

                                                        }
                                                        return true;
                                                    }
                                                }.execute();

                                            }
                                        }).negativeText(R.string.btn_cancel)
                                        .onNegative(null).show();
                                break;
                            case 8:
                                Intent i = new Intent(mContext, Profile.class);
                                i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                                mContext.startActivity(i);
                                break;
                            case 20:
                                new AsyncTask<Void, Void, ArrayList<String>>() {
                                    List<FlairTemplate> flair;

                                    @Override
                                    protected ArrayList<String> doInBackground(Void... params) {
                                        FlairReference allFlairs = new FluentRedditClient(Authentication.reddit).subreddit(submission.getSubredditName()).flair();
                                        try {
                                            flair = allFlairs.options(submission);
                                            final ArrayList<String> finalFlairs = new ArrayList<>();
                                            for (FlairTemplate temp : flair) {
                                                finalFlairs.add(temp.getText());
                                            }
                                            return finalFlairs;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            //sub probably has no flairs?
                                        }


                                        return null;
                                    }

                                    @Override
                                    public void onPostExecute(final ArrayList<String> data) {
                                        if (data.isEmpty()) {
                                            new AlertDialogWrapper.Builder(mContext)
                                                    .setTitle("No flair found for this subreddit")
                                                    .setPositiveButton(R.string.btn_ok, null)
                                                    .show();
                                        } else {
                                            new MaterialDialog.Builder(mContext).items(data)
                                                    .title("Select flair")
                                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                                        @Override
                                                        public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                                            final FlairTemplate t = flair.get(which);
                                                            if (t.isTextEditable()) {
                                                                new MaterialDialog.Builder(mContext).title("Set flair text")
                                                                        .input("Flair text", t.getText(), true, new MaterialDialog.InputCallback() {
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
                                                                                            new ModerationManager(Authentication.reddit).setFlair(submission.getSubredditName(), t, flair, submission);
                                                                                            return true;
                                                                                        } catch (ApiException e) {
                                                                                            e.printStackTrace();
                                                                                            return false;
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    protected void onPostExecute(Boolean done) {
                                                                                        Snackbar s = null;
                                                                                        if (done) {
                                                                                            if (recyclerview != null)
                                                                                                s = Snackbar.make(recyclerview, "Flair set successfully", Snackbar.LENGTH_SHORT);
                                                                                        } else {
                                                                                            if (recyclerview != null)
                                                                                                s = Snackbar.make(recyclerview, "Error setting flair, try again soon", Snackbar.LENGTH_SHORT);
                                                                                        }
                                                                                        if (s != null) {
                                                                                            View view = s.getView();
                                                                                            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                                            tv.setTextColor(Color.WHITE);
                                                                                            s.show();
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
                                                                            new ModerationManager(Authentication.reddit).setFlair(submission.getSubredditName(), t, null, submission);
                                                                            return true;
                                                                        } catch (ApiException e) {
                                                                            e.printStackTrace();
                                                                            return false;
                                                                        }
                                                                    }

                                                                    @Override
                                                                    protected void onPostExecute(Boolean done) {
                                                                        Snackbar s = null;
                                                                        if (done) {
                                                                            if (recyclerview != null)
                                                                                s = Snackbar.make(recyclerview, "Flair set successfully", Snackbar.LENGTH_SHORT);
                                                                        } else {
                                                                            if (recyclerview != null)
                                                                                s = Snackbar.make(recyclerview, "Error setting flair, try again soon", Snackbar.LENGTH_SHORT);
                                                                        }
                                                                        if (s != null) {
                                                                            View view = s.getView();
                                                                            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                            tv.setTextColor(Color.WHITE);
                                                                            s.show();
                                                                        }
                                                                    }
                                                                }.execute();
                                                            }
                                                        }
                                                    }).show();
                                        }
                                    }
                                }.execute();

                                break;

                        }
                    }
                });


        b.show();
    }

    public static String getSubmissionScoreString(int score, Resources res, Submission submission) {
        switch (submission.getSubredditName().toLowerCase()) {
            case "androidcirclejerk":
                return score + " upDuARTes"; //Praise DuARTe
            case "xdacirclejerk":
                return score + " thanks"; //Hit Thanks and Pls buy me a beer! (XDA)
            default:
                return res.getQuantityString(R.plurals.submission_points, score, score);
        }
    }

    public void doInfoLine(SubmissionViewHolder holder, Submission submission, Context mContext, String baseSub, boolean full) {

        String spacer = mContext.getString(R.string.submission_properties_seperator);
        SpannableStringBuilder titleString = new SpannableStringBuilder();

        SpannableStringBuilder subreddit = new SpannableStringBuilder(" /r/" + submission.getSubredditName() + " ");

        String subname = submission.getSubredditName().toLowerCase();
        if (baseSub == null || baseSub.isEmpty()) baseSub = subname;
        if ((SettingValues.colorSubName && Palette.getColor(subname) != Palette.getDefaultColor()) || (baseSub.equals("nomatching") && (SettingValues.colorSubName && Palette.getColor(subname) != Palette.getDefaultColor()))) {
            boolean secondary = (baseSub.equalsIgnoreCase("frontpage") || (baseSub.equalsIgnoreCase("all")) || (baseSub.equalsIgnoreCase("friends")) || (baseSub.equalsIgnoreCase("mod")) || baseSub.contains(".") || baseSub.contains("+"));
            if (!secondary && !SettingValues.colorEverywhere || secondary) {
                subreddit.setSpan(new ForegroundColorSpan(Palette.getColor(subname)), 0, subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                subreddit.setSpan(new StyleSpan(Typeface.BOLD), 0, subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        titleString.append(subreddit);
        titleString.append(spacer);

        try {
            String time = TimeUtils.getTimeAgo(submission.getCreated().getTime(), mContext);
            titleString.append(time);
        } catch (Exception e) {
            titleString.append("just now");
        }
        titleString.append(spacer);

        SpannableStringBuilder author = new SpannableStringBuilder(" " + submission.getAuthor() + " ");
        int authorcolor = Palette.getFontColorUser(submission.getAuthor());

        if(submission.getAuthor() != null) {
            if (Authentication.name != null && submission.getAuthor().toLowerCase().equals(Authentication.name.toLowerCase())) {
                author.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_300, false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (submission.getDistinguishedStatus() == DistinguishedStatus.MODERATOR || submission.getDistinguishedStatus() == DistinguishedStatus.ADMIN) {
                author.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (authorcolor != 0) {
                author.setSpan(new ForegroundColorSpan(authorcolor), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            titleString.append(author);
        }


      /*todo maybe?  titleString.append(((comment.hasBeenEdited() && comment.getEditDate() != null) ? " *" + TimeUtils.getTimeAgo(comment.getEditDate().getTime(), mContext) : ""));
        titleString.append("  ");*/

        if (UserTags.isUserTagged(submission.getAuthor())) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(" " + UserTags.getUserTag(submission.getAuthor()) + " ");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_blue_500, false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }

        if (UserSubscriptions.friends.contains(submission.getAuthor())) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(" " + mContext.getString(R.string.profile_friend) + " ");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_500, false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        /* too big, might add later todo
        if (submission.getAuthorFlair() != null && submission.getAuthorFlair().getText() != null && !submission.getAuthorFlair().getText().isEmpty()) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.activity_background, typedValue, true);
            int color = typedValue.data;
            SpannableStringBuilder pinned = new SpannableStringBuilder(" " + submission.getAuthorFlair().getText() + " ");
            pinned.setSpan(new RoundedBackgroundSpan(holder.title.getCurrentTextColor(), color, false, mContext), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }



        if (holder.leadImage.getVisibility() == View.GONE && !full) {
            String text = "";

            switch (ContentType.getImageType(submission)) {
                case NSFW_IMAGE:
                    text = mContext.getString(R.string.type_nsfw_img);
                    break;

                case NSFW_GIF:
                case NSFW_GFY:
                    text = mContext.getString(R.string.type_nsfw_gif);
                    break;

                case REDDIT:
                    text = mContext.getString(R.string.type_reddit);
                    break;

                case LINK:
                case IMAGE_LINK:
                    text = mContext.getString(R.string.type_link);
                    break;

                case NSFW_LINK:
                    text = mContext.getString(R.string.type_nsfw_link);

                    break;
                case STREAMABLE:
                    text = ("Streamable");
                    break;
                case SELF:
                    text = ("Selftext");
                    break;

                case ALBUM:
                    text = mContext.getString(R.string.type_album);
                    break;

                case IMAGE:
                    text = mContext.getString(R.string.type_img);
                    break;
                case IMGUR:
                    text = mContext.getString(R.string.type_imgur);
                    break;
                case GFY:
                case GIF:
                case NONE_GFY:
                case NONE_GIF:
                    text = mContext.getString(R.string.type_gif);
                    break;

                case NONE:
                    text = mContext.getString(R.string.type_title_only);
                    break;

                case NONE_IMAGE:
                    text = mContext.getString(R.string.type_img);
                    break;

                case VIDEO:
                    text = mContext.getString(R.string.type_vid);
                    break;

                case EMBEDDED:
                    text = mContext.getString(R.string.type_emb);
                    break;

                case NONE_URL:
                    text = mContext.getString(R.string.type_link);
                    break;
            }
            if(!text.isEmpty()) {
                titleString.append(" \n");
                text = text.toUpperCase();
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = mContext.getTheme();
                theme.resolveAttribute(R.attr.activity_background, typedValue, true);
                int color = typedValue.data;
                SpannableStringBuilder pinned = new SpannableStringBuilder(" " + text + " ");
                pinned.setSpan(new RoundedBackgroundSpan(holder.title.getCurrentTextColor(), color, false, mContext), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                titleString.append(pinned);
            }
        }*/
        if (SettingValues.showDomain) {
            titleString.append(spacer);
            titleString.append(submission.getDomain());
        }
        holder.info.setText(titleString);
    }


    public <T extends Contribution> void populateSubmissionViewHolder(final SubmissionViewHolder holder, final Submission submission, final Activity mContext, boolean fullscreen, final boolean full, final List<T> posts, final RecyclerView recyclerview, final boolean same, final boolean offline, final String baseSub) {
        holder.itemView.findViewById(R.id.vote).setVisibility(View.GONE);
        SpannableStringBuilder titleString = new SpannableStringBuilder();
        titleString.append(Html.fromHtml(submission.getTitle()));
        if (submission.isStickied()) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0" + mContext.getString(R.string.submission_stickied).toUpperCase() + "\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, true), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }
        if (!submission.getDataNode().get("approved_by").asText().equals("null")) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0Approved by " + submission.getDataNode().get("approved_by").asText().trim() + "\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, true), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }
        if (submission.getTimesGilded() > 0) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0★\u200A" + submission.getTimesGilded() + "\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_orange_500, true), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }
        if (submission.isNsfw()) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0NSFW\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_red_300, true), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }
        if (submission.getSubmissionFlair().getText() != null && !submission.getSubmissionFlair().getText().isEmpty()) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.activity_background, typedValue, false);
            int color = typedValue.data;
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0" + submission.getSubmissionFlair().getText() + "\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(holder.title.getCurrentTextColor(), color, true, mContext), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }
        holder.title.setText(titleString); // title is a spoiler roboto textview so it will format the html


        if (!offline && UserSubscriptions.modOf != null && UserSubscriptions.modOf.contains(submission.getSubredditName().toLowerCase())) {
            holder.mod.setVisibility(View.VISIBLE);
            final Map<String, Integer> reports = submission.getUserReports();
            final Map<String, String> reports2 = submission.getModeratorReports();
            if (reports.size() + reports2.size() > 0) {
                ((ImageView) holder.mod).setColorFilter(ContextCompat.getColor(mContext, R.color.md_red_300), PorterDuff.Mode.SRC_ATOP);
            } else {
                ((ImageView) holder.mod).setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

            }

            holder.mod.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showModBottomSheet(mContext, submission, posts, holder, recyclerview, reports, reports2);

                }
            });
        } else {
            holder.mod.setVisibility(View.GONE);
        }

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (offline) {
                    Snackbar s = Snackbar.make(holder.itemView, R.string.offline_msg, Snackbar.LENGTH_SHORT);
                    View view2 = s.getView();
                    TextView tv = (TextView) view2.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } else {
                    showBottomSheet(mContext, submission, holder, posts, baseSub, recyclerview, full);
                }
            }
        });
        int commentCount = submission.getCommentCount();
        int more = LastComments.commentsSince(submission);
        holder.comments.setText("" + commentCount + ((more != 0 && SettingValues.commentLastVisit) ? " (+" + more + ")" : ""));
        holder.score.setText("" + submission.getScore());

        final ImageView downvotebutton = (ImageView) holder.downvote;
        final ImageView upvotebutton = (ImageView) holder.upvote;
        if (submission.isArchived() || submission.isLocked()) {
            downvotebutton.setVisibility(View.GONE);
            upvotebutton.setVisibility(View.GONE);
        } else if (Authentication.isLoggedIn && !offline && Authentication.didOnline) {
            if (SettingValues.actionbarVisible && downvotebutton.getVisibility() != View.VISIBLE) {
                downvotebutton.setVisibility(View.VISIBLE);
                upvotebutton.setVisibility(View.VISIBLE);
            }
            switch (ActionStates.getVoteDirection(submission)) {
                case UPVOTE: {
                    holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_500));
                    upvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
                    holder.score.setTypeface(null, Typeface.BOLD);
                    holder.score.setText("" + (submission.getScore() + (submission.getAuthor().equals(Authentication.name) ? 0 : 1)));
                    downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                    break;
                }
                case DOWNVOTE: {
                    holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_500));
                    downvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_blue_500), PorterDuff.Mode.SRC_ATOP);
                    holder.score.setTypeface(null, Typeface.BOLD);
                    holder.score.setText("" + (submission.getScore() + (submission.getAuthor().equals(Authentication.name) ? 0 : -1)));
                    upvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                    break;
                }
                case NO_VOTE: {
                    holder.score.setTextColor(holder.comments.getCurrentTextColor());
                    holder.score.setText("" + (submission.getScore()));
                    holder.score.setTypeface(null, Typeface.NORMAL);
                    downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                    upvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                    break;
                }
            }
        }

        final ImageView hideButton = (ImageView) holder.hide;

        if (hideButton != null) {
            if (SettingValues.hideButton && Authentication.isLoggedIn) {
                hideButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideSubmission(submission, posts, baseSub, recyclerview);
                    }
                });
            } else {
                hideButton.setVisibility(View.GONE);
            }
        }
        if (Authentication.isLoggedIn && Authentication.didOnline) {
            if (ActionStates.isSaved(submission)) {
                ((ImageView) holder.save).setColorFilter(ContextCompat.getColor(mContext, R.color.md_amber_500), PorterDuff.Mode.SRC_ATOP);
            } else {
                ((ImageView) holder.save).setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
            }
            holder.save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                if (ActionStates.isSaved(submission)) {
                                    new AccountManager(Authentication.reddit).unsave(submission);
                                    ActionStates.setSaved(submission, false);
                                } else {
                                    new AccountManager(Authentication.reddit).save(submission);
                                    ActionStates.setSaved(submission, true);
                                }
                            } catch (ApiException e) {
                                e.printStackTrace();
                            }


                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            if (!full && !SettingValues.actionbarVisible)
                                CreateCardView.toggleActionbar(holder.itemView);
                            Snackbar s;
                            if (ActionStates.isSaved(submission)) {
                                ((ImageView) holder.save).setColorFilter(ContextCompat.getColor(mContext, R.color.md_amber_500), PorterDuff.Mode.SRC_ATOP);
                                s = Snackbar.make(holder.itemView, R.string.submission_info_saved, Snackbar.LENGTH_SHORT);
                                AnimateHelper.setFlashAnimation(holder.itemView, holder.save, ContextCompat.getColor(mContext, R.color.md_amber_500));
                            } else {
                                s = Snackbar.make(holder.itemView, R.string.submission_info_unsaved, Snackbar.LENGTH_SHORT);
                                ((ImageView) holder.save).setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                            }
                            View view = s.getView();
                            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.show();

                        }
                    }.execute();


                }
            });
        }

        if (!SettingValues.saveButton && !full || !Authentication.isLoggedIn || !Authentication.didOnline) {
            holder.save.setVisibility(View.GONE);
        }

        ImageView thumbImage2 = ((ImageView) holder.thumbimage);

        if (holder.leadImage.thumbImage2 == null)
            holder.leadImage.setThumbnail(thumbImage2);
        if (full)
            holder.leadImage.setWrapArea(holder.itemView.findViewById(R.id.wraparea));

        holder.leadImage.setSubmission(submission, full, baseSub);

        final ContentType.ImageType type = ContentType.getImageType(submission);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (offline) {
                    Snackbar s = Snackbar.make(holder.itemView, mContext.getString(R.string.offline_msg), Snackbar.LENGTH_SHORT);
                    View view = s.getView();
                    TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } else {
                    if (SettingValues.actionbarTap && !full) {
                        CreateCardView.toggleActionbar(holder.itemView);
                    } else {
                        holder.itemView.findViewById(R.id.menu).callOnClick();
                    }
                }
                return true;
            }

        });

        doInfoLine(holder, submission, mContext, baseSub, full);


        if (!full && SettingValues.cardText && submission.isSelfPost() && !submission.getSelftext().isEmpty()) {
            holder.body.setVisibility(View.VISIBLE);
            String text = submission.getDataNode().get("selftext_html").asText();
            holder.body.setTextHtml(Html.fromHtml(text.substring(0, text.contains("\n") ? text.indexOf("\n") : text.length())));
            if (holder.body.getText().toString().trim().isEmpty()) {
                holder.body.setVisibility(View.GONE);
            }
        } else if (!full) {
            holder.body.setVisibility(View.GONE);
        }

        if (fullscreen) {
            if (!submission.getSelftext().isEmpty()) {
                setViews(submission.getDataNode().get("selftext_html").asText(), submission.getSubredditName(), holder);
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.GONE);
            }
        }

        addClickFunctions(holder.leadImage, type, mContext, submission, holder, full);


        if (holder.thumbimage != null) {
            addClickFunctions(holder.thumbimage, type, mContext, submission, holder, full);
        } else {
            addClickFunctions(thumbImage2, type, mContext, submission, holder, full);
        }

        if (full)
            addClickFunctions(holder.itemView.findViewById(R.id.wraparea), type, mContext, submission, holder, full);

        try {
            final TextView points = holder.score;
            final TextView comments = holder.comments;
            if (Authentication.isLoggedIn && !offline && Authentication.didOnline) {
                {

                    downvotebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (SettingValues.storeHistory && !full) {
                                if ((type != ContentType.ImageType.NSFW_LINK && type != ContentType.ImageType.NSFW_IMAGE
                                        && type != ContentType.ImageType.NSFW_GFY && type != ContentType.ImageType.NSFW_GIF) || SettingValues.storeNSFWHistory) {
                                    HasSeen.addSeen(submission.getFullName());
                                    if (mContext instanceof MainActivity) {
                                        holder.title.setAlpha(0.54f);
                                    }
                                }
                            }
                            if (ActionStates.getVoteDirection(submission) != VoteDirection.DOWNVOTE) { //has not been downvoted
                                points.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_500));
                                downvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_blue_500), PorterDuff.Mode.SRC_ATOP);
                                upvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                                AnimateHelper.setFlashAnimation(holder.itemView, downvotebutton, ContextCompat.getColor(mContext, R.color.md_blue_500));
                                holder.score.setTypeface(null, Typeface.BOLD);
                                holder.score.setText("" + (submission.getScore() - 1));
                                new Vote(false, points, mContext).execute(submission);
                                ActionStates.setVoteDirection(submission, VoteDirection.DOWNVOTE);
                            } else {
                                points.setTextColor(comments.getCurrentTextColor());
                                new Vote(points, mContext).execute(submission);
                                holder.score.setTypeface(null, Typeface.NORMAL);
                                holder.score.setText("" + (submission.getScore()));
                                ActionStates.setVoteDirection(submission, VoteDirection.NO_VOTE);
                                downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                            }
                            if (!full && !SettingValues.actionbarVisible)
                                CreateCardView.toggleActionbar(holder.itemView);
                        }
                    });
                }
                {
                    upvotebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (SettingValues.storeHistory && !full) {
                                if ((type != ContentType.ImageType.NSFW_LINK && type != ContentType.ImageType.NSFW_IMAGE
                                        && type != ContentType.ImageType.NSFW_GFY && type != ContentType.ImageType.NSFW_GIF) || SettingValues.storeNSFWHistory) {
                                    HasSeen.addSeen(submission.getFullName());
                                    if (mContext instanceof MainActivity) {
                                        holder.title.setAlpha(0.54f);
                                    }
                                }
                            }

                            if (ActionStates.getVoteDirection(submission) != VoteDirection.UPVOTE) { //has not been upvoted
                                points.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_500));
                                upvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
                                downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                                AnimateHelper.setFlashAnimation(holder.itemView, upvotebutton, ContextCompat.getColor(mContext, R.color.md_orange_500));
                                holder.score.setTypeface(null, Typeface.BOLD);
                                holder.score.setText("" + (submission.getScore() + 1));
                                new Vote(true, points, mContext).execute(submission);
                                ActionStates.setVoteDirection(submission, VoteDirection.UPVOTE);
                            } else {
                                points.setTextColor(comments.getCurrentTextColor());
                                new Vote(points, mContext).execute(submission);
                                holder.score.setTypeface(null, Typeface.NORMAL);
                                holder.score.setText("" + (submission.getScore()));
                                ActionStates.setVoteDirection(submission, VoteDirection.NO_VOTE);
                                upvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                            }
                            if (!full && !SettingValues.actionbarVisible)
                                CreateCardView.toggleActionbar(holder.itemView);
                        }
                    });
                }
            } else {
                upvotebutton.setVisibility(View.GONE);
                downvotebutton.setVisibility(View.GONE);

            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        if (HasSeen.getSeen(submission) && !full) {
            holder.title.setAlpha(0.54f);
        } else {
            holder.title.setAlpha(1f);
        }

    }

    private void setViews(String rawHTML, String subredditName, SubmissionViewHolder holder) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        if (!blocks.get(0).startsWith("<table>") && !blocks.get(0).startsWith("<pre>")) {
            holder.firstTextView.setTextHtml(blocks.get(0), subredditName);
            startIndex = 1;
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                holder.commentOverflow.setViews(blocks, subredditName);
            } else {
                holder.commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName);
            }
        }
    }
}