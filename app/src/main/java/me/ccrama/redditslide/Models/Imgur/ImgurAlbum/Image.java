
package me.ccrama.redditslide.Models.Imgur.ImgurAlbum;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Image {

    @SerializedName("hash")
    @Expose
    private String hash;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("width")
    @Expose
    private int width;
    @SerializedName("height")
    @Expose
    private int height;
    @SerializedName("size")
    @Expose
    private int size;
    @SerializedName("ext")
    @Expose
    private String ext;
    @SerializedName("animated")
    @Expose
    private boolean animated;
    @SerializedName("prefer_video")
    @Expose
    private boolean preferVideo;
    @SerializedName("looping")
    @Expose
    private boolean looping;
    @SerializedName("datetime")
    @Expose
    private String datetime;

    /**
     *
     * @return
     * The hash
     */
    public String getHash() {
        return hash;
    }

    /**
     *
     * @param hash
     * The hash
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     *
     * @return
     * The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     * The width
     */
    public int getWidth() {
        return width;
    }

    /**
     *
     * @param width
     * The width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     *
     * @return
     * The height
     */
    public int getHeight() {
        return height;
    }

    /**
     *
     * @param height
     * The height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     *
     * @return
     * The size
     */
    public int getSize() {
        return size;
    }

    /**
     *
     * @param size
     * The size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     *
     * @return
     * The ext
     */
    public String getExt() {
        return ext;
    }

    /**
     *
     * @param ext
     * The ext
     */
    public void setExt(String ext) {
        this.ext = ext;
    }

    /**
     *
     * @return
     * The animated
     */
    public boolean isAnimated() {
        return animated;
    }

    /**
     *
     * @param animated
     * The animated
     */
    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    /**
     *
     * @return
     * The preferVideo
     */
    public boolean isPreferVideo() {
        return preferVideo;
    }

    /**
     *
     * @param preferVideo
     * The prefer_video
     */
    public void setPreferVideo(boolean preferVideo) {
        this.preferVideo = preferVideo;
    }

    /**
     *
     * @return
     * The looping
     */
    public boolean isLooping() {
        return looping;
    }

    /**
     *
     * @param looping
     * The looping
     */
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    /**
     *
     * @return
     * The datetime
     */
    public String getDatetime() {
        return datetime;
    }

    /**
     *
     * @param datetime
     * The datetime
     */
    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getImageUrl() {
        return "https://i.imgur.com/" + getHash() + getExt();
    }

    public String getThumbnailUrl() {
        return "https://i.imgur.com/" + getHash() + "s" + getExt();
    }
}
