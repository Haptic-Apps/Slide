package me.ccrama.redditslide.Views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.devbrackets.android.exomedia.listener.OnBufferUpdateListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoControls;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.devbrackets.android.exomedia.util.TimeFormatUtil;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by vishna on 22/07/15.
 */
//
//VideoView
//
//
//Created by Alex Ross on 1/29/13
//Modified to accept a Matrix by Wiseman Designs
//

public class MediaVideoView extends VideoView {

    private static final String LOG_TAG = "VideoView";
    public int  number;
    public View mute;
    OnPreparedListener mOnPreparedListener;
    private       int     currentBufferPercentage;
    private       Uri     uri;
    private final Context mContext;
    // Listeners
    private final OnBufferUpdateListener bufferingUpdateListener = new OnBufferUpdateListener() {
        @Override
        public void onBufferingUpdate(int percent) {
            currentBufferPercentage = percent;
        }
    };
    private final OnPreparedListener     preparedListener        = new OnPreparedListener() {
        @Override
        public void onPrepared() {
            LogUtil.v("Video prepared for " + number);

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared();
            }

            requestLayout();
            invalidate();
            start();
        }
    };
    private final OnErrorListener        errorListener           = new OnErrorListener() {
        @Override
        public boolean onError(Exception e) {
            Log.e(LOG_TAG, "There was an error during video playback.");
            return true;
        }
    };

    public MediaVideoView(final Context context) {
        super(context);
        mContext = context;
        initVideoView();
    }

    public MediaVideoView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initVideoView();
    }

    public MediaVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initVideoView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
                && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
                && keyCode != KeyEvent.KEYCODE_VOLUME_MUTE
                && keyCode != KeyEvent.KEYCODE_MENU
                && keyCode != KeyEvent.KEYCODE_CALL
                && keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isPlaying() && isKeyCodeSupported) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (isPlaying()) {
                        pause();
                        getVideoControls().show();
                    } else {
                        start();
                        getVideoControls().hide();
                    }
                    return true;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    if (!isPlaying()) {
                        start();
                        getVideoControls().hide();
                    }
                    return true;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    if (isPlaying()) {
                        pause();
                        getVideoControls().show();
                    }
                    return true;
                default:
                    toggleMediaControlsVisiblity();
                    break;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isPlaying()) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isPlaying() && ev.getAction() == MotionEvent.ACTION_UP) {
            toggleMediaControlsVisiblity();
        }
        return true;
    }

    public void setVideoURI(Uri _videoURI) {
        uri = _videoURI;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void setVideoPath(String path) {
        LogUtil.v("Setting video path to: " + path);
        setVideoURI(Uri.parse(path));
    }

    @Override
    public int getBufferPercentage() {
        return currentBufferPercentage;
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        this.mOnPreparedListener = onPreparedListener;
    }

    public void attachMediaControls() {
        setControls(new SlideVideoControls(mContext));
    }

    public void initVideoView() {
        LogUtil.v("Initializing video view.");
        setAlpha(0);
        setHandleAudioFocus(false);
        setFocusable(false);
    }

    public void openVideo() {
        if ((uri == null)) {
            LogUtil.v("Cannot open video, uri or surface is null number " + number);
            return;
        }
        animate().alpha(1);
        if (mute != null) {
            if (!SettingValues.isMuted) {
                setVolume(1f);
                ((ImageView) mute).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            } else {
                setVolume(0);
                ((ImageView) mute).setColorFilter(getResources().getColor(R.color.md_red_500),
                        PorterDuff.Mode.SRC_ATOP);
            }
            mute.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SettingValues.isMuted) {
                        setVolume(1f);
                        SettingValues.isMuted = false;
                        SettingValues.prefs.edit()
                                .putBoolean(SettingValues.PREF_MUTE, false)
                                .apply();
                        ((ImageView) mute).setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    } else {
                        setVolume(0);
                        SettingValues.isMuted = true;
                        SettingValues.prefs.edit()
                                .putBoolean(SettingValues.PREF_MUTE, true)
                                .apply();
                        ((ImageView) mute).setColorFilter(
                                getResources().getColor(R.color.md_red_500),
                                PorterDuff.Mode.SRC_ATOP);
                    }
                }
            });
        }

        try {

            attachMediaControls();
            setOnBufferUpdateListener(bufferingUpdateListener);
            setOnPreparedListener(preparedListener);
            setOnErrorListener(errorListener);
            setKeepScreenOn(true);

            DataSource.Factory dataSourceFactory =
                    new CacheDataSourceFactory(getContext(), 100 * 1024 * 1024, 5 * 1024 * 1024);

            setVideoURI(uri, null);

            audioFocusHelper.abandonFocus();

            LogUtil.v("Preparing media player.");
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        LogUtil.v("Resolve called.");
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                /* Parent says we can be as big as we want. Just don't be larger
                 * than max size imposed on ourselves.
                 */
                result = desiredSize;
                break;

            case MeasureSpec.AT_MOST:
                /* Parent says we can be as big as we want, up to specSize.
                 * Don't be larger than specSize, and don't be larger than
                 * the max size imposed on ourselves.
                 */
                result = Math.min(desiredSize, specSize);
                break;

            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    private void toggleMediaControlsVisiblity() {
        if (getVideoControls() != null) {
            if (getVideoControls().isVisible()) {
                getVideoControls().hide();
            } else {
                getVideoControls().show();
            }
        }
    }
}

class CacheDataSourceFactory implements DataSource.Factory {
    private final Context                  context;
    private final DefaultDataSourceFactory defaultDatasourceFactory;
    private final long                     maxFileSize, maxCacheSize;

    @Override
    public DataSource createDataSource() {
        LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(maxCacheSize);
        SimpleCache simpleCache =
                new SimpleCache(new File(context.getCacheDir(), "media"), evictor);
        return new CacheDataSource(simpleCache, defaultDatasourceFactory.createDataSource(),
                new FileDataSource(), new CacheDataSink(simpleCache, maxFileSize),
                CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                null);
    }

    CacheDataSourceFactory(Context context, long maxCacheSize, long maxFileSize) {
        super();
        this.context = context;
        this.maxCacheSize = maxCacheSize;
        this.maxFileSize = maxFileSize;
        String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        defaultDatasourceFactory = new DefaultDataSourceFactory(this.context, bandwidthMeter,
                new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter));
    }
}

class FadeInAnimation extends AnimationSet {
    private final View    animationView;
    private final boolean toVisible;

    public FadeInAnimation(View view, boolean toVisible, long duration) {
        super(false);
        this.toVisible = toVisible;
        this.animationView = view;

        //Creates the Alpha animation for the transition
        float startAlpha = toVisible ? 0 : 1;
        float endAlpha = toVisible ? 1 : 0;

        AlphaAnimation alphaAnimation = new AlphaAnimation(startAlpha, endAlpha);
        alphaAnimation.setDuration(duration);


        addAnimation(alphaAnimation);

        setAnimationListener(new FadeInAnimation.Listener());
    }

    private class Listener implements AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            animationView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            animationView.setVisibility(toVisible ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            //Purposefully left blank
        }
    }
}

class SlideVideoControls extends VideoControls {
    protected SeekBar      seekBar;
    protected LinearLayout extraViewsContainer;

    protected boolean userInteracting = false;

    public SlideVideoControls(Context context) {
        super(context);
    }

    public SlideVideoControls(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideVideoControls(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SlideVideoControls(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setPosition(@IntRange(from = 0) long position) {
        currentTimeTextView.setText(TimeFormatUtil.formatMs(position));
        seekBar.setProgress((int) position);
    }

    @Override
    public void setDuration(@IntRange(from = 0) long duration) {
        if (duration != seekBar.getMax()) {
            endTimeTextView.setText(TimeFormatUtil.formatMs(duration));
            seekBar.setMax((int) duration);
        }
    }

    @Override
    public void updateProgress(@IntRange(from = 0) long position, @IntRange(from = 0) long duration,
            @IntRange(from = 0, to = 100) int bufferPercent) {
        if (!userInteracting) {
            seekBar.setSecondaryProgress((int) (seekBar.getMax() * ((float) bufferPercent / 100)));
            seekBar.setProgress((int) position);
            currentTimeTextView.setText(TimeFormatUtil.formatMs(position));
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.media_controls;
    }

    @Override
    protected void animateVisibility(boolean toVisible) {
        if (isVisible == toVisible) {
            return;
        }

        if (!hideEmptyTextContainer || !isTextContainerEmpty()) {
            textContainer.startAnimation(new FadeInAnimation(textContainer, toVisible, 100));
        }

        if (!isLoading) {
            controlsContainer.startAnimation(
                    new FadeInAnimation(controlsContainer, toVisible, 100));
        }

        isVisible = toVisible;
        onVisibilityChanged();
    }

    @Override
    protected void updateTextContainerVisibility() {
        if (!isVisible) {
            return;
        }

        boolean emptyText = isTextContainerEmpty();
        if (hideEmptyTextContainer && emptyText && textContainer.getVisibility() == VISIBLE) {
            textContainer.clearAnimation();
            textContainer.startAnimation(
                    new FadeInAnimation(textContainer, false, CONTROL_VISIBILITY_ANIMATION_LENGTH));
        } else if ((!hideEmptyTextContainer || !emptyText)
                && textContainer.getVisibility() != VISIBLE) {
            textContainer.clearAnimation();
            textContainer.startAnimation(
                    new FadeInAnimation(textContainer, true, CONTROL_VISIBILITY_ANIMATION_LENGTH));
        }
    }

    @Override
    public void showLoading(boolean initialLoad) {
        if (isLoading) {
            return;
        }

        isLoading = true;
        loadingProgressBar.setVisibility(View.GONE);

        if (initialLoad) {
            controlsContainer.setVisibility(View.GONE);
        } else {
            playPauseButton.setEnabled(false);
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
        }
    }

    @Override
    public void finishLoading() {
        if (!isLoading) {
            return;
        }

        isLoading = false;
        loadingProgressBar.setVisibility(View.GONE);

        playPauseButton.setEnabled(true);
        previousButton.setEnabled(true);
        nextButton.setEnabled(true);
        updatePlaybackState(true);
        hide();
    }

    @Override
    public void updatePlaybackState(boolean isPlaying) {
        updatePlayPauseImage(isPlaying);
        progressPollRepeater.start();
    }

    @Override
    public void addExtraView(@NonNull View view) {
        extraViewsContainer.addView(view);
    }

    @Override
    public void removeExtraView(@NonNull View view) {
        extraViewsContainer.removeView(view);
    }

    @NonNull
    @Override
    public List<View> getExtraViews() {
        int childCount = extraViewsContainer.getChildCount();
        if (childCount <= 0) {
            return super.getExtraViews();
        }

        //Retrieves the layouts children
        List<View> children = new LinkedList<>();
        for (int i = 0; i < childCount; i++) {
            children.add(extraViewsContainer.getChildAt(i));
        }

        return children;
    }

    @Override
    public void show() {
        controlsContainer.setVisibility(View.VISIBLE);
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
        controlsContainer.setVisibility(View.GONE);

    }

    @Override
    public void hideDelayed(long delay) {
        hideDelay = delay;

        LogUtil.v("Hiding delayed");

        if (delay < 0 || !canViewHide || isLoading) {
            return;
        }

        //If the user is interacting with controls we don't want to start the delayed hide yet
        if (!userInteracting) {
            visibilityHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateVisibility(false);
                }
            }, delay);
        }
    }

    @Override
    protected void retrieveViews() {
        super.retrieveViews();
        seekBar = findViewById(com.devbrackets.android.exomedia.R.id.exomedia_controls_video_seek);
        extraViewsContainer = findViewById(
                com.devbrackets.android.exomedia.R.id.exomedia_controls_extra_container);
    }

    @Override
    protected void registerListeners() {
        super.registerListeners();
        seekBar.setOnSeekBarChangeListener(new SlideVideoControls.SeekBarChanged());
    }

    /**
     * Listens to the seek bar change events and correctly handles the changes
     */
    protected class SeekBarChanged implements SeekBar.OnSeekBarChangeListener {
        private long seekToTime;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }

            seekToTime = progress;
            if (currentTimeTextView != null) {
                currentTimeTextView.setText(TimeFormatUtil.formatMs(seekToTime));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            userInteracting = true;
            if (seekListener == null || !seekListener.onSeekStarted()) {
                internalListener.onSeekStarted();
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            userInteracting = false;
            if (seekListener == null || !seekListener.onSeekEnded(seekToTime)) {
                internalListener.onSeekEnded(seekToTime);
            }
        }
    }
}