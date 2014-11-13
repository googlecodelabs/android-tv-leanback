/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.widget.MediaController;
import android.widget.Toast;

import com.android.example.leanback.data.Video;
import com.android.example.leanback.fastlane.PlaybackOverlayFragment;
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

    private static final String TAG = PlayerActivity.class.getSimpleName();

    private Video mVideo;

    private MediaController mediaController;
    private View shutterView;
    private VideoSurfaceView surfaceView;

    private ExoPlayer player;
    private MediaCodecVideoTrackRenderer videoRenderer;

    private boolean autoPlay = true;

    private PlaybackOverlayFragment mPlaybackOverlayFragment;
    private boolean mIsOnTv;
    private PlayerControl playerControl;
    private int mPlayerPosition;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_player);

        mVideo = (Video)getIntent().getSerializableExtra(Video.INTENT_EXTRA_VIDEO);

        // We will use the PlaybackOverlayFragment when running on TV.
        mIsOnTv = MyUtil.isRunningInTvMode(this);
        if (mIsOnTv) {
            // On TV we will use the resource in layout-televsion
            mPlaybackOverlayFragment = (PlaybackOverlayFragment)
                    getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        } else {
            shutterView = findViewById(R.id.shutter);
            View root = findViewById(R.id.root);
            mediaController = new MediaController(this);

            //overscan safe on 1980 * 1080 TV
            mediaController.setPadding(48, 27, 48, 27);
            mediaController.setAnchorView(root);
        }
        surfaceView = (VideoSurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);

        preparePlayer();
    }

    private void preparePlayer() {
        Log.d(TAG, "preparePlayer()");
        SampleSource sampleSource =
                new FrameworkSampleSource(this, Uri.parse(mVideo.getContentUrl()), /* headers */ null, RENDERER_COUNT);

        // Build the track renderers
        videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);

        // Setup the player
        player = ExoPlayer.Factory.newInstance(RENDERER_COUNT, 1000, 5000);
        player.addListener(this);
        player.prepare(videoRenderer, audioRenderer);
        if (mIsOnTv) {
            // This PlayerControl must stay in sync with PlaybackOverlayFragment.
            // We created methods such as PlaybackOverlayFragment.pressPlay() to request
            // that the fragment change the playback state. When the fragment receives a playback
            // request, it updates the UI and then calls a method in this activity according to
            // PlaybackOverlayFragment.OnPlayPauseClickedListener.
            playerControl = new PlayerControl(player);
        } else {
            // Build the player controls
            mediaController.setMediaPlayer(new PlayerControl(player));
            mediaController.setEnabled(true);
        }
        maybeStartPlayback();
    }

    private void releasePlayer() {
        Log.d(TAG, "releasePlayer()");
        if (player != null) {
            player.release();
            player = null;
        }
        videoRenderer = null;
    }

    private void reloadVideo() {
        Log.d(TAG, "reloadVideo()");
        releasePlayer();
        preparePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mIsOnTv) {
            if (playerControl != null && playerControl.isPlaying()) {
                // Allows video to play behind the launcher screen when the user preses
                // the Home button.
                // This is only available on API level 21+, and we are assuming
                // all TV devices on running Android 21+.
                requestVisibleBehind(true);
            }
        }
        super.onPause();
    }

    @Override
    public void onVisibleBehindCanceled() {
        super.onVisibleBehindCanceled();
        releasePlayer();
        if (!mIsOnTv) {
            shutterView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
        if (!mIsOnTv) {
            shutterView.setVisibility(View.VISIBLE);
        }
    }

    private void maybeStartPlayback() {
        Log.d(TAG, "maybeStartPlayback");
        Surface surface = surfaceView.getHolder().getSurface();
        if (videoRenderer == null || surface == null || !surface.isValid()) {
            // We're not ready yet.
            return;
        }
        player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
        if (autoPlay) {
            if (mIsOnTv) {
                // This will update the player controls and the activity will receive the callback
                // OnPlayPauseClickedListener.onFragmentPlayPause(Video, int, Boolean)
                mPlaybackOverlayFragment.pressPlay();
            } else {
                player.setPlayWhenReady(true);
            }
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
        Log.d(TAG, "onPlayerStateChanged(playbackState=" + playbackState + ")");
        if (!mIsOnTv && playbackState == ExoPlayer.STATE_READY) {
            shutterView.setVisibility(View.GONE);
            mediaController.show(0);
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
        if (!mIsOnTv) {
            shutterView.setVisibility(View.GONE);
            mediaController.show(0);
        }
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
            player.blockingSendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, null);
        }
    }

    /**
     * Implementation of PlaybackOverlayFragment.OnPlayPauseClickedListener
     */
    public void onFragmentPlayPause(Video video, int position, Boolean playPause) {
        Log.d(TAG, "onFragmentPlayPause()");
        if (mVideo == null || !mVideo.getTitle().equals(video.getTitle())) {
            // When the user selects another video from the PlaybackOverlayFragment, we need
            // to recognize that the video has changed and reload the player.
            mVideo = video;
            reloadVideo();
        }
        if (mVideo == null) {
            return;
        }

        mPlayerPosition = position;
        // seekTo(), start(), pause() are ONLY be called in response to the fragment callbacks
        playerControl.seekTo(mPlayerPosition);
        if (playPause) {
            Log.d(TAG, "Play");
            playerControl.start();
        } else {
            Log.d(TAG, "Pause");
            playerControl.pause();
        }
    }

    /**
     * Implementation of PlaybackOverlayFragment.OnPlayPauseClickedListener
     */
    public void onFragmentFfwRwd(Video video, int position) {
        Log.d(TAG, "onFragmentFfwRwd() seek to " + position);
        if (mVideo == null || !mVideo.getTitle().equals(video.getTitle())) {
            // When the user selects another video from the PlaybackOverlayFragment, we need
            // to recognize that the video has changed and reload the player.
            mVideo = video;
            reloadVideo();
        }
        if (mVideo == null) {
            return;
        }

        mPlayerPosition = position;
        // seekTo() is ONLY be called in response to the fragment callbacks
        playerControl.seekTo(mPlayerPosition);
    }

}