package me.ccrama.redditslide;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.core.text.HtmlCompat;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.dean.jraw.models.Submission;

import java.util.List;

import me.ccrama.redditslide.util.NetworkUtil;

public class PhotoLoader {

    public static void loadPhoto(final Context c, Submission submission) {
        String url;
        ContentType.Type type = ContentType.getContentType(submission);
        if (submission.getThumbnails() != null) {

            if (type == ContentType.Type.IMAGE
                    || type == ContentType.Type.SELF
                    || (submission.getThumbnailType() == Submission.ThumbnailType.URL)) {
                if (type == ContentType.Type.IMAGE) {
                    if (((!NetworkUtil.isConnectedWifi(c) && SettingValues.lowResMobile)
                            || SettingValues.lowResAlways)
                            && submission.getThumbnails() != null
                            && submission.getThumbnails().getVariations() != null
                            && submission.getThumbnails().getVariations().length > 0) {

                        int length = submission.getThumbnails().getVariations().length;
                        if (SettingValues.lqLow && length >= 3) {
                            url = HtmlCompat.fromHtml(
                                    submission.getThumbnails().getVariations()[2].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else if (SettingValues.lqMid && length >= 4) {
                            url = HtmlCompat.fromHtml(
                                    submission.getThumbnails().getVariations()[3].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else if (length >= 5) {
                            url = HtmlCompat.fromHtml(submission.getThumbnails().getVariations()[length - 1].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else {
                            url = HtmlCompat.fromHtml(submission.getThumbnails().getSource().getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
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

                    ((Reddit) c.getApplicationContext()).getImageLoader()
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

                } else if (submission.getThumbnails() != null) {

                    if (((!NetworkUtil.isConnectedWifi(c) && SettingValues.lowResMobile)
                            || SettingValues.lowResAlways)
                            && submission.getThumbnails().getVariations().length != 0) {

                        int length = submission.getThumbnails().getVariations().length;
                        if (SettingValues.lqLow && length >= 3) {
                            url = HtmlCompat.fromHtml(
                                    submission.getThumbnails().getVariations()[2].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else if (SettingValues.lqMid && length >= 4) {
                            url = HtmlCompat.fromHtml(
                                    submission.getThumbnails().getVariations()[3].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else if (length >= 5) {
                            url = HtmlCompat.fromHtml(submission.getThumbnails().getVariations()[length - 1].getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        } else {
                            url = HtmlCompat.fromHtml(submission.getThumbnails().getSource().getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                    .toString(); //unescape url characters
                        }

                    } else {
                        url = HtmlCompat.fromHtml(submission.getThumbnails().getSource().getUrl(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                                .toString(); //unescape url characters
                    }

                    ((Reddit) c.getApplicationContext()).getImageLoader()
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

                } else if (submission.getThumbnail() != null && (submission.getThumbnailType()
                        == Submission.ThumbnailType.URL
                        || submission.getThumbnailType() == Submission.ThumbnailType.NSFW)) {

                    ((Reddit) c.getApplicationContext()).getImageLoader()
                            .loadImage(submission.getUrl(), new ImageLoadingListener() {
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
            }
        }
    }

    public static void loadPhotos(final Context c, List<Submission> submissions) {
        for (Submission submission : submissions) {
            loadPhoto(c, submission);
        }
    }
}
