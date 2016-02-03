package me.ccrama.redditslide.Views;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.bottomsheet.BottomSheet;
import com.fasterxml.jackson.databind.JsonNode;

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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import me.ccrama.redditslide.Adapters.SubmissionAdapter;
import me.ccrama.redditslide.Adapters.SubmissionViewHolder;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.Vote;
import me.ccrama.redditslide.util.CustomTabUtil;

/**
 * Created by ccrama on 9/19/2015.
 */
public class PopulateSubmissionViewHolder {


    private static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public static int getStyleAttribColorValue(final Context context, final int attribResId, final int defaultValue) {
        final TypedValue tv = new TypedValue();
        final boolean found = context.getTheme().resolveAttribute(attribResId, tv, true);
        return found ? tv.data : defaultValue;
    }

    private static void addClickFunctions(final View base, final View clickingArea, final ContentType.ImageType type, final Activity contextActivity, final Submission submission, final View back) {
        base.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HasSeen.addSeen(submission.getFullName());
                if (contextActivity instanceof MainActivity)
                    back.setAlpha(0.5f);
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
                        if (back != null) {
                            back.performClick();
                        }
                        break;
                    case GFY:
                        openGif(true, contextActivity, submission);
                        break;
                    case ALBUM:
                        if (SettingValues.album) {
                            if(SettingValues.albumSwipe){
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
                        if (back != null) {
                            back.performClick();
                        }
                        break;
                    case NONE_IMAGE:
                        openImage(contextActivity, submission);
                        break;
                    case NONE_URL:
                        CustomTabUtil.openUrl(submission.getUrl(), Palette.getColor(submission.getSubredditName()), contextActivity);
                        break;
                    case VIDEO:
                        if (SettingValues.video) {
                            Intent intent = new Intent(contextActivity, FullscreenVideo.class);
                            intent.putExtra(FullscreenVideo.EXTRA_HTML, submission.getUrl());
                            contextActivity.startActivity(intent);
                        } else {
                            Reddit.defaultShare(submission.getUrl(), contextActivity);
                        }
                }
            }
        });
    }


    public static void openRedditContent(String url, Context c) {
        new OpenRedditLink(c, url);
    }

    private static boolean isBlurry(JsonNode s, Context mC, String title) {
        if (SettingValues.blurCheck) {
            return false;
        } else {
            int pixesl = s.get("preview").get("images").get(0).get("source").get("width").asInt();
            float density = mC.getResources().getDisplayMetrics().density;
            float dp = pixesl / density;
            Configuration configuration = mC.getResources().getConfiguration();
            int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.

            return dp < screenWidthDp / 3;
        }
    }

    public static void openImage(Activity contextActivity, Submission submission) {
        if (SettingValues.image) {
            DataShare.sharedSubmission = submission;
            Intent myIntent = new Intent(contextActivity, FullscreenImage.class);
            myIntent.putExtra(FullscreenImage.EXTRA_URL, submission.getUrl());
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
    public static int getCurrentTintColor(Context v){
        return getStyleAttribColorValue(v, R.attr.tint, Color.WHITE);

    }
    public static int getWhiteTintColor(){
        return Palette.ThemeEnum.DARK.getTint();
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

    public <T> void populateSubmissionViewHolder(final SubmissionViewHolder holder, final Submission submission, final Activity mContext, boolean fullscreen, final boolean full, final List<T> posts, final RecyclerView recyclerview, final boolean same, final boolean offline) {
        String distingush = "";
        if (submission.getDistinguishedStatus() == DistinguishedStatus.MODERATOR)
            distingush = "[M]";
        else if (submission.getDistinguishedStatus() == DistinguishedStatus.ADMIN)
            distingush = "[A]";

        holder.title.setText(Html.fromHtml(submission.getTitle()));

        holder.info.setText(submission.getAuthor() + distingush + " " + TimeUtils.getTimeAgo(submission.getCreated().getTime(), mContext));

        holder.subreddit.setText(submission.getSubredditName());

        if (!offline && SubredditStorage.modOf != null && SubredditStorage.modOf.contains(submission.getSubredditName().toLowerCase())) {
            holder.itemView.findViewById(R.id.mod).setVisibility(View.VISIBLE);
            final Map<String, Integer> reports = submission.getUserReports();
            final Map<String, String> reports2 = submission.getModeratorReports();
            if (reports.size() + reports2.size() > 0) {
                ((ImageView) holder.itemView.findViewById(R.id.mod)).setColorFilter(ContextCompat.getColor(mContext, R.color.md_red_300), PorterDuff.Mode.SRC_ATOP);
            } else {
                ((ImageView) holder.itemView.findViewById(R.id.mod)).setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

            }

            holder.itemView.findViewById(R.id.mod).setOnClickListener(new View.OnClickListener() {
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
            holder.itemView.findViewById(R.id.mod).setVisibility(View.GONE);
        }

        holder.itemView.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (offline) {

                    Snackbar.make(holder.itemView, R.string.offline_msg, Snackbar.LENGTH_SHORT).show();

                } else {
                    LayoutInflater inflater = (mContext).getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.postmenu, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    title.setText(Html.fromHtml(submission.getTitle()));

                    ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + submission.getAuthor());
                    ((TextView) dialoglayout.findViewById(R.id.subpopup)).setText("/r/" + submission.getSubredditName());
                    dialoglayout.findViewById(R.id.sidebar).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, Profile.class);
                            i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                            mContext.startActivity(i);
                        }
                    });

                    dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, SubredditView.class);
                            i.putExtra(SubredditView.EXTRA_SUBREDDIT, submission.getSubredditName());
                            mContext.startActivity(i);
                        }
                    });


                    dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (submission.saved) {
                                ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_save);
                                submission.saved = false;
                            } else {
                                ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);
                                submission.saved = true;

                            }
                            new SubmissionAdapter.AsyncSave(holder.itemView).execute(submission);

                        }
                    });
                    if (submission.saved) {
                        ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);
                    }
                    dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String urlString = "https://reddit.com" + submission.getPermalink();
                            OpenRedditLink.customIntentChooser(urlString, mContext);
                        }
                    });
                    dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (submission.isSelfPost())
                                Reddit.defaultShareText("https://reddit.com" + submission.getPermalink(), mContext);
                            else {
                                new BottomSheet.Builder(mContext, R.style.BottomSheet_Dialog)
                                        .title(R.string.submission_share_title)
                                        .grid()
                                        .sheet(R.menu.share_menu)
                                        .listener(new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case R.id.reddit_url:
                                                        Reddit.defaultShareText("https://reddit.com" + submission.getPermalink(), mContext);
                                                        break;
                                                    case R.id.link_url:
                                                        Reddit.defaultShareText(submission.getUrl(), mContext);
                                                        break;
                                                }
                                            }
                                        }).show();
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.copy).setVisibility(View.GONE);
                    if (!Authentication.isLoggedIn || !Authentication.didOnline) {
                        dialoglayout.findViewById(R.id.save).setVisibility(View.GONE);
                        dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);

                    }
                    title.setBackgroundColor(Palette.getColor(submission.getSubredditName()));

                    builder.setView(dialoglayout);
                    final Dialog d = builder.show();
                    if (!SettingValues.hideButton) {
                        dialoglayout.findViewById(R.id.hide).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final int pos = posts.indexOf(submission);
                                final T t = posts.get(pos);
                                posts.remove(submission);

                                recyclerview.getAdapter().notifyItemRemoved(pos);
                                d.dismiss();
                                Hidden.setHidden((Contribution) t);

                                Snackbar.make(recyclerview, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        posts.add(pos, t);
                                        recyclerview.getAdapter().notifyItemInserted(pos);
                                        Hidden.undoHidden((Contribution) t);
                                    }
                                }).show();
                            }
                        });
                    } else {
                        dialoglayout.findViewById(R.id.hide).setVisibility(View.GONE);
                    }

                }
            }
        });
        int commentCount = submission.getCommentCount();
        final Resources res = mContext.getResources();
        holder.comments.setText(res.getQuantityString(R.plurals.submission_comment_count, commentCount, commentCount));

        holder.score.setText(getSubmissionScoreString(submission.getScore(), res, submission));

        final ImageView downvotebutton = (ImageView) holder.itemView.findViewById(R.id.downvote);
        final ImageView upvotebutton = (ImageView) holder.itemView.findViewById(R.id.upvote);
        if (submission.isArchived()) {
            downvotebutton.setVisibility(View.GONE);
            upvotebutton.setVisibility(View.GONE);
        } else if (Authentication.isLoggedIn && !submission.voted() && !offline && Authentication.didOnline) {
            if (submission.getVote() == VoteDirection.UPVOTE) {

                submission.setVote(true);
                submission.setVoted(true);
                holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_500));
                holder.score.setText(getSubmissionScoreString(submission.getScore(), res, submission));
                upvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
                downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

            } else if (submission.getVote() == VoteDirection.DOWNVOTE) {
                holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_500));
                downvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_blue_500), PorterDuff.Mode.SRC_ATOP);
                holder.score.setText(getSubmissionScoreString(submission.getScore(), res, submission));
                upvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full))? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                submission.setVote(false);
                submission.setVoted(true);
            } else {
                holder.score.setTextColor(holder.comments.getCurrentTextColor());
                holder.score.setText(getSubmissionScoreString(submission.getScore(), res, submission));
                downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                upvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                submission.setVote(false);
                submission.setVoted(false);

            }
        }

        final ImageView hideButton = (ImageView) holder.itemView.findViewById(R.id.hide);

        if (hideButton != null) {
            if (SettingValues.hideButton) {
                hideButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int pos = posts.indexOf(submission);
                        final T t = posts.get(pos);
                        posts.remove(submission);

                        recyclerview.getAdapter().notifyItemRemoved(pos);
                        if (!offline)
                            Hidden.setHidden((Contribution) t);

                        Snackbar.make(recyclerview, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                posts.add(pos, t);
                                recyclerview.getAdapter().notifyItemInserted(pos);
                                Hidden.undoHidden((Contribution) t);
                            }
                        }).show();
                    }
                });
            } else {
                hideButton.setVisibility(View.GONE);
            }
            if (SettingValues.saveButton && Authentication.isLoggedIn) {
                holder.itemView.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    if (submission.isSaved()) {
                                        new AccountManager(Authentication.reddit).unsave(submission);
                                        submission.saved = false;
                                    } else {
                                        new AccountManager(Authentication.reddit).save(submission);
                                        submission.saved = true;

                                    }
                                } catch (ApiException e) {
                                    e.printStackTrace();
                                }

                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                if (submission.saved) {
                                    Snackbar.make(recyclerview, R.string.submission_info_saved, Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(recyclerview, R.string.submission_info_unsaved, Snackbar.LENGTH_SHORT).show();

                                }

                            }
                        }.execute();


                    }
                });
            } else {
                holder.itemView.findViewById(R.id.save).setVisibility(View.GONE);
            }
        }


        ContentType.ImageType type = ContentType.getImageType(submission);

        String url = "";

        final String subreddit = "";


        ImageView thumbImage2 = ((ImageView) holder.itemView.findViewById(R.id.thumbimage2));


        if (submission.isNsfw() && !SettingValues.NSFWPreviews) {
            holder.imageArea.setVisibility(View.GONE);
            if (!full) {
                thumbImage2.setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.wraparea).setVisibility(View.VISIBLE);
            }
            if (submission.isSelfPost()) thumbImage2.setVisibility(View.GONE);
            else thumbImage2.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.nsfw));
        } else  if ( type != ContentType.ImageType.IMAGE && type != ContentType.ImageType.SELF && (submission.getThumbnailType() != Submission.ThumbnailType.URL)) {
            holder.imageArea.setVisibility(View.GONE);
            if (!full) {
                thumbImage2.setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.wraparea).setVisibility(View.VISIBLE);
            }

            thumbImage2.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.web));
        } else if (type == ContentType.ImageType.IMAGE) {
            url = submission.getUrl();
            if (!full && !SettingValues.bigPicEnabled ) {
                if (!full) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    holder.itemView.findViewById(R.id.wraparea).setVisibility(View.VISIBLE);
                }
                ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, thumbImage2);
                holder.imageArea.setVisibility(View.GONE);

            } else {
                ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, holder.leadImage);
                holder.imageArea.setVisibility(View.VISIBLE);
                if (!full) {
                    thumbImage2.setVisibility(View.GONE);
                } else {
                    holder.itemView.findViewById(R.id.wraparea).setVisibility(View.GONE);
                }
            }
        } else if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("height")) {

            url = submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
            if (!SettingValues.bigPicEnabled && !full) {
                if (!full) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    holder.itemView.findViewById(R.id.wraparea).setVisibility(View.VISIBLE);
                }
                ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, thumbImage2);
                holder.imageArea.setVisibility(View.GONE);

            } else {
                ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, holder.leadImage);
                holder.imageArea.setVisibility(View.VISIBLE);
                if (!full) {
                    thumbImage2.setVisibility(View.GONE);
                } else {
                    holder.itemView.findViewById(R.id.wraparea).setVisibility(View.GONE);
                }
            }
        } else if (submission.getThumbnail() != null && (submission.getThumbnailType() == Submission.ThumbnailType.URL || submission.getThumbnailType() == Submission.ThumbnailType.NSFW)) {

            if (!full) {
                thumbImage2.setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.wraparea).setVisibility(View.VISIBLE);
            }
            ((Reddit) mContext.getApplicationContext()).getImageLoader().displayImage(url, thumbImage2);
            holder.imageArea.setVisibility(View.GONE);


        } else {
            if (!full) {
                thumbImage2.setVisibility(View.GONE);
            } else {
                holder.itemView.findViewById(R.id.wraparea).setVisibility(View.GONE);
            }
            holder.imageArea.setVisibility(View.GONE);
        }

        TextView title;
        TextView info;
        if (!full) {
            title = holder.textImage;
            info = holder.subTextImage;
        } else if (holder.itemView.findViewById(R.id.wraparea).getVisibility() == View.VISIBLE) {
            title = holder.contentTitle;
            info = holder.contentURL;
        } else {
            title = holder.textImage;
            info = holder.subTextImage;
        }

        if(full && SettingValues.cropImage){
            holder.leadImage.setMaxHeight(dpToPx(200));
        }



            title.setVisibility(View.VISIBLE);
            info.setVisibility(View.VISIBLE);

            switch (type) {
                case NSFW_IMAGE:
                    title.setText(R.string.type_nsfw_img);
                    break;

                case NSFW_GIF:
                case NSFW_GFY:
                    title.setText(R.string.type_nsfw_gif);
                    break;

                case REDDIT:
                    title.setText(R.string.type_reddit);
                    break;

                case LINK:
                case IMAGE_LINK:
                    title.setText(R.string.type_link);
                    break;

                case NSFW_LINK:
                    title.setText(R.string.type_nsfw_link);

                    break;
                case SELF:
                    title.setVisibility(View.GONE);
                    info.setVisibility(View.GONE);

                    break;

                case ALBUM:
                    title.setText(R.string.type_album);
                    break;

                case IMAGE:
                    if (submission.isNsfw() && !SettingValues.NSFWPreviews) {
                        title.setText(R.string.type_nsfw_img);

                    } else {
                        title.setVisibility(View.GONE);
                        info.setVisibility(View.GONE);
                    }
                    break;
                case IMGUR:
                    title.setText(R.string.type_imgur);
                    break;
                case GFY:
                case GIF:
                case NONE_GFY:
                case NONE_GIF:
                    title.setText(R.string.type_gif);
                    break;

                case NONE:
                    title.setText(R.string.type_title_only);
                    break;

                case NONE_IMAGE:
                    title.setText(R.string.type_img);
                    break;

                case VIDEO:
                    title.setText(R.string.type_vid);
                    break;

                case EMBEDDED:
                    title.setText(R.string.type_emb);
                    break;

                case NONE_URL:
                    title.setText(R.string.type_link);
                    break;
            }

            View baseView;

            View back = holder.itemView;

            try {
                info.setText(getDomainName(submission.getUrl()));
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }

            baseView = holder.imageArea;

            addClickFunctions(holder.imageArea, baseView, type, mContext, submission, back);
            addClickFunctions(holder.leadImage, baseView, type, mContext, submission, back);

            addClickFunctions(thumbImage2, holder.imageArea, type, mContext, submission, holder.itemView);




        if (full) {
            addClickFunctions(holder.itemView.findViewById(R.id.wraparea), holder.imageArea, type, mContext, submission, holder.itemView);

        }
        View pinned = holder.itemView.findViewById(R.id.pinned);


        View flair = holder.itemView.findViewById(R.id.flairbubble);

        if (submission.getSubmissionFlair().getText() == null || submission.getSubmissionFlair() == null || submission.getSubmissionFlair().getText().isEmpty() || submission.getSubmissionFlair().getText() == null) {
            flair.setVisibility(View.GONE);
        } else {
            flair.setVisibility(View.VISIBLE);
            ((TextView) flair.findViewById(R.id.text)).setText(Html.fromHtml(submission.getSubmissionFlair().getText()));
        }

        if (fullscreen) {
            SpoilerRobotoTextView bod = ((SpoilerRobotoTextView) holder.itemView.findViewById(R.id.body));
            if (!submission.getSelftext().isEmpty()) {
                new MakeTextviewClickable().ParseTextWithLinksTextView(submission.getDataNode().get("selftext_html").asText(), bod, mContext, submission.getSubredditName());
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.GONE);
            }
        }
        View nsfw = holder.itemView.findViewById(R.id.nsfw);
        int pinnedVisibility = submission.isStickied() ? View.VISIBLE : View.GONE;
        if (pinned.getVisibility() != pinnedVisibility) {
            pinned.setVisibility(pinnedVisibility);
        }

        int nsfwVisibility = submission.isNsfw() ? View.VISIBLE : View.GONE;
        if (nsfw.getVisibility() != nsfwVisibility) {
            nsfw.setVisibility(nsfwVisibility);
        }

        try {
            final TextView points = holder.score;
            final TextView comments = holder.comments;
            if (Authentication.isLoggedIn && !offline && Authentication.didOnline) {
                {
                    downvotebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!submission.voted()) {
                                points.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_500));

                                submission.setVote(false);
                                downvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_blue_500), PorterDuff.Mode.SRC_ATOP);

                                submission.setVoted(true);
                                holder.score.setText(getSubmissionScoreString(submission.getScore() - 1, res, submission));

                                new Vote(false, points, mContext).execute(submission);
                            } else if (submission.voted() && submission.getIsUpvoted()) {
                                new Vote(false, points, mContext).execute(submission);
                                points.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_500));
                                downvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_blue_500), PorterDuff.Mode.SRC_ATOP);
                                upvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                                submission.setVoted(true);
                                submission.setVote(false);
                                holder.score.setText(getSubmissionScoreString(submission.getScore() - 1, res, submission));


                            } else if (submission.voted() && !submission.getIsUpvoted()) {
                                new Vote(points, mContext).execute(submission);
                                points.setTextColor(comments.getCurrentTextColor());
                                downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                                holder.score.setText(getSubmissionScoreString(submission.getScore(), res, submission));

                                submission.setVoted(false);
                                submission.setVote(false);

                            }
                        }
                    });
                }
                {
                    upvotebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!submission.voted()) {
                                upvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
                                submission.setVote(true);
                                submission.setVoted(true);
                                holder.score.setText(getSubmissionScoreString(submission.getScore() + 1, res, submission));

                                new Vote(true, points, mContext).execute(submission);
                                points.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_500));
                            } else if (submission.voted() && !submission.getIsUpvoted()) {
                                new Vote(true, points, mContext).execute(submission);
                                points.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_500));
                                submission.setVote(true);
                                submission.setVoted(true);

                                upvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500), PorterDuff.Mode.SRC_ATOP);
                                downvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                                holder.score.setText(getSubmissionScoreString(submission.getScore() + 1, res, submission));

                            } else if (submission.voted() && submission.getIsUpvoted()) {
                                points.setTextColor(comments.getCurrentTextColor());
                                new Vote(points, mContext).execute(submission);
                                submission.setVote(false);

                                holder.score.setText(getSubmissionScoreString(submission.getScore(), res, submission));

                                upvotebutton.setColorFilter((((holder.itemView.getTag(holder.itemView.getId())) != null && holder.itemView.getTag(holder.itemView.getId()).equals("none") || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);

                            }
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
        if (HasSeen.getSeen(submission.getFullName()) && !full) {
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }


        int timesGilded = submission.getTimesGilded();
        if (timesGilded > 0) {
            holder.gildLayout.setVisibility(View.VISIBLE);
            holder.gildText.setText(Integer.toString(timesGilded));
        } else {
            holder.gildLayout.setVisibility(View.GONE);
        }


    }
    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
