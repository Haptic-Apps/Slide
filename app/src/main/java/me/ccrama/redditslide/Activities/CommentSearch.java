package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import net.dean.jraw.models.CommentNode;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Adapters.CommentAdapterSearch;
import me.ccrama.redditslide.Adapters.CommentItem;
import me.ccrama.redditslide.Adapters.CommentObject;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;

/**
 * Created by ccrama on 9/17/2015.
 * <p/>
 * This activity takes the shared comment data and allows for searching through the text of the
 * CommentNodes.
 */
public class CommentSearch extends BaseActivityAnim {

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideRedditSwipeAnywhere();

        super.onCreate(savedInstance);
        applyColorTheme();
        setContentView(R.layout.activity_filtercomments);

        final EditText search = (EditText) findViewById(R.id.search);
        RecyclerView rv = (RecyclerView) findViewById(R.id.vertical_content);
        final PreCachingLayoutManager mLayoutManager;
        mLayoutManager = new PreCachingLayoutManager(this);
        rv.setLayoutManager(mLayoutManager);
        ArrayList<CommentNode> comments = new ArrayList<>();
        List<CommentObject> commentsOld = DataShare.sharedComments;
        if (commentsOld != null && !commentsOld.isEmpty())
            for (CommentObject o : commentsOld) {
                if (o instanceof CommentItem)
                    comments.add(o.comment);
            }
        else
            finish();
        final CommentAdapterSearch adapter = new CommentAdapterSearch(this, comments);
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
                adapter.setResult(result);
                adapter.getFilter().filter(result);
            }
        });
    }
}
