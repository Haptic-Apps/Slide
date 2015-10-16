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
import android.view.View;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

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

public class MakeTextviewClickable {

    public String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
    public static void openImage(Activity contextActivity, String submission) {
        Intent myIntent = new Intent(contextActivity, FullscreenImage.class);
        myIntent.putExtra("url", submission);
        contextActivity.startActivity(myIntent);
        contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);

    }
    public static void openGif(final boolean gfy, Activity contextActivity, String submission) {

        Intent myIntent = new Intent(contextActivity, GifView.class);
        if (gfy) {
            myIntent.putExtra("url", "gfy" + submission);
        } else {
            myIntent.putExtra("url", "" + submission);

        }
        contextActivity.startActivity(myIntent);
        contextActivity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);


    }

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span, final Activity c)
    {
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

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1).toString();
    }
    ArrayList<HTMLLinkExtractor.HtmlLink> links;
    Context c;
    public void ParseTextWithLinks(String rawHTML, TextView comm, Context c) throws URISyntaxException {
        rawHTML = rawHTML.substring(0, rawHTML.length() - 8);

        rawHTML = trimTrailingWhitespace(rawHTML);

        rawHTML = rawHTML.replace("<li>", "\n•");



        this.c = c;

        CharSequence sequence = Html.fromHtml(noTrailingwhiteLines(rawHTML));
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span, (Activity) c);
        }
        comm.setText(strBuilder);

      //TODO  comm.setMovementMethod(CustomMovementMethod.getInstance());

        links = new HTMLLinkExtractor().grabHTMLLinks(rawHTML);

    }

    public static String noTrailingwhiteLines(String text) {

        while (text.charAt(text.length() - 1) == '\n') {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
    public  void ParseTextWithLinksTextViewComment(String rawHTML, final ActiveTextView comm, final Activity c, final String subreddit) {
        comm.refreshDrawableState();
        if(rawHTML.length() > 0) {
            rawHTML = rawHTML.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&").replace("<li><p>", "<p>• ").replace("</li>", "<br>").replaceAll("<li.*?>", "• ").replace("<p>", "<div>").replace("</p>", "</div>");

            rawHTML = rawHTML.substring(0, rawHTML.lastIndexOf("\n") );

            this.c = c;


            CharSequence sequence = trim(Html.fromHtml(noTrailingwhiteLines(rawHTML)));
            /* DEPRECATED
            SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
            URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
            for (URLSpan span : urls) {
                Log.v("Slide", "DOING URL!");
                makeLinkClickable(strBuilder, span, (Activity) c);
            }*/
            comm.setText(sequence);
            comm.setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
                @Override
                public void onClick(String url) {
                    // Decide what to do when a link is clicked.
                    // (This is useful if you want to open an in app-browser)
                    if(url!=null){
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
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case IMAGE_LINK:
                            {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case NSFW_LINK:
                            {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case SELF:

                                break;
                            case GFY:
                                openGif(true, c, url);

                                break;
                            case ALBUM:

                                Intent i = new Intent(c, Album.class);
                                i.putExtra("url", url);
                                c.startActivity(i);
                                c.overridePendingTransition(R.anim.slideright, R.anim.fade_out);



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
                            {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case VIDEO:

                                Intent intent = new Intent(c, FullscreenVideo.class);
                                intent.putExtra("html", url);
                                c.startActivity(intent);




                        }

                    }
                }
            });

            // Set a long pressed link listener (required if you want to show the additional 
            // options menu when links are long pressed)
            comm.setLongPressedLinkListener(new ActiveTextView.OnLongPressedLinkListener() {
                @Override
                public void onLongPressed() {
                    
                }
            }, false);

            links = new HTMLLinkExtractor().grabHTMLLinks(rawHTML);

            comm.setLinkTextColor(new ColorPreferences(c).getColor(subreddit));


        }


    }

    public  void ParseTextWithLinksTextViewComment(String rawHTML, final ActiveTextView comm, final Activity c, final String subreddit, String search) {
        comm.refreshDrawableState();
        if(rawHTML.length() > 0) {
            rawHTML = rawHTML.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&").replace("<li><p>", "<p>• ").replace("</li>", "<br>").replaceAll("<li.*?>", "• ").replace("<p>", "<div>").replace("</p>", "</div>");

            rawHTML = rawHTML.substring(0, rawHTML.lastIndexOf("\n") );

            rawHTML.replace(search, "<font color='red'>" + search + "</font>" );

            this.c = c;


            CharSequence sequence = trim(Html.fromHtml(noTrailingwhiteLines(rawHTML)));
            /* DEPRECATED
            SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
            URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
            for (URLSpan span : urls) {
                Log.v("Slide", "DOING URL!");
                makeLinkClickable(strBuilder, span, (Activity) c);
            }*/
            comm.setText(sequence);
            comm.setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
                @Override
                public void onClick(String url) {
                    // Decide what to do when a link is clicked.
                    // (This is useful if you want to open an in app-browser)
                    if(url!=null){
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
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case IMAGE_LINK:
                            {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case NSFW_LINK:
                            {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case SELF:

                                break;
                            case GFY:
                                openGif(true, c, url);

                                break;
                            case ALBUM:

                                Intent i = new Intent(c, Album.class);
                                i.putExtra("url", url);
                                c.startActivity(i);
                                c.overridePendingTransition(R.anim.slideright, R.anim.fade_out);



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
                            {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                builder.setToolbarColor(Pallete.getColor(subreddit)).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case VIDEO:

                                Intent intent = new Intent(c, FullscreenVideo.class);
                                intent.putExtra("html", url);
                                c.startActivity(intent);




                        }

                    }
                }
            });

            // Set a long pressed link listener (required if you want to show the additional
            // options menu when links are long pressed)
            comm.setLongPressedLinkListener(new ActiveTextView.OnLongPressedLinkListener() {
                @Override
                public void onLongPressed() {

                }
            }, false);

            links = new HTMLLinkExtractor().grabHTMLLinks(rawHTML);

            comm.setLinkTextColor(new ColorPreferences(c).getColor(subreddit));


        }


    }

    public  void ParseTextWithLinksTextView(String rawHTML, ActiveTextView comm, final Activity c, String subreddit) {
        if(rawHTML.length() > 0) {
            rawHTML = rawHTML.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'").replace("&amp;", "&").replace("<li><p>", "<p>• ").replace("</li>", "<br>").replaceAll("<li.*?>", "• ").replace("<p>", "<div>").replace("</p>","</div>");
            rawHTML = rawHTML.substring(15, rawHTML.lastIndexOf("<!-- SC_ON -->"));


            this.c = c;



            /*DEPRECATED
            Log.v("Spiral", sequence.toString());
            SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
            URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
            for (URLSpan span : urls) {
                Log.v("Slide", "DOING URL!");
                makeLinkClickable(strBuilder, span, (Activity) c);
            }*/
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
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                //COLOR todo  builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case IMAGE_LINK: {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                //COLOR todo  builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case NSFW_LINK: {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                //COLOR todo  builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case SELF:

                                break;
                            case GFY:
                                openGif(true, c, url);

                                break;
                            case ALBUM:

                                Intent i = new Intent(c, Album.class);
                                i.putExtra("url", url);
                                c.startActivity(i);
                                c.overridePendingTransition(R.anim.slideright, R.anim.fade_out);


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
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(Reddit.getSession());
                                //COLOR todo  builder.setToolbarColor(Pallete.getColor(submission.getSubredditName())).setShowTitle(true);

                                builder.setStartAnimations(c, R.anim.slideright, R.anim.fading_out_real);
                                builder.setExitAnimations(c, R.anim.fade_out, R.anim.fade_in_real);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(c, Uri.parse(url));

                            }
                            break;
                            case VIDEO:

                                Intent intent = new Intent(c, FullscreenVideo.class);
                                intent.putExtra("html", url);
                                c.startActivity(intent);


                        }

                    }
                }
            });
            comm.setLinkTextColor(new ColorPreferences(c).getColor(subreddit));

            // Set a long pressed link listener (required if you want to show the additional
            // options menu when links are long pressed)
            comm.setLongPressedLinkListener(new ActiveTextView.OnLongPressedLinkListener() {
                @Override
                public void onLongPressed() {

                }
            }, false);

            links = new HTMLLinkExtractor().grabHTMLLinks(rawHTML);


        }


    }
    public static CharSequence trim(CharSequence s) {
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
