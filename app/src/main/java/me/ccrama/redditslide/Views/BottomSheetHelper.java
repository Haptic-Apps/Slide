package me.ccrama.redditslide.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;

public class BottomSheetHelper {

    private BottomSheetDialog mBottomSheetDialog;
    private ViewGroup         mBottomSheetItems;
    private Context           mContext;
    private TextView          mHeader;
    private TextView          mItem;

    public BottomSheetHelper(Context context) {
        mContext = context;
        mBottomSheetDialog = getBottomSheetDialog();
    }

    private BottomSheetDialog getBottomSheetDialog() {
        mBottomSheetDialog = new BottomSheetDialog(mContext);
        setTheme();
        mBottomSheetDialog.setContentView(R.layout.dialog_fragment_bottom_sheet_vertical);
        mBottomSheetItems = mBottomSheetDialog.findViewById(R.id.bottom_sheet_vertical_items);
        return mBottomSheetDialog;
    }

    private void setTheme() {
        int theme = new ColorPreferences(mContext).getFontStyle().getThemeType();
        if (theme == ColorPreferences.Theme.light_amber.getThemeType()) {
            mBottomSheetDialog =
                    new BottomSheetDialog(mContext, R.style.BottomSheet_StyleDialog_Light);
        } else if (theme == ColorPreferences.Theme.sepia_amber.getThemeType()) {
            mBottomSheetDialog =
                    new BottomSheetDialog(mContext, R.style.BottomSheet_StyleDialog_Sepia);
        } else if (theme == ColorPreferences.Theme.blue_amber.getThemeType()) {
            mBottomSheetDialog =
                    new BottomSheetDialog(mContext, R.style.BottomSheet_StyleDialog_Dark_Blue);
        } else if (theme == ColorPreferences.Theme.dark_amber.getThemeType()) {
            mBottomSheetDialog =
                    new BottomSheetDialog(mContext, R.style.BottomSheet_StyleDialog_Dark);
        } else if (theme == ColorPreferences.Theme.pixel_amber.getThemeType()) {
            mBottomSheetDialog =
                    new BottomSheetDialog(mContext, R.style.BottomSheet_StyleDialog_PIXEL);
        } else if (theme == ColorPreferences.Theme.deep_amber.getThemeType()) {
            mBottomSheetDialog =
                    new BottomSheetDialog(mContext, R.style.BottomSheet_StyleDialog_Deep);
        } else if (theme == ColorPreferences.Theme.amoled_light_amber.getThemeType()
                || theme == ColorPreferences.Theme.amoled_amber.getThemeType()) {
            mBottomSheetDialog =
                    new BottomSheetDialog(mContext, R.style.BottomSheet_StyleDialog_AMOLED);
        } else if (theme == ColorPreferences.Theme.night_red_amber.getThemeType()) {
            mBottomSheetDialog =
                    new BottomSheetDialog(mContext, R.style.BottomSheet_StyleDialog_Night_Red);
        }
    }

    public BottomSheetDialog build() {
        return mBottomSheetDialog;
    }

    public void dismiss() {
        if (mBottomSheetDialog.isShowing()) {
            mBottomSheetDialog.dismiss();
        }
    }

    public void header(@StringRes int stringId) {
        header(mContext.getString(stringId));
    }

    public void header(Spanned text) {
        header(text.toString());
    }

    public void header(String text) {
        mHeader = mBottomSheetDialog.findViewById(R.id.header);
        if (mHeader != null) {
            mHeader.setVisibility(View.VISIBLE);
            mHeader.setText(text);
        }
    }

    public void header(@StringRes int stringId,
            @NonNull View.OnLongClickListener longClickListener) {
        header(mContext.getString(stringId), longClickListener);
    }

    public void header(Spanned text, @NonNull View.OnLongClickListener longClickListener) {
        header(text.toString(), longClickListener);
    }

    public void header(String text, @NonNull View.OnLongClickListener longClickListener) {
        header(text);
        if (mHeader != null) {
            mHeader.setClickable(true);
            mHeader.setFocusable(true);
            mHeader.setOnLongClickListener(longClickListener);
        }
    }

    private void createItem() {
        mItem = (TextView) mBottomSheetDialog.getLayoutInflater()
                .inflate(R.layout.sheet_item_vertical, null, false);
        mBottomSheetItems.addView(mItem);
        mItem.setId(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? View.generateViewId()
                        : generateViewId());
    }

    public void textView(@StringRes int stringId, @DrawableRes int drawableResId,
            @NonNull View.OnClickListener clickListener) {
        textView(mContext.getString(stringId), drawableResId, clickListener);
    }

    public void textView(String text, @DrawableRes int drawableResId,
            @NonNull View.OnClickListener clickListener) {
        createItem();
        if (mItem != null) {
            mItem.setVisibility(View.VISIBLE);
            mItem.setText(text);
            Drawable drawable = ContextCompat.getDrawable(mContext, drawableResId);
            if (drawable != null) {
                int[] attrs = new int[]{R.attr.tintColor};
                TypedArray ta = mContext.obtainStyledAttributes(attrs);
                int color = ta.getColor(0, Color.WHITE);
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                ta.recycle();
                mItem.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }
            mItem.setOnClickListener(clickListener);
        }
    }

    public void textView(@StringRes int stringId, @DrawableRes int drawableResId,
            @NonNull View.OnClickListener clickListener,
            @NonNull View.OnLongClickListener longClickListener) {
        textView(mContext.getString(stringId), drawableResId, clickListener, longClickListener);
    }

    public void textView(String text, @DrawableRes int drawableResId,
            @NonNull View.OnClickListener clickListener,
            @NonNull View.OnLongClickListener longClickListener) {
        textView(text, drawableResId, clickListener);
        if (mItem != null) {
            mItem.setOnLongClickListener(longClickListener);
        }
    }

    // This is needed since the method called in View only works on API 17+
    // Remove this once the minimum API version is 17+
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }
}
