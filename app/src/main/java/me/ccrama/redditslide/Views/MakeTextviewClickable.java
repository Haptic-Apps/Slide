package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by ccrama on 7/17/2015.
 */
public class MakeTextviewClickable {

    private static final String SPOILER_TEXT = "spoiler";
    private static final int SPOILER_TEXT_LENGTH = SPOILER_TEXT.length();
    private Context c;
    private List<CharacterStyle> storedSpoilerSpans = new ArrayList<>();
    private List<Integer> storedSpoilerStarts = new ArrayList<>();
    private List<Integer> storedSpoilerEnds = new ArrayList<>();

    private static void openImage(Activity contextActivity, String submission) {
        if (Reddit.image) {
            Intent myIntent = new Intent(contextActivity, FullscreenImage.class);
            myIntent.putExtra("url", submission);
            contextActivity.startActivity(myIntent);
        } else {
            Reddit.defaultShare(submission, contextActivity);
        }

    }

    private static void openGif(final boolean gfy, Activity contextActivity, String submission) {
        if (Reddit.gif) {
            Intent myIntent = new Intent(contextActivity, GifView.class);
            if (gfy) {
                myIntent.putExtra("url", "gfy" + submission);
            } else {
                myIntent.putExtra("url", "" + submission);

            }
            contextActivity.startActivity(myIntent);
            contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
        } else {
            Reddit.defaultShare(submission, contextActivity);
        }

    }

    public static String trimTrailingWhitespace(String source) {
        if (source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i + 1).toString();
    }

    private static String noTrailingwhiteLines(String text) {
        while (text.charAt(text.length() - 1) == '\n') {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    private static CharSequence trim(CharSequence s) {
        int start = 0;
        int end = s.length();
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    public String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span, final Activity c) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        final ClickableSpan clickable = new ClickableSpan() {

            public void onClick(View view) {
                //TODO make clickable ContentOpen.openingText(url, true, c);


            }
        };

        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    /**
     * For code within <code>&lt;pre&gt;</code> tags, line breaks are converted to
     * <code>&lt;br /&gt;</code> tags, and spaces to &amp;nbsp;. This allows for Html.fromHtml
     * to preserve indents of these blocks.
     *
     * In addition, <code>[[&lt;[</code> and <code>]&gt;]]</code> are inserted to denote the
     * beginning and end of code segments, for styling later.
     * @param html  the unparsed HTML
     * @return  the code parsed HTML with additional markers
     */
    private String parseCodeTags(String html) {
        String unparsed = html;
        String code;
        while (unparsed.contains("<pre><code>")) {
            String[] split = html.split("<pre><code>");
            int closeIndex = split[1].indexOf("</code></pre>");

            unparsed = split[1].substring(closeIndex);

            code = split[1].substring(0, closeIndex);
            code = code.replace("\n", "<br/>");
            code = code.replace(" ", "&nbsp;");
            html = split[0] + "<lpre><lcode>" + code + "" + unparsed;
        }
        html = html.replace("<lpre><lcode>", "<pre><code>");
        html = html.replace("<code>", "<code>[[&lt;[");
        html = html.replace("</code>", "]&gt;]]</code>");

        return html;
    }

    /**
     * Sets the styling for string with code segments.
     *
     * The general process is to search for <code>[[&lt;[</code> and <code>]&gt;]]</code> tokens to
     * find the code fragments within the escaped text. A <code>Spannable</code> is created which
     * which breaks up the origin sequence into non-code and code fragments, and applies a monospace
     * font to the code fragments.
     * @param sequence the Spannable generated from Html.fromHtml
     * @return  the message with monospace font applied to code fragments
     */
    private CharSequence setCodeFont(SpannableStringBuilder sequence) {
        int start = 0;
        int end = 0;
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == '[' && i < sequence.length() - 3) {
                if (sequence.charAt(i + 1) == '[' && sequence.charAt(i + 2) == '<' && sequence.charAt(i + 3) == '[') {
                    start = i;
                }
            } else if (sequence.charAt(i) == ']' && i < sequence.length() - 3) {
                if (sequence.charAt(i + 1) == '>' && sequence.charAt(i + 2) == ']' && sequence.charAt(i + 3) == ']') {
                    end = i;
                }
            }

            if (end > start) {
                sequence.delete(end, end + 4);
                sequence.delete(start, start + 4);
                sequence.setSpan(new TypefaceSpan("monospace"), start, end - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                start = 0;
                end = 0;
                i = i - 4; // move back to compensate for removal of [[<[
            }
        }

        return sequence;
    }

    /**
     * Set the necessary spans for each spoiler.
     *
     * The algorithm works in the same way as <code>setCodeFont</code>.
     * @param sequence
     * @return
     */
    private CharSequence setSpoilerStyle(SpannableStringBuilder sequence) {
        int start = 0;
        int end = 0;
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == '[' && i < sequence.length() - 3) {
                if (sequence.charAt(i + 1) == '[' && sequence.charAt(i + 2) == 's' && sequence.charAt(i + 3) == '[') {
                    start = i;
                }
            } else if (sequence.charAt(i) == ']' && i < sequence.length() - 3) {
                if (sequence.charAt(i + 1) == 's' && sequence.charAt(i + 2) == ']' && sequence.charAt(i + 3) == ']') {
                    end = i;
                }
            }

            if (end > start) {
                sequence.delete(end, end + 4);
                sequence.delete(start, start + 4);
                BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(Color.BLACK);
                ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.BLACK);

                URLSpan urlSpan = sequence.getSpans(start, start, URLSpan.class)[0];
                sequence.setSpan(urlSpan, sequence.getSpanStart(urlSpan), start - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                // spoiler text has a space at the front
                sequence.setSpan(backgroundColorSpan, start + 1, end - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                sequence.setSpan(foregroundColorSpan, start, end - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                storedSpoilerSpans.add(foregroundColorSpan);
                storedSpoilerSpans.add(backgroundColorSpan);
                // Shift 1 to account for remove of beginning "<"
                storedSpoilerStarts.add(start - 1);
                storedSpoilerStarts.add(start - 1);
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

    /**
     * Moved the spoiler text inside of the "title" attribute to inside the link
     * tag. Then surround the spoiler text with <code>[[s[</code> and <code>]s]]</code>.
     *
     * If there is no text inside of the link tag, insert "spoiler".
     *
     * @param html
     * @return
     */
    private String parseSpoilerTags(String html) {
        String spoilerText;
        String tag;
        String spoilerTeaser;
        Pattern spoilerPattern = Pattern.compile("<a.*title=\"(.*)\".*>(.*[^\\]]?[^\\]]?)</a>");
        Matcher matcher = spoilerPattern.matcher(html);

        while (matcher.find()) {
            tag = matcher.group(0);
            spoilerText = matcher.group(1);
            spoilerTeaser = matcher.group(2);
            // Remove the last </a> tag, but keep the < for parsing.
            html = html.replace(tag, tag.substring(0, tag.length() - 4) + (spoilerTeaser.isEmpty() ? "spoiler" : "") + "&lt; [[s[ " + spoilerText + "]s]]</a>");
        }

        return html;
    }

    private CharSequence convertHtmlToCharSequence(String html) {
        html = parseCodeTags(html);
        html = parseSpoilerTags(html);
        CharSequence sequence = trim(Html.fromHtml(noTrailingwhiteLines(html)));
        sequence = setCodeFont((SpannableStringBuilder)sequence);
        sequence = setSpoilerStyle((SpannableStringBuilder)sequence);
        return sequence;
    }

    public void ParseTextWithLinksTextViewComment(String rawHTML, final ActiveTextView comm, final Activity c, final String subreddit) {
        if (rawHTML.isEmpty()) {
            return;
        }

        this.c = c;

        rawHTML = rawHTML.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&").replace("<li><p>", "<p>• ").replace("</li>", "<br>").replaceAll("<li.*?>", "• ").replace("<p>", "<div>").replace("</p>", "</div>").replace("</del>", "</strike>").replace("<del>", "<strike>");
        if (rawHTML.contains("\n")) {
            rawHTML = rawHTML.substring(0, rawHTML.lastIndexOf("\n"));
        }

        final CharSequence sequence = convertHtmlToCharSequence(rawHTML);
        comm.setText(sequence, TextView.BufferType.SPANNABLE);

        comm.setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
            @Override
            public void onClick(String url, int xOffset) {
                // Decide what to do when a link is clicked.
                // (This is useful if you want to open an in app-browser)
                if (url == null) {
                    return;
                }

                ContentType.ImageType type = ContentType.getImageType(url);
                switch (type) {
                    case NSFW_IMAGE:
                        openImage(c, url);
                        break;
                    case NSFW_GIF:
                        openGif(false, c, url);
                        break;
                    case NSFW_GFY:
                        openGif(true, c, url);
                        break;
                    case REDDIT:
                        new OpenRedditLink(c, url);
                        break;
                    case LINK:
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            builder.setToolbarColor(Palette.getColor(subreddit)).setShowTitle(true);

                            builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            try {
                                customTabsIntent.launchUrl(c, Uri.parse(url));
                            } catch (ActivityNotFoundException anfe) {
                                Log.w("MakeTextViewClickable", "Unknown url: " + anfe);
                            }
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                        break;
                    case IMAGE_LINK:
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            builder.setToolbarColor(Palette.getColor(subreddit)).setShowTitle(true);

                            builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            try {
                                customTabsIntent.launchUrl(c, Uri.parse(url));
                            } catch (ActivityNotFoundException anfe) {
                                Log.w("MakeTextViewClickable", "Unknown url: " + anfe);
                            }
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                        break;
                    case NSFW_LINK:
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            builder.setToolbarColor(Palette.getColor(subreddit)).setShowTitle(true);

                            builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            try {
                                customTabsIntent.launchUrl(c, Uri.parse(url));
                            } catch (ActivityNotFoundException anfe) {
                                Log.w("MakeTextViewClickable", "Unknown url: " + anfe);
                            }
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                        break;
                    case SELF:
                        break;
                    case GFY:
                        openGif(true, c, url);
                        break;
                    case ALBUM:
                        if (Reddit.album) {
                            Intent i = new Intent(c, Album.class);
                            i.putExtra("url", url);
                            c.startActivity(i);
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                        break;
                    case IMAGE:
                        openImage(c, url);
                        break;
                    case GIF:
                        openGif(false, c, url);
                        break;
                    case NONE_GFY:
                        openGif(true, c, url);
                        break;
                    case NONE_GIF:
                        openGif(false, c, url);
                        break;
                    case NONE:
                        break;
                    case NONE_IMAGE:
                        openImage(c, url);
                        break;
                    case NONE_URL:
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            builder.setToolbarColor(Palette.getColor(subreddit)).setShowTitle(true);

                            builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            try {
                                customTabsIntent.launchUrl(c, Uri.parse(url));
                            } catch (ActivityNotFoundException anfe) {
                                Log.w("MakeTextViewClickable", "Unknown url: " + anfe);
                            }
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                        break;
                    case VIDEO:
                        if (Reddit.video) {
                            Intent intent = new Intent(c, FullscreenVideo.class);
                            intent.putExtra("html", url);
                            c.startActivity(intent);
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                    case SPOILER:
                        setOrRemoveSpoilerSpans(comm, (Spannable) sequence, xOffset);
                        break;
                }

            }
        });
        comm.setLinkTextColor(new ColorPreferences(c).getColor(subreddit));
    }

    public void setOrRemoveSpoilerSpans(ActiveTextView commentView, Spannable text, int endOfLink) {
        // add 2 to end of link since there is a white space between the link text and the spoiler
        ForegroundColorSpan[] foregroundColors = text.getSpans(endOfLink + 2, endOfLink + 2, ForegroundColorSpan.class);

        if (foregroundColors.length > 0) {
            text.removeSpan(foregroundColors[0]);
            commentView.setText(text);
        } else {
            for (int i = 0; i < storedSpoilerStarts.size(); i++) {
                if (storedSpoilerStarts.get(i) < endOfLink + 2 && storedSpoilerEnds.get(i) > endOfLink + 2) {
                    text.setSpan(storedSpoilerSpans.get(i), storedSpoilerStarts.get(i), storedSpoilerEnds.get(i), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
            commentView.setText(text);
        }
    }

    public void ParseTextWithLinksTextView(String rawHTML, final ActiveTextView comm, final Activity c, String subreddit) {
        if (rawHTML.isEmpty()) {
            return;
        }

        this.c = c;

        rawHTML = rawHTML.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&").replace("<li><p>", "<p>• ").replace("</li>", "<br>").replaceAll("<li.*?>", "• ").replace("<p>", "<div>").replace("</p>", "</div>").replace("</del>", "</strike>").replace("<del>", "<strike>");
        rawHTML = rawHTML.substring(15, rawHTML.lastIndexOf("<!-- SC_ON -->"));

        CharSequence sequence = convertHtmlToCharSequence(rawHTML);
        comm.setText(sequence);
        comm.setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
            @Override
            public void onClick(String url, int xOffset) {
                // Decide what to do when a link is clicked.
                // (This is useful if you want to open an in app-browser)
                if (url == null) {
                    return;
                }

                if (url.startsWith("/")) {
                    url = "reddit.com" + url;
                }
                ContentType.ImageType type = ContentType.getImageType(url);
                switch (type) {
                    case NSFW_IMAGE:
                        openImage(c, url);
                        break;
                    case NSFW_GIF:
                        openGif(false, c, url);
                        break;
                    case NSFW_GFY:
                        openGif(true, c, url);
                        break;
                    case REDDIT:
                        new OpenRedditLink(c, url);
                        break;
                    case LINK:
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            //COLOR todo  builder.setToolbarColor(Palette.getColor(submission.getSubredditName())).setShowTitle(true);

                            builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            try {
                                customTabsIntent.launchUrl(c, Uri.parse(url));
                            } catch (ActivityNotFoundException anfe) {
                                Log.w("MakeTextViewClickable", "Unknown url: " + anfe);
                            }
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                        break;
                    case IMAGE_LINK:
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            //COLOR todo  builder.setToolbarColor(Palette.getColor(submission.getSubredditName())).setShowTitle(true);

                            builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            try {
                                customTabsIntent.launchUrl(c, Uri.parse(url));
                            } catch (ActivityNotFoundException anfe) {
                                Log.w("MakeTextViewClickable", "Unknown url: " + anfe);
                            }
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                        break;
                    case NSFW_LINK:
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            //COLOR todo  builder.setToolbarColor(Palette.getColor(submission.getSubredditName())).setShowTitle(true);

                            builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            try {
                                customTabsIntent.launchUrl(c, Uri.parse(url));
                            } catch (ActivityNotFoundException anfe) {
                                Log.w("MakeTextViewClickable", "Unknown url: " + anfe);
                            }
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                        break;
                    case SELF:
                        break;
                    case GFY:
                        openGif(true, c, url);
                        break;
                    case ALBUM:
                        if (Reddit.album) {
                            Intent i = new Intent(c, Album.class);
                            i.putExtra("url", url);
                            c.startActivity(i);
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                        break;
                    case IMAGE:
                        openImage(c, url);
                        break;
                    case GIF:
                        openGif(false, c, url);
                        break;
                    case NONE_GFY:
                        openGif(true, c, url);
                        break;
                    case NONE_GIF:
                        openGif(false, c, url);
                        break;
                    case NONE:
                        break;
                    case NONE_IMAGE:
                        openImage(c, url);
                        break;
                    case NONE_URL:
                        if (Reddit.web) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                            //COLOR todo  builder.setToolbarColor(Palette.getColor(submission.getSubredditName())).setShowTitle(true);

                            builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                            builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                            CustomTabsIntent customTabsIntent = builder.build();
                            try {
                                customTabsIntent.launchUrl(c, Uri.parse(url));
                            } catch (ActivityNotFoundException anfe) {
                                Log.w("MakeTextViewClickable", "Unknown url: " + anfe);
                            }
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                        break;
                    case VIDEO:
                        if (Reddit.video) {
                            Intent intent = new Intent(c, FullscreenVideo.class);
                            intent.putExtra("html", url);
                            c.startActivity(intent);
                        } else {
                            Reddit.defaultShare(url, c);
                        }
                }
            }
        });
        comm.setLinkTextColor(new ColorPreferences(c).getColor(subreddit));
    }
}
