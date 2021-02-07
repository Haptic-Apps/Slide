package me.ccrama.redditslide.util;

/**
 * Created by Carlos on 7/15/2016.
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
        "url",
        "author_name",
        "author_url",
        "html",
        "width",
        "height",
        "type",
        "cache_age",
        "provider_name",
        "provider_url",
        "version"
})
public class TwitterObject {

    @JsonProperty("url")
    private String url;
    @JsonProperty("author_name")
    private String authorName;
    @JsonProperty("author_url")
    private String authorUrl;
    @JsonProperty("html")
    private String html;
    @JsonProperty("width")
    private Integer width;
    @JsonProperty("height")
    private Integer height;
    @JsonProperty("type")
    private String type;
    @JsonProperty("cache_age")
    private String cacheAge;
    @JsonProperty("provider_name")
    private String providerName;
    @JsonProperty("provider_url")
    private String providerUrl;
    @JsonProperty("version")
    private String version;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The url
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return The authorName
     */
    @JsonProperty("author_name")
    public String getAuthorName() {
        return authorName;
    }

    /**
     * @param authorName The author_name
     */
    @JsonProperty("author_name")
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * @return The authorUrl
     */
    @JsonProperty("author_url")
    public String getAuthorUrl() {
        return authorUrl;
    }

    /**
     * @param authorUrl The author_url
     */
    @JsonProperty("author_url")
    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    /**
     * @return The html
     */
    @JsonProperty("html")
    public String getHtml() {
        return html;
    }

    /**
     * @param html The html
     */
    @JsonProperty("html")
    public void setHtml(String html) {
        this.html = html;
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
     * @return The cacheAge
     */
    @JsonProperty("cache_age")
    public String getCacheAge() {
        return cacheAge;
    }

    /**
     * @param cacheAge The cache_age
     */
    @JsonProperty("cache_age")
    public void setCacheAge(String cacheAge) {
        this.cacheAge = cacheAge;
    }

    /**
     * @return The providerName
     */
    @JsonProperty("provider_name")
    public String getProviderName() {
        return providerName;
    }

    /**
     * @param providerName The provider_name
     */
    @JsonProperty("provider_name")
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /**
     * @return The providerUrl
     */
    @JsonProperty("provider_url")
    public String getProviderUrl() {
        return providerUrl;
    }

    /**
     * @param providerUrl The provider_url
     */
    @JsonProperty("provider_url")
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    /**
     * @return The version
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * @param version The version
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
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