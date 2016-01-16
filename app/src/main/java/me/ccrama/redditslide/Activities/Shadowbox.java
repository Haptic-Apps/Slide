package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Fragments.AlbumFull;
import me.ccrama.redditslide.Fragments.Gif;
import me.ccrama.redditslide.Fragments.ImageFull;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Shadowbox extends FullScreenActivity {
    private List<Submission> posts;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);
        applyColorTheme();
        setContentView(R.layout.activity_slide);

        posts = DataShare.sharedSubreddit;
        ViewPager pager = (ViewPager) findViewById(R.id.content_view);

        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {
            Fragment f;
            ContentType.ImageType t = ContentType.getImageType(posts.get(i));
            switch (t) {

                case NSFW_IMAGE: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NSFW_GIF: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NSFW_GFY: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case REDDIT: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case EMBEDDED: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case LINK: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case IMAGE_LINK: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NSFW_LINK: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case SELF: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case GFY: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case ALBUM: {
                    f = new AlbumFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case IMAGE: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case GIF: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NONE_GFY: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NONE_GIF: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NONE: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NONE_IMAGE: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case VIDEO: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NONE_URL: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                default: {
                    f = new ImageFull();
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
            if (posts == null) {
                return 1;
            } else {
                return posts.size();
            }
        }


    }

}
