package com.android.example.leanback;


import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaCodec.CryptoException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

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
public class PlayerActivity extends Activity implements SurfaceHolder.Callback,
        ExoPlayer.Listener, MediaCodecVideoTrackRenderer.EventListener,
        PlaybackOverlayFragment.OnPlayPauseClickedListener {


    public static final int RENDERER_COUNT = 2;

    private static final String TAG = "PlayerActivity";
    String url = "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4";


//    private MediaController mediaController;
    private View shutterView;
    private PlaybackOverlayFragment mPlaybackOverlayFragment;
    private VideoSurfaceView mSurfaceView;

    private ExoPlayer player;
    private MediaCodecVideoTrackRenderer mVideoRenderer;
    private TrackRenderer mAudioRenderer;

    private boolean autoPlay = true;
    private Video mVideo;
    private int mPlayerPosition;
    private PlayerControl mPlayerControl;
    private boolean mPlayerNeedsPrepare;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mVideo = (Video)getIntent().getSerializableExtra(Video.INTENT_EXTRA_VIDEO);

        shutterView = findViewById(R.id.shutter);
        mPlaybackOverlayFragment = (PlaybackOverlayFragment) getFragmentManager().
                findFragmentById(R.id.playback_controls_fragment);
        mSurfaceView = (VideoSurfaceView) findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);

        preparePlayer();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        requestVisibleBehind(true);
        super.onPause();
    }

    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
        if (player != null) {
            player.release();
            player = null;
        }
        mVideoRenderer = null;
        shutterView.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
        mVideoRenderer = null;
        shutterView.setVisibility(View.VISIBLE);

    }

    private void maybeStartPlayback() {
        Log.d(TAG, "maybeStartPlayback");
        Surface surface = mSurfaceView.getHolder().getSurface();
        if (mVideoRenderer == null || surface == null || !surface.isValid()) {
            // We're not ready yet.
            return;
        }
        player.sendMessage(mVideoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
        if (autoPlay) {
            startPlayback();
            autoPlay = false;
        }
    }

    private void startPlayback() {
        mPlaybackOverlayFragment.pressPlay();
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
            shutterView.setVisibility(View.GONE);
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
        mSurfaceView.setVideoWidthHeightRatio(height == 0 ? 1 : (float) width / height);
    }

    @Override
    public void onDrawnToSurface(Surface surface) {
        shutterView.setVisibility(View.GONE);
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
        if (mVideoRenderer != null) {
            player.blockingSendMessage(mVideoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, null);
        }
    }

    /**
     * Implementation of PlaybackOverlayFragment.OnPlayPauseClickedListener
     */
    @Override
    public void onFragmentPlayPause(Video movie, int position, Boolean playPause) {
        if (!mVideo.getTitle().equals(movie.getTitle())) {
            mVideo = movie;
            reloadVideo();
        }

        mPlayerPosition = position;
        mPlayerControl.seekTo(mPlayerPosition);
        if (playPause) {
            mPlayerControl.start();
        } else {
            mPlayerControl.pause();
        }
    }

    /**
     * Implementation of PlaybackOverlayFragment.OnPlayPauseClickedListener
     */
    @Override
    public void onFragmentFfwRwd(Video movie, int position) {
        if (!mVideo.getTitle().equals(movie.getTitle())) {
            mVideo = movie;
            reloadVideo();
        }

        mPlayerPosition = position;
        mPlayerControl.seekTo(mPlayerPosition);
    }

    private void reloadVideo() {
        releasePlayer();
        preparePlayer();
    }


    private void preparePlayer() {
        if (player == null) {
            SampleSource sampleSource =
                    new FrameworkSampleSource(this, Uri.parse(url), /* headers */ null, RENDERER_COUNT);
            // Build the track renderers
            mVideoRenderer = new MediaCodecVideoTrackRenderer(sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            mAudioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);
            // Setup the player
            player = ExoPlayer.Factory.newInstance(RENDERER_COUNT, 1000, 5000);
            player.addListener(this);
            // Build the player controls
            mPlayerControl = new PlayerControl(player);
            player.prepare(mVideoRenderer, mAudioRenderer);

            player.seekTo(mPlayerPosition);
            mPlayerNeedsPrepare = true;
        }
        if (mPlayerNeedsPrepare) {
            player.prepare();
            mPlayerNeedsPrepare = false;
        }
        // TODO(cartland): Setting the surface must be implemented.
        // https://github.com/cartland/videoland/blob/master/app/src/main/java/com/chriscartland/videoland/player/MyPlayer.java#L218
        // player.setSurface(mSurfaceView.getHolder().getSurface());
        maybeStartPlayback();
    }


    private void releasePlayer() {
        if (player != null) {
            mPlayerPosition = player.getCurrentPosition();
            player.release();
            player= null;
        }
    }

}