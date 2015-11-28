package me.ccrama.redditslide.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import net.dean.jraw.models.CommentNode;

import java.util.ArrayList;

import me.ccrama.redditslide.adapter.CommentAdapterSearch;
import me.ccrama.redditslide.adapter.CommentObject;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.view.PreCachingLayoutManager;
import me.ccrama.redditslide.visual.FontPreferences;

/**
 * Created by ccrama on 9/17/2015.
 */
public class CommentSearch extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        ArrayList<CommentObject> commentsOld = DataShare.sharedComments;
        setContentView(R.layout.activity_filtercomments);

        final EditText search = (EditText) findViewById(R.id.search);

        RecyclerView rv = (RecyclerView) findViewById(R.id.vertical_content);
        final PreCachingLayoutManager mLayoutManager;
        mLayoutManager = new PreCachingLayoutManager(this);
        rv.setLayoutManager(mLayoutManager);
        ArrayList<CommentNode> comments = new ArrayList<>();
        for (CommentObject o : commentsOld) {
            comments.add(o.getCommentNode());

        }
        final CommentAdapterSearch adapter = new CommentAdapterSearch(this, comments, rv, DataShare.subAuthor);
        rv.setAdapter(adapter);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String result = search.getText().toString();
                adapter.getFilter().filter(result);


            }
        });


    }


}
