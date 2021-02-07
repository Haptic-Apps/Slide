package me.ccrama.redditslide.util;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods to transform html received from Reddit into a more parsable
 * format.
 *
 * The output will unescape all html, except for table tags and some special delimiter
 * token such as for code blocks.
 */
public class SubmissionParser {
    private static final Pattern SPOILER_PATTERN = Pattern.compile("<a[^>]*title=\"([^\"]*)\"[^>]*>([^<]*)</a>");
    private static final String TABLE_START_TAG = "<table>";
    private static final String HR_TAG = "<hr/>";
    private static final String TABLE_END_TAG = "</table>";

    private SubmissionParser() {
    }

    /**
     * Parses html and returns a list corresponding to blocks of text to be
     * formatted.
     *
     * Each block is one of:
     *  - Vanilla text
     *  - Code block
     *  - Table
     *
     * Note that this method will unescape html entities, so this is best called
     * with the raw html received from reddit.
     *
     * @param html html to be formatted. Can be raw from the api
     * @return list of text blocks
     */
    public static List<String> getBlocks(String html) {
        html = StringEscapeUtils.unescapeHtml4(html)
                .replace("<p>", "<div>")
                .replace("</p>", "</div>")
                .replace("<li>\\s*<div>", "<li>")
                .replace("</div>\\s*</li>", "</li>")
                .replace("<li><div>", "<li>")
                .replace("</div></li>", "</li>")
                .replace("<del>", "[[d[")
                .replace("<sup>", "<sup><small>")
                .replace("</sup>", "</small></sup>")
                .replace("</del>", "]d]]");

        if (html.contains("\n")) {
            html = html.substring(0, html.lastIndexOf("\n"));
        }

        if (html.contains("<!-- SC_ON -->")) {
            html = html.substring(15, html.lastIndexOf("<!-- SC_ON -->"));
        }

        html = parseSpoilerTags(html);
        if (html.contains("<ol") || html.contains("<ul")) {
            html = parseLists(html);
        }



        List<String> codeBlockSeperated = parseCodeTags(html);

        if (html.contains(HR_TAG)) {
            codeBlockSeperated = parseHR(codeBlockSeperated);
        }

        if (html.contains("<table")) {
            return parseTableTags(codeBlockSeperated);
        } else {
            return codeBlockSeperated;
        }
    }

    private static String parseLists(String html) {
        int firstIndex;
        boolean isNumbered;
        int firstOl = html.indexOf("<ol");
        int firstUl = html.indexOf("<ul");

        if ((firstUl != -1 && firstOl > firstUl) || firstOl == -1) {
            firstIndex = firstUl;
            isNumbered = false;
        } else {
            firstIndex = firstOl;
            isNumbered = true;
        }
        List<Integer> listNumbers = new ArrayList<>();
        int indent = -1;

        int i = firstIndex;
        while (i < html.length() - 4 && i != -1) {
            if (html.substring(i, i + 3).equals("<ol") || html.substring(i, i + 3).equals("<ul")) {
                if (html.substring(i, i + 3).equals("<ol")) {
                    isNumbered = true;
                    indent++;
                    listNumbers.add(indent, 1);
                } else {
                    isNumbered = false;
                }
                i = html.indexOf("<li", i);
            } else if (html.substring(i, i + 3).equals("<li")) {
                int tagEnd = html.indexOf(">", i);
                int itemClose = html.indexOf("</li", tagEnd);
                int ulClose = html.indexOf("<ul", tagEnd);
                int olClose = html.indexOf("<ol", tagEnd);
                int closeTag;

                // Find what is closest: </li>, <ul>, or <ol>
                if (((ulClose == -1 && itemClose != -1) || (itemClose != -1 && ulClose != -1 && itemClose < ulClose)) && ((olClose == -1 && itemClose != -1) || (itemClose != -1 && olClose != -1 && itemClose < olClose))) {
                    closeTag = itemClose;
                } else if (((ulClose == -1 && olClose != -1) || (olClose != -1 && ulClose != -1 && olClose < ulClose)) && ((olClose == -1 && itemClose != -1) || (olClose != -1 && itemClose != -1 && olClose < itemClose))) {
                    closeTag = olClose;
                } else {
                    closeTag = ulClose;
                }

                String text = html.substring(tagEnd + 1, closeTag);
                StringBuilder indentSpacingBuilder = new StringBuilder();
                for (int j = 0; j < indent; j++) {
                    indentSpacingBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                }
                String indentSpacing = indentSpacingBuilder.toString();
                if (isNumbered) {
                    html = html.substring(0, tagEnd + 1)
                            + indentSpacing +
                            listNumbers.get(indent)+ ". " +
                            text + "<br/>" +
                            html.substring(closeTag);
                    listNumbers.set(indent, listNumbers.get(indent) + 1);
                    i = closeTag + 3;
                } else {
                    html = html.substring(0, tagEnd + 1) + indentSpacing + "• " + text + "<br/>" + html.substring(closeTag);
                    i = closeTag + 2;
                }
            } else {
                i = html.indexOf("<", i + 1);
                if (i != -1 && html.substring(i, i + 4).equals("</ol")) {
                    indent--;
                    if(indent == -1){
                        isNumbered = false;
                    }
                }
            }
        }

        html = html.replace("<ol>","").replace("<ul>","").replace("<li>","").replace("</li>","").replace("</ol>", "").replace("</ul>",""); //Remove the tags, which actually work in Android 7.0 on

        return html;
    }

    private static List<String> parseHR(List<String> blocks) {
        List<String> newBlocks = new ArrayList<>();
        for (String block : blocks) {
            if (block.contains(HR_TAG)) {
                for(String s : block.split(HR_TAG)) {
                    newBlocks.add(s);
                    newBlocks.add(HR_TAG);
                }
                newBlocks.remove(newBlocks.size() - 1);
            } else {
                newBlocks.add(block);
            }
        }

        return newBlocks;
    }

    /**
     * For code within <code>&lt;pre&gt;</code> tags, line breaks are converted to
     * <code>&lt;br /&gt;</code> tags, and spaces to &amp;nbsp;. This allows for Html.fromHtml
     * to preserve indents of these blocks.
     * <p/>
     * In addition, <code>[[&lt;[</code> and <code>]&gt;]]</code> are inserted to denote the
     * beginning and end of code segments, for styling later.
     *
     * @param html the unparsed HTML
     * @return the code parsed HTML with additional markers, split but code blocks
     */
    private static List<String> parseCodeTags(String html) {
        final String startTag = "<pre><code>";
        final String endTag = "</code></pre>";
        String[] startSeperated = html.split(startTag);
        List<String> preSeperated = new ArrayList<>();

        String text;
        String code;
        String[] split;

        preSeperated.add(startSeperated[0].replace("<code>", "<code>[[&lt;[").replace("</code>", "]&gt;]]</code>"));
        for (int i = 1; i < startSeperated.length; i++) {
            text = startSeperated[i];
            split = text.split(endTag);
            code = split[0];
            code = code.replace("\n", "<br/>");
            code = code.replace(" ", "&nbsp;");

            preSeperated.add(startTag + "[[&lt;[" + code + "]&gt;]]" + endTag);
            if (split.length > 1) {
                preSeperated.add(split[1].replace("<code>", "<code>[[&lt;[").replace("</code>", "]&gt;]]</code>"));
            }
        }

        return preSeperated;
    }


    /**
     * Move the spoil text inside of the "title" attribute to inside the link
     * tag. Then surround the spoil text with <code>[[s[</code> and <code>]s]]</code>.
     * <p/>
     * If there is no text inside of the link tag, insert "spoil".
     *
     * @param html
     * @return
     */
    private static String parseSpoilerTags(String html) {
        String spoilerText;
        String tag;
        String spoilerTeaser;
        Matcher matcher = SPOILER_PATTERN.matcher(html);

        while (matcher.find()) {
            tag = matcher.group(0);
            spoilerText = matcher.group(1);
            spoilerTeaser = matcher.group(2);
            // Remove the last </a> tag, but keep the < for parsing.
            if (!tag.contains("<a href=\"http")) {
                html = html.replace(tag, tag.substring(0, tag.length() - 4) + (spoilerTeaser.isEmpty() ? "spoiler" : "") + "&lt; [[s[ " + spoilerText + "]s]]</a>");
            }
        }

        return html;
    }

    /**
     * Parse a given list of html strings, splitting by table blocks.
     *
     * All table tags are html escaped.
     *
     * @param blocks list of html with or individual table blocks
     * @return list of html with tables split into it's entry
     */
    private static List<String> parseTableTags(List<String> blocks) {
        List<String> newBlocks = new ArrayList<>();
        for (String block : blocks) {
            if (block.contains(TABLE_START_TAG)) {
                String[] startSeperated = block.split(TABLE_START_TAG);
                newBlocks.add(startSeperated[0].trim());
                for (int i = 1; i < startSeperated.length; i++) {
                    String [] split = startSeperated[i].split(TABLE_END_TAG);
                    newBlocks.add("<table>" + split[0] + "</table>");
                    if (split.length > 1) {
                        newBlocks.add(split[1]);
                    }
                }
            } else {
                newBlocks.add(block);
            }
        }

        return newBlocks;
    }
}
