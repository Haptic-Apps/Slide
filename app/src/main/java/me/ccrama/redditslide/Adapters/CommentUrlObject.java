package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.CommentNode;

/**
 * Created by carlo_000 on 10/27/2015.
 */
public class CommentUrlObject {
    public String url;

    public CommentNode comment;

    public CommentUrlObject(CommentNode comment, String url) {
        this.comment = comment;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
