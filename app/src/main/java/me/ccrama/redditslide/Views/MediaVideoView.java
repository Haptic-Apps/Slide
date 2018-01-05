package me.ccrama.redditslide.Views;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.MediaController;

import com.devbrackets.android.exomedia.listener.OnBufferUpdateListener;
import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.listener.OnVideoSizeChangedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoControlsMobile;
import com.devbrackets.android.exomedia.ui.widget.VideoView;

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

    static final         int    NONE                     = 0;
    static final         int    DRAG                     = 1;
    static final         int    ZOOM                     = 2;
    static final         int    CLICK                    = 3;
    private static final String LOG_TAG                  = "VideoView";
    // all possible internal states
    private static final int    STATE_ERROR              = -1;
    private static final int    STATE_IDLE               = 0;
    private static final int    STATE_PREPARING          = 1;
    private static final int    STATE_PREPARED           = 2;
    private static final int    STATE_PLAYING            = 3;
    private static final int    STATE_PAUSED             = 4;
    private static final int    STATE_PLAYBACK_COMPLETED = 5;
    public int number;
    int    mode   = NONE;
    Matrix matrix = new Matrix();
    ScaleGestureDetector mScaleDetector;
    float minScale = 1f;
    float maxScale = 5f;
    float[] m;
    PointF last  = new PointF();
    PointF start = new PointF();
    float redundantXSpace, redundantYSpace;
    float width, height;
    float saveScale = 1f;
    float right, bottom, origWidth, origHeight, bmWidth, bmHeight;
    OnPreparedListener mOnPreparedListener;
    float              lastFocusX;
    float              lastFocusY;

    // currentState is a VideoView object's current state.
    // targetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int currentState = STATE_IDLE;
    private int targetState  = STATE_IDLE;
    // Stuff we need for playing and showing a video
    private int                              videoWidth;
    private int                              videoHeight;
    private int                              surfaceWidth;
    private int                              surfaceHeight;
    private MediaPlayer.OnCompletionListener onCompletionListener;
    private MediaPlayer.OnPreparedListener   onPreparedListener;
    private int                              currentBufferPercentage;
    private MediaPlayer.OnErrorListener      onErrorListener;
    private MediaPlayer.OnInfoListener       onInfoListener;
    private int                              mSeekWhenPrepared;
    private int                              mSeekMode;
    // recording the seek position while preparing
    private boolean                          mCanPause;
    private boolean                          mCanSeekBack;
    private boolean                          mCanSeekForward;
    private Uri                              uri;
    //scale stuff
    private float widthScale  = 1.0f;
    private float heightScale = 1.0f;
    private Context mContext;
    private int     mAudioSession;
    // Listeners
    private OnBufferUpdateListener bufferingUpdateListener = new OnBufferUpdateListener() {
        @Override
        public void onBufferingUpdate(int percent) {
            currentBufferPercentage = percent;
        }
    };
    private OnPreparedListener     preparedListener        = new OnPreparedListener() {
        @Override
        public void onPrepared() {
            currentState = STATE_PREPARED;
            LogUtil.v("Video prepared for " + number);


            mCanPause = mCanSeekBack = mCanSeekForward = true;

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared();
            }

            requestLayout();
            invalidate();
            if ((videoWidth != 0) && (videoHeight != 0)) {
                LogUtil.v(
                        "Video size for number " + number + ": " + videoWidth + '/' + videoHeight);
                if (targetState == STATE_PLAYING) {
                    start();
                }
            } else {
                if (targetState == STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    private OnVideoSizeChangedListener videoSizeChangedListener =
            new OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(final int width,
                        final int height) {
                    LogUtil.v("Video size changed " + width + '/' + height + " number " + number);
                }
            };
    private OnErrorListener            errorListener            =
            new OnErrorListener() {
                @Override
                public boolean onError(Exception e) {
                    currentState = STATE_ERROR;
                    targetState = STATE_ERROR;
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
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (isPlaying()) {
                    pause();
                    getVideoControls().show();
                } else {
                    start();
                    getVideoControls().hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!isPlaying()) {
                    start();
                    getVideoControls().hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (isPlaying()) {
                    pause();
                    getVideoControls().show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
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

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // Will resize the view if the video dimensions have been found.
        // video dimensions are found after onPrepared has been called by MediaPlayer
        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);
        if ((videoWidth > 0) && (videoHeight > 0)) {
            if ((videoWidth * height) > (width * videoHeight)) {
                height = (width * videoHeight) / videoWidth;
            } else if ((videoWidth * height) < (width * videoHeight)) {
                width = (height * videoWidth) / videoHeight;
            } else {
            }
        }
        setMeasuredDimension((int) (width * widthScale), (int) (height * heightScale));
    }



    @Override
    public int getBufferPercentage() {
        return currentBufferPercentage;
    }


    public void initVideoView() {
        LogUtil.v("Initializing video view.");
        setAlpha(0);
        videoHeight = 0;
        videoWidth = 0;
        setFocusable(false);
        //todo make this work better! setOnTouchListener(new ZoomOnTouchListeners());

        if (Build.VERSION.SDK_INT >= 26) {
            ActivityManager am =
                    (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

            // Only seek to seek-points if low ram device, otherwise seek to frame
            if (am.isLowRamDevice()) {
                LogUtil.d("MediaVideoView: using SEEK_CLOSEST_SYNC (low ram device)");
                mSeekMode = MediaPlayer.SEEK_CLOSEST_SYNC;
            } else {
                LogUtil.d("MediaVideoView: using SEEK_CLOSEST");
                mSeekMode = MediaPlayer.SEEK_CLOSEST;
            }
        } else {
            LogUtil.d("MediaVideoView: using SEEK_PREVIOUS_SYNC (API<26)");
        }
    }

    public void openVideo() {
        if ((uri == null)) {
            LogUtil.v("Cannot open video, uri or surface is null number " + number);
            return;
        }
        animate().alpha(1);

        // Tell the music playback service to pause

        try {
            setHandleAudioFocus(false);
            attachMediaControls();
            setOnBufferUpdateListener(bufferingUpdateListener);
            setOnPreparedListener(preparedListener);
            setOnErrorListener(errorListener);
            setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion() {
                    seekTo(0);
                    start();
                }
            });
            setKeepScreenOn(true);
            setOnVideoSizedChangedListener(videoSizeChangedListener);
            setVideoURI(uri, null);
            LogUtil.v("Preparing media player.");
            currentState = STATE_PREPARING;
        } catch (IllegalStateException e) {
            currentState = STATE_ERROR;
            targetState = STATE_ERROR;
            e.printStackTrace();
        }
    }

    public void attachMediaControls(){
        setControls(new VideoControlsMobile(mContext));
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

    public void resume() {
        openVideo();
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        this.mOnPreparedListener = onPreparedListener;
    }

    public void setVideoPath(String path) {
        LogUtil.v("Setting video path to: " + path);
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri _videoURI) {
        uri = _videoURI;
        openVideo();
        requestLayout();
        invalidate();
    }

    private void toggleMediaControlsVisiblity() {
        if (getVideoControls().isVisible()) {
            getVideoControls().hide();
        } else {
            getVideoControls().show();
        }
    }
}