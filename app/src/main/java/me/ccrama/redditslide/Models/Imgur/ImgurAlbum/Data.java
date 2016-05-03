
package me.ccrama.redditslide.Models.Imgur.ImgurAlbum;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Data {

    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("images")
    @Expose
    private List<Image> images = new ArrayList<>();

    /**
     *
     * @return
     *     The count
     */
    public int getCount() {
        return count;
    }

    /**
     *
     * @param count
     *     The count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     *
     * @return
     *     The images
     */
    public List<Image> getImages() {
        return images;
    }

    /**
     *
     * @param images
     *     The images
     */
    public void setImages(List<Image> images) {
        this.images = images;
    }
}
