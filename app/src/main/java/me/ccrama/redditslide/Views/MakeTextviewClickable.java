package me.ccrama.redditslide.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
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
import me.ccrama.redditslide.Visuals.Pallete;

/**
 * Created by ccrama on 7/17/2015.
 */
public class MakeTextviewClickable {

    public String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

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

        if(Reddit.gif) {
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

    public static String trimTrailingWhitespace(String source) {

        if (source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i + 1).toString();
    }

    private Context c;


    private static String noTrailingwhiteLines(String text) {

        while (text.charAt(text.length() - 1) == '\n') {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    public void ParseTextWithLinksTextViewComment(String rawHTML, final ActiveTextView comm, final Activity c, final String subreddit) {
        if (rawHTML.length() > 0) {
            rawHTML = rawHTML.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&").replace("<li><p>", "<p>• ").replace("</li>", "<br>").replaceAll("<li.*?>", "• ").replace("<p>", "<div>").replace("</p>", "</div>").replace("</del>", "</strike>").replace("<del>", "<strike>");

            try {
                rawHTML = rawHTML.substring(0, rawHTML.lastIndexOf("\n"));

            } catch (Exception ignored){

            }
            this.c = c;


            CharSequence sequence = trim(Html.fromHtml(noTrailingwhiteLines(rawHTML)));

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
                                    builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                    builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                    builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                    CustomTabsIntent customTabsIntent = builder.build();
                                    customTabsIntent.launchUrl(c, Uri.parse(url));
                                } else {
                                    Reddit.defaultShare(url, c);
                                }
                            }
                            break;
                            case IMAGE_LINK: {
                                if (Reddit.web) {
                                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                    builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                    builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                    builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                    CustomTabsIntent customTabsIntent = builder.build();
                                    customTabsIntent.launchUrl(c, Uri.parse(url));
                                } else {
                                    Reddit.defaultShare(url, c);
                                }
                            }
                            break;
                            case NSFW_LINK: {
                                if (Reddit.web) {
                                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                    builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                    builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                    builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                    CustomTabsIntent customTabsIntent = builder.build();
                                    customTabsIntent.launchUrl(c, Uri.parse(url));
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
                                    builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                    builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                    builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                    CustomTabsIntent customTabsIntent = builder.build();
                                    customTabsIntent.launchUrl(c, Uri.parse(url));
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
    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }
    public void ParseTextWithLinksTextView(String rawHTML, final ActiveTextView comm, final Activity c, String subreddit) {
        if (rawHTML.length() > 0) {
            rawHTML = rawHTML.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&").replace("<li><p>", "<p>• ").replace("</li>", "<br>").replaceAll("<li.*?>", "• ").replace("<p>", "<div>").replace("</p>", "</div>").replace("</del>", "</strike>").replace("<del>", "<strike>");
            rawHTML = rawHTML.substring(15, rawHTML.lastIndexOf("<!-- SC_ON -->"));


            this.c = c;

            CharSequence sequence = trim(Html.fromHtml(noTrailingwhiteLines(rawHTML)));
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
                                    //COLOR todo  builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                                    builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                    builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                    CustomTabsIntent customTabsIntent = builder.build();
                                    customTabsIntent.launchUrl(c, Uri.parse(url));
                                } else {
                                    Reddit.defaultShare(url, c);
                                }
                            }
                            break;
                            case IMAGE_LINK: {
                                if (Reddit.web) {
                                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                    //COLOR todo  builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                                    builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                    builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                    CustomTabsIntent customTabsIntent = builder.build();
                                    customTabsIntent.launchUrl(c, Uri.parse(url));
                                } else {
                                    Reddit.defaultShare(url, c);
                                }
                            }
                            break;
                            case NSFW_LINK: {
                                if (Reddit.web) {
                                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                    //COLOR todo  builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                                    builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                    builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                    CustomTabsIntent customTabsIntent = builder.build();
                                    customTabsIntent.launchUrl(c, Uri.parse(url));
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
                                    //COLOR todo  builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                                    builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                    builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                    CustomTabsIntent customTabsIntent = builder.build();
                                    customTabsIntent.launchUrl(c, Uri.parse(url));
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
}
