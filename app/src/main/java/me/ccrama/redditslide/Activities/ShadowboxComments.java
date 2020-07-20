package me.ccrama.redditslide.Activities;

import android.os.Bundle;

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
        commentPager = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(commentPager);
    }

    OverviewPagerAdapter commentPager;

    public static class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = null;
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
                case EMBEDDED:
                case LINK:
                case STREAMABLE:
                case VIDEO:
                {
                    f = new MediaFragmentComment();
                    Bundle args = new Bundle();
                    args.putString("contentUrl", url);
                    args.putString("firstUrl", url);
                    args.putInt("page", i);
                    args.putString("sub", comment.getSubredditName());
                    f.setArguments(args);
                }
                break;
                case ALBUM: {
                    f = new AlbumFullComments();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
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
