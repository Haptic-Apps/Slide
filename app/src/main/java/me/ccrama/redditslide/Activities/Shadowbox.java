package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.WindowManager;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Fragments.AlbumFull;
import me.ccrama.redditslide.Fragments.GifFull;
import me.ccrama.redditslide.Fragments.ImageFull;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.FontPreferences;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Shadowbox extends BaseActivity {
    private ArrayList<Submission> posts;
    @Override
    public void onCreate(Bundle savedInstance) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstance);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_slide);

            posts = DataShare.sharedSubreddit;
        ViewPager pager = (ViewPager) findViewById(R.id.contentView);

        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));


    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {
            Fragment f ;
            ContentType.ImageType t = ContentType.getImageType(posts.get(i));
            switch(t){

                case NSFW_IMAGE: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case NSFW_GIF:
                {
                    f = new GifFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case NSFW_GFY:
                {
                    f = new GifFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case REDDIT:
                {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case EMBEDDED:
                {
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
                case SELF:
                {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case GFY:
                {
                    f = new GifFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case ALBUM:
                {
                    f = new AlbumFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case IMAGE:
                {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case GIF:
                {
                    f = new GifFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case NONE_GFY:
                {
                    f = new GifFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case NONE_GIF:
                {
                    f = new GifFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case NONE:
                {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case NONE_IMAGE:
                {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case VIDEO:
                {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                case NONE_URL:
                {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                    break;
                default:
                {
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
