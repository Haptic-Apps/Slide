package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.ccrama.redditslide.Adapters.TumblrView;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.BlankFragment;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.Notifications.ImageDownloadNotificationService;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Tumblr.Photo;
import me.ccrama.redditslide.Tumblr.TumblrUtils;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.ToolbarColorizeHelper;
import me.ccrama.redditslide.util.LinkUtil;

/**
 * Created by ccrama on 9/7/2016. <p/> This class is responsible for accessing the Tumblr api to get
 * the image-related json data from a URL. It extends FullScreenActivity and supports swipe from
 * anywhere.
 */
public class Tumblr extends FullScreenActivity implements FolderChooserDialogCreate.FolderCallback {
    public static final String EXTRA_URL = "url";
    private List<Photo> images;
    public static final String SUBREDDIT = "subreddit";
    private int    adapterPosition;
    public  String subreddit;

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
            Intent i = new Intent(Tumblr.this, TumblrPager.class);
            int adapterPosition = getIntent().getIntExtra(MediaView.ADAPTER_POSITION, -1);
            i.putExtra(MediaView.ADAPTER_POSITION, adapterPosition);
            if (getIntent().hasExtra(MediaView.SUBMISSION_URL)) {
                i.putExtra(MediaView.SUBMISSION_URL,
                        getIntent().getStringExtra(MediaView.SUBMISSION_URL));
            }
            if(getIntent().hasExtra(SUBREDDIT)){
                i.putExtra(SUBREDDIT, getIntent().getStringExtra(SUBREDDIT));
            }
            i.putExtra("url", url);
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
            for (final Photo elem : images) {
                doImageSave(false, elem.getOriginalSize().getUrl());
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void doImageSave(boolean isGif, String contentUrl) {
        if (!isGif) {
            if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
                showFirstDialog();
            } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
                showErrorDialog();
            } else {
                Intent i = new Intent(this, ImageDownloadNotificationService.class);
                i.putExtra("actuallyLoaded", contentUrl);
                if (subreddit != null && !subreddit.isEmpty()) i.putExtra("subreddit", subreddit);
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
                            new FolderChooserDialogCreate.Builder(Tumblr.this).chooseButton(
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
        new AlertDialogWrapper.Builder(Tumblr.this).setTitle(R.string.err_something_wrong)
                .setMessage(R.string.err_couldnt_save_choose_new)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new FolderChooserDialogCreate.Builder(Tumblr.this).chooseButton(
                                R.string.btn_select)  // changes label of the choose button
                                .initialPath(Environment.getExternalStorageDirectory()
                                        .getPath())  // changes initial path, defaults to external storage directory
                                .show();
                    }
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    private void saveImageGallery(final Bitmap bitmap, String URL) {
        if (Reddit.appRestart.getString("imagelocation", "").isEmpty()) {
            showFirstDialog();
        } else if (!new File(Reddit.appRestart.getString("imagelocation", "")).exists()) {
            showErrorDialog();
        } else {
            File f = new File(Reddit.appRestart.getString("imagelocation", "")
                    + File.separator
                    + UUID.randomUUID().toString()
                    + ".png");


            FileOutputStream out = null;
            try {
                f.createNewFile();
                out = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog();
                }
            }
        }

    }

    public String url;

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

    public OverviewPagerAdapter album;

    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(
                new ColorPreferences(this).getDarkThemeSubreddit(ColorPreferences.FONT_STYLE),
                true);
        setContentView(R.layout.album);

        //Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final ViewPager pager = (ViewPager) findViewById(R.id.images);

        album = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(album);
        pager.setCurrentItem(1);
        if(getIntent().hasExtra(SUBREDDIT)){
            subreddit = getIntent().getStringExtra(SUBREDDIT);
        }
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                          @Override
                                          public void onPageScrolled(int position, float positionOffset,
                                                  int positionOffsetPixels) {
                                              if (position == 0 && positionOffsetPixels == 0) {
                                                  finish();
                                              }
                                              if (position == 0
                                                      && ((OverviewPagerAdapter) pager.getAdapter()).blankPage != null) {
                                                  if (((OverviewPagerAdapter) pager.getAdapter()).blankPage != null) {
                                                      ((OverviewPagerAdapter) pager.getAdapter()).blankPage.doOffset(
                                                              positionOffset);
                                                  }
                                                  ((OverviewPagerAdapter) pager.getAdapter()).blankPage.realBack.setBackgroundColor(
                                                          adjustAlpha(positionOffset * 0.7f));
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

    public static class OverviewPagerAdapter extends FragmentStatePagerAdapter {
        public BlankFragment blankPage;
        public AlbumFrag     album;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

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

    public int adjustAlpha(float factor) {
        int alpha = Math.round(Color.alpha(Color.BLACK) * factor);
        int red = Color.red(Color.BLACK);
        int green = Color.green(Color.BLACK);
        int blue = Color.blue(Color.BLACK);
        return Color.argb(alpha, red, green, blue);
    }

    public static class AlbumFrag extends Fragment {
        View rootView;
        public RecyclerView recyclerView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_verticalalbum, container, false);

            final PreCachingLayoutManager mLayoutManager;
            mLayoutManager = new PreCachingLayoutManager(getActivity());
            recyclerView = rootView.findViewById(R.id.images);
            recyclerView.setLayoutManager(mLayoutManager);
            ((Tumblr) getActivity()).url =
                    getActivity().getIntent().getExtras().getString(EXTRA_URL, "");

            ((BaseActivity) getActivity()).setShareUrl(((Tumblr) getActivity()).url);

            new LoadIntoRecycler(((Tumblr) getActivity()).url, getActivity()).executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
            ((Tumblr) getActivity()).mToolbar = rootView.findViewById(R.id.toolbar);
            ((Tumblr) getActivity()).mToolbar.setTitle(R.string.type_album);
            ToolbarColorizeHelper.colorizeToolbar(((Tumblr) getActivity()).mToolbar, Color.WHITE,
                    (getActivity()));
            ((Tumblr) getActivity()).setSupportActionBar(((Tumblr) getActivity()).mToolbar);
            ((Tumblr) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            ((Tumblr) getActivity()).mToolbar.setPopupTheme(
                    new ColorPreferences(getActivity()).getDarkThemeSubreddit(
                            ColorPreferences.FONT_STYLE));
            return rootView;
        }

        public class LoadIntoRecycler extends TumblrUtils.GetTumblrPostWithCallback {

            String url;

            public LoadIntoRecycler(@NotNull String url, @NotNull Activity baseActivity) {
                super(url, baseActivity);
                this.url = url;
            }

            @Override
            public void onError() {
                Intent i = new Intent(getActivity(), Website.class);
                i.putExtra(LinkUtil.EXTRA_URL, url);
                startActivity(i);
                getActivity().finish();
            }

            @Override
            public void doWithData(final List<Photo> jsonElements) {
                super.doWithData(jsonElements);
                if (getActivity() != null) {
                    getActivity().findViewById(R.id.progress).setVisibility(View.GONE);
                    ((Tumblr) getActivity()).images = new ArrayList<>(jsonElements);
                    TumblrView adapter =
                            new TumblrView(baseActivity, ((Tumblr) getActivity()).images,
                                    getActivity().findViewById(R.id.toolbar).getHeight(), ((Tumblr) getActivity()).subreddit);
                    recyclerView.setAdapter(adapter);
                }
            }
        }
    }

}