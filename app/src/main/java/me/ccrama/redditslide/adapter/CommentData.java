package me.ccrama.redditslide.adapter;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.MoreChildren;

/**
 * Created by ccrama on 10/3/2015.
 */
class CommentData {
    private final CommentNode n;
    private final boolean more;

    public CommentData(CommentNode n) {
        this.n = n;
        more = true;
    }

    public CommentData(MoreChildren moreChildren, CommentNode n) {
        MoreChildren moreChildren1 = moreChildren;
        more = false;
        this.n = n;

    }

    public Comment getComment() {
        return n.getComment();
    }

    public String getFullName() {
        if (isCommentNode()) {
            return n.getComment().getFullName();
        } else {
            return n.getComment().getFullName() + "LOAD";
        }
    }

    private boolean isCommentNode() {
        return more;
    }
}
