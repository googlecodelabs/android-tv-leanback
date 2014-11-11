/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.leanback.fastlane;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.util.DisplayMetrics;

import com.android.example.leanback.BlurTransform;
import com.android.example.leanback.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by anirudhd on 11/3/14.
 */
public class BackgroundHelper {

    private final Handler mHandler = new Handler();

    private Activity mActivity;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private String mBackgroundURL;


    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;

    public BackgroundHelper(Activity mActivity) {
        this.mActivity = mActivity;
    }

    private long BACKGROUND_UPDATE_DELAY = 200;


    public void prepareBackgroundManager() {
        BackgroundManager backgroundManager = BackgroundManager.getInstance(mActivity);
        backgroundManager.attach(mActivity.getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);
        mDefaultBackground = mActivity.getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.mBackgroundURL = backgroundUrl;
    }


    static class PicassoBackgroundManagerTarget implements Target {
        BackgroundManager mBackgroundManager;

        public PicassoBackgroundManagerTarget(BackgroundManager backgroundManager) {
            this.mBackgroundManager = backgroundManager;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            this.mBackgroundManager.setBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            this.mBackgroundManager.setDrawable(drawable);
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            // Do nothing, default_background manager has its own transitions
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            PicassoBackgroundManagerTarget that = (PicassoBackgroundManagerTarget) o;

            if (!mBackgroundManager.equals(that.mBackgroundManager))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return mBackgroundManager.hashCode();
        }
    }


    protected void setDefaultBackground(Drawable background) {
        mDefaultBackground = background;
    }

    protected void setDefaultBackground(int resourceId) {
        mDefaultBackground = mActivity.getResources().getDrawable(resourceId);
    }

    protected void updateBackground(String url) {
        Picasso.with(mActivity)
                .load(url)
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .centerCrop()
                .transform(BlurTransform.getInstance(mActivity))
                .error(mDefaultBackground)
                .into(mBackgroundTarget);
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
    }

    protected void updateBackground(Drawable drawable) {
        BackgroundManager.getInstance(mActivity).setDrawable(drawable);
    }

    protected void clearBackground() {
        BackgroundManager.getInstance(mActivity).setDrawable(mDefaultBackground);
    }

    public void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURL != null) {
                        updateBackground(mBackgroundURL);
                    }
                }
            });
        }
    }
}
