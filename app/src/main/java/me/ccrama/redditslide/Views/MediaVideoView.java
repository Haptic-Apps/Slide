package me.ccrama.redditslide.Views;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.MediaController;

import java.io.IOException;

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

public class MediaVideoView extends TextureView implements MediaController.MediaPlayerControl {

    static final int NONE  = 0;
    static final int DRAG  = 1;
    static final int ZOOM  = 2;
    static final int CLICK = 3;
    private static final String LOG_TAG = "VideoView";
    // all possible internal states
    private static final int STATE_ERROR              = -1;
    private static final int STATE_IDLE               = 0;
    private static final int STATE_PREPARING          = 1;
    private static final int STATE_PREPARED           = 2;
    private static final int STATE_PLAYING            = 3;
    private static final int STATE_PAUSED             = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    public int number;
    int mode = NONE;
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
    MediaPlayer.OnPreparedListener mOnPreparedListener;
    float lastFocusX;
    float lastFocusY;
    SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width,
                final int height) {
            LogUtil.v("Surface texture now avaialble.");
            surfaceTexture = surface;
            openVideo();
        }

        @Override
        public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width,
                final int height) {
            LogUtil.v("Resized surface texture: " + width + '/' + height);
            surfaceWidth = width;
            surfaceHeight = height;
            boolean isValidState = (targetState == STATE_PLAYING);
            boolean hasValidSize = (videoWidth == width && videoHeight == height);
            if (mediaPlayer != null && isValidState && hasValidSize) {
                start();
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
            Log.i(LOG_TAG, "Destroyed surface number " + number);

            if (mediaController != null) mediaController.hide();

            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture surface) {

        }
    };
    // currentState is a VideoView object's current state.
    // targetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int currentState = STATE_IDLE;
    private int targetState  = STATE_IDLE;
    // Stuff we need for playing and showing a video
    public MediaPlayer                      mediaPlayer;
    private int                              videoWidth;
    private int                              videoHeight;
    private int                              surfaceWidth;
    private int                              surfaceHeight;
    private SurfaceTexture                   surfaceTexture;
    private Surface                          surface;
    private MediaController                  mediaController;
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
    private int mAudioSession;
    // Listeners
    private MediaPlayer.OnBufferingUpdateListener bufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(final MediaPlayer mp, final int percent) {
                    currentBufferPercentage = percent;
                }
            };
    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(final MediaPlayer mp) {
            currentState = STATE_PREPARED;
            LogUtil.v("Video prepared for " + number);
            videoWidth = mp.getVideoWidth();
            videoHeight = mp.getVideoHeight();


            mCanPause = mCanSeekBack = mCanSeekForward = true;

            if (mediaController != null) {
                mediaController.setEnabled(true);
            }
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mediaPlayer);
            }

            requestLayout();
            invalidate();
            if ((videoWidth != 0) && (videoHeight != 0)) {
                LogUtil.v(
                        "Video size for number " + number + ": " + videoWidth + '/' + videoHeight);
                if (targetState == STATE_PLAYING) {
                    mediaPlayer.start();
                }
            } else {
                if (targetState == STATE_PLAYING) {
                    mediaPlayer.start();
                }
            }
        }
    };
    private MediaPlayer.OnVideoSizeChangedListener videoSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(final MediaPlayer mp, final int width,
                        final int height) {
                    LogUtil.v("Video size changed " + width + '/' + height + " number " + number);
                }
            };
    private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(final MediaPlayer mp, final int what, final int extra) {
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
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mediaPlayer.isPlaying()) {
                    pause();
                    mediaController.show();
                } else {
                    start();
                    mediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mediaPlayer.isPlaying()) {
                    start();
                    mediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mediaPlayer.isPlaying()) {
                    pause();
                    mediaController.show();
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
        if (isInPlaybackState() && mediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState()
                && mediaController != null
                && ev.getAction() == MotionEvent.ACTION_UP) {
            toggleMediaControlsVisiblity();
        }
        return true;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // Will resize the view if the video dimensions have been found.
        // video dimensions are found after onPrepared has been called by MediaPlayer
        LogUtil.v("onMeasure number " + number);
        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);
        if ((videoWidth > 0) && (videoHeight > 0)) {
            if ((videoWidth * height) > (width * videoHeight)) {
                LogUtil.v("Image too tall, correcting.");
                height = (width * videoHeight) / videoWidth;
            } else if ((videoWidth * height) < (width * videoHeight)) {
                LogUtil.v("Image too wide, correcting.");
                width = (height * videoWidth) / videoHeight;
            } else {
                LogUtil.v("Aspect ratio is correct.");
            }
        }
        LogUtil.v("Setting size: " + width + '/' + height + " for number " + number);
        setMeasuredDimension((int) (width * widthScale), (int) (height * heightScale));
    }

    public void setSurfaceTexture(SurfaceTexture _surfaceTexture) {
        surfaceTexture = _surfaceTexture;
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mediaPlayer.start();
            currentState = STATE_PLAYING;
        }
        targetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                currentState = STATE_PAUSED;
            }
        }
        targetState = STATE_PAUSED;
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mediaPlayer.getDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            if (Build.VERSION.SDK_INT >= 26) {
                mediaPlayer.seekTo(msec, mSeekMode);
            } else {
                mediaPlayer.seekTo(msec);
            }

            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mediaPlayer != null) {
            return currentBufferPercentage;
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    public void initVideoView() {
        LogUtil.v("Initializing video view.");
        setAlpha(0);
        videoHeight = 0;
        videoWidth = 0;
        setFocusable(false);
        setSurfaceTextureListener(surfaceTextureListener);
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
        if ((uri == null) || (surfaceTexture == null)) {
            LogUtil.v("Cannot open video, uri or surface is null number " + number);
            return;
        }
        animate().alpha(1);

        // Tell the music playback service to pause

        LogUtil.v("Opening video.");
        release(false);
        try {
            surface = new Surface(surfaceTexture);
            LogUtil.v("Creating media player number " + number);
            mediaPlayer = new MediaPlayer();
            if (mAudioSession != 0) {
                mediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mediaPlayer.getAudioSessionId();
            }
            LogUtil.v("Setting surface.");
            mediaPlayer.setSurface(surface);
            LogUtil.v("Setting data source.");
            mediaPlayer.setDataSource(mContext, uri);
            LogUtil.v("Setting media player listeners.");

            mediaPlayer.setOnBufferingUpdateListener(bufferingUpdateListener);
            mediaPlayer.setOnPreparedListener(preparedListener);
            mediaPlayer.setOnErrorListener(errorListener);

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnVideoSizeChangedListener(videoSizeChangedListener);
            LogUtil.v("Preparing media player.");
            mediaPlayer.prepareAsync();
            currentState = STATE_PREPARING;
            attachMediaController();
        } catch (IllegalStateException e) {
            currentState = STATE_ERROR;
            targetState = STATE_ERROR;
            e.printStackTrace();
        } catch (IOException e) {
            currentState = STATE_ERROR;
            targetState = STATE_ERROR;
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

    public void resume() {
        openVideo();
    }

    public void setMatrix(Matrix mMatrix) {
        this.setTransform(mMatrix);
    }

    public void setMediaController(MediaController controller) {
        if (mediaController != null) {
            mediaController.hide();
        }
        mediaController = controller;
        attachMediaController();
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener onPreparedListener) {
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

    public void setViewScale(float width, float height) {
        this.widthScale = width;
        this.heightScale = height;
        this.invalidate();
    }

    public void setZOrderOnTop(boolean b) {
    }

    public void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void suspend() {
        release(false);
    }

    private void attachMediaController() {
        if (mediaPlayer != null && mediaController != null) {
            mediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
            mediaController.setAnchorView(anchorView);
            mediaController.setEnabled(isInPlaybackState());

        }
    }

    private boolean isInPlaybackState() {
        return (mediaPlayer != null &&
                currentState != STATE_ERROR &&
                currentState != STATE_IDLE &&
                currentState != STATE_PREPARING);
    }

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        LogUtil.v("Releasing media player.");
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            currentState = STATE_IDLE;
            if (cleartargetstate) {
                targetState = STATE_IDLE;
            }
            LogUtil.v("Released media player.");
        } else {
            LogUtil.v("Media player was null, did not release.");
        }
    }

    private void toggleMediaControlsVisiblity() {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            mediaController.show();
        }
    }

    private class ZoomOnTouchListeners implements View.OnTouchListener {


        public ZoomOnTouchListeners() {
            super();
            m = new float[9];
            mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        }


        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            mScaleDetector.onTouchEvent(motionEvent);

            matrix.getValues(m);
            float x = m[Matrix.MTRANS_X];
            float y = m[Matrix.MTRANS_Y];
            PointF curr = new PointF(motionEvent.getX(), motionEvent.getY());


            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    last.set(motionEvent.getX(), motionEvent.getY());
                    start.set(last);
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_UP:
                    mode = NONE;
                    int xDiff = (int) Math.abs(curr.x - start.x);
                    int yDiff = (int) Math.abs(curr.y - start.y);
                    //if (xDiff < CLICK && yDiff < CLICK)//TODO click event?
                    // performClick();
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    last.set(motionEvent.getX(), motionEvent.getY());
                    start.set(last);
                    mode = ZOOM;
                    break;
                case MotionEvent.ACTION_MOVE:
                    //if the mode is ZOOM or
                    //if the mode is DRAG and already zoomed
                    if (mode == ZOOM || (mode == DRAG && saveScale > minScale)) {
                        float deltaX = curr.x - last.x;// x difference
                        float deltaY = curr.y - last.y;// y difference
                        float scaleWidth = Math.round(
                                origWidth * saveScale);// width after applying current scale
                        float scaleHeight = Math.round(
                                origHeight * saveScale);// height after applying current scale
                        //if scaleWidth is smaller than the views width
                        //in other words if the image width fits in the view
                        //limit left and right movement
                        if (scaleWidth < width) {
                            deltaX = 0;
                            if (y + deltaY > 0) {
                                deltaY = -y;
                            } else if (y + deltaY < -bottom) deltaY = -(y + bottom);
                        }
                        //if scaleHeight is smaller than the views height
                        //in other words if the image height fits in the view
                        //limit up and down movement
                        else if (scaleHeight < height) {
                            deltaY = 0;
                            if (x + deltaX > 0) {
                                deltaX = -x;
                            } else if (x + deltaX < -right) deltaX = -(x + right);
                        }
                        //if the image doesnt fit in the width or height
                        //limit both up and down and left and right
                        else {
                            if (x + deltaX > 0) {
                                deltaX = -x;
                            } else if (x + deltaX < -right) deltaX = -(x + right);

                            if (y + deltaY > 0) {
                                deltaY = -y;
                            } else if (y + deltaY < -bottom) deltaY = -(y + bottom);
                        }
                        //move the image with the matrix
                        matrix.postTranslate(deltaX, deltaY);
                        //set the last touch location to the current
                        last.set(curr.x, curr.y);
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }
            MediaVideoView.this.setTransform(matrix);
            MediaVideoView.this.invalidate();
            return true;
        }

        private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float mScaleFactor = detector.getScaleFactor();
                float origScale = saveScale;
                saveScale *= mScaleFactor;
                if (saveScale > maxScale) {
                    saveScale = maxScale;
                    mScaleFactor = maxScale / origScale;
                } else if (saveScale < minScale) {
                    saveScale = minScale;
                    mScaleFactor = minScale / origScale;
                }
                right = width * saveScale - width - (2 * redundantXSpace * saveScale);
                bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
                if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
                    matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
                    if (mScaleFactor < 1) {
                        matrix.getValues(m);
                        float x = m[Matrix.MTRANS_X];
                        float y = m[Matrix.MTRANS_Y];
                        if (mScaleFactor < 1) {
                            if (Math.round(origWidth * saveScale) < width) {
                                if (y < -bottom) {
                                    matrix.postTranslate(0, -(y + bottom));
                                } else if (y > 0) matrix.postTranslate(0, -y);
                            } else {
                                if (x < -right) {
                                    matrix.postTranslate(-(x + right), 0);
                                } else if (x > 0) matrix.postTranslate(-x, 0);
                            }
                        }
                    }
                } else {
                    matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(),
                            detector.getFocusY());
                    matrix.getValues(m);
                    float x = m[Matrix.MTRANS_X];
                    float y = m[Matrix.MTRANS_Y];
                    if (mScaleFactor < 1) {
                        if (x < -right) {
                            matrix.postTranslate(-(x + right), 0);
                        } else if (x > 0) matrix.postTranslate(-x, 0);
                        if (y < -bottom) {
                            matrix.postTranslate(0, -(y + bottom));
                        } else if (y > 0) matrix.postTranslate(0, -y);
                    }
                }
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mode = ZOOM;
                return true;
            }
        }
    }
}