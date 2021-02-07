
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
    "name",
    "active",
    "theme",
    "share_likes",
    "share_following"
})
public class Blog_ {

    @JsonProperty("name")
    private String name;
    @JsonProperty("active")
    private Boolean active;
    @JsonProperty("theme")
    private Theme theme;
    @JsonProperty("share_likes")
    private Boolean shareLikes;
    @JsonProperty("share_following")
    private Boolean shareFollowing;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *     The active
     */
    @JsonProperty("active")
    public Boolean getActive() {
        return active;
    }

    /**
     * 
     * @param active
     *     The active
     */
    @JsonProperty("active")
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * 
     * @return
     *     The theme
     */
    @JsonProperty("theme")
    public Theme getTheme() {
        return theme;
    }

    /**
     * 
     * @param theme
     *     The theme
     */
    @JsonProperty("theme")
    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    /**
     * 
     * @return
     *     The shareLikes
     */
    @JsonProperty("share_likes")
    public Boolean getShareLikes() {
        return shareLikes;
    }

    /**
     * 
     * @param shareLikes
     *     The share_likes
     */
    @JsonProperty("share_likes")
    public void setShareLikes(Boolean shareLikes) {
        this.shareLikes = shareLikes;
    }

    /**
     * 
     * @return
     *     The shareFollowing
     */
    @JsonProperty("share_following")
    public Boolean getShareFollowing() {
        return shareFollowing;
    }

    /**
     * 
     * @param shareFollowing
     *     The share_following
     */
    @JsonProperty("share_following")
    public void setShareFollowing(Boolean shareFollowing) {
        this.shareFollowing = shareFollowing;
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
