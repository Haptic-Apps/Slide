package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Adapters.MultiredditPosts;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.Fragments.AlbumFull;
import me.ccrama.redditslide.Fragments.AlbumFullComments;
import me.ccrama.redditslide.Fragments.MediaFragment;
import me.ccrama.redditslide.Fragments.MediaFragmentComment;
import me.ccrama.redditslide.Fragments.SelftextFull;
import me.ccrama.redditslide.Fragments.TitleFull;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.LastComments;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ShadowboxComments extends FullScreenActivity {
    public static ArrayList<Comment> comments;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        if(comments == null || comments.isEmpty()){
            finish();
        }
        applyDarkColorTheme(comments.get(0).getSubredditName());
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_slide);

        ViewPager pager = (ViewPager) findViewById(R.id.content_view);
        commentPager = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(commentPager);
    }

    OverviewPagerAdapter commentPager;

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = null;
            Comment comment = comments.get(i);

            String body = comment.getDataNode().get("body_html").asText();
            int start = body.indexOf("&lt;a href=\"");
            String url = body.substring(start + 12, body.indexOf("\"", start + 13));
            ContentType.Type t = ContentType.getContentType(url);

            switch (t) {
                case GIF:
                case IMAGE:
                case IMGUR:
                case REDDIT:
                case EXTERNAL:
                case SPOILER:
                case DEVIANTART:
                case EMBEDDED:
                case LINK:
                case VID_ME:
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
