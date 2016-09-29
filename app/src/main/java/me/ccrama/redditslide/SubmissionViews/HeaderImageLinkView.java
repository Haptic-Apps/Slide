package me.ccrama.redditslide.SubmissionViews;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import me.ccrama.redditslide.ForceTouch.callback.SimpleOnPeek;
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
    public String  loadedUrl;
    public boolean lq;
    public ImageView thumbImage2;
    public TextView secondTitle;
    public TextView secondSubTitle;
    public View     wrapArea;
    boolean done;
    String lastDone = "";
    ContentType.Type type;
    DisplayImageOptions bigOptions = new DisplayImageOptions.Builder().resetViewBeforeLoading(false)
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheInMemory(false)
            .displayer(new FadeInBitmapDisplayer(250))
            .build();
    Activity activity = null;
    boolean     clickHandled;
    Handler     handler;
    MotionEvent event;
    Runnable    longClicked;
    float       position;
    private TextView  title;
    private TextView  info;
    private ImageView backdrop;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        x += getScrollX();
        y += getScrollY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            position = event.getY(); //used to see if the user scrolled or not
        }
        if (!(event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_DOWN)) {
            if (Math.abs((position - event.getY())) > 25) {
                handler.removeCallbacksAndMessages(null);
            }
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clickHandled = false;
                this.event = event;
                if (SettingValues.peek) {
                    handler.postDelayed(longClicked,
                            android.view.ViewConfiguration.getTapTimeout() + 50);
                } else {
                    handler.postDelayed(longClicked,
                            android.view.ViewConfiguration.getTapTimeout() + 250);
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

    public void doImageAndText(final Submission submission, boolean full, String baseSub) {

        boolean fullImage = ContentType.fullImage(type);

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
            case VID_ME:
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

            if (SettingValues.noImages && loadLq) {
                setVisibility(View.GONE);
                if (!full && !submission.isSelfPost()) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    if (full && !submission.isSelfPost()) wrapArea.setVisibility(View.VISIBLE);
                }
                thumbImage2.setImageDrawable(
                        ContextCompat.getDrawable(getContext(), R.drawable.web));
            } else if (submission.isNsfw()
                    && submission.getThumbnailType() == Submission.ThumbnailType.NSFW) {
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
                loadedUrl = submission.getUrl();
            } else if (type == ContentType.Type.IMAGE && !thumbnail.isNull() && !thumbnail.asText()
                    .isEmpty()) {
                if (loadLq
                        && submission.getThumbnails() != null
                        && submission.getThumbnails().getVariations() != null
                        && submission.getThumbnails().getVariations().length > 0) {

                    if (ContentType.isImgurImage(submission.getUrl())) {
                        url = submission.getUrl();
                        url = url.substring(0, url.lastIndexOf(".")) + (SettingValues.imgurLq ? "m"
                                : "h") + url.substring(url.lastIndexOf("."), url.length());
                    } else {
                        int length = submission.getThumbnails().getVariations().length;
                        url = Html.fromHtml(
                                submission.getThumbnails().getVariations()[length / 2].getUrl())
                                .toString(); //unescape url characters
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
                        url = url.substring(0, url.lastIndexOf(".")) + (SettingValues.imgurLq ? "m"
                                : "h") + url.substring(url.lastIndexOf("."), url.length());
                    } else {
                        int length = submission.getThumbnails().getVariations().length;
                        url = Html.fromHtml(
                                submission.getThumbnails().getVariations()[length / 2].getUrl())
                                .toString(); //unescape url characters
                    }
                    lq = true;

                } else {
                    url = Html.fromHtml(submission.getThumbnails().getSource().getUrl())
                            .toString(); //unescape url characters
                }
                if (!SettingValues.isPicsEnabled(baseSub) && !full || forceThumb) {

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
                    .isNull() && submission.getThumbnailType() == Submission.ThumbnailType.NSFW))) {

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
                    setBottomSheet(wrapArea, submission.getUrl());
                } else {
                    title = (TextView) findViewById(R.id.textimage);
                    info = (TextView) findViewById(R.id.subtextimage);
                    if (forceThumb || (submission.isNsfw()
                            && submission.getThumbnailType() == Submission.ThumbnailType.NSFW
                            || type != ContentType.Type.IMAGE
                            && type != ContentType.Type.SELF
                            && !submission.getDataNode().get("thumbnail").isNull()
                            && (submission.getThumbnailType() != Submission.ThumbnailType.URL))) {
                        setBottomSheet(thumbImage2, submission.getUrl());
                    } else {
                        setBottomSheet(this, submission.getUrl());
                    }
                }
            } else {
                title = (TextView) findViewById(R.id.textimage);
                info = (TextView) findViewById(R.id.subtextimage);
                setBottomSheet(thumbImage2, submission.getUrl());
                setBottomSheet(this, submission.getUrl());

            }


            if (SettingValues.smallTag && !full) {
                title = (TextView) findViewById(R.id.tag);
                findViewById(R.id.tag).setVisibility(View.VISIBLE);
                info = null;
            } else {
                findViewById(R.id.tag).setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                info.setVisibility(View.VISIBLE);
            }

            if (SettingValues.smallTag && !full) {
                ((TransparentTagTextView) title).init(getContext());
            }

            title.setText(ContentType.getContentDescription(submission, getContext()));

            if (info != null) info.setText(submission.getDomain());

        }
        /* todo possibly: "3d touch" images
        if (activity == null) {
            Context context = getContext();
            if (context instanceof Activity) {
                activity = (Activity) context;
            } else if (context instanceof android.support.v7.view.ContextThemeWrapper) {
                activity = (Activity) ((android.support.v7.view.ContextThemeWrapper) context).getBaseContext();
            } else if (context instanceof ContextWrapper) {
                Context context1 = ((ContextWrapper) context).getBaseContext();
                if (context1 instanceof Activity) {
                    activity = (Activity) context1;
                } else if (context1 instanceof ContextWrapper) {
                    Context context2 = ((ContextWrapper) context1).getBaseContext();
                    if (context2 instanceof Activity) {
                        activity = (Activity) context2;
                    } else if (context2 instanceof ContextWrapper) {
                        activity = (Activity) ((android.support.v7.view.ContextThemeWrapper) context2).getBaseContext();
                    }
                }
            } else {
                throw new RuntimeException("Could not find activity from context:" + context);
            }
        }

        final PeekAndPop peekAndPop = new PeekAndPop.Builder((activity))
                .peekLayout(R.layout.peek_media)
                .longClickViews(this)
                .cancelIfMove(true)
                .build();

        peekAndPop.addHoldAndReleaseView(R.id.comments);

        peekAndPop.setOnHoldAndReleaseListener(new PeekAndPop.OnHoldAndReleaseListener() {
            @Override
            public void onHold(View view, int i) {

            }

            @Override
            public void onLeave(View view, int i) {

            }

            @Override
            public void onRelease(View view, int i) {
                if(i == R.id.comments){
                    ((View)getParent()).callOnClick();
                }
            }
        });

        peekAndPop.setOnGeneralActionListener(new PeekAndPop.OnGeneralActionListener() {
            @Override
            public void onPeek(View view, int i) {
                new PopMediaView().doPop(peekAndPop.getPeekView(), submission.getUrl(), activity);
            }

            @Override
            public void onPop(View view, int i) {
            }

            @Override
            public void onDismiss(View view, int i){
                ((MediaVideoView) peekAndPop.getPeekView().findViewById(R.id.gif)).setVisibility(GONE);
                ((MediaVideoView) peekAndPop.getPeekView().findViewById(R.id.gif)).setVisibility(VISIBLE);
                ((MediaVideoView) peekAndPop.getPeekView().findViewById(R.id.gif)).stopPlayback();
                (peekAndPop.getPeekView().findViewById(R.id.gifarea)).setVisibility(View.GONE);

            }
        });
*/
    }

    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public double getHeightFromAspectRatio(int imageHeight, int imageWidth) {
        double ratio = (double) imageHeight / (double) imageWidth;
        double width = getWidth();
        return (width * ratio);

    }

    public void onLinkLongClick(final String url, MotionEvent event) {
        if (url == null) {
            return;
        }
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        Activity activity = null;
        final Context context = getContext();
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof android.support.v7.view.ContextThemeWrapper) {
            activity =
                    (Activity) ((android.support.v7.view.ContextThemeWrapper) context).getBaseContext();
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
                            (Activity) ((android.support.v7.view.ContextThemeWrapper) context2).getBaseContext();
                }
            }
        } else {
            throw new RuntimeException("Could not find activity from context:" + context);
        }

        if (activity != null && !activity.isFinishing()) {
            if (SettingValues.peek) {
                Peek.into(R.layout.peek_view, new SimpleOnPeek() {
                    @Override
                    public void onInflated(final PeekView peekView, final View rootView) {
                        //do stuff
                        ((Toolbar) rootView.findViewById(R.id.title)).setTitle(url);
                        ((Toolbar) rootView.findViewById(R.id.title)).setTitleTextColor(Color.WHITE);
                        ((PeekMediaView) rootView.findViewById(R.id.peek)).setUrl(url);

                        peekView.addButton((R.id.copy), new OnButtonUp() {
                            @Override
                            public void onButtonUp() {
                                ClipboardManager clipboard =
                                        (ClipboardManager) rootView.getContext()
                                                .getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Link", url);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(rootView.getContext(),
                                        R.string.submission_link_copied, Toast.LENGTH_SHORT).show();
                            }
                        });

                        peekView.addButton((R.id.share), new OnButtonUp() {
                            @Override
                            public void onButtonUp() {
                                Reddit.defaultShareText("", url, rootView.getContext());
                            }
                        });

                        peekView.addButton((R.id.pop), new OnButtonUp() {
                            @Override
                            public void onButtonUp() {
                                Reddit.defaultShareText("", url, rootView.getContext());
                            }
                        });

                        peekView.addButton((R.id.external), new OnButtonUp() {
                            @Override
                            public void onButtonUp() {
                                LinkUtil.openExternally(url, context, false);
                            }
                        });
                    }
                })
                        .with(new PeekViewOptions().setFullScreenPeek(true))
                        .show((PeekViewActivity) activity, event);
            } else {
                BottomSheet.Builder b = new BottomSheet.Builder(activity).title(url).grid();
                int[] attrs = new int[]{R.attr.tint};
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
                                LinkUtil.openExternally(url, context, false);
                                break;
                            case R.id.share_link:
                                Reddit.defaultShareText("", url, finalActivity);
                                break;
                            case R.id.copy_link:
                                ClipboardManager clipboard =
                                        (ClipboardManager) finalActivity.getSystemService(
                                                Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Link", url);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(finalActivity, R.string.submission_link_copied,
                                        Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }).show();
            }
        }
    }

    public void setBottomSheet(View v, final String url) {
        handler = new Handler();
        longClicked = new Runnable() {
            @Override
            public void run() {
                // long click
                clickHandled = true;

                handler.removeCallbacksAndMessages(null);
                onLinkLongClick(url, event);
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
            doImageAndText(submission, full, baseSub);
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
        this.title = (TextView) findViewById(R.id.textimage);
        this.info = (TextView) findViewById(R.id.subtextimage);
        this.backdrop = (ImageView) findViewById(R.id.leadimage);
    }
}