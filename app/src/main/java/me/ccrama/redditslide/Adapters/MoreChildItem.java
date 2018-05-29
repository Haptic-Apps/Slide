package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.MoreChildren;

/**
 * Created by carlo_000 on 1/23/2016.
 */
public class MoreChildItem extends CommentObject {
    public MoreChildren children;

    @Override
    public boolean isComment() {
        return false;
    }

    public MoreChildItem(CommentNode node, MoreChildren children) {
        comment = node;
        this.children = children;
    }

    @Override
    public String getName() {
        return comment.getComment().getFullName() + "more";
    }
}
