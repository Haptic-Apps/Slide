
package me.ccrama.redditslide.Models.Imgur.ImgurAlbum;

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
    private int width;
    @JsonProperty("height")
    private int height;
    @JsonProperty("size")
    private int size;
    @JsonProperty("ext")
    private String ext;
    @JsonProperty("animated")
    private boolean animated;
    @JsonProperty("prefer_video")
    private boolean preferVideo;
    @JsonProperty("looping")
    private boolean looping;
    @JsonProperty("datetime")
    private String datetime;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
    public int getWidth() {
        return width;
    }

    /**
     * @param width The width
     */
    @JsonProperty("width")
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return The height
     */
    @JsonProperty("height")
    public int getHeight() {
        return height;
    }

    /**
     * @param height The height
     */
    @JsonProperty("height")
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return The size
     */
    @JsonProperty("size")
    public int getSize() {
        return size;
    }

    /**
     * @param size The size
     */
    @JsonProperty("size")
    public void setSize(int size) {
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
    public boolean isAnimated() {
        return animated;
    }

    /**
     * @param animated The animated
     */
    @JsonProperty("animated")
    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    /**
     * @return The preferVideo
     */
    @JsonProperty("prefer_video")
    public boolean getPreferVideo() {
        return preferVideo;
    }

    /**
     * @param preferVideo The prefer_video
     */
    @JsonProperty("prefer_video")
    public void setPreferVideo(boolean preferVideo) {
        this.preferVideo = preferVideo;
    }

    /**
     * @return The looping
     */
    @JsonProperty("looping")
    public boolean getLooping() {
        return looping;
    }

    /**
     * @param looping The looping
     */
    @JsonProperty("looping")
    public void setLooping(boolean looping) {
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
        return "https://i.imgur.com/" + getHash() + getExt();
    }

    public String getThumbnailUrl() {
        return "https://i.imgur.com/" + getHash() + "s" + getExt();
    }
}
