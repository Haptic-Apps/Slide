package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.CommentNode;

/**
 * Created by carlo_000 on 10/27/2015.
 */
public class CommentItem extends CommentObject {
    public CommentItem(CommentNode node) {
        comment = node;
        this.name = comment.getComment().getFullName();
    }

    @Override
    public boolean isComment() {
        return true;
    }

}
