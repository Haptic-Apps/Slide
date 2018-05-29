package me.ccrama.redditslide.ImgurAlbum;

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
        "count", "images"
})
public class Data {

    @JsonProperty("count")
    private Integer count;
    @JsonProperty("images")
    private       List<Image>         images               = new ArrayList<>();
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * @return The count
     */
    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    /**
     * @param count The count
     */
    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * @return The images
     */
    @JsonProperty("images")
    public List<Image> getImages() {
        return images;
    }

    /**
     * @param images The images
     */
    @JsonProperty("images")
    public void setImages(List<Image> images) {
        this.images = images;
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
