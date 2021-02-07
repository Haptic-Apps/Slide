package me.ccrama.redditslide.Activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import net.dean.jraw.models.Comment;

import java.util.ArrayList;

import me.ccrama.redditslide.Adapters.CommentUrlObject;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.Fragments.AlbumFullComments;
import me.ccrama.redditslide.Fragments.MediaFragmentComment;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ShadowboxComments extends FullScreenActivity {
    public static ArrayList<CommentUrlObject> comments;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        if(comments == null || comments.isEmpty()){
            finish();
        }
        applyDarkColorTheme(comments.get(0).comment.getComment().getSubredditName());
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_slide);

        ViewPager pager = (ViewPager) findViewById(R.id.content_view);
        commentPager = new ShadowboxCommentsPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(commentPager);
    }

    ShadowboxCommentsPagerAdapter commentPager;

    private static class ShadowboxCommentsPagerAdapter extends FragmentStatePagerAdapter {

        ShadowboxCommentsPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int i) {

            Fragment f = null;
            Bundle args = new Bundle();
            Comment comment = comments.get(i).comment.getComment();

            String url = comments.get(i).url;

            ContentType.Type t = ContentType.getContentType(url);

            switch (t) {
                case GIF:
                case IMAGE:
                case IMGUR:
                case REDDIT:
                case EXTERNAL:
                case XKCD:
                case SPOILER:
                case DEVIANTART:
                case REDDIT_GALLERY:
                case EMBEDDED:
                case LINK:
                case STREAMABLE:
                case VIDEO:
                    f = new MediaFragmentComment();
                    args.putString("contentUrl", url);
                    args.putString("firstUrl", url);
                    args.putInt("page", i);
                    args.putString("sub", comment.getSubredditName());
                    f.setArguments(args);
                    break;
                case ALBUM:
                    f = new AlbumFullComments();
                    args.putInt("page", i);
                    f.setArguments(args);
                    break;
            }
            return f;
        }

        @Override
        public int getCount() {
            return comments.size() ;
        }
    }
}
