package me.ccrama.redditslide.Views;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.os.BuildCompat;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;


/**
 * Created by Carlos on 11/5/2016.
 */

public class ImageInsertEditText extends android.support.v7.widget.AppCompatEditText {

    public interface ImageSelectedCallback {
        void onImageSelected(Uri content, String mimeType);
    }

    // region view constructors
    public ImageInsertEditText(Context context) {
        super(context);
    }

    public ImageInsertEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageInsertEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // endregion

    private ImageSelectedCallback callback;

    public void setImageSelectedCallback(ImageSelectedCallback callback) {
        this.callback = callback;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo attrs) {
        InputConnection con = super.onCreateInputConnection(attrs);
        EditorInfoCompat.setContentMimeTypes(attrs, new String[]{"image/gif", "image/png"});

        return InputConnectionCompat.createWrapper(con, attrs,
                new InputConnectionCompat.OnCommitContentListener() {
                    @Override
                    public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                            int flags, Bundle opts) {
                        if (callback != null) {
                            if (BuildCompat.isAtLeastNMR1()
                                    && (flags
                                    & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION)
                                    != 0) {
                                try {
                                    inputContentInfo.requestPermission();
                                } catch (Exception e) {
                                    return false;
                                }
                            }

                            callback.onImageSelected(inputContentInfo.getContentUri(),
                                    inputContentInfo.getDescription().getMimeType(0));

                            return true;
                        } else {
                            return false;
                        }
                    }
                });
    }

}