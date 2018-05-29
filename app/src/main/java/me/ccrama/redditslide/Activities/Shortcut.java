package me.ccrama.redditslide.Activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import me.ccrama.redditslide.Adapters.SubChooseAdapter;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.FontPreferences;

/**
 * Created by ccrama on 10/2/2015.
 */
public class Shortcut extends BaseActivity {
    private String name = "";

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1,
                    Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap =
                    Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                            Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTheme().applyStyle(new FontPreferences(this).getCommentFontStyle().getResId(), true);
        getTheme().applyStyle(new FontPreferences(this).getPostFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        super.onCreate(savedInstanceState);


        doShortcut();


    }

    View header;

    public void doShortcut() {

        setContentView(R.layout.activity_setup_widget);
        setupAppBar(R.id.toolbar, R.string.shortcut_creation_title, true, true);
        header = getLayoutInflater().inflate(R.layout.shortcut_header, null);
        ListView list = (ListView) findViewById(R.id.subs);

        list.addHeaderView(header);

        final ArrayList<String> sorted =
                UserSubscriptions.getSubscriptionsForShortcut(Shortcut.this);
        final SubChooseAdapter adapter =
                new SubChooseAdapter(this, sorted, UserSubscriptions.getAllSubreddits(this));
        list.setAdapter(adapter);

        (header.findViewById(R.id.sort)).clearFocus();
        ((EditText) header.findViewById(R.id.sort)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String result = editable.toString();
                adapter.getFilter().filter(result);
            }
        });
    }


}
