package me.ccrama.redditslide.SubmissionViews;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

import com.cocosw.bottomsheet.BottomSheet;
import com.fasterxml.jackson.databind.JsonNode;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import net.dean.jraw.models.Submission;

import java.net.URI;
import java.net.URISyntaxException;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.ForceTouch.PeekView;
import me.ccrama.redditslide.ForceTouch.PeekViewActivity;
import me.ccrama.redditslide.ForceTouch.builder.Peek;
import me.ccrama.redditslide.ForceTouch.builder.PeekViewOptions;
import me.ccrama.redditslide.ForceTouch.callback.OnButtonUp;
import me.ccrama.redditslide.ForceTouch.callback.OnPop;
import me.ccrama.redditslide.ForceTouch.callback.OnRemove;
import me.ccrama.redditslide.ForceTouch.callback.SimpleOnPeek;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.PeekMediaView;
import me.ccrama.redditslide.Views.TransparentTagTextView;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by carlo_000 on 2/7/2016.
 */
public class HeaderImageLinkView extends RelativeLayout {
    public String    loadedUrl;
    public boolean   lq;
    public ImageView thumbImage2;
    public TextView  secondTitle;
    public TextView  secondSubTitle;
    public View      wrapArea;
    boolean done;
    String lastDone = "";
    ContentType.Type type;
    DisplayImageOptions bigOptions = new DisplayImageOptions.Builder().resetViewBeforeLoading(false)
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheInMemory(false)
            .displayer(new FadeInBitmapDisplayer(250))
            .build();
    Activity            activity   = null;
    boolean     clickHandled;
    Handler     handler;
    MotionEvent event;
    Runnable    longClicked;
    float       position;
    private TextView  title;
    private TextView  info;
    public  ImageView backdrop;

    public HeaderImageLinkView(Context context) {
        super(context);
        init();
    }

    public HeaderImageLinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeaderImageLinkView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    boolean thumbUsed;

    public void doImageAndText(final Submission submission, boolean full, String baseSub, boolean news) {

        boolean fullImage = ContentType.fullImage(type);
        thumbUsed = false;

        setVisibility(View.VISIBLE);
        String url = "";
        boolean forceThumb = false;
        thumbImage2.setImageResource(android.R.color.transparent);

        boolean loadLq =
                (((!NetworkUtil.isConnectedWifi(getContext()) && SettingValues.lowResMobile)
                        || SettingValues.lowResAlways));

       /* todo, maybe if(thumbImage2 != null && thumbImage2 instanceof RoundImageTriangleView)
            switch (ContentType.getContentType(submission)) {
            case ALBUM:
                ((RoundImageTriangleView)(thumbImage2)).setFlagColor(R.color.md_blue_300);
                break;
            case EXTERNAL:
            case LINK:
            case REDDIT:
                ((RoundImageTriangleView)(thumbImage2)).setFlagColor(R.color.md_red_300);
                break;
            case SELF:
                ((RoundImageTriangleView)(thumbImage2)).setFlagColor(R.color.md_grey_300);
                break;
            case EMBEDDED:
            case GIF:
            case STREAMABLE:
            case VIDEO:
                ((RoundImageTriangleView)(thumbImage2)).setFlagColor(R.color.md_green_300);
                break;
            default:
                ((RoundImageTriangleView)(thumbImage2)).setFlagColor(Color.TRANSPARENT);
                break;
        }*/

        if (type == ContentType.Type.SELF && SettingValues.hideSelftextLeadImage
                || SettingValues.noImages && submission.isSelfPost()) {
            setVisibility(View.GONE);
            if (wrapArea != null) wrapArea.setVisibility(View.GONE);
            thumbImage2.setVisibility(View.GONE);
        } else {
            if (submission.getThumbnails() != null) {

                int height = submission.getThumbnails().getSource().getHeight();
                int width = submission.getThumbnails().getSource().getWidth();

                if (full) {
                    if (!fullImage && height < dpToPx(50) && type != ContentType.Type.SELF) {
                        forceThumb = true;
                    } else if (SettingValues.cropImage) {
                        backdrop.setLayoutParams(
                                new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                        dpToPx(200)));
                    } else {
                        double h = getHeightFromAspectRatio(height, width);
                        if (h != 0) {
                            if (h > 3200) {
                                backdrop.setLayoutParams(
                                        new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                                3200));
                            } else {
                                backdrop.setLayoutParams(
                                        new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                                (int) h));
                            }
                        } else {
                            backdrop.setLayoutParams(
                                    new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                            LayoutParams.WRAP_CONTENT));
                        }
                    }
                } else if (SettingValues.bigPicCropped) {
                    if (!fullImage && height < dpToPx(50)) {
                        forceThumb = true;
                    } else {
                        backdrop.setLayoutParams(
                                new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                        dpToPx(200)));
                    }
                } else if (fullImage || height >= dpToPx(50)) {
                    double h = getHeightFromAspectRatio(height, width);
                    if (h != 0) {
                        if (h > 3200) {
                            backdrop.setLayoutParams(
                                    new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                            3200));
                        } else {
                            backdrop.setLayoutParams(
                                    new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                            (int) h));
                        }
                    } else {
                        backdrop.setLayoutParams(
                                new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                        LayoutParams.WRAP_CONTENT));
                    }
                } else {
                    forceThumb = true;
                }

            }

            JsonNode thumbnail = submission.getDataNode().get("thumbnail");
            Submission.ThumbnailType thumbnailType;
            if (!submission.getDataNode().get("thumbnail").isNull()) {
                thumbnailType = submission.getThumbnailType();
            } else {
                thumbnailType = Submission.ThumbnailType.NONE;
            }

            JsonNode node = submission.getDataNode();
            if(!SettingValues.ignoreSubSetting && node != null && node.has("sr_detail") && node.get("sr_detail").has("show_media") && !node.get("sr_detail").get("show_media").asBoolean()){
                thumbnailType = Submission.ThumbnailType.NONE;
            }

            if (SettingValues.noImages && loadLq) {
                setVisibility(View.GONE);
                if (!full && !submission.isSelfPost()) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    if (full && !submission.isSelfPost()) wrapArea.setVisibility(View.VISIBLE);
                }
                thumbImage2.setImageDrawable(
                        ContextCompat.getDrawable(getContext(), R.drawable.web));
                thumbUsed = true;
            } else if (submission.isNsfw()
                    && SettingValues.getIsNSFWEnabled() || (baseSub != null && submission.isNsfw() && SettingValues.hideNSFWCollection && (baseSub.equals("frontpage") || baseSub.equals("all") || baseSub.contains("+") || baseSub.equals("popular")) )) {
                setVisibility(View.GONE);
                if (!full || forceThumb) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    wrapArea.setVisibility(View.VISIBLE);
                }
                if (submission.isSelfPost() && full) {
                    wrapArea.setVisibility(View.GONE);
                } else {
                    thumbImage2.setImageDrawable(
                            ContextCompat.getDrawable(getContext(), R.drawable.nsfw));
                    thumbUsed = true;
                }
                loadedUrl = submission.getUrl();
            } else if (submission.getDataNode().get("spoiler").asBoolean()) {
                setVisibility(View.GONE);
                if (!full || forceThumb) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    wrapArea.setVisibility(View.VISIBLE);
                }
                if (submission.isSelfPost() && full) {
                    wrapArea.setVisibility(View.GONE);
                } else {
                    thumbImage2.setImageDrawable(
                            ContextCompat.getDrawable(getContext(), R.drawable.spoiler));
                    thumbUsed = true;
                }
                loadedUrl = submission.getUrl();
            } else if (type != ContentType.Type.IMAGE
                    && type != ContentType.Type.SELF
                    && (!thumbnail.isNull() && (thumbnailType != Submission.ThumbnailType.URL))
                    || thumbnail.asText().isEmpty() && !submission.isSelfPost()) {

                setVisibility(View.GONE);
                if (!full) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    wrapArea.setVisibility(View.VISIBLE);
                }

                thumbImage2.setImageDrawable(
                        ContextCompat.getDrawable(getContext(), R.drawable.web));
                thumbUsed = true;
                loadedUrl = submission.getUrl();
            } else if (type == ContentType.Type.IMAGE && !thumbnail.isNull() && !thumbnail.asText()
                    .isEmpty()) {
                if (loadLq
                        && submission.getThumbnails() != null
                        && submission.getThumbnails().getVariations() != null
                        && submission.getThumbnails().getVariations().length > 0) {

                    if (ContentType.isImgurImage(submission.getUrl())) {
                        url = submission.getUrl();
                        url = url.substring(0, url.lastIndexOf(".")) + (SettingValues.lqLow ? "m"
                                : (SettingValues.lqMid ? "l" : "h")) + url.substring(
                                url.lastIndexOf("."), url.length());
                    } else {
                        int length = submission.getThumbnails().getVariations().length;
                        if (SettingValues.lqLow && length >= 3) {
                            url = Html.fromHtml(
                                    submission.getThumbnails().getVariations()[2].getUrl())
                                    .toString(); //unescape url characters
                        } else if (SettingValues.lqMid && length >= 4) {
                            url = Html.fromHtml(
                                    submission.getThumbnails().getVariations()[3].getUrl())
                                    .toString(); //unescape url characters
                        } else if (length >= 5) {
                            url = Html.fromHtml(
                                    submission.getThumbnails().getVariations()[length - 1].getUrl())
                                    .toString(); //unescape url characters
                        } else {
                            url = Html.fromHtml(submission.getThumbnails().getSource().getUrl())
                                    .toString(); //unescape url characters
                        }
                    }
                    lq = true;

                } else {
                    if (submission.getDataNode().has("preview") && submission.getDataNode()
                            .get("preview")
                            .get("images")
                            .get(0)
                            .get("source")
                            .has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                        url = submission.getDataNode()
                                .get("preview")
                                .get("images")
                                .get(0)
                                .get("source")
                                .get("url")
                                .asText();
                    } else {
                        url = submission.getUrl();
                    }
                }

                if (!full && !SettingValues.isPicsEnabled(baseSub) || forceThumb) {

                    if (!submission.isSelfPost() || full) {
                        if (!full) {
                            thumbImage2.setVisibility(View.VISIBLE);
                        } else {
                            wrapArea.setVisibility(View.VISIBLE);
                        }

                        loadedUrl = url;
                        if (!full) {
                            ((Reddit) getContext().getApplicationContext()).getImageLoader()
                                    .displayImage(url, thumbImage2);
                        } else {
                            ((Reddit) getContext().getApplicationContext()).getImageLoader()
                                    .displayImage(url, thumbImage2, bigOptions);
                        }
                    } else {
                        thumbImage2.setVisibility(View.GONE);
                    }
                    setVisibility(View.GONE);

                } else {
                    loadedUrl = url;
                    if (!full) {
                        ((Reddit) getContext().getApplicationContext()).getImageLoader()
                                .displayImage(url, backdrop);
                    } else {
                        ((Reddit) getContext().getApplicationContext()).getImageLoader()
                                .displayImage(url, backdrop, bigOptions);
                    }
                    setVisibility(View.VISIBLE);
                    if (!full) {
                        thumbImage2.setVisibility(View.GONE);
                    } else {
                        wrapArea.setVisibility(View.GONE);
                    }
                }
            } else if (submission.getThumbnails() != null) {

                if (loadLq && submission.getThumbnails().getVariations().length != 0) {
                    if (ContentType.isImgurImage(submission.getUrl())) {
                        url = submission.getUrl();
                        url = url.substring(0, url.lastIndexOf(".")) + (SettingValues.lqLow ? "m"
                                : (SettingValues.lqMid ? "l" : "h")) + url.substring(
                                url.lastIndexOf("."), url.length());
                    } else {
                        int length = submission.getThumbnails().getVariations().length;
                        if (SettingValues.lqLow && length >= 3) {
                            url = Html.fromHtml(
                                    submission.getThumbnails().getVariations()[2].getUrl())
                                    .toString(); //unescape url characters
                        } else if (SettingValues.lqMid && length >= 4) {
                            url = Html.fromHtml(
                                    submission.getThumbnails().getVariations()[3].getUrl())
                                    .toString(); //unescape url characters
                        } else if (length >= 5) {
                            url = Html.fromHtml(
                                    submission.getThumbnails().getVariations()[length - 1].getUrl())
                                    .toString(); //unescape url characters
                        } else {
                            url = Html.fromHtml(submission.getThumbnails().getSource().getUrl())
                                    .toString(); //unescape url characters
                        }
                    }
                    lq = true;

                } else {
                    url = Html.fromHtml(submission.getThumbnails().getSource().getUrl())
                            .toString(); //unescape url characters
                }
                if (!SettingValues.isPicsEnabled(baseSub) && !full || forceThumb || (news && submission.getScore() < 5000)) {

                    if (!full) {
                        thumbImage2.setVisibility(View.VISIBLE);
                    } else {
                        wrapArea.setVisibility(View.VISIBLE);
                    }
                    loadedUrl = url;
                    ((Reddit) getContext().getApplicationContext()).getImageLoader()
                            .displayImage(url, thumbImage2);
                    setVisibility(View.GONE);

                } else {
                    loadedUrl = url;

                    if (!full) {
                        ((Reddit) getContext().getApplicationContext()).getImageLoader()
                                .displayImage(url, backdrop);
                    } else {
                        ((Reddit) getContext().getApplicationContext()).getImageLoader()
                                .displayImage(url, backdrop, bigOptions);
                    }
                    setVisibility(View.VISIBLE);
                    if (!full) {
                        thumbImage2.setVisibility(View.GONE);
                    } else {
                        wrapArea.setVisibility(View.GONE);
                    }
                }
            } else if (!thumbnail.isNull()
                    && submission.getThumbnail() != null
                    && (submission.getThumbnailType() == Submission.ThumbnailType.URL || (!thumbnail
                    .isNull() && submission.isNsfw() && SettingValues.getIsNSFWEnabled()))) {

                if (!full) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    wrapArea.setVisibility(View.VISIBLE);
                }
                loadedUrl = url;

                ((Reddit) getContext().getApplicationContext()).getImageLoader()
                        .displayImage(url, thumbImage2);
                setVisibility(View.GONE);


            } else {

                if (!full) {
                    thumbImage2.setVisibility(View.GONE);
                } else {
                    wrapArea.setVisibility(View.GONE);
                }
                setVisibility(View.GONE);
            }

            if (full) {
                if (wrapArea.getVisibility() == View.VISIBLE) {
                    title = secondTitle;
                    info = secondSubTitle;
                    setBottomSheet(wrapArea, submission, full);
                } else {
                    title = findViewById(R.id.textimage);
                    info = findViewById(R.id.subtextimage);
                    if (forceThumb || (submission.isNsfw()
                            && submission.getThumbnailType() == Submission.ThumbnailType.NSFW
                            || type != ContentType.Type.IMAGE
                            && type != ContentType.Type.SELF
                            && !submission.getDataNode().get("thumbnail").isNull()
                            && (submission.getThumbnailType() != Submission.ThumbnailType.URL))) {
                        setBottomSheet(thumbImage2, submission, full);
                    } else {
                        setBottomSheet(this, submission, full);
                    }
                }
            } else {
                title = findViewById(R.id.textimage);
                info = findViewById(R.id.subtextimage);
                setBottomSheet(thumbImage2, submission, full);
                setBottomSheet(this, submission, full);

            }


            if (SettingValues.smallTag && !full && !news) {
                title = findViewById(R.id.tag);
                findViewById(R.id.tag).setVisibility(View.VISIBLE);
                info = null;
            } else {
                findViewById(R.id.tag).setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                info.setVisibility(View.VISIBLE);
            }

            if (SettingValues.smallTag && !full && !news) {
                ((TransparentTagTextView) title).init(getContext());
            }

            title.setText(ContentType.getContentDescription(submission, getContext()));

            if (info != null) info.setText(submission.getDomain());

        }
    }

    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    boolean popped;

    public double getHeightFromAspectRatio(int imageHeight, int imageWidth) {
        double ratio = (double) imageHeight / (double) imageWidth;
        double width = getWidth();
        return (width * ratio);

    }

    public void onLinkLongClick(final String url, MotionEvent event) {
        popped = false;
        if (url == null) {
            return;
        }
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        Activity activity = null;
        final Context context = getContext();
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof ContextThemeWrapper) {
            activity =
                    (Activity) ((ContextThemeWrapper) context).getBaseContext();
        } else if (context instanceof ContextWrapper) {
            Context context1 = ((ContextWrapper) context).getBaseContext();
            if (context1 instanceof Activity) {
                activity = (Activity) context1;
            } else if (context1 instanceof ContextWrapper) {
                Context context2 = ((ContextWrapper) context1).getBaseContext();
                if (context2 instanceof Activity) {
                    activity = (Activity) context2;
                } else if (context2 instanceof ContextWrapper) {
                    activity =
                            (Activity) ((ContextThemeWrapper) context2).getBaseContext();
                }
            }
        } else {
            throw new RuntimeException("Could not find activity from context:" + context);
        }

        if (activity != null && !activity.isFinishing()) {
            if (SettingValues.peek) {
                Peek.into(R.layout.peek_view_submission, new SimpleOnPeek() {
                    @Override
                    public void onInflated(final PeekView peekView, final View rootView) {
                        //do stuff
                        TextView text = rootView.findViewById(R.id.title);
                        text.setText(url);
                        text.setTextColor(Color.WHITE);
                        ((PeekMediaView) rootView.findViewById(R.id.peek)).setUrl(url);

                        peekView.addButton((R.id.share), new OnButtonUp() {
                            @Override
                            public void onButtonUp() {
                                Reddit.defaultShareText("", url, rootView.getContext());
                            }
                        });

                        peekView.addButton((R.id.upvoteb), new OnButtonUp() {
                            @Override
                            public void onButtonUp() {
                                ((View) getParent()).findViewById(R.id.upvote).callOnClick();
                            }
                        });

                        peekView.setOnRemoveListener(new OnRemove() {
                            @Override
                            public void onRemove() {
                                ((PeekMediaView) rootView.findViewById(R.id.peek)).doClose();
                            }
                        });

                        peekView.addButton((R.id.comments), new OnButtonUp() {
                            @Override
                            public void onButtonUp() {
                                ((View) getParent().getParent()).callOnClick();
                            }
                        });

                        peekView.setOnPop(new OnPop() {
                            @Override
                            public void onPop() {
                                popped = true;
                                callOnClick();
                            }
                        });
                    }

                })
                        .with(new PeekViewOptions().setFullScreenPeek(true))
                        .show((PeekViewActivity) activity, event);
            } else {
                BottomSheet.Builder b = new BottomSheet.Builder(activity).title(url).grid();
                int[] attrs = new int[]{R.attr.tintColor};
                TypedArray ta = getContext().obtainStyledAttributes(attrs);

                int color = ta.getColor(0, Color.WHITE);
                Drawable open = getResources().getDrawable(R.drawable.ic_open_in_browser);
                open.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                Drawable share = getResources().getDrawable(R.drawable.ic_share);
                share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                Drawable copy = getResources().getDrawable(R.drawable.ic_content_copy);
                copy.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

                ta.recycle();

                b.sheet(R.id.open_link, open,
                        getResources().getString(R.string.submission_link_extern));
                b.sheet(R.id.share_link, share, getResources().getString(R.string.share_link));
                b.sheet(R.id.copy_link, copy,
                        getResources().getString(R.string.submission_link_copy));
                final Activity finalActivity = activity;
                b.listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.open_link:
                                LinkUtil.openExternally(url);
                                break;
                            case R.id.share_link:
                                Reddit.defaultShareText("", url, finalActivity);
                                break;
                            case R.id.copy_link:
                                LinkUtil.copyUrl(url, finalActivity);
                                break;
                        }
                    }
                }).show();
            }
        }
    }

    public void setBottomSheet(View v, final Submission submission, final boolean full) {
        handler = new Handler();
        v.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                x += getScrollX();
                y += getScrollY();

                HeaderImageLinkView.this.event = event;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    position = event.getY(); //used to see if the user scrolled or not
                }
                if (!(event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_DOWN)) {
                    if (Math.abs((position - event.getY())) > 25) {
                        handler.removeCallbacksAndMessages(null);
                    }
                    return false;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        clickHandled = false;
                        if (SettingValues.peek) {
                            handler.postDelayed(longClicked,
                                    android.view.ViewConfiguration.getTapTimeout() + 50);
                        } else {
                            handler.postDelayed(longClicked,
                                    android.view.ViewConfiguration.getLongPressTimeout());
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacksAndMessages(null);

                        if (!clickHandled) {
                            // regular click
                            callOnClick();
                        }
                        break;
                }
                return true;
            }
        });
        longClicked = new Runnable() {
            @Override
            public void run() {
                // long click
                clickHandled = true;

                handler.removeCallbacksAndMessages(null);
                if (SettingValues.storeHistory && !full) {
                    if (!submission.isNsfw() || SettingValues.storeNSFWHistory) {
                        HasSeen.addSeen(submission.getFullName());
                        ((View) getParent()).findViewById(R.id.title).setAlpha(0.54f);
                        ((View) getParent()).findViewById(R.id.body).setAlpha(0.54f);

                    }
                }
                onLinkLongClick(submission.getUrl(), event);
            }
        };
    }

    public void setSecondSubtitle(TextView v) {
        secondSubTitle = v;
    }

    public void setSecondTitle(TextView v) {
        secondTitle = v;
    }

    public void setSubmission(final Submission submission, final boolean full, String baseSub,
            ContentType.Type type) {
        this.type = type;
        if (!lastDone.equals(submission.getFullName())) {
            lq = false;
            lastDone = submission.getFullName();
            backdrop.setImageResource(
                    android.R.color.transparent); //reset the image view in case the placeholder is still visible
            thumbImage2.setImageResource(android.R.color.transparent);
            doImageAndText(submission, full, baseSub, false);
        }
    }

    public void setSubmissionNews(final Submission submission, final boolean full, String baseSub,
            ContentType.Type type) {
        this.type = type;
        if (!lastDone.equals(submission.getFullName())) {
            lq = false;
            lastDone = submission.getFullName();
            backdrop.setImageResource(
                    android.R.color.transparent); //reset the image view in case the placeholder is still visible
            thumbImage2.setImageResource(android.R.color.transparent);
            doImageAndText(submission, full, baseSub, true);
        }
    }

    public void setThumbnail(ImageView v) {
        thumbImage2 = v;
    }

    public void setUrl(String url) {

    }

    public void setWrapArea(View v) {
        wrapArea = v;
        setSecondTitle((TextView) v.findViewById(R.id.contenttitle));
        setSecondSubtitle((TextView) v.findViewById(R.id.contenturl));

    }

    private String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain != null && !domain.isEmpty()) {
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } else {
            return "";
        }
    }

    private void init() {
        inflate(getContext(), R.layout.header_image_title_view, this);
        this.title = findViewById(R.id.textimage);
        this.info = findViewById(R.id.subtextimage);
        this.backdrop = findViewById(R.id.leadimage);
    }
}