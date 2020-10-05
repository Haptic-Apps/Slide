package me.ccrama.redditslide;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.cocosw.bottomsheet.BottomSheet;
import com.devspark.robototextview.widget.RobotoTextView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.TumblrPager;
import me.ccrama.redditslide.ForceTouch.PeekView;
import me.ccrama.redditslide.ForceTouch.PeekViewActivity;
import me.ccrama.redditslide.ForceTouch.builder.Peek;
import me.ccrama.redditslide.ForceTouch.builder.PeekViewOptions;
import me.ccrama.redditslide.ForceTouch.callback.OnButtonUp;
import me.ccrama.redditslide.ForceTouch.callback.OnPop;
import me.ccrama.redditslide.ForceTouch.callback.OnRemove;
import me.ccrama.redditslide.ForceTouch.callback.SimpleOnPeek;
import me.ccrama.redditslide.SubmissionViews.OpenVRedditTask;
import me.ccrama.redditslide.Views.CustomQuoteSpan;
import me.ccrama.redditslide.Views.PeekMediaView;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.TextViewLinkHandler;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by carlo_000 on 1/11/2016.
 */
public class SpoilerRobotoTextView extends RobotoTextView implements ClickableText {
    private              List<CharacterStyle> storedSpoilerSpans  = new ArrayList<>();
    private              List<Integer>        storedSpoilerStarts = new ArrayList<>();
    private              List<Integer>        storedSpoilerEnds   = new ArrayList<>();
    private static final Pattern              htmlSpoilerPattern  =
            Pattern.compile("<a href=\"[#/](?:spoiler|sp|s)\">([^<]*)</a>");
    private static final Pattern nativeSpoilerPattern =
            Pattern.compile("<span class=\"[^\"]*md-spoiler-text+[^\"]*\">([^<]*)</span>");

    public SpoilerRobotoTextView(Context context) {
        super(context);
        setLineSpacing(0, 1.1f);
    }

    public SpoilerRobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLineSpacing(0, 1.1f);
    }

    public SpoilerRobotoTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLineSpacing(0, 1.1f);
    }

    public boolean isSpoilerClicked() {
        return spoilerClicked;
    }

    public void resetSpoilerClicked() {
        spoilerClicked = false;
    }

    public boolean spoilerClicked = false;

    private static SpannableStringBuilder removeNewlines(SpannableStringBuilder s) {
        int start = 0;
        int end = s.length();
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return (SpannableStringBuilder) s.subSequence(start, end);
    }

    /**
     * Set the text from html. Handles formatting spoilers, links etc. <p/> The text must be valid
     * html.
     *
     * @param text html text
     */
    public void setTextHtml(CharSequence text) {
        setTextHtml(text, "");
    }

    /**
     * Set the text from html. Handles formatting spoilers, links etc. <p/> The text must be valid
     * html.
     *
     * @param baseText  html text
     * @param subreddit the subreddit to theme
     */
    public void setTextHtml(CharSequence baseText, String subreddit) {
        String text = wrapAlternateSpoilers(saveEmotesFromDestruction(baseText.toString().trim()));
        SpannableStringBuilder builder = (SpannableStringBuilder) HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY);

        replaceQuoteSpans(
                builder); //replace the <blockquote> blue line with something more colorful

        if (text.contains("<a")) {
            setEmoteSpans(builder); //for emote enabled subreddits
        }
        if (text.contains("[")) {
            setCodeFont(builder);
            setSpoilerStyle(builder, subreddit);
        }
        if (text.contains("[[d[")) {
            setStrikethrough(builder);
        }
        if (text.contains("[[h[")) {
            setHighlight(builder, subreddit);
        }
        if (subreddit != null && !subreddit.isEmpty()) {
            setMovementMethod(new TextViewLinkHandler(this, subreddit, builder));
            setFocusable(false);
            setClickable(false);
            if (subreddit.equals("FORCE_LINK_CLICK")) {
                setLongClickable(false);
            }

        }

        builder = removeNewlines(builder);

        builder.append(" ");

        super.setText(builder, BufferType.SPANNABLE);
    }


    /**
     * Replaces the blue line produced by <blockquote>s with something more visible
     *
     * @param spannable parsed comment text #fromHtml
     */
    private void replaceQuoteSpans(Spannable spannable) {
        QuoteSpan[] quoteSpans = spannable.getSpans(0, spannable.length(), QuoteSpan.class);

        for (QuoteSpan quoteSpan : quoteSpans) {
            final int start = spannable.getSpanStart(quoteSpan);
            final int end = spannable.getSpanEnd(quoteSpan);
            final int flags = spannable.getSpanFlags(quoteSpan);

            spannable.removeSpan(quoteSpan);

            //If the theme is Light or Sepia, use a darker blue; otherwise, use a lighter blue
            final int barColor = ContextCompat.getColor(getContext(),
                    SettingValues.currentTheme == 1 || SettingValues.currentTheme == 5
                            ? R.color.md_blue_600 : R.color.md_blue_400);

            final int BAR_WIDTH = 4;
            final int GAP = 5;

            spannable.setSpan(new CustomQuoteSpan(Color.TRANSPARENT, //background color
                            barColor, //bar color
                            BAR_WIDTH, //bar width
                            GAP), //bar + text gap
                    start, end, flags);
        }
    }

    private String wrapAlternateSpoilers(String html) {
        String replacement = "<a href=\"/spoiler\">spoiler&lt; [[s[$1]s]]</a>";

        html = htmlSpoilerPattern.matcher(html).replaceAll(replacement);
        html = nativeSpoilerPattern.matcher(html).replaceAll(replacement);
        return html;
    }

    private String saveEmotesFromDestruction(String html) {
        //Emotes often have no spoiler caption, and therefore are converted to empty anchors. Html.fromHtml removes anchors with zero length node text. Find zero length anchors that start with "/" and add "." to them.
        Pattern htmlEmotePattern = Pattern.compile("<a href=\"/.*\"></a>");
        Matcher htmlEmoteMatcher = htmlEmotePattern.matcher(html);
        while (htmlEmoteMatcher.find()) {
            String newPiece = htmlEmoteMatcher.group();
            //Ignore empty tags marked with sp.
            if (!htmlEmoteMatcher.group().contains("href=\"/sp\"")) {
                newPiece = newPiece.replace("></a", ">.</a");
                html = html.replace(htmlEmoteMatcher.group(), newPiece);
            }
        }
        return html;
    }

    private void setEmoteSpans(SpannableStringBuilder builder) {
        for (URLSpan span : builder.getSpans(0, builder.length(), URLSpan.class)) {
            if (SettingValues.typeInText) {
                setLinkTypes(builder, span);
            }
            if (SettingValues.largeLinks) {
                setLargeLinks(builder, span);
            }
            File emoteDir = new File(Environment.getExternalStorageDirectory(), "RedditEmotes");
            File emoteFile = new File(emoteDir, span.getURL().replace("/", "").replaceAll("-.*", "")
                    + ".png"); //BPM uses "-" to add dynamics for emotes in browser. Fall back to original here if exists.
            boolean startsWithSlash = span.getURL().startsWith("/");
            boolean hasOnlyOneSlash = StringUtils.countMatches(span.getURL(), "/") == 1;

            if (emoteDir.exists() && startsWithSlash && hasOnlyOneSlash && emoteFile.exists()) {
                //We've got an emote match
                int start = builder.getSpanStart(span);
                int end = builder.getSpanEnd(span);
                CharSequence textCovers = builder.subSequence(start, end);

                //Make sure bitmap loaded works well with screen density.
                BitmapFactory.Options options = new BitmapFactory.Options();
                DisplayMetrics metrics = new DisplayMetrics();
                ContextCompat.getSystemService(getContext(), WindowManager.class).getDefaultDisplay().getMetrics(metrics);
                options.inDensity = 240;
                options.inScreenDensity = metrics.densityDpi;
                options.inScaled = true;

                //Since emotes are not directly attached to included text, add extra character to attach image to.
                builder.removeSpan(span);
                if (builder.subSequence(start, end).charAt(0) != '.') {
                    builder.insert(start, ".");
                }
                Bitmap emoteBitmap = BitmapFactory.decodeFile(emoteFile.getAbsolutePath(), options);
                builder.setSpan(new ImageSpan(getContext(), emoteBitmap), start, start + 1,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                //Check if url span has length. If it does, it's a spoiler/caption
                if (textCovers.length() > 1) {
                    builder.setSpan(new URLSpan("/sp"), start + 1, end + 1,
                            Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    builder.setSpan(new StyleSpan(Typeface.ITALIC), start + 1, end + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                builder.append("\n"); //Newline to fix text wrapping issues
            }
        }
    }

    private void setLinkTypes(SpannableStringBuilder builder, URLSpan span) {
        String url = span.getURL();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        String text = builder.subSequence(builder.getSpanStart(span), builder.getSpanEnd(span))
                .toString();
        if (!text.equalsIgnoreCase(url)) {
            ContentType.Type contentType = ContentType.getContentType(url);
            String bod;
            try {
                bod = " (" + ((url.contains("/") && url.startsWith("/") && !(url.split("/").length
                        > 2)) ? url
                        : (getContext().getString(ContentType.getContentID(contentType, false)) + (
                                contentType == ContentType.Type.LINK ? " " + Uri.parse(url)
                                        .getHost() : ""))) + ")";
            } catch (Exception e) {
                bod = " ("
                        + getContext().getString(ContentType.getContentID(contentType, false))
                        + ")";
            }
            SpannableStringBuilder b = new SpannableStringBuilder(bod);
            b.setSpan(new StyleSpan(Typeface.BOLD), 0, b.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            b.setSpan(new RelativeSizeSpan(0.8f), 0, b.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.insert(builder.getSpanEnd(span), b);
        }
    }

    private void setLargeLinks(SpannableStringBuilder builder, URLSpan span) {
        builder.setSpan(new RelativeSizeSpan(1.3f), builder.getSpanStart(span),
                builder.getSpanEnd(span), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void setStrikethrough(SpannableStringBuilder builder) {
        final int offset = "[[d[".length(); // == "]d]]".length()

        int start = -1;
        int end;

        for (int i = 0; i < builder.length() - 3; i++) {
            if (builder.charAt(i) == '['
                    && builder.charAt(i + 1) == '['
                    && builder.charAt(i + 2) == 'd'
                    && builder.charAt(i + 3) == '[') {
                start = i + offset;
            } else if (builder.charAt(i) == ']'
                    && builder.charAt(i + 1) == 'd'
                    && builder.charAt(i + 2) == ']'
                    && builder.charAt(i + 3) == ']') {
                end = i;
                builder.setSpan(new StrikethroughSpan(), start, end,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                builder.delete(end, end + offset);
                builder.delete(start - offset, start);
                i -= offset + (end - start); // length of text
            }
        }
    }

    private void setHighlight(SpannableStringBuilder builder, String subreddit) {
        final int offset = "[[h[".length(); // == "]h]]".length()

        int start = -1;
        int end;
        for (int i = 0; i < builder.length() - 4; i++) {
            if (builder.charAt(i) == '['
                    && builder.charAt(i + 1) == '['
                    && builder.charAt(i + 2) == 'h'
                    && builder.charAt(i + 3) == '[') {
                start = i + offset;
            } else if (builder.charAt(i) == ']'
                    && builder.charAt(i + 1) == 'h'
                    && builder.charAt(i + 2) == ']'
                    && builder.charAt(i + 3) == ']') {
                end = i;
                builder.setSpan(new BackgroundColorSpan(Palette.getColor(subreddit)), start, end,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                builder.delete(end, end + offset);
                builder.delete(start - offset, start);
                i -= offset + (end - start); // length of text
            }
        }
    }

    @Override
    public void onLinkClick(String url, int xOffset, String subreddit, URLSpan span) {
        if (url == null) {
            ((View) getParent()).callOnClick();
            return;
        }

        ContentType.Type type = ContentType.getContentType(url);
        Context context = getContext();
        Activity activity = null;
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

        if (!PostMatch.openExternal(url) || type == ContentType.Type.VIDEO) {
            switch (type) {
                case DEVIANTART:
                case IMGUR:
                case XKCD:
                    if (SettingValues.image) {
                        Intent intent2 = new Intent(activity, MediaView.class);
                        intent2.putExtra(MediaView.EXTRA_URL, url);
                        intent2.putExtra(MediaView.SUBREDDIT, subreddit);
                        activity.startActivity(intent2);
                    } else {
                        LinkUtil.openExternally(url);
                    }
                    break;
                case REDDIT:
                    new OpenRedditLink(activity, url);
                    break;
                case LINK:
                    LogUtil.v("Opening link");
                    LinkUtil.openUrl(url, Palette.getColor(subreddit), activity);
                    break;
                case SELF:
                case NONE:
                    break;
                case STREAMABLE:
                    openStreamable(url, subreddit);
                    break;
                case ALBUM:
                    if (SettingValues.album) {
                        if (SettingValues.albumSwipe) {
                            Intent i = new Intent(activity, AlbumPager.class);
                            i.putExtra(Album.EXTRA_URL, url);
                            i.putExtra(AlbumPager.SUBREDDIT, subreddit);
                            activity.startActivity(i);
                        } else {
                            Intent i = new Intent(activity, Album.class);
                            i.putExtra(Album.SUBREDDIT, subreddit);
                            i.putExtra(Album.EXTRA_URL, url);
                            activity.startActivity(i);
                        }
                    } else {
                        LinkUtil.openExternally(url);
                    }
                    break;
                case TUMBLR:
                    if (SettingValues.image) {
                        if (SettingValues.albumSwipe) {
                            Intent i = new Intent(activity, TumblrPager.class);
                            i.putExtra(Album.EXTRA_URL, url);
                            activity.startActivity(i);
                        } else {
                            Intent i = new Intent(activity, TumblrPager.class);
                            i.putExtra(Album.EXTRA_URL, url);
                            activity.startActivity(i);
                        }
                    } else {
                        LinkUtil.openExternally(url);
                    }
                    break;
                case IMAGE:
                    openImage(url, subreddit);
                    break;
                case VREDDIT_REDIRECT:
                    openVReddit(url, subreddit, activity);
                    break;
                case GIF:
                case VREDDIT_DIRECT:
                    openGif(url, subreddit, activity);
                    break;
                case VIDEO:
                    if (!LinkUtil.tryOpenWithVideoPlugin(url)) {
                        LinkUtil.openUrl(url, Palette.getStatusBarColor(), activity);
                    }
                case SPOILER:
                    spoilerClicked = true;
                    setOrRemoveSpoilerSpans(xOffset, span);
                    break;
                case EXTERNAL:
                    LinkUtil.openExternally(url);
                    break;
            }
        } else {
            LinkUtil.openExternally(url);
        }
    }

    @Override
    public void onLinkLongClick(final String baseUrl, MotionEvent event) {
        if (baseUrl == null) {
            return;
        }
        final String url = StringEscapeUtils.unescapeHtml4(baseUrl);

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
                Peek.into(R.layout.peek_view, new SimpleOnPeek() {
                    @Override
                    public void onInflated(final PeekView peekView, final View rootView) {
                        //do stuff
                        TextView text = rootView.findViewById(R.id.title);
                        text.setText(url);
                        text.setTextColor(Color.WHITE);
                        ((PeekMediaView) rootView.findViewById(R.id.peek)).setUrl(url);

                        peekView.addButton((R.id.copy), new OnButtonUp() {
                            @Override
                            public void onButtonUp() {
                                ClipboardManager clipboard =
                                        ContextCompat.getSystemService(rootView.getContext(), ClipboardManager.class);
                                ClipData clip = ClipData.newPlainText("Link", url);
                                if (clipboard != null) {
                                    clipboard.setPrimaryClip(clip);
                                }
                                Toast.makeText(rootView.getContext(),
                                        R.string.submission_link_copied, Toast.LENGTH_SHORT).show();
                            }
                        });

                        peekView.setOnRemoveListener(new OnRemove() {
                            @Override
                            public void onRemove() {
                                ((PeekMediaView) rootView.findViewById(R.id.peek)).doClose();
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
                                LinkUtil.openExternally(url);
                            }
                        });
                        peekView.setOnPop(new OnPop() {
                            @Override
                            public void onPop() {
                                onLinkClick(url, 0, "", null);
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
                Drawable open = getResources().getDrawable(R.drawable.open_in_browser);
                open.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
                Drawable share = getResources().getDrawable(R.drawable.share);
                share.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
                Drawable copy = getResources().getDrawable(R.drawable.copy);
                copy.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));

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

    private void openVReddit(String url, String subreddit, Activity activity) {
        new OpenVRedditTask(activity, subreddit).executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR, url);
    }


    private void openGif(String url, String subreddit, Activity activity) {
        if (SettingValues.gif) {
            if(GifUtils.AsyncLoadGif.getVideoType(url).shouldLoadPreview()){
                LinkUtil.openUrl(url, Palette.getColor(subreddit), activity);
            } else {
                Intent myIntent = new Intent(getContext(), MediaView.class);
                myIntent.putExtra(MediaView.EXTRA_URL, url);
                myIntent.putExtra(MediaView.SUBREDDIT, subreddit);
                getContext().startActivity(myIntent);
            }
        } else {
            LinkUtil.openExternally(url);
        }
    }

    private void openStreamable(String url, String subreddit) {
        if (SettingValues.video) { //todo maybe streamable here?
            Intent myIntent = new Intent(getContext(), MediaView.class);

            myIntent.putExtra(MediaView.EXTRA_URL, url);
            myIntent.putExtra(MediaView.SUBREDDIT, subreddit);
            getContext().startActivity(myIntent);

        } else {
            LinkUtil.openExternally(url);
        }
    }

    private void openImage(String submission, String subreddit) {
        if (SettingValues.image) {
            Intent myIntent = new Intent(getContext(), MediaView.class);
            myIntent.putExtra(MediaView.EXTRA_URL, submission);
            myIntent.putExtra(MediaView.SUBREDDIT, subreddit);
            getContext().startActivity(myIntent);
        } else {
            LinkUtil.openExternally(submission);
        }

    }

    public void setOrRemoveSpoilerSpans(int endOfLink, URLSpan span) {
        if (span != null) {
            int offset = (span.getURL().contains("hidden")) ? -1 : 2;
            Spannable text = (Spannable) getText();
            // add 2 to end of link since there is a white space between the link text and the spoiler
            ForegroundColorSpan[] foregroundColors =
                    text.getSpans(endOfLink + offset, endOfLink + offset,
                            ForegroundColorSpan.class);

            if (foregroundColors.length > 1) {
                text.removeSpan(foregroundColors[1]);
            } else {
                for (int i = 1; i < storedSpoilerStarts.size(); i++) {
                    if (storedSpoilerStarts.get(i) < endOfLink + offset
                            && storedSpoilerEnds.get(i) > endOfLink + offset) {
                        try {
                            text.setSpan(storedSpoilerSpans.get(i), storedSpoilerStarts.get(i),
                                    storedSpoilerEnds.get(i) > text.toString().length() ?
                                            storedSpoilerEnds.get(i)
                                                    + offset : storedSpoilerEnds.get(i),
                                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        } catch (Exception ignored) {
                            //catch out of bounds
                            ignored.printStackTrace();
                        }
                    }
                }
            }
            setText(text);
        }
    }

    /**
     * Set the necessary spans for each spoiler. <p/> The algorithm works in the same way as
     * <code>setCodeFont</code>.
     *
     * @param sequence
     * @return
     */
    private CharSequence setSpoilerStyle(SpannableStringBuilder sequence, String subreddit) {
        int start = 0;
        int end = 0;
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == '[' && i < sequence.length() - 3) {
                if (sequence.charAt(i + 1) == '['
                        && sequence.charAt(i + 2) == 's'
                        && sequence.charAt(i + 3) == '[') {
                    start = i;
                }
            } else if (sequence.charAt(i) == ']' && i < sequence.length() - 3) {
                if (sequence.charAt(i + 1) == 's'
                        && sequence.charAt(i + 2) == ']'
                        && sequence.charAt(i + 3) == ']') {
                    end = i;
                }
            }

            if (end > start) {
                sequence.delete(end, end + 4);
                sequence.delete(start, start + 4);

                BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(
                        Palette.getDarkerColor(Palette.getColor(subreddit)));
                ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(
                        Palette.getDarkerColor(Palette.getColor(subreddit)));
                ForegroundColorSpan underneathColorSpan = new ForegroundColorSpan(Color.WHITE);

                URLSpan urlSpan = sequence.getSpans(start, start, URLSpan.class)[0];
                sequence.setSpan(urlSpan, sequence.getSpanStart(urlSpan), start - 1,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);


                sequence.setSpan(new URLSpanNoUnderline("#spoilerhidden"), start, end - 4,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                // spoiler text has a space at the front
                sequence.setSpan(backgroundColorSpan, start, end - 4,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                sequence.setSpan(underneathColorSpan, start, end - 4,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                sequence.setSpan(foregroundColorSpan, start, end - 4,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                storedSpoilerSpans.add(underneathColorSpan);
                storedSpoilerSpans.add(foregroundColorSpan);
                storedSpoilerSpans.add(backgroundColorSpan);
                // Shift 1 to account for remove of beginning "<"

                storedSpoilerStarts.add(start - 1);
                storedSpoilerStarts.add(start - 1);
                storedSpoilerStarts.add(start - 1);
                storedSpoilerEnds.add(end - 5);
                storedSpoilerEnds.add(end - 5);
                storedSpoilerEnds.add(end - 5);

                sequence.delete(start - 2, start - 1); // remove the trailing <
                start = 0;
                end = 0;
                i = i - 5; // move back to compensate for removal of [[s[
            }
        }

        return sequence;
    }

    private static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }

    /**
     * Sets the styling for string with code segments. <p/> The general process is to search for
     * <code>[[&lt;[</code> and <code>]&gt;]]</code> tokens to find the code fragments within the
     * escaped text. A <code>Spannable</code> is created which which breaks up the origin sequence
     * into non-code and code fragments, and applies a monospace font to the code fragments.
     *
     * @param sequence the Spannable generated from Html.fromHtml
     * @return the message with monospace font applied to code fragments
     */
    private SpannableStringBuilder setCodeFont(SpannableStringBuilder sequence) {
        int start = 0;
        int end = 0;
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == '[' && i < sequence.length() - 3) {
                if (sequence.charAt(i + 1) == '['
                        && sequence.charAt(i + 2) == '<'
                        && sequence.charAt(i + 3) == '[') {
                    start = i;
                }
            } else if (sequence.charAt(i) == ']' && i < sequence.length() - 3) {
                if (sequence.charAt(i + 1) == '>'
                        && sequence.charAt(i + 2) == ']'
                        && sequence.charAt(i + 3) == ']') {
                    end = i;
                }
            }

            if (end > start) {
                sequence.delete(end, end + 4);
                sequence.delete(start, start + 4);
                sequence.setSpan(new TypefaceSpan("monospace"), start, end - 4,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                start = 0;
                end = 0;
                i = i - 4; // move back to compensate for removal of [[<[
            }
        }

        return sequence;
    }


}
