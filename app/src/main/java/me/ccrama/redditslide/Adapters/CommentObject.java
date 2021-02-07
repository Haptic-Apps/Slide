package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.CommentNode;

/**
 * Created by carlo_000 on 10/27/2015.
 */
public class CommentObject {
    public String name = "";

    public boolean isComment() {
        return false;
    }

    public String getName() {
        return name;
    }

    public CommentNode comment;
}
