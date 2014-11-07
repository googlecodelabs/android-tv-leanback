package com.android.example.leanback;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.android.example.leanback.data.Video;

/**
 * Created by anirudhd on 11/5/14.
 */
public class VideoDetailsActivity extends Activity {

    private static final String TAG = VideoDetailsActivity.class.getSimpleName();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Video video = (Video)getIntent().getExtras().getSerializable(Video.INTENT_EXTRA_VIDEO);
        VideoDetailsFragment frag = (VideoDetailsFragment)getFragmentManager().findFragmentById(R.id.details_frag);
        Log.d(TAG, "setmVideo=" + video);
        frag.setmVideo(video);


    }

}
