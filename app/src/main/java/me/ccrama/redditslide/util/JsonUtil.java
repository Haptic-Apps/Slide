package me.ccrama.redditslide.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.GalleryImage;

/**
 * Created by TacoTheDank on 04/04/2021.
 */
public class JsonUtil {
    public static void getGalleryData(final JsonNode data, final ArrayList<GalleryImage> urls) {
        for (JsonNode identifier : data.get("gallery_data").get("items")) {
            if (data.has("media_metadata") && data.get(
                    "media_metadata")
                    .has(identifier.get("media_id").asText())
            ) {
                urls.add(new GalleryImage(data.get("media_metadata")
                        .get(identifier.get("media_id").asText())
                        .get("s")));
            }
        }
    }
}
