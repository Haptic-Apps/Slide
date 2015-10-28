package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.MoreChildren;

/**
 * Created by carlo_000 on 10/27/2015.
 */
public class CommentObject {
    private CommentNode commentNode;
    public MoreChildren moreChildren;
    private CommentNode moreCommentNode;

    public CommentObject(CommentNode n){
        commentNode = n;
    }



    public CommentNode getCommentNode() {
        return commentNode;
    }


    public MoreChildren getMoreChildren() {
        return moreChildren;
    }

    public void setMoreChildren(MoreChildren moreChildren, CommentNode moreCommentNode) {
        this.moreChildren = moreChildren;
        this.moreCommentNode = moreCommentNode;
    }

    public CommentNode getMoreCommentNode() {
        return moreCommentNode;
    }
}
