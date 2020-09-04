package me.ccrama.redditslide.Activities;


import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.text.StringEscapeUtils;

import java.io.Serializable;

/**
 * Created by ccrama on 09/22/2020.
 */
public class GalleryImage implements Serializable {
    public String url;
    public int width;
    public int height;

    public GalleryImage(JsonNode data) {
        url = StringEscapeUtils.unescapeHtml4(data.get("u").asText());
        width = data.get("x").asInt();
        height = data.get("y").asInt();
    }
}
