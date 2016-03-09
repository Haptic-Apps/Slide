package me.ccrama.redditslide.SubmissionViews;


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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
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

import it.sephiroth.android.library.tooltip.Tooltip;
import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.Activities.Imgur;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.ModQueue;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Adapters.SubmissionViewHolder;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.AnimateHelper;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.Vote;
import me.ccrama.redditslide.util.CustomTabUtil;
import me.ccrama.redditslide.util.LogUtil;
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

    private static void addClickFunctions(final View base, final ContentType.ImageType type, final Activity contextActivity, final Submission submission, final SubmissionViewHolder holder) {
        base.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HasSeen.addSeen(submission.getFullName());
                if (contextActivity instanceof MainActivity) {
                    holder.title.setAlpha(0.65f);
                    holder.leadImage.setAlpha(0.65f);
                    holder.thumbimage.setAlpha(0.65f);
                }
                switch (type) {
                    case NSFW_IMAGE:
                        openImage(contextActivity, submission);
                        break;
                    case IMGUR:
                        Intent i2 = new Intent(contextActivity, Imgur.class);
                        i2.putExtra(Imgur.EXTRA_URL, submission.getUrl());
                        contextActivity.startActivity(i2);
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

                }
            }
        });
    }


    public static void openRedditContent(String url, Context c) {
        new OpenRedditLink(c, url);
    }


    public static void openImage(Activity contextActivity, Submission submission) {
        if (SettingValues.image) {
            DataShare.sharedSubmission = submission;
            Intent myIntent = new Intent(contextActivity, FullscreenImage.class);
            String url;
            if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                url = submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
            } else {
                url = submission.getUrl();
            }
            myIntent.putExtra(FullscreenImage.EXTRA_URL, url);
            myIntent.putExtra(FullscreenImage.EXTRA_SHARE_URL, submission.getUrl());

            contextActivity.startActivity(myIntent);
        } else {
            Reddit.defaultShare(submission.getUrl(), contextActivity);
        }

    }

    public static void openGif(final boolean gfy, Activity contextActivity, Submission submission) {
        if (SettingValues.gif) {
            DataShare.sharedSubmission = submission;

            Intent myIntent = new Intent(contextActivity, GifView.class);
            if (gfy) {
                myIntent.putExtra(GifView.EXTRA_URL, "gfy" + submission.getUrl());
            } else {
                myIntent.putExtra(GifView.EXTRA_URL, "" + submission.getUrl());

            }
            contextActivity.startActivity(myIntent);
            contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
        } else {
            Reddit.defaultShare(submission.getUrl(), contextActivity);

        }

    }

    public static int getCurrentTintColor(Context v) {
        return getStyleAttribColorValue(v, R.attr.tint, Color.WHITE);

    }

    public static int getWhiteTintColor() {
        return Palette.ThemeEnum.DARK.getTint();
    }

    public <T extends Contribution> void showBottomSheet(final Activity mContext, final Submission submission, final SubmissionViewHolder holder, final List<T> posts, final String baseSub, final RecyclerView recyclerview) {

        int[] attrs = new int[]{R.attr.tint};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
        Drawable sub = mContext.getResources().getDrawable(R.drawable.sub);
        Drawable saved = mContext.getResources().getDrawable(R.drawable.iconstarfilled);
        Drawable hide = mContext.getResources().getDrawable(R.drawable.hide);
        Drawable open = mContext.getResources().getDrawable(R.drawable.openexternal);
        Drawable share = mContext.getResources().getDrawable(R.drawable.share);
        Drawable reddit = mContext.getResources().getDrawable(R.drawable.commentchange);
        profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        sub.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        saved.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        hide.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        open.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        reddit.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        new BottomSheet.Builder(mContext)
                .title(Html.fromHtml(submission.getTitle()))
                .sheet(1, profile, "/u/" + submission.getAuthor())
                .sheet(2, sub, "/r/" + submission.getSubredditName())
                .sheet(3, saved, mContext.getString(R.string.submission_save))
                .sheet(5, hide, mContext.getString(R.string.submission_hide))
                .sheet(7, open, mContext.getString(R.string.submission_link_extern))
                .sheet(4, share, mContext.getString(R.string.submission_share_permalink))
                .sheet(8, reddit, mContext.getString(R.string.submission_share_reddit_url)
                )

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
                                        if (ActionStates.isSaved(submission)) {
                                            ((ImageView) holder.save).setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
                                            Snackbar.make(holder.itemView, R.string.submission_info_saved, Snackbar.LENGTH_SHORT).show();
                                        } else {
                                            Snackbar.make(holder.itemView, R.string.submission_info_unsaved, Snackbar.LENGTH_SHORT).show();
                                            ((ImageView) holder.save).setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none"))) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                                        }

                                    }
                                }.execute();
                                break;
                            case 5: {
                                final int pos = posts.indexOf(submission);
                                final T t = posts.get(pos);
                                posts.remove(submission);

                                recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                                Hidden.setHidden((Contribution) t);

                                if (baseSub != null) {
                                    OfflineSubreddit.getSubreddit(baseSub).hide(pos);
                                }


                                Snackbar.make(recyclerview, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (baseSub != null) {
                                            OfflineSubreddit.getSubreddit(baseSub).unhideLast();
                                        }
                                        posts.add(pos, t);
                                        recyclerview.getAdapter().notifyItemInserted(pos + 1);
                                        Hidden.undoHidden((Contribution) t);

                                    }
                                }).show();
                            }
                            break;
                            case 7:
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(submission.getUrl()));
                                mContext.startActivity(browserIntent);
                                break;
                            case 4:
                                Reddit.defaultShareText(submission.getTitle() + " \n" + submission.getUrl(), mContext);
                                break;
                            case 8:
                                Reddit.defaultShareText(submission.getTitle() + " \n" + submission.getPermalink(), mContext);
                                break;
                            case 6:
                                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Link", submission.getUrl());
                                clipboard.setPrimaryClip(clip);

                                Toast.makeText(mContext, "Link copied", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }).show();
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

    public <T extends Contribution> void populateSubmissionViewHolder(final SubmissionViewHolder holder, final Submission submission, final Activity mContext, boolean fullscreen, final boolean full, final List<T> posts, final RecyclerView recyclerview, final boolean same, final boolean offline, final String baseSub) {
        holder.itemView.findViewById(R.id.vote).setVisibility(View.GONE);
        String distingush = "";
        if (submission.getDistinguishedStatus() == DistinguishedStatus.MODERATOR)
            distingush = "[M]";
        else if (submission.getDistinguishedStatus() == DistinguishedStatus.ADMIN)
            distingush = "[A]";

        holder.title.setTextHtml(submission.getTitle()); // title is a spoiler roboto textview so it will format the html

        String separator = mContext.getResources().getString(R.string.submission_properties_seperator);
        holder.info.setText("/r/" + submission.getSubredditName() + distingush + separator + TimeUtils.getTimeAgo(submission.getCreated().getTime(), mContext) + separator + "/u/" + submission.getAuthor() + separator + submission.getDomain());

        if (!offline && SubredditStorage.modOf != null && SubredditStorage.modOf.contains(submission.getSubredditName().toLowerCase())) {
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
                    LayoutInflater inflater = (mContext).getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.modmenu, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                    builder.setView(dialoglayout);
                    final Dialog d = builder.show();
                    dialoglayout.findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AsyncTask<Void, Void, ArrayList<String>>() {
                                @Override
                                protected ArrayList<String> doInBackground(Void... params) {

                                    ArrayList<String> finalReports = new ArrayList<>();
                                    for (String s : reports.keySet()) {
                                        finalReports.add("x" + reports.get(s) + " " + s);
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

                        }
                    });

                    dialoglayout.findViewById(R.id.approve).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_approve)
                                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog, int which) {

                                            new AsyncTask<Void, Void, Boolean>() {

                                                @Override
                                                public void onPostExecute(Boolean b) {
                                                    if (b) {
                                                        dialog.dismiss();
                                                        d.dismiss();
                                                        if (mContext instanceof ModQueue) {

                                                            final int pos = posts.indexOf(submission);
                                                            posts.remove(submission);

                                                            recyclerview.getAdapter().notifyItemRemoved(pos);
                                                            dialog.dismiss();
                                                        }
                                                        Snackbar.make(recyclerview, R.string.mod_approved, Snackbar.LENGTH_LONG).show();

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
                    });
                    dialoglayout.findViewById(R.id.spam).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });
                    dialoglayout.findViewById(R.id.nsfw).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!submission.isNsfw()) {
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
                            } else {
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
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.flair).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                                        ((Activity) mContext).runOnUiThread(new Runnable() {
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

                        }
                    });
                    dialoglayout.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialogWrapper.Builder(mContext).setTitle(R.string.mod_remove)
                                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                        String reason;
                                        String flair;

                                        @Override
                                        public void onClick(final DialogInterface dialog, int which) {
                                            new MaterialDialog.Builder(mContext)
                                                    .title(R.string.mod_remove_hint)
                                                    .input(mContext.getString(R.string.mod_remove_hint_msg), "", false, new MaterialDialog.InputCallback() {
                                                        @Override
                                                        public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                                            reason = charSequence.toString();
                                                        }
                                                    }).positiveText(R.string.misc_continue).onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                                    new MaterialDialog.Builder(mContext)
                                                            .title(R.string.mod_flair)
                                                            .content(R.string.mod_flair_desc)
                                                            .input(mContext.getString(R.string.mod_flair_hint), "", true, new MaterialDialog.InputCallback() {
                                                                @Override
                                                                public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                                                    flair = charSequence.toString();
                                                                }
                                                            }).positiveText(R.string.btn_remove).onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                                            new AsyncTask<Void, Void, Boolean>() {

                                                                @Override
                                                                public void onPostExecute(Boolean b) {
                                                                    if (b) {
                                                                        dialog.dismiss();
                                                                        d.dismiss();
                                                                        if (mContext instanceof
                                                                                ModQueue ||
                                                                                mContext
                                                                                        instanceof MainActivity) {
                                                                            final int pos = posts.indexOf(submission);
                                                                            posts.remove(submission);

                                                                            recyclerview.getAdapter().notifyItemRemoved(pos);
                                                                        }

                                                                        Snackbar.make(recyclerview, R.string.mod_post_removed, Snackbar.LENGTH_LONG).show();
                                                                    } else {
                                                                        new AlertDialogWrapper.Builder(mContext)
                                                                                .setTitle(R.string.err_general)
                                                                                .setMessage(R.string.err_retry_later).show();
                                                                    }
                                                                }

                                                                @Override
                                                                protected Boolean doInBackground(Void... params) {
                                                                    try {
                                                                        new ModerationManager(Authentication.reddit).remove(submission, true);
                                                                        if (!flair.isEmpty()) {
                                                                            //todo   new ModerationManager(Authentication.reddit).setFlair(submission.getSubredditName(), new , flair);
                                                                        }
                                                                        new AccountManager(Authentication.reddit).reply(submission, reason);

                                                                        return true;
                                                                    } catch (ApiException e) {
                                                                        e.printStackTrace();
                                                                        return false;

                                                                    }

                                                                }
                                                            }.execute();
                                                        }
                                                    }).show();
                                                }
                                            }).show();

                                        }
                                    }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }
                    });
                    dialoglayout.findViewById(R.id.ban).setVisibility(View.GONE);


                }
            });
        } else {
            holder.mod.setVisibility(View.GONE);
        }

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (offline) {

                    Snackbar.make(holder.itemView, R.string.offline_msg, Snackbar.LENGTH_SHORT).show();

                } else {
                    showBottomSheet(mContext, submission, holder, posts, baseSub, recyclerview);
                }
            }
        });
        int commentCount = submission.getCommentCount();
        final Resources res = mContext.getResources();
        holder.comments.setText("" + commentCount);

        holder.score.setText("" + submission.getScore());

        final ImageView downvotebutton = (ImageView) holder.downvote;
        final ImageView upvotebutton = (ImageView) holder.upvote;
        if (submission.isArchived()) {
            downvotebutton.setVisibility(View.GONE);
            upvotebutton.setVisibility(View.GONE);
        } else if (Authentication.isLoggedIn && !offline && Authentication.didOnline) {
            switch (ActionStates.getVoteDirection(submission)) {

                case UPVOTE: {
                    holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_500));
                    holder.score.setText("" + (submission.getScore() + 1));
                    upvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
                    downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                }
                break;
                case DOWNVOTE: {
                    holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_500));
                    downvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_blue_500), PorterDuff.Mode.SRC_ATOP);
                    holder.score.setText("" + (submission.getScore() - 1));
                    upvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                }
                break;
                case NO_VOTE: {
                    holder.score.setTextColor(holder.comments.getCurrentTextColor());
                    holder.score.setText("" + (submission.getScore()));
                    downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                    upvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                }
                break;
            }
        }

        final ImageView hideButton = (ImageView) holder.hide;

        if (hideButton != null) {
            if (SettingValues.hideButton && Authentication.isLoggedIn) {
                hideButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (posts != null) {
                            final int pos = posts.indexOf(submission);
                            if (pos != -1) {

                                final T t = posts.get(pos);
                                posts.remove(submission);

                                recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                                if (!offline)
                                    Hidden.setHidden((Contribution) t);
                                if (baseSub != null) {
                                    Log.v(LogUtil.getTag(), "HIDING POST2");

                                    OfflineSubreddit.getSubreddit(baseSub).hide(pos);
                                }
                                Snackbar.make(recyclerview, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (baseSub != null) {
                                            OfflineSubreddit.getSubreddit(baseSub).unhideLast();
                                        }
                                        posts.add(pos, t);
                                        recyclerview.getAdapter().notifyItemInserted(pos + 1);
                                        Hidden.undoHidden((Contribution) t);

                                    }
                                }).show();
                            }
                        }
                    }
                });
            } else {
                hideButton.setVisibility(View.GONE);
            }
        }
        if (Authentication.isLoggedIn) {
            if (ActionStates.isSaved(submission)) {
                ((ImageView) holder.save).setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
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
                            if (ActionStates.isSaved(submission)) {
                                ((ImageView) holder.save).setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
                                Snackbar.make(holder.itemView, R.string.submission_info_saved, Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(holder.itemView, R.string.submission_info_unsaved, Snackbar.LENGTH_SHORT).show();
                                ((ImageView) holder.save).setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                            }

                        }
                    }.execute();


                }
            });
        }

        if (!SettingValues.saveButton && !full || !Authentication.isLoggedIn) {
            holder.save.setVisibility(View.GONE);
        }


        ImageView thumbImage2 = ((ImageView) holder.thumbimage);


        holder.leadImage.setThumbnail(thumbImage2);
        if (full)
            holder.leadImage.setWrapArea(holder.itemView.findViewById(R.id.wraparea));

        holder.leadImage.setSubmission(submission, full, baseSub);

        ContentType.ImageType type = ContentType.getImageType(submission);

        addClickFunctions(holder.leadImage, type, mContext, submission, holder);


        if (holder.thumbimage != null) {
            addClickFunctions(holder.thumbimage, type, mContext, submission, holder);
        } else {
            addClickFunctions(thumbImage2, type, mContext, submission, holder);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (mContext instanceof MainActivity && ((MainActivity) mContext).t != null) {
                    Tooltip.removeAll(mContext);
                    Reddit.appRestart.edit().putString("tutorial_4", "A").apply();
                }
                if (offline) {
                    Snackbar.make(holder.itemView, mContext.getString(R.string.offline_msg), Snackbar.LENGTH_SHORT).show();
                } else {
                    if (SettingValues.actionbarTap) {
                        CreateCardView.toggleActionbar(holder.itemView);
                    } else {
                        holder.itemView.findViewById(R.id.menu).callOnClick();
                    }
                }
                return true;
            }

        });
        if (full) {
            addClickFunctions(holder.itemView.findViewById(R.id.wraparea), type, mContext, submission, holder);
        }

        if (submission.getSubmissionFlair().getText() == null || submission.getSubmissionFlair() == null || submission.getSubmissionFlair().getText().isEmpty() || submission.getSubmissionFlair().getText() == null) {
            holder.flair.setVisibility(View.GONE);
        } else {
            holder.flair.setVisibility(View.VISIBLE);
            holder.flairText.setText(Html.fromHtml(submission.getSubmissionFlair().getText()));
        }

        if (fullscreen) {
            if (!submission.getSelftext().isEmpty()) {
                setViews(submission.getDataNode().get("selftext_html").asText(), submission.getSubredditName(), holder);
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.GONE);
            }
        }
        int pinnedVisibility = submission.isStickied() ? View.VISIBLE : View.GONE;
        if (holder.pinned.getVisibility() != pinnedVisibility) {
            holder.pinned.setVisibility(pinnedVisibility);
        }

        int nsfwVisibility = submission.isNsfw() ? View.VISIBLE : View.GONE;
        if (holder.nsfw.getVisibility() != nsfwVisibility) {
            holder.nsfw.setVisibility(nsfwVisibility);
        }


        try {
            final TextView points = holder.score;
            final TextView comments = holder.comments;
            if (Authentication.isLoggedIn && !offline && Authentication.didOnline) {
                {
                    downvotebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (ActionStates.getVoteDirection(submission) != VoteDirection.DOWNVOTE) { //has not been downvoted
                                points.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_500));
                                downvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_blue_500), PorterDuff.Mode.SRC_ATOP);
                                upvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                                holder.score.setText("" + (submission.getScore() - 1));
                                AnimateHelper.setFlashAnimation(holder.itemView, downvotebutton, ContextCompat.getColor(mContext, R.color.md_blue_500));
                                new Vote(false, points, mContext).execute(submission);
                                ActionStates.setVoteDirection(submission, VoteDirection.DOWNVOTE);
                            } else {
                                new Vote(points, mContext).execute(submission);
                                points.setTextColor(comments.getCurrentTextColor());
                                downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                                holder.score.setText("" + (submission.getScore()));
                                ActionStates.setVoteDirection(submission, VoteDirection.NO_VOTE);

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

                            if (ActionStates.getVoteDirection(submission) != VoteDirection.UPVOTE) { //has not been upvoted
                                upvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
                                holder.score.setText("" + (submission.getScore() + 1));
                                AnimateHelper.setFlashAnimation(holder.itemView, upvotebutton, ContextCompat.getColor(mContext, R.color.md_orange_500));
                                downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                                new Vote(true, points, mContext).execute(submission);
                                ActionStates.setVoteDirection(submission, VoteDirection.UPVOTE);

                                points.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_500));
                            } else {
                                points.setTextColor(comments.getCurrentTextColor());
                                new Vote(points, mContext).execute(submission);
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
            holder.title.setAlpha(0.65f);
            holder.leadImage.setAlpha(0.65f);
            holder.thumbimage.setAlpha(0.65f);
        } else {
            holder.title.setAlpha(1f);
            holder.leadImage.setAlpha(1f);
            holder.thumbimage.setAlpha(1f);
        }


        int timesGilded = submission.getTimesGilded();
        if (timesGilded > 0) {
            holder.gildLayout.setVisibility(View.VISIBLE);
            holder.gildText.setText(Integer.toString(timesGilded));
        } else {
            holder.gildLayout.setVisibility(View.GONE);
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
