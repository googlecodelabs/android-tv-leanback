package com.android.example.leanback.fastlane;


import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaCodec.CryptoException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.android.example.leanback.R;
import com.android.example.leanback.data.Video;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.VideoSurfaceView;
import com.google.android.exoplayer.util.PlayerControl;

/**
 * An activity that plays media using {@link com.google.android.exoplayer.ExoPlayer}.
 */
public class TvPlayerActivity extends Activity implements SurfaceHolder.Callback,
        ExoPlayer.Listener, MediaCodecVideoTrackRenderer.EventListener,
        PlaybackOverlayFragment.OnPlayPauseClickedListener {

    public static final int RENDERER_COUNT = 2;

    private static final String TAG = "PlayerActivity";

    private VideoSurfaceView surfaceView;

    private ExoPlayer mPlayer;
    private MediaCodecVideoTrackRenderer videoRenderer;

    private PlaybackOverlayFragment mPlaybackOverlayFragment;

    private boolean autoPlay = true;
    private Video mVideo;
    private PlayerControl playerControl;
    private int mPlayerPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tv_activity_player);

        surfaceView = (VideoSurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        mPlaybackOverlayFragment = (PlaybackOverlayFragment)
                getFragmentManager().findFragmentById(R.id.playback_controls_fragment);

        mVideo = (Video)getIntent().getSerializableExtra(Video.INTENT_EXTRA_VIDEO);
        preparePlayer();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        if (playerControl.isPlaying()) {
            requestVisibleBehind(true);
        }
        super.onPause();
    }

    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        videoRenderer = null;
    }


    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    private void maybeStartPlayback() {
        Log.d(TAG, "maybeStartPlayback");
        Surface surface = surfaceView.getHolder().getSurface();
        if (videoRenderer == null || surface == null || !surface.isValid()) {
            // We're not ready yet.
            return;
        }
        mPlayer.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
        if (autoPlay) {
            mPlaybackOverlayFragment.pressPlay();
            autoPlay = false;
        }
    }


    private void onError(Exception e) {
        Log.e(TAG, "Playback failed", e);
        Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
        finish();
    }

    // ExoPlayer.Listener implementation

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.d(TAG, "player state " + playbackState);
        if (playbackState == ExoPlayer.STATE_READY) {
        }

    }

    @Override
    public void onPlayWhenReadyCommitted() {
        // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        onError(e);
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        surfaceView.setVideoWidthHeightRatio(height == 0 ? 1 : (float) width / height);
    }

    @Override
    public void onDrawnToSurface(Surface surface) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsed) {
        Log.d(TAG, "Dropped frames: " + count);
    }

    @Override
    public void onDecoderInitializationError(DecoderInitializationException e) {
        // This is for informational purposes only. Do nothing.
    }

    @Override
    public void onCryptoError(CryptoException e) {
        // This is for informational purposes only. Do nothing.
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "maybeStartPlayback");
        maybeStartPlayback();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (videoRenderer != null) {
            mPlayer.blockingSendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, null);
        }
    }

    /**
     * Implementation of PlaybackOverlayFragment.OnPlayPauseClickedListener
     */
    public void onFragmentPlayPause(Video video, int position, Boolean playPause) {
        Log.d(TAG, "Play/Pause Video + " + video);
        if (mVideo == null || !mVideo.getTitle().equals(video.getTitle())) {
            mVideo = video;
            reloadVideo();
        }
        if (mVideo == null) {
            return;
        }

        mPlayerPosition = position;
        playerControl.seekTo(mPlayerPosition);
        if (playPause) {
            Log.d(TAG, "Play");
            playerControl.start();
            mPlayer.setPlayWhenReady(true);
        } else {
            Log.d(TAG, "Pause");
            playerControl.pause();
            mPlayer.setPlayWhenReady(false);
        }
    }

    /**
     * Implementation of PlaybackOverlayFragment.OnPlayPauseClickedListener
     */
    public void onFragmentFfwRwd(Video video, int position) {
        Log.d(TAG, "Video + " + video + ", Seek to " + position);
        if (mVideo == null || !mVideo.getTitle().equals(video.getTitle())) {
            mVideo = video;
            reloadVideo();
        }
        if (mVideo == null) {
            return;
        }

        mPlayerPosition = position;
        playerControl.seekTo(mPlayerPosition);
    }

    private void reloadVideo() {
        releasePlayer();
        preparePlayer();
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        videoRenderer = null;
    }

    private void preparePlayer() {
        // TODO(cartland): Remove after we can handle more videos.

    //    Log.d(TAG, "mVideo.getContentUrl()=" + mVideo.getContentUrl());

        SampleSource sampleSource =
                new FrameworkSampleSource(this, Uri.parse(mVideo.getContentUrl()), /* headers */ null, RENDERER_COUNT);

        // Build the track renderers
        videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

        // Setup the player
        mPlayer = ExoPlayer.Factory.newInstance(RENDERER_COUNT, 1000, 5000);
        mPlayer.addListener(this);
        // Build the player controls
        mPlayer.prepare(videoRenderer, audioRenderer);
        playerControl = new PlayerControl(mPlayer);
    }
}