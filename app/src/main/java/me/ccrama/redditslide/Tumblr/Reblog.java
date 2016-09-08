
package me.ccrama.redditslide.Tumblr;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)

@JsonPropertyOrder({
    "tree_html",
    "comment"
})
public class Reblog {

    @JsonProperty("tree_html")
    private String treeHtml;
    @JsonProperty("comment")
    private String comment;

    /**
     * 
     * @return
     *     The treeHtml
     */
    @JsonProperty("tree_html")
    public String getTreeHtml() {
        return treeHtml;
    }

    /**
     * 
     * @param treeHtml
     *     The tree_html
     */
    @JsonProperty("tree_html")
    public void setTreeHtml(String treeHtml) {
        this.treeHtml = treeHtml;
    }

    /**
     * 
     * @return
     *     The comment
     */
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    /**
     * 
     * @param comment
     *     The comment
     */
    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

}
