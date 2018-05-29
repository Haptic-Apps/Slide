package me.ccrama.redditslide.Tumblr;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)

@JsonPropertyOrder({
        "meta", "response"
})
public class TumblrPost {

    @JsonProperty("meta")
    private Meta     meta;
    @JsonProperty("response")
    private Response response;

    /**
     * @return The meta
     */
    @JsonProperty("meta")
    public Meta getMeta() {
        return meta;
    }

    /**
     * @param meta The meta
     */
    @JsonProperty("meta")
    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    /**
     * @return The response
     */
    @JsonProperty("response")
    public Response getResponse() {
        return response;
    }

    /**
     * @param response The response
     */
    @JsonProperty("response")
    public void setResponse(Response response) {
        this.response = response;
    }

}
