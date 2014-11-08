package com.android.example.leanback;

import android.app.Activity;
import android.os.Bundle;

import com.android.example.leanback.data.Video;

/**
 * Created by anirudhd on 11/5/14.
 */
public class VideoDetailsActivity extends Activity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);


        Video video = (Video)getIntent().getExtras().get(Video.INTENT_EXTRA_VIDEO);
        VideoDetailsFragment frag = (VideoDetailsFragment)getFragmentManager().findFragmentById(R.id.details_frag);
        frag.setmVideo(video);


    }

}
