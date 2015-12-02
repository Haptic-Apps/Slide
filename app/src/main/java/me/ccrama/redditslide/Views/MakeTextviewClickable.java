package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.net.URI;
import java.net.URISyntaxException;

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

    private Context c;

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

    private CharSequence convertHtmlToCharSequence(String html) {
        html = parseCodeTags(html);
        CharSequence sequence = trim(Html.fromHtml(noTrailingwhiteLines(html)));
        sequence = setCodeFont((SpannableStringBuilder)sequence);
        return sequence;
    }

    public void ParseTextWithLinksTextViewComment(String rawHTML, final ActiveTextView comm, final Activity c, final String subreddit) {
        if (rawHTML.length() > 0) {
            rawHTML = rawHTML.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&").replace("<li><p>", "<p>• ").replace("</li>", "<br>").replaceAll("<li.*?>", "• ").replace("<p>", "<div>").replace("</p>", "</div>").replace("</del>", "</strike>").replace("<del>", "<strike>");

            try {
                rawHTML = rawHTML.substring(0, rawHTML.lastIndexOf("\n"));

            } catch (Exception ignored) {

            }
            this.c = c;

            CharSequence sequence = convertHtmlToCharSequence(rawHTML);
            comm.setText(sequence);

            comm.setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
                @Override
                public void onClick(String url) {
                    // Decide what to do when a link is clicked.
                    // (This is useful if you want to open an in app-browser)
                    if (url != null) {
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

                            {
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
                            }
                            break;
                            case IMAGE_LINK: {
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
                            }
                            break;
                            case NSFW_LINK: {
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
                            case NONE_URL: {
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
                }
            });


            comm.setLinkTextColor(new ColorPreferences(c).getColor(subreddit));


        }


    }

    public void ParseTextWithLinksTextView(String rawHTML, final ActiveTextView comm, final Activity c, String subreddit) {
        if (rawHTML.length() > 0) {
            rawHTML = rawHTML.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&").replace("<li><p>", "<p>• ").replace("</li>", "<br>").replaceAll("<li.*?>", "• ").replace("<p>", "<div>").replace("</p>", "</div>").replace("</del>", "</strike>").replace("<del>", "<strike>");
            rawHTML = rawHTML.substring(15, rawHTML.lastIndexOf("<!-- SC_ON -->"));


            this.c = c;

            CharSequence sequence = convertHtmlToCharSequence(rawHTML);
            comm.setText(sequence);
            comm.setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
                @Override
                public void onClick(String url) {
                    // Decide what to do when a link is clicked.
                    // (This is useful if you want to open an in app-browser)
                    if (url != null) {
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

                            {
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
                            }
                            break;
                            case IMAGE_LINK: {
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
                            }
                            break;
                            case NSFW_LINK: {
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
                            case NONE_URL: {
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
                }
            });
            comm.setLinkTextColor(new ColorPreferences(c).getColor(subreddit));


        }


    }
}
