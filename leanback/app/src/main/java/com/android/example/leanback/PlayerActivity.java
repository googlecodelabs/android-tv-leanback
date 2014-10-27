package com.android.example.leanback;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;


public class PlayerActivity extends Activity {

    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setVideoURI(Uri.parse("http://208.92.36.158/test/live/nba_android.m3u8"));
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.start();
    }

    @Override
    protected void onStop() {
        if(mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }
        super.onStop();
    }
}
