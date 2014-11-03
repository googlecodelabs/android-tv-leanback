package com.android.example.leanback.fastlane;

import android.app.Activity;
import android.os.Bundle;

import com.android.example.leanback.R;

/**
 * Created by anirudhd on 11/2/14.
 */
public class LeanbackActivity extends Activity {

    public  static final String VIDEO = "video";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leanback);
    }
}
