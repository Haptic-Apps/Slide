package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Adapters.RedditGalleryView;
import me.ccrama.redditslide.Fragments.BlankFragment;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.Notifications.ImageDownloadNotificationService;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.ToolbarColorizeHelper;
import me.ccrama.redditslide.Visuals.ColorPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LinkUtil;

import static me.ccrama.redditslide.Notifications.ImageDownloadNotificationService.EXTRA_SUBMISSION_TITLE;

public class RedditGallery extends FullScreenActivity implements FolderChooserDialogCreate.FolderCallback {
    public static final String SUBREDDIT = "subreddit";
    public static final String GALLERY_URLS = "galleryurls";
    private List<GalleryImage> images;
    private int         adapterPosition;

    @Override
    public void onFolderSelection(FolderChooserDialogCreate dialog, File folder, boolean isSaveToLocation) {
        if (folder != null) {
            Reddit.appRestart.edit().putString("imagelocation", folder.getAbsolutePath()).apply();
            Toast.makeText(this,
                    getString(R.string.settings_set_image_location, folder.getAbsolutePath()),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        if (id == R.id.slider) {
            SettingValues.albumSwipe = true;
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ALBUM_SWIPE, true).apply();
            Intent i = new Intent(RedditGallery.this, RedditGalleryPager.class);
            int adapterPosition = getIntent().getIntExtra(MediaView.ADAPTER_POSITION, -1);
            i.putExtra(MediaView.ADAPTER_POSITION, adapterPosition);
            if (getIntent().hasExtra(MediaView.SUBMISSION_URL)) {
                i.putExtra(MediaView.SUBMISSION_URL,
                        getIntent().getStringExtra(MediaView.SUBMISSION_URL));
            }
            if (subreddit != null && !subreddit.isEmpty()) i.putExtra(RedditGalleryPager.SUBREDDIT, subreddit);
            if (submissionTitle != null) i.putExtra(EXTRA_SUBMISSION_TITLE, submissionTitle);
            Bundle urlsBundle = new Bundle();
            urlsBundle.putSerializable(RedditGallery.GALLERY_URLS, new ArrayList<GalleryImage>(images));
            i.putExtras(urlsBundle);

            startActivity(i);
            finish();
        }
        if (id == R.id.grid) {
            mToolbar.findViewById(R.id.grid).callOnClick();
        }
        if (id == R.id.comments) {
            SubmissionsView.datachanged(adapterPosition);
            finish();
        }
        if (id == R.id.external) {
            LinkUtil.openExternally(url);
        }
        if (id == R.id.download) {
            int index = 0;
            for (final GalleryImage elem : images) {
                doImageSave(false, elem.url, index);
                index++;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void doImageSave(boolean isGif, String contentUrl, int index) {
        if (!isGif) {
            if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
                showFirstDialog();
            } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
                showErrorDialog();
            } else {
                Intent i = new Intent(this, ImageDownloadNotificationService.class);
                i.putExtra("actuallyLoaded", contentUrl);
                if (subreddit != null && !subreddit.isEmpty()) i.putExtra("subreddit", subreddit);
                if (submissionTitle != null) i.putExtra(EXTRA_SUBMISSION_TITLE, submissionTitle);
                i.putExtra("index", index);
                startService(i);
            }
        } else {
            MediaView.doOnClick.run();
        }
    }

    public void showFirstDialog() {
        try {
            new AlertDialogWrapper.Builder(this).setTitle(R.string.set_save_location)
                    .setMessage(R.string.set_save_location_msg)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new FolderChooserDialogCreate.Builder(RedditGallery.this).chooseButton(
                                    R.string.btn_select)  // changes label of the choose button
                                    .initialPath(Environment.getExternalStorageDirectory()
                                            .getPath())  // changes initial path, defaults to external storage directory
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.btn_no, null)
                    .show();
        } catch (Exception ignored) {

        }
    }

    public void showErrorDialog() {
        new AlertDialogWrapper.Builder(RedditGallery.this).setTitle(R.string.err_something_wrong)
                .setMessage(R.string.err_couldnt_save_choose_new)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialogCreate.Builder(RedditGallery.this).chooseButton(
                                R.string.btn_select)  // changes label of the choose button
                                .initialPath(Environment.getExternalStorageDirectory()
                                        .getPath())  // changes initial path, defaults to external storage directory
                                .show();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    public String url;
    public String subreddit;
    private String submissionTitle;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.album_vertical, menu);
        adapterPosition = getIntent().getIntExtra(MediaView.ADAPTER_POSITION, -1);
        if (adapterPosition < 0) {
            menu.findItem(R.id.comments).setVisible(false);
        }
        return true;
    }

    public RedditGalleryPagerAdapter album;

    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(
                new ColorPreferences(this).getDarkThemeSubreddit(ColorPreferences.FONT_STYLE),
                true);
        setContentView(R.layout.album);

        //Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(getIntent().hasExtra(SUBREDDIT)){
            this.subreddit = getIntent().getExtras().getString(SUBREDDIT);
        }
        if (getIntent().hasExtra(EXTRA_SUBMISSION_TITLE)) {
            this.submissionTitle = getIntent().getExtras().getString(EXTRA_SUBMISSION_TITLE);
        }

        final ViewPager pager = (ViewPager) findViewById(R.id.images);

        album = new RedditGalleryPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(album);
        pager.setCurrentItem(1);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                          @Override
                                          public void onPageScrolled(int position, float positionOffset,
                                                  int positionOffsetPixels) {
                                              if (position == 0 && positionOffsetPixels == 0) {
                                                  finish();
                                              }
                                              if (position == 0
                                                      && ((RedditGalleryPagerAdapter) pager.getAdapter()).blankPage != null) {
                                                  if (((RedditGalleryPagerAdapter) pager.getAdapter()).blankPage
                                                          != null) {
                                                      ((RedditGalleryPagerAdapter) pager.getAdapter()).blankPage
                                                              .doOffset(positionOffset);
                                                  }
                                                  ((RedditGalleryPagerAdapter) pager.getAdapter()).blankPage.realBack.setBackgroundColor(
                                                          Palette.adjustAlpha(positionOffset * 0.7f));
                                              }
                                          }

                                          @Override
                                          public void onPageSelected(int position) {
                                          }

                                          @Override
                                          public void onPageScrollStateChanged(int state) {

                                          }
                                      }

        );

        if (!Reddit.appRestart.contains("tutorialSwipe")) {
            startActivityForResult(new Intent(this, SwipeTutorial.class), 3);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3) {
            Reddit.appRestart.edit().putBoolean("tutorialSwipe", true).apply();

        }
    }

    private static class RedditGalleryPagerAdapter extends FragmentStatePagerAdapter {
        BlankFragment blankPage;
        AlbumFrag     album;

        RedditGalleryPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                blankPage = new BlankFragment();
                return blankPage;
            } else {
                album = new AlbumFrag();
                return album;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static class AlbumFrag extends Fragment {
        View rootView;
        public RecyclerView recyclerView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_verticalalbum, container, false);

            final PreCachingLayoutManager mLayoutManager = new PreCachingLayoutManager(getActivity());
            recyclerView = rootView.findViewById(R.id.images);
            recyclerView.setLayoutManager(mLayoutManager);
            final RedditGallery galleryActivity = (RedditGallery) getActivity();
            galleryActivity.images = (ArrayList<GalleryImage>)
                    getActivity().getIntent().getSerializableExtra(RedditGallery.GALLERY_URLS);

            ((BaseActivity) getActivity()).setShareUrl(galleryActivity.url);

            galleryActivity.mToolbar = rootView.findViewById(R.id.toolbar);
            galleryActivity.mToolbar.setTitle(R.string.type_album);
            ToolbarColorizeHelper.colorizeToolbar(galleryActivity.mToolbar, Color.WHITE,
                    (getActivity()));
            galleryActivity.setSupportActionBar(galleryActivity.mToolbar);
            galleryActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            galleryActivity.mToolbar.setPopupTheme(
                    new ColorPreferences(getActivity()).getDarkThemeSubreddit(
                            ColorPreferences.FONT_STYLE));

            rootView.post(new Runnable() {
                @Override
                public void run() {
                    rootView.findViewById(R.id.progress).setVisibility(View.GONE);
                    RedditGalleryView adapter = new RedditGalleryView(galleryActivity, galleryActivity.images,
                            rootView.findViewById(R.id.toolbar).getHeight(), galleryActivity.subreddit,
                            galleryActivity.submissionTitle);
                    recyclerView.setAdapter(adapter);
                }
            });

            return rootView;
        }
    }

}
