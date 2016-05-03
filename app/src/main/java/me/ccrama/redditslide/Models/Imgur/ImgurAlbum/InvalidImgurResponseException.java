package me.ccrama.redditslide.Models.Imgur.ImgurAlbum;

/**
 * Created by fb on 5/3/16.
 */
public class InvalidImgurResponseException extends Exception {
    private final static String ERROR_MESSAGE = "Imgur JSON response could not be de-serialized";

    public InvalidImgurResponseException() {
        super(ERROR_MESSAGE);
    }
}
