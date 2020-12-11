package me.ccrama.redditslide;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.core.text.HtmlCompat;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Thumbnails;

import java.util.List;

import me.ccrama.redditslide.util.NetworkUtil;

public class PhotoLoader {

    public static void loadPhoto(final Context c, Submission submission) {
        String url;
        ContentType.Type type = ContentType.getContentType(submission);
        Thumbnails thumbnails = submission.getThumbnails();
        Submission.ThumbnailType thumbnailType = submission.getThumbnailType();

        if (thumbnails != null) {

            if (type == ContentType.Type.IMAGE
                    || type == ContentType.Type.SELF
                    || (thumbnailType == Submission.ThumbnailType.URL)) {
                if (type == ContentType.Type.IMAGE) {
                    if ((!NetworkUtil.isConnectedWifi(c) && SettingValues.lowResMobile
                            || SettingValues.lowResAlways)
                            && thumbnails.getVariations() != null
                            && thumbnails.getVariations().length > 0) {

                        int length = thumbnails.getVariations().length;
                        if (SettingValues.lqLow && length >= 3) {
                            url = HtmlCompat.fromHtml(
                                    thumbnails.getVariations()[2].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else if (SettingValues.lqMid && length >= 4) {
                            url = HtmlCompat.fromHtml(
                                    thumbnails.getVariations()[3].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else if (length >= 5) {
                            url = HtmlCompat.fromHtml(thumbnails.getVariations()[length - 1].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else {
                            url = HtmlCompat.fromHtml(thumbnails.getSource().getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        }

                    } else {
                        if (submission.getDataNode().has("preview") && submission.getDataNode()
                                .get("preview")
                                .get("images")
                                .get(0)
                                .get("source")
                                .has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                            url = submission.getDataNode()
                                    .get("preview")
                                    .get("images")
                                    .get(0)
                                    .get("source")
                                    .get("url")
                                    .asText();
                        } else {
                            url = submission.getUrl();
                        }
                    }

                } else {

                    if ((!NetworkUtil.isConnectedWifi(c) && SettingValues.lowResMobile
                            || SettingValues.lowResAlways)
                            && thumbnails.getVariations().length != 0) {

                        int length = thumbnails.getVariations().length;
                        if (SettingValues.lqLow && length >= 3) {
                            url = HtmlCompat.fromHtml(
                                    thumbnails.getVariations()[2].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else if (SettingValues.lqMid && length >= 4) {
                            url = HtmlCompat.fromHtml(
                                    thumbnails.getVariations()[3].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else if (length >= 5) {
                            url = HtmlCompat.fromHtml(thumbnails.getVariations()[length - 1].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else {
                            url = HtmlCompat.fromHtml(thumbnails.getSource().getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        }

                    } else {
                        url = HtmlCompat.fromHtml(thumbnails.getSource().getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                .toString(); //unescape url characters
                    }
                }
                imageLoadingListener(c, url);
            }
        }
    }

    private static void imageLoadingListener(Context c, String url) {
        Reddit appContext = (Reddit) c.getApplicationContext();

        appContext.getImageLoader()
                .loadImage(url, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                                                FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                                                  Bitmap loadedImage) {
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                    }
                });
    }

    public static void loadPhotos(final Context c, List<Submission> submissions) {
        for (Submission submission : submissions) {
            loadPhoto(c, submission);
        }
    }
}
