package me.ccrama.redditslide.Activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.ExoVideoView;
import me.ccrama.redditslide.util.GifUtils;

/**
 * Created by ccrama on 01/29/2016.
 *
 * This activity is the basis for the possible inclusion of some sort of "Force Touch" preview system for comment links.
 *
 */
public class ForceTouchLink extends BaseActivityAnim {

    @Override
    public void onCreate(Bundle savedInstance) {

        overridePendingTransition(0, 0);
        super.onCreate(savedInstance);
        applyColorTheme();
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_force_touch_content);

        findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_POINTER_UP){
                    finish();
                }
                return false;
            }
        });

        final String url = getIntent().getExtras().getString("url");

        ContentType.Type t = ContentType.getContentType(url);

        final ImageView mainImage = (ImageView) findViewById(R.id.image);
        ExoVideoView mainVideo = (ExoVideoView) findViewById(R.id.gif);
        mainVideo.setVisibility(View.GONE);
        switch(t){
            case REDDIT:
            case IMGUR:
            case ALBUM:
            case REDDIT_GALLERY:
            case VIDEO:
                break;
            case IMAGE:
                ((Reddit)getApplication()).getImageLoader().displayImage(url, mainImage);

                break;
            case GIF:
                mainVideo.setVisibility(View.VISIBLE);
                new GifUtils.AsyncLoadGif(this, mainVideo,null,null,false, true, "").execute(url);
                break;
            case LINK:
                new AsyncTask<Void, Void, Void>() {
                    String urlGotten;
                    @Override
                    protected Void doInBackground(Void... params) {
                        /*try {
                           urlGotten =  ImageExtractor.extractImageUrl(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        ((Reddit)getApplication()).getImageLoader().displayImage(urlGotten, mainImage);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }

    }

}
