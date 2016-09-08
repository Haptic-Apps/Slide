
package me.ccrama.redditslide.Tumblr;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)

@JsonPropertyOrder({
    "caption",
    "alt_sizes",
    "original_size"
})
public class Photo {

    @JsonProperty("caption")
    private String caption;
    @JsonProperty("alt_sizes")
    private List<AltSize> altSizes = new ArrayList<AltSize>();
    @JsonProperty("original_size")
    private OriginalSize originalSize;

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
     *     The altSizes
     */
    @JsonProperty("alt_sizes")
    public List<AltSize> getAltSizes() {
        return altSizes;
    }

    /**
     * 
     * @param altSizes
     *     The alt_sizes
     */
    @JsonProperty("alt_sizes")
    public void setAltSizes(List<AltSize> altSizes) {
        this.altSizes = altSizes;
    }

    /**
     * 
     * @return
     *     The originalSize
     */
    @JsonProperty("original_size")
    public OriginalSize getOriginalSize() {
        return originalSize;
    }

    /**
     * 
     * @param originalSize
     *     The original_size
     */
    @JsonProperty("original_size")
    public void setOriginalSize(OriginalSize originalSize) {
        this.originalSize = originalSize;
    }

}
