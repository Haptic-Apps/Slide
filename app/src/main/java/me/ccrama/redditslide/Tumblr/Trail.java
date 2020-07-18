
package me.ccrama.redditslide.Tumblr;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)

@JsonPropertyOrder({
    "blog",
    "post",
    "content_raw",
    "content",
    "is_current_item",
    "is_root_item"
})
public class Trail {

    @JsonProperty("blog")
    private Blog_ blog;
    @JsonProperty("post")
    private Post_ post;
    @JsonProperty("content_raw")
    private String contentRaw;
    @JsonProperty("content")
    private String content;
    @JsonProperty("is_current_item")
    private Boolean isCurrentItem;
    @JsonProperty("is_root_item")
    private Boolean isRootItem;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The blog
     */
    @JsonProperty("blog")
    public Blog_ getBlog() {
        return blog;
    }

    /**
     * 
     * @param blog
     *     The blog
     */
    @JsonProperty("blog")
    public void setBlog(Blog_ blog) {
        this.blog = blog;
    }

    /**
     * 
     * @return
     *     The post
     */
    @JsonProperty("post")
    public Post_ getPost() {
        return post;
    }

    /**
     * 
     * @param post
     *     The post
     */
    @JsonProperty("post")
    public void setPost(Post_ post) {
        this.post = post;
    }

    /**
     * 
     * @return
     *     The contentRaw
     */
    @JsonProperty("content_raw")
    public String getContentRaw() {
        return contentRaw;
    }

    /**
     * 
     * @param contentRaw
     *     The content_raw
     */
    @JsonProperty("content_raw")
    public void setContentRaw(String contentRaw) {
        this.contentRaw = contentRaw;
    }

    /**
     * 
     * @return
     *     The content
     */
    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    /**
     * 
     * @param content
     *     The content
     */
    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 
     * @return
     *     The isCurrentItem
     */
    @JsonProperty("is_current_item")
    public Boolean getIsCurrentItem() {
        return isCurrentItem;
    }

    /**
     * 
     * @param isCurrentItem
     *     The is_current_item
     */
    @JsonProperty("is_current_item")
    public void setIsCurrentItem(Boolean isCurrentItem) {
        this.isCurrentItem = isCurrentItem;
    }

    /**
     * 
     * @return
     *     The isRootItem
     */
    @JsonProperty("is_root_item")
    public Boolean getIsRootItem() {
        return isRootItem;
    }

    /**
     * 
     * @param isRootItem
     *     The is_root_item
     */
    @JsonProperty("is_root_item")
    public void setIsRootItem(Boolean isRootItem) {
        this.isRootItem = isRootItem;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
