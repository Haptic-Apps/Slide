package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.MoreChildren;

/**
 * Created by ccrama on 10/3/2015.
 */
class CommentData {
    private final CommentNode n;
    private MoreChildren moreChildren;
    public Comment getComment(){
        return n.getComment();
    }
    public String getFullName(){
        if(isCommentNode()){
            return n.getComment().getFullName();
        } else {
            return n.getComment().getFullName() + "LOAD";
        }
    }

    public CommentData(CommentNode n){
        this.n = n;
        more = true;
    }
    private final boolean more;

    public CommentData(MoreChildren moreChildren, CommentNode n){
        this.moreChildren = moreChildren;
        more = false;
        this.n = n;

    }
    private boolean isCommentNode(){
        return more;
    }
}
