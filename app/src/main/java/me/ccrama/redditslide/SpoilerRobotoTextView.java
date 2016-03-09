package me.ccrama.redditslide;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.devspark.robototextview.widget.RobotoTextView;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.FullscreenImage;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.Activities.Imgur;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.TextViewLinkHandler;
import me.ccrama.redditslide.util.CustomTabUtil;

/**
 * Created by carlo_000 on 1/11/2016.
 */
public class SpoilerRobotoTextView extends RobotoTextView implements ClickableText {
    private List<CharacterStyle> storedSpoilerSpans = new ArrayList<>();
    private List<Integer> storedSpoilerStarts = new ArrayList<>();
    private List<Integer> storedSpoilerEnds = new ArrayList<>();

    public SpoilerRobotoTextView(Context context) {

        super(context);

    }

    public SpoilerRobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpoilerRobotoTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
     * Set the text from html. Handles formatting spoilers, links etc.
     * <p/>
     * The text must be valid html.
     *
     * @param text html text
     */
    public void setTextHtml(CharSequence text) {
        setTextHtml(text, "");
    }

    /**
     * Set the text from html. Handles formatting spoilers, links etc.
     * <p/>
     * The text must be valid html.
     *
     * @param text      html text
     * @param subreddit the subreddit to theme
     */
    public void setTextHtml(CharSequence text, String subreddit) {
        SpannableStringBuilder builder = (SpannableStringBuilder) Html.fromHtml(saveEmotesFromDestruction(text.toString().trim()));

        if(text.toString().contains("<a")) {
            setEmoteSpans(builder); //for emote enabled subreddits
        }
        if(text.toString().contains("[")) {
            setCodeFont(builder);
            setSpoilerStyle(builder);
        }
        if (text.toString().contains("[[d[")) {
            setStrikethrough(builder);
        }

        if (!subreddit.isEmpty()) {
            setMovementMethod(new TextViewLinkHandler(this, subreddit, builder));
            setFocusable(false);
            setClickable(false);
            if(subreddit.equals("FORCE_LINK_CLICK")) {
                setLongClickable(false);
            }

        }

        builder = removeNewlines(builder);

        super.setText(builder, BufferType.SPANNABLE);
    }

    private String saveEmotesFromDestruction(String html) {
        //Emotes often have no spoiler caption, and therefore are converted to empty anchors. Html.fromHtml removes anchors with zero length node text. Find zero length anchors that start with "/" and add "." to them.
        Pattern htmlEmotePattern = Pattern.compile("<a href=\"/.*\"></a>");
        Matcher htmlEmoteMatcher = htmlEmotePattern.matcher(html);
        while(htmlEmoteMatcher.find()) {
            String newPiece = htmlEmoteMatcher.group();
            //Ignore empty tags marked with sp.
            if(!htmlEmoteMatcher.group().contains("href=\"/sp\"")) {
                newPiece = newPiece.replace("></a",">.</a");
                html = html.replace(htmlEmoteMatcher.group(),newPiece);
            }
        }
        return html;
    }

    private void setEmoteSpans(SpannableStringBuilder builder) {
        for(URLSpan span:builder.getSpans(0,builder.length(),URLSpan.class)) {
            File emoteDir = new File(Environment.getExternalStorageDirectory(),"RedditEmotes");
            File emoteFile = new File(emoteDir,span.getURL().replace("/", "").replaceAll("-.*","")+".png"); //BPM uses "-" to add dynamics for emotes in browser. Fall back to original here if exists.
            boolean startsWithSlash = span.getURL().startsWith("/");
            boolean hasOnlyOneSlash = StringUtils.countMatches(span.getURL(),"/") == 1;

            if(emoteDir.exists() && startsWithSlash && hasOnlyOneSlash && emoteFile.exists()) {
                //We've got an emote match
                int start = builder.getSpanStart(span);
                int end = builder.getSpanEnd(span);
                CharSequence textCovers = builder.subSequence(start,end);

                //Make sure bitmap loaded works well with screen density.
                BitmapFactory.Options options = new BitmapFactory.Options();
                DisplayMetrics metrics = new DisplayMetrics();
                ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
                options.inDensity = 240;
                options.inScreenDensity = metrics.densityDpi;
                options.inScaled = true;

                //Since emotes are not directly attached to included text, add extra character to attach image to.
                builder.removeSpan(span);
                if(builder.subSequence(start,end).charAt(0) != '.') {
                    builder.insert(start, ".");
                }
                Bitmap emoteBitmap = BitmapFactory.decodeFile(emoteFile.getAbsolutePath(),options);
                builder.setSpan(new ImageSpan(getContext(), emoteBitmap), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //Check if url span has length. If it does, it's a spoiler/caption
                if(textCovers.length()>1) {
                    builder.setSpan(new URLSpan("/sp"), start + 1, end + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new StyleSpan(Typeface.ITALIC), start + 1, end+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

    private void setStrikethrough(SpannableStringBuilder builder) {
        final int offset = "[[d[".length(); // == "]d]]".length()

        int start = -1;
        int end;
        for (int i = 0; i < builder.length() - 4; i++) {
            if (builder.charAt(i) == '[' && builder.charAt(i + 1) == '[' && builder.charAt(i + 2) == 'd' && builder.charAt(i + 3) == '[') {
                start = i + offset;
            } else if (builder.charAt(i) == ']' && builder.charAt(i + 1) == 'd' && builder.charAt(i + 2) == ']' && builder.charAt(i + 3) == ']') {
                end = i;
                builder.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                builder.delete(end, end + offset);
                builder.delete(start - offset, start);
                i -= offset + (end - start); // length of text
            }
        }
    }

    @Override
    public void onLinkClick(String url, int xOffset, String subreddit) {
        if (url == null) {
            ((View) getParent()).callOnClick();
            return;
        }

        ContentType.ImageType type = ContentType.getImageType(url);
        Context context = getContext();
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof android.support.v7.view.ContextThemeWrapper) {
            activity = (Activity) ((android.support.v7.view.ContextThemeWrapper) context).getBaseContext();
        } else {
            throw new RuntimeException("Could not find activity from context:" + context);
        }

        switch (type) {
            case IMGUR:
                Intent intent2 = new Intent(activity, Imgur.class);
                intent2.putExtra(Imgur.EXTRA_URL, url);
                activity.startActivity(intent2);
                break;
            case NSFW_IMAGE:
                openImage(url);
                break;
            case NSFW_GIF:
                openGif(false, url);
                break;
            case NSFW_GFY:
                openGif(true, url);
                break;
            case REDDIT:
                new OpenRedditLink(activity, url);
                break;
            case LINK:
            case IMAGE_LINK:
            case NSFW_LINK:
                CustomTabUtil.openUrl(url.startsWith("//")?url.replace("//", "http://"):url, Palette.getColor(subreddit), activity);
                break;
            case SELF:
                break;
            case GFY:
                openGif(true, url);
                break;
            case ALBUM:
                if (SettingValues.album) {
                    if (SettingValues.albumSwipe) {
                        Intent i = new Intent(activity, AlbumPager.class);
                        i.putExtra(Album.EXTRA_URL, url);
                        activity.startActivity(i);

                        activity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
                    } else {
                        Intent i = new Intent(activity, Album.class);
                        i.putExtra(Album.EXTRA_URL, url);
                        activity.startActivity(i);
                        activity.overridePendingTransition(R.anim.slideright, R.anim.fade_out);
                    }
                } else {
                    Reddit.defaultShare(url, activity);
                }
                break;
            case IMAGE:
                openImage(url);
                break;
            case GIF:
                openGif(false, url);
                break;
            case NONE_GFY:
                openGif(true, url);
                break;
            case NONE_GIF:
                openGif(false, url);
                break;
            case NONE:
                break;
            case NONE_IMAGE:
                openImage(url);
                break;
            case NONE_URL:
                CustomTabUtil.openUrl(url, Palette.getColor(subreddit), activity);
                break;
            case VIDEO:
                Reddit.defaultShare(url, activity);
            case SPOILER:
                spoilerClicked = true;
                setOrRemoveSpoilerSpans(xOffset);
                break;
        }
    }

    @Override
    public void onLinkLongClick(final String url) {
        if (url == null) {
            return;
        }
        final Activity activity;
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        if (getContext() instanceof ContextThemeWrapper) {
            activity = (Activity) ((android.support.v7.view.ContextThemeWrapper) getContext()).getBaseContext();
        } else {
            activity = (Activity) getContext();
        }

        if (!activity.isFinishing()) {
            new BottomSheet.Builder(activity)
                    .title(url)
                    .grid()
                    .sheet(R.menu.link_menu)
                    .listener(new DialogInterface.OnClickListener() {
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

        }
    }

    private void openGif(boolean gfy, String url) {
        if (SettingValues.gif) {
            Intent myIntent = new Intent(getContext(), GifView.class);
            if (gfy) {
                myIntent.putExtra(GifView.EXTRA_URL, "gfy" + url);
            } else {
                myIntent.putExtra(GifView.EXTRA_URL, "" + url);

            }
            getContext().startActivity(myIntent);

        } else {
            Reddit.defaultShare(url, getContext());
        }
    }

    private void openImage(String submission) {
        if (SettingValues.image) {
            Intent myIntent = new Intent(getContext(), FullscreenImage.class);
            myIntent.putExtra(FullscreenImage.EXTRA_URL, submission);
            getContext().startActivity(myIntent);
        } else {
            Reddit.defaultShare(submission, getContext());
        }

    }

    public void setOrRemoveSpoilerSpans(int endOfLink) {
        Spannable text = (Spannable) getText();
        // add 2 to end of link since there is a white space between the link text and the spoiler
        ForegroundColorSpan[] foregroundColors = text.getSpans(endOfLink + 2, endOfLink + 2, ForegroundColorSpan.class);

        if (foregroundColors.length > 1) {
            text.removeSpan(foregroundColors[1]);
            setText(text);
        } else {
            for (int i = 1; i < storedSpoilerStarts.size(); i++) {
                if (storedSpoilerStarts.get(i) < endOfLink + 2 && storedSpoilerEnds.get(i) > endOfLink + 2) {
                    text.setSpan(storedSpoilerSpans.get(i), storedSpoilerStarts.get(i), storedSpoilerEnds.get(i), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
            setText(text);
        }
    }

    /**
     * Set the necessary spans for each spoiler.
     * <p/>
     * The algorithm works in the same way as <code>setCodeFont</code>.
     *
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
                ForegroundColorSpan underneathColorSpan = new ForegroundColorSpan(Color.WHITE);

                URLSpan urlSpan = sequence.getSpans(start, start, URLSpan.class)[0];
                sequence.setSpan(urlSpan, sequence.getSpanStart(urlSpan), start - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                // spoiler text has a space at the front
                sequence.setSpan(backgroundColorSpan, start + 1, end - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                sequence.setSpan(underneathColorSpan, start, end - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                sequence.setSpan(foregroundColorSpan, start, end - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

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

    /**
     * Sets the styling for string with code segments.
     * <p/>
     * The general process is to search for <code>[[&lt;[</code> and <code>]&gt;]]</code> tokens to
     * find the code fragments within the escaped text. A <code>Spannable</code> is created which
     * which breaks up the origin sequence into non-code and code fragments, and applies a monospace
     * font to the code fragments.
     *
     * @param sequence the Spannable generated from Html.fromHtml
     * @return the message with monospace font applied to code fragments
     */
    private SpannableStringBuilder setCodeFont(SpannableStringBuilder sequence) {
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


}
