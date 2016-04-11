package me.ccrama.redditslide.SubmissionViews;

import android.app.Activity;
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
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import net.dean.jraw.models.Submission;

import java.net.URI;
import java.net.URISyntaxException;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.TransparentTagTextView;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by carlo_000 on 2/7/2016.
 */
public class HeaderImageLinkView extends RelativeLayout {
    private TextView title;
    private TextView info;
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

    public double getHeightFromAspectRatio(int imageHeight, int imageWidth) {
        double ratio = (double) imageHeight / (double) imageWidth;
        double width = getWidth();
        return (width * ratio);

    }

    boolean done;


    String lastDone = "";

    public void setSubmission(final Submission submission, final boolean full, String baseSub) {
        if (!lastDone.equals(submission.getFullName())) {
            lastDone = submission.getFullName();
            backdrop.setImageResource(android.R.color.transparent); //reset the image view in case the placeholder is still visible
            thumbImage2.setImageResource(android.R.color.transparent);
            doImageAndText(submission, full, baseSub);
        }
    }

    DisplayImageOptions bigOptions = new DisplayImageOptions.Builder()
            .resetViewBeforeLoading(true)
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheInMemory(false)
            .displayer(new FadeInBitmapDisplayer(250))
            .build();

    public void doImageAndText(Submission submission, boolean full, String baseSub) {

        final ContentType.ImageType type = ContentType.getImageType(submission);

        setVisibility(View.VISIBLE);
        String url = "";
        boolean forceThumb = false;

        if (type == ContentType.ImageType.SELF && full && SettingValues.hideSelftextLeadImage) {
            setVisibility(View.GONE);
            wrapArea.setVisibility(View.GONE);
        } else {
            if (submission.getThumbnails() != null) {

                int height = submission.getThumbnails().getSource().getHeight();
                int width = submission.getThumbnails().getSource().getWidth();

                if (full) {
                    if (height < dpToPx(50) && type != ContentType.ImageType.SELF) {
                        forceThumb = true;
                    } else if (SettingValues.cropImage) {
                        backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(200)));
                    } else {
                        double h = getHeightFromAspectRatio(height, width);
                        if (h != 0) {
                            if (h > 3500) {
                                backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 3500));
                            } else {
                                backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) h));
                            }
                        } else {
                            if (height > 3500) {
                                backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 3500));
                            } else {
                                backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                            }
                        }

                    }
                } else if (SettingValues.bigPicCropped) {
                    if (height < dpToPx(50)) {
                        forceThumb = true;
                    } else {
                        backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(200)));
                    }
                } else if (height >= dpToPx(50)) {
                    double h = getHeightFromAspectRatio(height, width);
                    if (h != 0) {
                        if (h > 3500) {
                            backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 3500));
                        } else {
                            backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) h));
                        }
                    } else {
                        if (height > 3500) {
                            backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 3500));
                        } else {
                            backdrop.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        }
                    }
                } else {
                    forceThumb = true;
                }

            }

            if (submission.isNsfw() && submission.getThumbnailType() == Submission.ThumbnailType.NSFW) {
                setVisibility(View.GONE);
                if (!full || forceThumb) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    wrapArea.setVisibility(View.VISIBLE);
                }
                if (submission.isSelfPost()) thumbImage2.setVisibility(View.GONE);
                else {
                    thumbImage2.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.nsfw));

                }
            } else if (type != ContentType.ImageType.IMAGE && type != ContentType.ImageType.SELF && (!submission.getDataNode().get("thumbnail").isNull() && (submission.getThumbnailType() != Submission.ThumbnailType.URL))) {

                setVisibility(View.GONE);
                if (!full) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    wrapArea.setVisibility(View.VISIBLE);
                }

                thumbImage2.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.web));
            } else if (type == ContentType.ImageType.IMAGE) {
                if (((!NetworkUtil.isConnectedWifi(getContext()) && SettingValues.lowResMobile) || SettingValues.lowResAlways) && submission.getThumbnails() != null && submission.getThumbnails().getVariations() != null) {

                    int length = submission.getThumbnails().getVariations().length;
                    url = Html.fromHtml(submission.getThumbnails().getVariations()[length / 2].getUrl()).toString(); //unescape url characters

                } else {
                    if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                        url = submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
                    } else {
                        url = submission.getUrl();
                    }
                }

                if (!full && !SettingValues.isPicsEnabled(baseSub) || forceThumb) {

                    if (!full) {
                        thumbImage2.setVisibility(View.VISIBLE);
                    } else {
                        wrapArea.setVisibility(View.VISIBLE);
                    }

                    if (!full) {
                        ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, thumbImage2);
                    } else {
                        ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, thumbImage2, bigOptions);
                    }
                    setVisibility(View.GONE);

                } else {

                    if (!full) {
                        ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, backdrop);
                    } else {
                        ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, backdrop, bigOptions);
                    }
                    setVisibility(View.VISIBLE);
                    if (!full) {
                        thumbImage2.setVisibility(View.GONE);
                    } else {
                        wrapArea.setVisibility(View.GONE);
                    }
                }
            } else if (submission.getThumbnails() != null) {

                if (((!NetworkUtil.isConnectedWifi(getContext()) && SettingValues.lowResMobile) || SettingValues.lowResAlways) && submission.getThumbnails().getVariations().length != 0) {

                    int length = submission.getThumbnails().getVariations().length;
                    url = Html.fromHtml(submission.getThumbnails().getVariations()[length / 2].getUrl()).toString(); //unescape url characters

                } else {
                    url = Html.fromHtml(submission.getThumbnails().getSource().getUrl()).toString(); //unescape url characters
                }
                if (!SettingValues.isPicsEnabled(baseSub) && !full || forceThumb) {

                    if (!full) {
                        thumbImage2.setVisibility(View.VISIBLE);
                    } else {
                        wrapArea.setVisibility(View.VISIBLE);
                    }
                    ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, thumbImage2);
                    setVisibility(View.GONE);

                } else {

                    if (!full) {
                        ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, backdrop);
                    } else {
                        ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, backdrop, bigOptions);
                    }
                    setVisibility(View.VISIBLE);
                    if (!full) {
                        thumbImage2.setVisibility(View.GONE);
                    } else {
                        wrapArea.setVisibility(View.GONE);
                    }
                }
            } else if (!submission.getDataNode().get("thumbnail").isNull() && submission.getThumbnail() != null && (submission.getThumbnailType() == Submission.ThumbnailType.URL || (!submission.getDataNode().get("thumbnail").isNull() && submission.getThumbnailType() == Submission.ThumbnailType.NSFW))) {

                if (!full) {
                    thumbImage2.setVisibility(View.VISIBLE);
                } else {
                    wrapArea.setVisibility(View.VISIBLE);
                }
                ((Reddit) getContext().getApplicationContext()).getImageLoader().displayImage(url, thumbImage2);
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
                    if (forceThumb
                            || (submission.isNsfw() && submission.getThumbnailType() == Submission.ThumbnailType.NSFW || type != ContentType.ImageType.IMAGE && type != ContentType.ImageType.SELF && !submission.getDataNode().get("thumbnail").isNull() && (submission.getThumbnailType() != Submission.ThumbnailType.URL))) {
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

            switch (type) {
                case NSFW_IMAGE:
                    title.setText(R.string.type_nsfw_img);
                    break;

                case DEVIANTART:
                    title.setText(R.string.type_deviantart);
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
                    String domain = submission.getDomain();
                    if (domain.contains("youtube") || domain.contains("youtu.be"))
                        title.setText(R.string.type_vid);
                    else
                        title.setText(R.string.type_link);
                    break;

                case NSFW_LINK:
                    title.setText(R.string.type_nsfw_link);

                    break;
                case STREAMABLE:
                    title.setText(R.string.type_streamable);
                    break;
                case SELF:
                    title.setVisibility(View.VISIBLE);
                    title.setText(R.string.type_selftext);
                    break;

                case ALBUM:
                    title.setText(R.string.type_album);
                    break;

                case IMAGE:
                    if (submission.isNsfw()) {
                        title.setText(R.string.type_nsfw_img);

                    } else {
                        title.setVisibility(View.GONE);
                        if (info != null)
                            info.setVisibility(View.GONE);
                    }
                    break;
                case IMGUR:
                    title.setText(R.string.type_imgur);
                    break;
                case VID_ME:
                    title.setText(R.string.type_vidme);
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

                if (info != null)
                    info.setText(submission.getDomain());

        }
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

    public ImageView thumbImage2;

    public void setThumbnail(ImageView v) {
        thumbImage2 = v;
    }

    public TextView secondTitle;
    public TextView secondSubTitle;
    public View wrapArea;

    public void setWrapArea(View v) {
        wrapArea = v;
        setSecondTitle((TextView) v.findViewById(R.id.contenttitle));
        setSecondSubtitle((TextView) v.findViewById(R.id.contenturl));

    }

    Activity activity = null;

    public void setBottomSheet(View v, final String url) {
        v.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (getContext() instanceof Activity) {
                    activity = (Activity) getContext();
                } else if (getContext() instanceof android.support.v7.view.ContextThemeWrapper) {
                    activity = (Activity) ((android.support.v7.view.ContextThemeWrapper) getContext()).getBaseContext();
                }
                if (activity != null) {
                    BottomSheet.Builder b = new BottomSheet.Builder(activity)
                            .title(url)
                            .grid();
                    int[] attrs = new int[]{R.attr.tint};
                    TypedArray ta = getContext().obtainStyledAttributes(attrs);

                    int color = ta.getColor(0, Color.WHITE);
                    Drawable open = getResources().getDrawable(R.drawable.ic_open_in_browser);
                    open.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    Drawable share = getResources().getDrawable(R.drawable.ic_share);
                    share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    Drawable copy = getResources().getDrawable(R.drawable.ic_content_copy);
                    copy.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

                    b.sheet(R.id.open_link, open, getResources().getString(R.string.submission_link_extern));
                    b.sheet(R.id.share_link, share, getResources().getString(R.string.share_link));
                    b.sheet(R.id.copy_link, copy, getResources().getString(R.string.submission_link_copy));

                    b.listener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case R.id.open_link:
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    getContext().startActivity(browserIntent);
                                    break;
                                case R.id.share_link:
                                    Reddit.defaultShareText(url, activity);
                                    break;
                                case R.id.copy_link:
                                    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Link", url);
                                    clipboard.setPrimaryClip(clip);

                                    Toast.makeText(activity, "Link copied", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }).show();
                    return true;
                }
                return false;
            }
        });
    }

    public void setSecondTitle(TextView v) {
        secondTitle = v;
    }

    public void setSecondSubtitle(TextView v) {
        secondSubTitle = v;
    }

    public void setUrl(String url) {

    }

    private void init() {
        inflate(getContext(), R.layout.header_image_title_view, this);
        this.title = (TextView) findViewById(R.id.textimage);
        this.info = (TextView) findViewById(R.id.subtextimage);
        this.backdrop = (ImageView) findViewById(R.id.leadimage);
    }

    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
