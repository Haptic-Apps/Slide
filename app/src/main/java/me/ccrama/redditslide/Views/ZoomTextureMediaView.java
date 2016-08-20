package me.ccrama.redditslide.Views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Vector;

import me.ccrama.redditslide.util.LogUtil;

/**
 * @author Carlos on 08/20/16
 *
 * Based on code by by vishna on 22/07/15.
 *
 * Zooming control code from https://github.com/Shuhrat-java/PanZoomPlayer
 */
public class ZoomTextureMediaView extends TextureView
        implements MediaController.MediaPlayerControl, TextureView.SurfaceTextureListener {
    private Matrix mMatrix;

    private ScaleGestureDetector mScaleDetector;

    private MoveGestureDetector mMoveDetector;

    private float mScaleFactor = 1.f;

    private float mFocusX = 0.f;

    private float mFocusY = 0.f;

    private MediaController controller;

    // all possible internal states
    private static final int    STATE_ERROR              = -1;
    private static final int    STATE_IDLE               = 0;
    private static final int    STATE_PREPARING          = 1;
    private static final int    STATE_PREPARED           = 2;
    private static final int    STATE_PLAYING            = 3;
    private static final int    STATE_PAUSED             = 4;
    private static final int    STATE_PLAYBACK_COMPLETED = 5;
    private              String TAG                      = "VideoView";
    // settable by the client
    private Uri                 mUri;
    private Map<String, String> mHeaders;
    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState  = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private Surface s;
    private MediaPlayer mMediaPlayer = null;
    private int mAudioSession;
    private int mVideoWidth;
    private int mVideoHeight;
    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        requestLayout();
                    }
                }
            };
    private MediaController                  mMediaController;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener   mOnPreparedListener;
    private int                              mCurrentBufferPercentage;
    private MediaPlayer.OnErrorListener      mOnErrorListener;
    private MediaPlayer.OnInfoListener       mOnInfoListener;
    private int                              mSeekWhenPrepared;
    // recording the seek position while preparing
    private boolean                          mCanPause;
    private boolean                          mCanSeekBack;
    private boolean                          mCanSeekForward;
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;

            // Get the capabilities of the player for this stream
//         Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
//            MediaPlayer.BYPASS_METADATA_FILTER);

//         if (data != null) {
//            mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
//               || data.getBoolean(Metadata.PAUSE_AVAILABLE);
//            mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
//               || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
//            mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
//               || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
//         } else {
            mCanPause = mCanSeekBack = mCanSeekForward = true;
//         }

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition =
                    mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }

            if (mTargetState == STATE_PLAYING) {
                start();
            }

        }
    };
    private Vector<Pair<InputStream, MediaFormat>> mPendingSubtitleTracks;
    private MediaPlayer.OnCompletionListener      mCompletionListener      =
            new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mCurrentState = STATE_PLAYBACK_COMPLETED;
                    mTargetState = STATE_PLAYBACK_COMPLETED;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }
            };
    private MediaPlayer.OnInfoListener            mInfoListener            =
            new MediaPlayer.OnInfoListener() {
                public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mp, arg1, arg2);
                    }
                    return true;
                }
            };
    private MediaPlayer.OnErrorListener           mErrorListener           =
            new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                    Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;
                    if (mMediaController != null) {
                        mMediaController.hide();
                    }

            /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

            /* Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog
             * if we're attached to a window. When we're going away and no
             * longer have a window, don't bother showing the user an error.
             */
//            if (getWindowToken() != null) {
//               Resources r = getContext().getResources();
//               int messageId;
//
//               if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
//                  messageId = com.android.internal.R.string.VideoView_error_text_invalid_progressive_playback;
//               } else {
//                  messageId = com.android.internal.R.string.VideoView_error_text_unknown;
//               }
//
//               new AlertDialogWrapper.Builder(getContext())
//                  .setMessage(messageId)
//                  .setPositiveButton(com.android.internal.R.string.VideoView_error_button,
//                     new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                                        /* If we get here, there is no onError listener, so
//                                         * at least inform them that the video is over.
//                                         */
//                           if (mOnCompletionListener != null) {
//                              mOnCompletionListener.onCompletion(mMediaPlayer);
//                           }
//                        }
//                     })
//                  .setCancelable(false)
//                  .show();
//            }
                    return true;
                }
            };
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };

    public ZoomTextureMediaView(Context context) {
        super(context);
        initVideoView();
    }

    public ZoomTextureMediaView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initVideoView();
    }

    public ZoomTextureMediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ZoomTextureMediaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVideoView();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
        //        + MeasureSpec.toString(heightMeasureSpec) + ")");

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(MediaVideoView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(MediaVideoView.class.getName());
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        s = new Surface(surface);
        LogUtil.v("Available");
        if (mUri != null) {
            start();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void setZOrderOnTop(boolean b) {
    }


    private void initVideoView() {
        mMatrix = new Matrix();

        // Setup Gesture Detectors
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        mMoveDetector = new MoveGestureDetector(getContext(), new MoveListener());

        setSurfaceTextureListener(this);
        mVideoWidth = 0;
        mVideoHeight = 0;
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mPendingSubtitleTracks = new Vector<>();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }


    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request. Note that the cross domain redirection is
     *                allowed by default, but that can be changed with key/value pairs through the
     *                headers parameter with "android-allow-cross-domain-redirect" as the key and
     *                "0" or "1" as the value to disallow or allow cross domain redirection.
     */
    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
    }

    private void openVideo() {
        if (mUri == null || s == null) {
            // not ready for playback just yet, will try again later
            return;
        }
//      AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
//      am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);
        try {
            mMediaPlayer = new MediaPlayer();
            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(getContext(), mUri, mHeaders);
            mMediaPlayer.setSurface(s);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepare();

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } finally {
            mPendingSubtitleTracks.clear();
        }
    }

    public void setMediaController(MediaController controller) {
        this.controller = controller;
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    /**
     * Register a callback to be invoked when the media file is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file has been reached during
     * playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs during playback or setup.  If no
     * listener is specified, or if the listener returned false, VideoView will inform the user of
     * any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event occurs during playback or
     * setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(MediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mPendingSubtitleTracks.clear();
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
        }
    }


    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
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
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

//      if (mSubtitleWidget != null) {
//         mSubtitleWidget.onAttachedToWindow();
//      }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

//      if (mSubtitleWidget != null) {
//         mSubtitleWidget.onDetachedFromWindow();
//      }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

//      if (mSubtitleWidget != null) {
//         measureAndLayoutSubtitleWidget();
//      }
    }

    public void setDisplayMetrics(int width, int height) {

        mFocusX = width / 2;

        mFocusY = height / 2;
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            mScaleFactor *= detector.getScaleFactor(); // scale change since previous event

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(1.f, Math.min(mScaleFactor, 4.0f));

            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {

            PointF d = detector.getFocusDelta();

            mFocusX += d.x;

            mFocusY += d.y;

            return true;
        }
    }

    /**
     * @author Almer Thie (code.almeros.com) Copyright (c) 2013, Almer Thie (code.almeros.com)
     *         All rights reserved.
     *         Redistribution and use in source and binary forms, with or without modification, are
     *         permitted provided that the following conditions are met:
     *         Redistributions of source code must retain the above copyright notice, this list of
     *         conditions and the following disclaimer. Redistributions in binary form must
     *         reproduce the above copyright notice, this list of conditions and the following
     *         disclaimer in the documentation and/or other materials provided with the
     *         distribution.
     *         THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
     *         EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     *         OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
     *         SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
     *         INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
     *         TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
     *         BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
     *         CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
     *         ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
     *         DAMAGE.
     */
    public static abstract class BaseGestureDetector {
        protected final Context mContext;
        protected       boolean mGestureInProgress;

        protected MotionEvent mPrevEvent;
        protected MotionEvent mCurrEvent;

        protected float mCurrPressure;
        protected float mPrevPressure;
        protected long  mTimeDelta;


        /**
         * This value is the threshold ratio between the previous combined pressure and the current
         * combined pressure. When pressure decreases rapidly between events the position values can
         * often be imprecise, as it usually indicates that the user is in the process of lifting a
         * pointer off of the device. This value was tuned experimentally.
         */
        protected static final float PRESSURE_THRESHOLD = 0.67f;


        public BaseGestureDetector(Context context) {
            mContext = context;
        }

        /**
         * All gesture detectors need to be called through this method to be able to detect
         * gestures. This method delegates work to handler methods (handleStartProgressEvent,
         * handleInProgressEvent) implemented in extending classes.
         *
         * @param event
         * @return
         */
        public boolean onTouchEvent(MotionEvent event) {
            final int actionCode = event.getAction() & MotionEvent.ACTION_MASK;
            if (!mGestureInProgress) {
                handleStartProgressEvent(actionCode, event);
            } else {
                handleInProgressEvent(actionCode, event);
            }
            return true;
        }

        /**
         * Called when the current event occurred when NO gesture is in progress yet. The handling
         * in this implementation may set the gesture in progress (via mGestureInProgress) or out of
         * progress
         *
         * @param actionCode
         * @param event
         */
        protected abstract void handleStartProgressEvent(int actionCode, MotionEvent event);

        /**
         * Called when the current event occurred when a gesture IS in progress. The handling in
         * this implementation may set the gesture out of progress (via mGestureInProgress).
         *
         * @param action
         * @param event
         */
        protected abstract void handleInProgressEvent(int actionCode, MotionEvent event);


        protected void updateStateByEvent(MotionEvent curr) {
            final MotionEvent prev = mPrevEvent;

            // Reset mCurrEvent
            if (mCurrEvent != null) {
                mCurrEvent.recycle();
                mCurrEvent = null;
            }
            mCurrEvent = MotionEvent.obtain(curr);


            // Delta time
            mTimeDelta = curr.getEventTime() - prev.getEventTime();

            // Pressure
            mCurrPressure = curr.getPressure(curr.getActionIndex());
            mPrevPressure = prev.getPressure(prev.getActionIndex());
        }

        protected void resetState() {
            if (mPrevEvent != null) {
                mPrevEvent.recycle();
                mPrevEvent = null;
            }
            if (mCurrEvent != null) {
                mCurrEvent.recycle();
                mCurrEvent = null;
            }
            mGestureInProgress = false;
        }


        /**
         * Returns {@code true} if a gesture is currently in progress.
         *
         * @return {@code true} if a gesture is currently in progress, {@code false} otherwise.
         */
        public boolean isInProgress() {
            return mGestureInProgress;
        }

        /**
         * Return the time difference in milliseconds between the previous accepted GestureDetector
         * event and the current GestureDetector event.
         *
         * @return Time difference since the last move event in milliseconds.
         */
        public long getTimeDelta() {
            return mTimeDelta;
        }

        /**
         * Return the event time of the current GestureDetector event being processed.
         *
         * @return Current GestureDetector event time in milliseconds.
         */
        public long getEventTime() {
            return mCurrEvent.getEventTime();
        }
    }

        /**
         * @author Almer Thie (code.almeros.com)
         *         Copyright (c) 2013, Almer Thie (code.almeros.com)
         *         <p/>
         *         All rights reserved.
         *         <p/>
         *         Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
         *         <p/>
         *         Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
         *         Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
         *         in the documentation and/or other materials provided with the distribution.
         *         <p/>
         *         THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
         *         INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
         *         IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
         *         OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
         *         OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
         *         OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
         *         OF SUCH DAMAGE.
         */
        public static class MoveGestureDetector extends BaseGestureDetector {

            /**
             * Listener which must be implemented which is used by MoveGestureDetector
             * to perform callbacks to any implementing class which is registered to a
             * MoveGestureDetector via the constructor.
             *
             * @see MoveGestureDetector.SimpleOnMoveGestureListener
             */
            public interface OnMoveGestureListener {
                public boolean onMove(MoveGestureDetector detector);

                public boolean onMoveBegin(MoveGestureDetector detector);

                public void onMoveEnd(MoveGestureDetector detector);
            }

            /**
             * Helper class which may be extended and where the methods may be
             * implemented. This way it is not necessary to implement all methods
             * of OnMoveGestureListener.
             */
            public static class SimpleOnMoveGestureListener implements OnMoveGestureListener {
                public boolean onMove(MoveGestureDetector detector) {
                    return false;
                }

                public boolean onMoveBegin(MoveGestureDetector detector) {
                    return true;
                }

                public void onMoveEnd(MoveGestureDetector detector) {
                    // Do nothing, overridden implementation may be used
                }
            }

            private static final PointF FOCUS_DELTA_ZERO = new PointF();

            private final OnMoveGestureListener mListener;

            private PointF mCurrFocusInternal;
            private PointF mPrevFocusInternal;
            private PointF mFocusExternal = new PointF();
            private PointF mFocusDeltaExternal = new PointF();

            public MoveGestureDetector(Context context, OnMoveGestureListener listener) {
                super(context);
                mListener = listener;
            }

            @Override
            protected void handleStartProgressEvent(int actionCode, MotionEvent event) {

                switch (actionCode) {

                    case MotionEvent.ACTION_DOWN:

                        resetState(); // In case we missed an UP/CANCEL event

                        mPrevEvent = MotionEvent.obtain(event);
                        mTimeDelta = 0;

                        updateStateByEvent(event);
                        break;

                    case MotionEvent.ACTION_MOVE:

                        mGestureInProgress = mListener.onMoveBegin(this);
                        break;
                }
            }

            @Override
            protected void handleInProgressEvent(int actionCode, MotionEvent event) {
                switch (actionCode) {

                    case MotionEvent.ACTION_UP:

                    case MotionEvent.ACTION_CANCEL:

                        mListener.onMoveEnd(this);
                        resetState();
                        break;

                    case MotionEvent.ACTION_MOVE:

                        updateStateByEvent(event);

                        // Only accept the event if our relative pressure is within
                        // a certain limit. This can help filter shaky data as a
                        // finger is lifted.
                        if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
                            final boolean updatePrevious = mListener.onMove(this);
                            if (updatePrevious) {
                                mPrevEvent.recycle();
                                mPrevEvent = MotionEvent.obtain(event);
                            }
                        }
                        break;
                }
            }

            protected void updateStateByEvent(MotionEvent curr) {
                super.updateStateByEvent(curr);

                final MotionEvent prev = mPrevEvent;

                // Focus internal
                mCurrFocusInternal = determineFocalPoint(curr);
                mPrevFocusInternal = determineFocalPoint(prev);

                // Focus external
                // - Prevent skipping of focus delta when a finger is added or removed
                boolean mSkipNextMoveEvent = prev.getPointerCount() != curr.getPointerCount();
                mFocusDeltaExternal = mSkipNextMoveEvent ? FOCUS_DELTA_ZERO : new PointF(mCurrFocusInternal.x - mPrevFocusInternal.x, mCurrFocusInternal.y - mPrevFocusInternal.y);

                // - Don't directly use mFocusInternal (or skipping will occur). Add
                // 	 unskipped delta values to mFocusExternal instead.
                mFocusExternal.x += mFocusDeltaExternal.x;
                mFocusExternal.y += mFocusDeltaExternal.y;
            }

            /**
             * Determine (multi)finger focal point (a.k.a. center point between all fingers)
             *
             * @param  e detected motion event
             * @return PointF focal point
             */
            private PointF determineFocalPoint(MotionEvent e) {
                // Number of fingers on screen
                final int pCount = e.getPointerCount();
                float x = 0f;
                float y = 0f;

                for (int i = 0; i < pCount; i++) {
                    x += e.getX(i);
                    y += e.getY(i);
                }

                return new PointF(x / pCount, y / pCount);
            }

            public float getFocusX() {
                return mFocusExternal.x;
            }

            public float getFocusY() {
                return mFocusExternal.y;
            }

            public PointF getFocusDelta() {
                return mFocusDeltaExternal;
            }

        }
}