
package me.ccrama.redditslide.Tumblr;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)

@JsonPropertyOrder({
    "blog_name",
    "id",
    "post_url",
    "slug",
    "type",
    "date",
    "timestamp",
    "state",
    "format",
    "reblog_key",
    "tags",
    "short_url",
    "summary",
    "recommended_source",
    "recommended_color",
    "highlighted",
    "note_count",
    "caption",
    "reblog",
    "trail",
    "photoset_layout",
    "photos",
    "can_send_in_message",
    "can_like",
    "can_reblog",
    "display_avatar"
})
public class Post {

    @JsonProperty("blog_name")
    private String blogName;
    @JsonProperty("id")
    private Double id;
    @JsonProperty("post_url")
    private String postUrl;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("type")
    private String type;
    @JsonProperty("date")
    private String date;
    @JsonProperty("timestamp")
    private Double timestamp;
    @JsonProperty("state")
    private String state;
    @JsonProperty("format")
    private String format;
    @JsonProperty("reblog_key")
    private String reblogKey;
    @JsonProperty("tags")
    private List<String> tags = new ArrayList<String>();
    @JsonProperty("short_url")
    private String shortUrl;
    @JsonProperty("summary")
    private String summary;
    @JsonProperty("recommended_source")
    private Object recommendedSource;
    @JsonProperty("recommended_color")
    private Object recommendedColor;
    @JsonProperty("highlighted")
    private List<Object> highlighted = new ArrayList<Object>();
    @JsonProperty("note_count")
    private Integer noteCount;
    @JsonProperty("caption")
    private String caption;
    @JsonProperty("reblog")
    private Reblog reblog;
    @JsonProperty("trail")
    private List<Trail> trail = new ArrayList<Trail>();
    @JsonProperty("photoset_layout")
    private String photosetLayout;
    @JsonProperty("photos")
    private List<Photo> photos = new ArrayList<Photo>();
    @JsonProperty("can_send_in_message")
    private Boolean canSendInMessage;
    @JsonProperty("can_like")
    private Boolean canLike;
    @JsonProperty("can_reblog")
    private Boolean canReblog;
    @JsonProperty("display_avatar")
    private Boolean displayAvatar;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The blogName
     */
    @JsonProperty("blog_name")
    public String getBlogName() {
        return blogName;
    }

    /**
     * 
     * @param blogName
     *     The blog_name
     */
    @JsonProperty("blog_name")
    public void setBlogName(String blogName) {
        this.blogName = blogName;
    }

    /**
     * 
     * @return
     *     The id
     */
    @JsonProperty("id")
    public Double getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(Double id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The postUrl
     */
    @JsonProperty("post_url")
    public String getPostUrl() {
        return postUrl;
    }

    /**
     * 
     * @param postUrl
     *     The post_url
     */
    @JsonProperty("post_url")
    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    /**
     * 
     * @return
     *     The slug
     */
    @JsonProperty("slug")
    public String getSlug() {
        return slug;
    }

    /**
     * 
     * @param slug
     *     The slug
     */
    @JsonProperty("slug")
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The date
     */
    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    /**
     * 
     * @param date
     *     The date
     */
    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * 
     * @return
     *     The timestamp
     */
    @JsonProperty("timestamp")
    public Double getTimestamp() {
        return timestamp;
    }

    /**
     * 
     * @param timestamp
     *     The timestamp
     */
    @JsonProperty("timestamp")
    public void setTimestamp(Double timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 
     * @return
     *     The state
     */
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    /**
     * 
     * @param state
     *     The state
     */
    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    /**
     * 
     * @return
     *     The format
     */
    @JsonProperty("format")
    public String getFormat() {
        return format;
    }

    /**
     * 
     * @param format
     *     The format
     */
    @JsonProperty("format")
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * 
     * @return
     *     The reblogKey
     */
    @JsonProperty("reblog_key")
    public String getReblogKey() {
        return reblogKey;
    }

    /**
     * 
     * @param reblogKey
     *     The reblog_key
     */
    @JsonProperty("reblog_key")
    public void setReblogKey(String reblogKey) {
        this.reblogKey = reblogKey;
    }

    /**
     * 
     * @return
     *     The tags
     */
    @JsonProperty("tags")
    public List<String> getTags() {
        return tags;
    }

    /**
     * 
     * @param tags
     *     The tags
     */
    @JsonProperty("tags")
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * 
     * @return
     *     The shortUrl
     */
    @JsonProperty("short_url")
    public String getShortUrl() {
        return shortUrl;
    }

    /**
     * 
     * @param shortUrl
     *     The short_url
     */
    @JsonProperty("short_url")
    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    /**
     * 
     * @return
     *     The summary
     */
    @JsonProperty("summary")
    public String getSummary() {
        return summary;
    }

    /**
     * 
     * @param summary
     *     The summary
     */
    @JsonProperty("summary")
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * 
     * @return
     *     The recommendedSource
     */
    @JsonProperty("recommended_source")
    public Object getRecommendedSource() {
        return recommendedSource;
    }

    /**
     * 
     * @param recommendedSource
     *     The recommended_source
     */
    @JsonProperty("recommended_source")
    public void setRecommendedSource(Object recommendedSource) {
        this.recommendedSource = recommendedSource;
    }

    /**
     * 
     * @return
     *     The recommendedColor
     */
    @JsonProperty("recommended_color")
    public Object getRecommendedColor() {
        return recommendedColor;
    }

    /**
     * 
     * @param recommendedColor
     *     The recommended_color
     */
    @JsonProperty("recommended_color")
    public void setRecommendedColor(Object recommendedColor) {
        this.recommendedColor = recommendedColor;
    }

    /**
     * 
     * @return
     *     The highlighted
     */
    @JsonProperty("highlighted")
    public List<Object> getHighlighted() {
        return highlighted;
    }

    /**
     * 
     * @param highlighted
     *     The highlighted
     */
    @JsonProperty("highlighted")
    public void setHighlighted(List<Object> highlighted) {
        this.highlighted = highlighted;
    }

    /**
     * 
     * @return
     *     The noteCount
     */
    @JsonProperty("note_count")
    public Integer getNoteCount() {
        return noteCount;
    }

    /**
     * 
     * @param noteCount
     *     The note_count
     */
    @JsonProperty("note_count")
    public void setNoteCount(Integer noteCount) {
        this.noteCount = noteCount;
    }

    /**
     * 
     * @return
     *     The caption
     */
    @JsonProperty("caption")
    public String getCaption() {
        return caption;
    }

    /**
     * 
     * @param caption
     *     The caption
     */
    @JsonProperty("caption")
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * 
     * @return
     *     The reblog
     */
    @JsonProperty("reblog")
    public Reblog getReblog() {
        return reblog;
    }

    /**
     * 
     * @param reblog
     *     The reblog
     */
    @JsonProperty("reblog")
    public void setReblog(Reblog reblog) {
        this.reblog = reblog;
    }

    /**
     * 
     * @return
     *     The trail
     */
    @JsonProperty("trail")
    public List<Trail> getTrail() {
        return trail;
    }

    /**
     * 
     * @param trail
     *     The trail
     */
    @JsonProperty("trail")
    public void setTrail(List<Trail> trail) {
        this.trail = trail;
    }

    /**
     * 
     * @return
     *     The photosetLayout
     */
    @JsonProperty("photoset_layout")
    public String getPhotosetLayout() {
        return photosetLayout;
    }

    /**
     * 
     * @param photosetLayout
     *     The photoset_layout
     */
    @JsonProperty("photoset_layout")
    public void setPhotosetLayout(String photosetLayout) {
        this.photosetLayout = photosetLayout;
    }

    /**
     * 
     * @return
     *     The photos
     */
    @JsonProperty("photos")
    public List<Photo> getPhotos() {
        return photos;
    }

    /**
     * 
     * @param photos
     *     The photos
     */
    @JsonProperty("photos")
    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    /**
     * 
     * @return
     *     The canSendInMessage
     */
    @JsonProperty("can_send_in_message")
    public Boolean getCanSendInMessage() {
        return canSendInMessage;
    }

    /**
     * 
     * @param canSendInMessage
     *     The can_send_in_message
     */
    @JsonProperty("can_send_in_message")
    public void setCanSendInMessage(Boolean canSendInMessage) {
        this.canSendInMessage = canSendInMessage;
    }

    /**
     * 
     * @return
     *     The canLike
     */
    @JsonProperty("can_like")
    public Boolean getCanLike() {
        return canLike;
    }

    /**
     * 
     * @param canLike
     *     The can_like
     */
    @JsonProperty("can_like")
    public void setCanLike(Boolean canLike) {
        this.canLike = canLike;
    }

    /**
     * 
     * @return
     *     The canReblog
     */
    @JsonProperty("can_reblog")
    public Boolean getCanReblog() {
        return canReblog;
    }

    /**
     * 
     * @param canReblog
     *     The can_reblog
     */
    @JsonProperty("can_reblog")
    public void setCanReblog(Boolean canReblog) {
        this.canReblog = canReblog;
    }

    /**
     * 
     * @return
     *     The displayAvatar
     */
    @JsonProperty("display_avatar")
    public Boolean getDisplayAvatar() {
        return displayAvatar;
    }

    /**
     * 
     * @param displayAvatar
     *     The display_avatar
     */
    @JsonProperty("display_avatar")
    public void setDisplayAvatar(Boolean displayAvatar) {
        this.displayAvatar = displayAvatar;
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
