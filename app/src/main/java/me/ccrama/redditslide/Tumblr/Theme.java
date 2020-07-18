
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
    "header_full_width",
    "header_full_height",
    "header_focus_width",
    "header_focus_height",
    "avatar_shape",
    "background_color",
    "body_font",
    "header_bounds",
    "header_image",
    "header_image_focused",
    "header_image_scaled",
    "header_stretch",
    "link_color",
    "show_avatar",
    "show_description",
    "show_header_image",
    "show_title",
    "title_color",
    "title_font",
    "title_font_weight"
})
public class Theme {

    @JsonProperty("header_full_width")
    private Integer headerFullWidth;
    @JsonProperty("header_full_height")
    private Integer headerFullHeight;
    @JsonProperty("header_focus_width")
    private Integer headerFocusWidth;
    @JsonProperty("header_focus_height")
    private Integer headerFocusHeight;
    @JsonProperty("avatar_shape")
    private String avatarShape;
    @JsonProperty("background_color")
    private String backgroundColor;
    @JsonProperty("body_font")
    private String bodyFont;
    @JsonProperty("header_bounds")
    private String headerBounds;
    @JsonProperty("header_image")
    private String headerImage;
    @JsonProperty("header_image_focused")
    private String headerImageFocused;
    @JsonProperty("header_image_scaled")
    private String headerImageScaled;
    @JsonProperty("header_stretch")
    private Boolean headerStretch;
    @JsonProperty("link_color")
    private String linkColor;
    @JsonProperty("show_avatar")
    private Boolean showAvatar;
    @JsonProperty("show_description")
    private Boolean showDescription;
    @JsonProperty("show_header_image")
    private Boolean showHeaderImage;
    @JsonProperty("show_title")
    private Boolean showTitle;
    @JsonProperty("title_color")
    private String titleColor;
    @JsonProperty("title_font")
    private String titleFont;
    @JsonProperty("title_font_weight")
    private String titleFontWeight;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The headerFullWidth
     */
    @JsonProperty("header_full_width")
    public Integer getHeaderFullWidth() {
        return headerFullWidth;
    }

    /**
     * 
     * @param headerFullWidth
     *     The header_full_width
     */
    @JsonProperty("header_full_width")
    public void setHeaderFullWidth(Integer headerFullWidth) {
        this.headerFullWidth = headerFullWidth;
    }

    /**
     * 
     * @return
     *     The headerFullHeight
     */
    @JsonProperty("header_full_height")
    public Integer getHeaderFullHeight() {
        return headerFullHeight;
    }

    /**
     * 
     * @param headerFullHeight
     *     The header_full_height
     */
    @JsonProperty("header_full_height")
    public void setHeaderFullHeight(Integer headerFullHeight) {
        this.headerFullHeight = headerFullHeight;
    }

    /**
     * 
     * @return
     *     The headerFocusWidth
     */
    @JsonProperty("header_focus_width")
    public Integer getHeaderFocusWidth() {
        return headerFocusWidth;
    }

    /**
     * 
     * @param headerFocusWidth
     *     The header_focus_width
     */
    @JsonProperty("header_focus_width")
    public void setHeaderFocusWidth(Integer headerFocusWidth) {
        this.headerFocusWidth = headerFocusWidth;
    }

    /**
     * 
     * @return
     *     The headerFocusHeight
     */
    @JsonProperty("header_focus_height")
    public Integer getHeaderFocusHeight() {
        return headerFocusHeight;
    }

    /**
     * 
     * @param headerFocusHeight
     *     The header_focus_height
     */
    @JsonProperty("header_focus_height")
    public void setHeaderFocusHeight(Integer headerFocusHeight) {
        this.headerFocusHeight = headerFocusHeight;
    }

    /**
     * 
     * @return
     *     The avatarShape
     */
    @JsonProperty("avatar_shape")
    public String getAvatarShape() {
        return avatarShape;
    }

    /**
     * 
     * @param avatarShape
     *     The avatar_shape
     */
    @JsonProperty("avatar_shape")
    public void setAvatarShape(String avatarShape) {
        this.avatarShape = avatarShape;
    }

    /**
     * 
     * @return
     *     The backgroundColor
     */
    @JsonProperty("background_color")
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * 
     * @param backgroundColor
     *     The background_color
     */
    @JsonProperty("background_color")
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * 
     * @return
     *     The bodyFont
     */
    @JsonProperty("body_font")
    public String getBodyFont() {
        return bodyFont;
    }

    /**
     * 
     * @param bodyFont
     *     The body_font
     */
    @JsonProperty("body_font")
    public void setBodyFont(String bodyFont) {
        this.bodyFont = bodyFont;
    }

    /**
     * 
     * @return
     *     The headerBounds
     */
    @JsonProperty("header_bounds")
    public String getHeaderBounds() {
        return headerBounds;
    }

    /**
     * 
     * @param headerBounds
     *     The header_bounds
     */
    @JsonProperty("header_bounds")
    public void setHeaderBounds(String headerBounds) {
        this.headerBounds = headerBounds;
    }

    /**
     * 
     * @return
     *     The headerImage
     */
    @JsonProperty("header_image")
    public String getHeaderImage() {
        return headerImage;
    }

    /**
     * 
     * @param headerImage
     *     The header_image
     */
    @JsonProperty("header_image")
    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

    /**
     * 
     * @return
     *     The headerImageFocused
     */
    @JsonProperty("header_image_focused")
    public String getHeaderImageFocused() {
        return headerImageFocused;
    }

    /**
     * 
     * @param headerImageFocused
     *     The header_image_focused
     */
    @JsonProperty("header_image_focused")
    public void setHeaderImageFocused(String headerImageFocused) {
        this.headerImageFocused = headerImageFocused;
    }

    /**
     * 
     * @return
     *     The headerImageScaled
     */
    @JsonProperty("header_image_scaled")
    public String getHeaderImageScaled() {
        return headerImageScaled;
    }

    /**
     * 
     * @param headerImageScaled
     *     The header_image_scaled
     */
    @JsonProperty("header_image_scaled")
    public void setHeaderImageScaled(String headerImageScaled) {
        this.headerImageScaled = headerImageScaled;
    }

    /**
     * 
     * @return
     *     The headerStretch
     */
    @JsonProperty("header_stretch")
    public Boolean getHeaderStretch() {
        return headerStretch;
    }

    /**
     * 
     * @param headerStretch
     *     The header_stretch
     */
    @JsonProperty("header_stretch")
    public void setHeaderStretch(Boolean headerStretch) {
        this.headerStretch = headerStretch;
    }

    /**
     * 
     * @return
     *     The linkColor
     */
    @JsonProperty("link_color")
    public String getLinkColor() {
        return linkColor;
    }

    /**
     * 
     * @param linkColor
     *     The link_color
     */
    @JsonProperty("link_color")
    public void setLinkColor(String linkColor) {
        this.linkColor = linkColor;
    }

    /**
     * 
     * @return
     *     The showAvatar
     */
    @JsonProperty("show_avatar")
    public Boolean getShowAvatar() {
        return showAvatar;
    }

    /**
     * 
     * @param showAvatar
     *     The show_avatar
     */
    @JsonProperty("show_avatar")
    public void setShowAvatar(Boolean showAvatar) {
        this.showAvatar = showAvatar;
    }

    /**
     * 
     * @return
     *     The showDescription
     */
    @JsonProperty("show_description")
    public Boolean getShowDescription() {
        return showDescription;
    }

    /**
     * 
     * @param showDescription
     *     The show_description
     */
    @JsonProperty("show_description")
    public void setShowDescription(Boolean showDescription) {
        this.showDescription = showDescription;
    }

    /**
     * 
     * @return
     *     The showHeaderImage
     */
    @JsonProperty("show_header_image")
    public Boolean getShowHeaderImage() {
        return showHeaderImage;
    }

    /**
     * 
     * @param showHeaderImage
     *     The show_header_image
     */
    @JsonProperty("show_header_image")
    public void setShowHeaderImage(Boolean showHeaderImage) {
        this.showHeaderImage = showHeaderImage;
    }

    /**
     * 
     * @return
     *     The showTitle
     */
    @JsonProperty("show_title")
    public Boolean getShowTitle() {
        return showTitle;
    }

    /**
     * 
     * @param showTitle
     *     The show_title
     */
    @JsonProperty("show_title")
    public void setShowTitle(Boolean showTitle) {
        this.showTitle = showTitle;
    }

    /**
     * 
     * @return
     *     The titleColor
     */
    @JsonProperty("title_color")
    public String getTitleColor() {
        return titleColor;
    }

    /**
     * 
     * @param titleColor
     *     The title_color
     */
    @JsonProperty("title_color")
    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    /**
     * 
     * @return
     *     The titleFont
     */
    @JsonProperty("title_font")
    public String getTitleFont() {
        return titleFont;
    }

    /**
     * 
     * @param titleFont
     *     The title_font
     */
    @JsonProperty("title_font")
    public void setTitleFont(String titleFont) {
        this.titleFont = titleFont;
    }

    /**
     * 
     * @return
     *     The titleFontWeight
     */
    @JsonProperty("title_font_weight")
    public String getTitleFontWeight() {
        return titleFontWeight;
    }

    /**
     * 
     * @param titleFontWeight
     *     The title_font_weight
     */
    @JsonProperty("title_font_weight")
    public void setTitleFontWeight(String titleFontWeight) {
        this.titleFontWeight = titleFontWeight;
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
