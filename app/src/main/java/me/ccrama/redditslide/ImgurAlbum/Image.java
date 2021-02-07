
package me.ccrama.redditslide.ImgurAlbum;

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
        "hash",
        "title",
        "description",
        "width",
        "height",
        "size",
        "ext",
        "animated",
        "prefer_video",
        "looping",
        "datetime"
})
public class Image {

    @JsonProperty("hash")
    private String hash;
    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("width")
    private Integer width;
    @JsonProperty("height")
    private Integer height;
    @JsonProperty("size")
    private Integer size;
    @JsonProperty("ext")
    private String ext;
    @JsonProperty("animated")
    private Boolean animated;
    @JsonProperty("prefer_video")
    private Boolean preferVideo;
    @JsonProperty("looping")
    private Boolean looping;
    @JsonProperty("datetime")
    private String datetime;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * @return The hash
     */
    @JsonProperty("hash")
    public String getHash() {
        return hash;
    }

    /**
     * @param hash The hash
     */
    @JsonProperty("hash")
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * @return The title
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The width
     */
    @JsonProperty("width")
    public Integer getWidth() {
        return width;
    }

    /**
     * @param width The width
     */
    @JsonProperty("width")
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * @return The height
     */
    @JsonProperty("height")
    public Integer getHeight() {
        return height;
    }

    /**
     * @param height The height
     */
    @JsonProperty("height")
    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     * @return The size
     */
    @JsonProperty("size")
    public Integer getSize() {
        return size;
    }

    /**
     * @param size The size
     */
    @JsonProperty("size")
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * @return The ext
     */
    @JsonProperty("ext")
    public String getExt() {
        return ext;
    }

    /**
     * @param ext The ext
     */
    @JsonProperty("ext")
    public void setExt(String ext) {
        this.ext = ext;
    }

    /**
     * @return The animated
     */
    @JsonProperty("animated")
    public Boolean isAnimated() {
        return animated;
    }

    /**
     * @param animated The animated
     */
    @JsonProperty("animated")
    public void setAnimated(Boolean animated) {
        this.animated = animated;
    }

    /**
     * @return The preferVideo
     */
    @JsonProperty("prefer_video")
    public Boolean getPreferVideo() {
        return preferVideo;
    }

    /**
     * @param preferVideo The prefer_video
     */
    @JsonProperty("prefer_video")
    public void setPreferVideo(Boolean preferVideo) {
        this.preferVideo = preferVideo;
    }

    /**
     * @return The looping
     */
    @JsonProperty("looping")
    public Boolean getLooping() {
        return looping;
    }

    /**
     * @param looping The looping
     */
    @JsonProperty("looping")
    public void setLooping(Boolean looping) {
        this.looping = looping;
    }

    /**
     * @return The datetime
     */
    @JsonProperty("datetime")
    public String getDatetime() {
        return datetime;
    }

    /**
     * @param datetime The datetime
     */
    @JsonProperty("datetime")
    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getImageUrl() {
        return "https://i.imgur.com/" + hash + ext;
    }

    public String getThumbnailUrl() {
        return "https://i.imgur.com/" + hash + "s" + ext;
    }
}
