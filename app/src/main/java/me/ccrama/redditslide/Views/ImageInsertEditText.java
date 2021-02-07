package me.ccrama.redditslide.Views;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;


/**
 * Created by Carlos on 11/5/2016.
 */

public class ImageInsertEditText extends AppCompatEditText {

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
        EditorInfoCompat.setContentMimeTypes(attrs, new String[] { "image/gif", "image/png" });

        return InputConnectionCompat.createWrapper(con, attrs, new InputConnectionCompat.OnCommitContentListener() {
            @Override
            public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags, Bundle opts) {
                if (callback != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 &&
                            (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                        try {
                            inputContentInfo.requestPermission();
                        } catch (Exception e) {
                            return false;
                        }
                    }

                    callback.onImageSelected(
                            inputContentInfo.getContentUri(),
                            inputContentInfo.getDescription().getMimeType(0)
                    );

                    return true;
                } else {
                    return false;
                }
            }
        });
    }

}