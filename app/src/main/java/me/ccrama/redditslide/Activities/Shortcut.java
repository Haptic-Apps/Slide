package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import me.ccrama.redditslide.Adapters.SubChooseAdapter;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.ColorPreferences;
import me.ccrama.redditslide.Visuals.FontPreferences;

/**
 * Created by ccrama on 10/2/2015.
 */
public class Shortcut extends BaseActivity {

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
        ListView list = (ListView)findViewById(R.id.subs);

        list.addHeaderView(header);

        final ArrayList<String> sorted = UserSubscriptions.getSubscriptionsForShortcut(Shortcut.this);
        final SubChooseAdapter adapter = new SubChooseAdapter(this, sorted, UserSubscriptions.getAllSubreddits(this));
        list.setAdapter(adapter);

        (header.findViewById(R.id.sort)).clearFocus();
        ((EditText)header.findViewById(R.id.sort)).addTextChangedListener(new TextWatcher() {
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
