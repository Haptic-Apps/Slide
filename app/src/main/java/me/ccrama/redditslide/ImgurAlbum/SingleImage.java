package me.ccrama.redditslide.ImgurAlbum;

/**
 * Created by carlo_000 on 5/3/2016.
 */

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
        "id", "title", "description", "datetime", "type", "animated", "width", "height", "size",
        "views", "bandwidth", "vote", "favorite", "nsfw", "section", "account_url", "account_id",
        "in_gallery", "link"
})
public class SingleImage {

    @JsonProperty("id")
    private String  id;
    @JsonProperty("title")
    private String  title;
    @JsonProperty("description")
    private String  description;
    @JsonProperty("datetime")
    private Double  datetime;
    @JsonProperty("type")
    private String  type;
    @JsonProperty("animated")
    private Boolean animated;
    @JsonProperty("width")
    private Integer width;
    @JsonProperty("height")
    private Integer height;
    @JsonProperty("size")
    private Double  size;
    @JsonProperty("views")
    private Double  views;
    @JsonProperty("bandwidth")
    private Double  bandwidth;
    @JsonProperty("vote")
    private Object  vote;
    @JsonProperty("favorite")
    private Boolean favorite;
    @JsonProperty("nsfw")
    private Boolean nsfw;
    @JsonProperty("section")
    private String  section;
    @JsonProperty("account_url")
    private Object  accountUrl;
    @JsonProperty("account_id")
    private Object  accountId;
    @JsonProperty("in_gallery")
    private Boolean inGallery;
    @JsonProperty("link")
    private String  link;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * @return The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
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
     * @return The datetime
     */
    @JsonProperty("datetime")
    public Double getDatetime() {
        return datetime;
    }

    /**
     * @param datetime The datetime
     */
    @JsonProperty("datetime")
    public void setDatetime(Double datetime) {
        this.datetime = datetime;
    }

    /**
     * @return The type
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * @param type The type
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return The animated
     */
    @JsonProperty("animated")
    public Boolean getAnimated() {
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
    public Double getSize() {
        return size;
    }

    /**
     * @param size The size
     */
    @JsonProperty("size")
    public void setSize(Double size) {
        this.size = size;
    }

    /**
     * @return The views
     */
    @JsonProperty("views")
    public Double getViews() {
        return views;
    }

    /**
     * @param views The views
     */
    @JsonProperty("views")
    public void setViews(Double views) {
        this.views = views;
    }

    /**
     * @return The bandwidth
     */
    @JsonProperty("bandwidth")
    public Double getBandwidth() {
        return bandwidth;
    }

    /**
     * @param bandwidth The bandwidth
     */
    @JsonProperty("bandwidth")
    public void setBandwidth(Double bandwidth) {
        this.bandwidth = bandwidth;
    }

    /**
     * @return The vote
     */
    @JsonProperty("vote")
    public Object getVote() {
        return vote;
    }

    /**
     * @param vote The vote
     */
    @JsonProperty("vote")
    public void setVote(Object vote) {
        this.vote = vote;
    }

    /**
     * @return The favorite
     */
    @JsonProperty("favorite")
    public Boolean getFavorite() {
        return favorite;
    }

    /**
     * @param favorite The favorite
     */
    @JsonProperty("favorite")
    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    /**
     * @return The nsfw
     */
    @JsonProperty("nsfw")
    public Boolean getNsfw() {
        return nsfw;
    }

    /**
     * @param nsfw The nsfw
     */
    @JsonProperty("nsfw")
    public void setNsfw(Boolean nsfw) {
        this.nsfw = nsfw;
    }

    /**
     * @return The section
     */
    @JsonProperty("section")
    public String getSection() {
        return section;
    }

    /**
     * @param section The section
     */
    @JsonProperty("section")
    public void setSection(String section) {
        this.section = section;
    }

    /**
     * @return The accountUrl
     */
    @JsonProperty("account_url")
    public Object getAccountUrl() {
        return accountUrl;
    }

    /**
     * @param accountUrl The account_url
     */
    @JsonProperty("account_url")
    public void setAccountUrl(Object accountUrl) {
        this.accountUrl = accountUrl;
    }

    /**
     * @return The accountId
     */
    @JsonProperty("account_id")
    public Object getAccountId() {
        return accountId;
    }

    /**
     * @param accountId The account_id
     */
    @JsonProperty("account_id")
    public void setAccountId(Object accountId) {
        this.accountId = accountId;
    }

    /**
     * @return The inGallery
     */
    @JsonProperty("in_gallery")
    public Boolean getInGallery() {
        return inGallery;
    }

    /**
     * @param inGallery The in_gallery
     */
    @JsonProperty("in_gallery")
    public void setInGallery(Boolean inGallery) {
        this.inGallery = inGallery;
    }

    /**
     * @return The link
     */
    @JsonProperty("link")
    public String getLink() {
        return link;
    }

    /**
     * @param link The link
     */
    @JsonProperty("link")
    public void setLink(String link) {
        this.link = link;
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