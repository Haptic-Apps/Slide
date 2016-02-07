package me.ccrama.redditslide.util;

import org.apache.commons.lang3.StringEscapeUtils;

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
    private static final Pattern SPOILER_PATTERN = Pattern.compile("<a.*title=\"(.*)\".*>(.*[^\\]]?[^\\]]?)</a>");
    private static final String TABLE_START_TAG = "<table>";
    private static final String TABLE_END_TAG = "</table>";

    /**
     * Parses html and returns a list corresponding to blocks of text
     * for be formatted.
     *
     * Each block is one of:
     *  - Vanilla text
     *  - Code block
     *  - Table
     *
     * @param html html to be formatted. Can be raw from the api
     * @return list of text blocks
     */
    public static List<String> getBlocks(String html) {
        html = StringEscapeUtils.unescapeHtml4(html)
                .replace("<li><p>", "<p>• ")
                .replace("</li>", "<br>")
                .replaceAll("<li.*?>", "• ")
                .replace("<p>", "<div>")
                .replace("</p>", "</div>")
                .replace("</del>", "</strike>")
                .replace("<del>", "<strike>");

        if (html.contains("\n")) {
            html = html.substring(0, html.lastIndexOf("\n"));
        }

        if (html.contains("<!-- SC_ON -->")) {
            html = html.substring(15, html.lastIndexOf("<!-- SC_ON -->"));
        }

        html = parseSpoilerTags(html);
        List<String> codeBlockSeperated = parseCodeTags(html);
        if (html.contains("<table")) {
            return parseTableTags(codeBlockSeperated);
        } else {
            return codeBlockSeperated;
        }
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
     * Move the spoiler text inside of the "title" attribute to inside the link
     * tag. Then surround the spoiler text with <code>[[s[</code> and <code>]s]]</code>.
     * <p/>
     * If there is no text inside of the link tag, insert "spoiler".
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
            html = html.replace(tag, tag.substring(0, tag.length() - 4) + (spoilerTeaser.isEmpty() ? "spoiler" : "") + "&lt; [[s[ " + spoilerText + "]s]]</a>");
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

                    newBlocks.add("<table>" + split[0]
                            .replaceAll("<td.*?>", "<td>")
                            .replaceAll("<th.*?>", "<th>")
                            + "</table>"
                    );

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
