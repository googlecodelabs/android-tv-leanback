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

package com.android.example.leanback.fastlane;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.CursorObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.FastForwardAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RepeatAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RewindAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.ShuffleAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipNextAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipPreviousAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsDownAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsUpAction;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SinglePresenterSelector;
import android.util.Log;

import com.android.example.leanback.R;
import com.android.example.leanback.data.Video;
import com.android.example.leanback.data.VideoDataManager;
import com.android.example.leanback.data.VideoItemContract;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Class for video playback with media control
 */
public class PlaybackOverlayFragment extends android.support.v17.leanback.app.PlaybackOverlayFragment {
    private static final String TAG = PlaybackOverlayFragment.class.getSimpleName();

    private static Activity sActivity;

    private static final boolean SHOW_DETAIL = true;
    private static final boolean HIDE_MORE_ACTIONS = false;
    private static final int PRIMARY_CONTROLS = 5;
    private static final boolean SHOW_IMAGE = PRIMARY_CONTROLS <= 5;
    private static final int BACKGROUND_TYPE = PlaybackOverlayFragment.BG_LIGHT;
    private static final int CARD_WIDTH = 200;
    private static final int CARD_HEIGHT = 240;
    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final int UPDATE_PERIOD = 16;
    private static final int SIMULATED_BUFFERED_TIME = 10000;
    private static final int CLICK_TRACKING_DELAY = 1000;

    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private ArrayObjectAdapter mSecondaryActionsAdapter;
    private PlayPauseAction mPlayPauseAction;
    private RepeatAction mRepeatAction;
    private ThumbsUpAction mThumbsUpAction;
    private ThumbsDownAction mThumbsDownAction;
    private ShuffleAction mShuffleAction;
    private FastForwardAction mFastForwardAction;
    private RewindAction mRewindAction;
    private SkipNextAction mSkipNextAction;
    private SkipPreviousAction mSkipPreviousAction;
    private PlaybackControlsRow mPlaybackControlsRow;
    private long mDuration;
    private Handler mHandler;
    private Runnable mRunnable;
    private PicassoPlaybackControlsRowTarget mPlaybackControlsRowTarget;
    private Timer mClickTrackingTimer;
    private int mClickCount;
    private final Handler mClickTrackingHandler = new Handler();

    OnPlayPauseClickedListener mCallback;
    private OnActionListener mOnActionListener = new OnActionListener();
    private VideoDataManager mManager;
    private int mCurrentIndex;
    private Video mVideo;
    private ArrayList<Video> mItems;

    public void pressPlay() {
        mPlayPauseAction.setIndex(PlayPauseAction.PLAY);
        mOnActionListener.onActionClicked(mPlayPauseAction);
    }

    public void pressPause() {
        mPlayPauseAction.setIndex(PlayPauseAction.PAUSE);
        mOnActionListener.onActionClicked(mPlayPauseAction);
    }

    public void pressSkipNext() {
        mOnActionListener.onActionClicked(mSkipNextAction);
    }

    public void pressSkipPrevious() {
        mOnActionListener.onActionClicked(mSkipPreviousAction);
    }

    public void pressFastForward() {
        mOnActionListener.onActionClicked(mFastForwardAction);
    }

    public void pressRewind() {
        mOnActionListener.onActionClicked(mRewindAction);
    }

    // Container Activity must implement this interface
    public interface OnPlayPauseClickedListener {
        public void onFragmentPlayPause(Video video, int position, Boolean playPause);
        public void onFragmentFfwRwd(Video video, int position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        sActivity = activity;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnPlayPauseClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPlayPauseClickedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        final ObjectAdapter rowContents = new CursorObjectAdapter((new SinglePresenterSelector(new CardPresenter())));
        rowContents.registerObserver(new ObjectAdapter.DataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                mItems = new ArrayList<Video>();
                for (int i = 0; i < rowContents.size(); i++) {
                    mItems.add((Video)rowContents.get(i));
                }
            }
        });
        mManager = new VideoDataManager(getActivity(), getLoaderManager(), VideoItemContract.VideoItem.buildDirUri(), rowContents );
        mManager.startDataLoading();

        mHandler = new Handler();

        mVideo = (Video)sActivity.getIntent().getSerializableExtra(Video.INTENT_EXTRA_VIDEO);
        Log.d(TAG, "onCreate mVideo=" + mVideo);

        setBackgroundType(BACKGROUND_TYPE);
        setFadingEnabled(false);

        setupRows();

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemSelected: " + item + " row " + row);
            }
        });
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                      RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemClicked: " + item + " row " + row);
                if (item instanceof Video) {
                    Video selected = (Video) item;
                    for (int index = 0; index < mItems.size(); index++) {
                        Video video = mItems.get(index);
                        if (selected.getTitle().equals(video.getTitle())) {
                            setVideoIndex(index);
                            return;
                        }
                    }
                }
            }
        });
    }

    private void setupRows() {

        ClassPresenterSelector ps = new ClassPresenterSelector();

        PlaybackControlsRowPresenter playbackControlsRowPresenter;
        if (SHOW_DETAIL) {
            playbackControlsRowPresenter = new PlaybackControlsRowPresenter(
                    new DescriptionPresenter());
        } else {
            playbackControlsRowPresenter = new PlaybackControlsRowPresenter();
        }
        playbackControlsRowPresenter.setOnActionClickedListener(mOnActionListener);
        playbackControlsRowPresenter.setSecondaryActionsHidden(HIDE_MORE_ACTIONS);

        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(ps);

        addPlaybackControlsRow();
        addOtherRows();

        setAdapter(mRowsAdapter);
    }

    private int getDuration() {
        Log.d(TAG, "getDuration()");
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Log.d(TAG, "mVideo.getContentUrl()=" + mVideo.getContentUrl());
            mmr.setDataSource(mVideo.getContentUrl(), new HashMap<String, String>());
        } else {
            mmr.setDataSource(mVideo.getContentUrl());
        }
        String time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        mDuration = Long.parseLong(time);
        Log.d(TAG, "mDuration=" + mDuration);

        return (int) mDuration;
    }

    private int getCurrentIndex() {
        return mCurrentIndex;
    }

    private void addPlaybackControlsRow() {
        if (mVideo != null) {
            mPlaybackControlsRow = new PlaybackControlsRow(mVideo);
        } else {
            mPlaybackControlsRow = new PlaybackControlsRow();
        }
        mRowsAdapter.add(mPlaybackControlsRow);

        updatePlaybackRow();

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
        mPlaybackControlsRow.setSecondaryActionsAdapter(mSecondaryActionsAdapter);
        mPlayPauseAction = new PlayPauseAction(sActivity);
        mRepeatAction = new RepeatAction(sActivity);
        mThumbsUpAction = new ThumbsUpAction(sActivity);
        mThumbsDownAction = new ThumbsDownAction(sActivity);
        mShuffleAction = new ShuffleAction(sActivity);
        mSkipNextAction = new SkipNextAction(sActivity);
        mSkipPreviousAction = new SkipPreviousAction(sActivity);
        mFastForwardAction = new FastForwardAction(sActivity);
        mRewindAction = new RewindAction(sActivity);

        if (PRIMARY_CONTROLS > 5) {
            mPrimaryActionsAdapter.add(mThumbsUpAction);
        } else {
            mSecondaryActionsAdapter.add(mThumbsUpAction);
        }
        mPrimaryActionsAdapter.add(mSkipPreviousAction);
        if (PRIMARY_CONTROLS > 3) {
            mPrimaryActionsAdapter.add(new RewindAction(sActivity));
        }
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        if (PRIMARY_CONTROLS > 3) {
            mPrimaryActionsAdapter.add(new FastForwardAction(sActivity));
        }
        mPrimaryActionsAdapter.add(mSkipNextAction);

        mSecondaryActionsAdapter.add(mRepeatAction);
        mSecondaryActionsAdapter.add(mShuffleAction);
        if (PRIMARY_CONTROLS > 5) {
            mPrimaryActionsAdapter.add(mThumbsDownAction);
        } else {
            mSecondaryActionsAdapter.add(mThumbsDownAction);
        }
        mSecondaryActionsAdapter.add(new PlaybackControlsRow.HighQualityAction(sActivity));
        mSecondaryActionsAdapter.add(new PlaybackControlsRow.ClosedCaptioningAction(sActivity));
    }

    private void notifyChanged(Action action) {
        ArrayObjectAdapter adapter = mPrimaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
        adapter = mSecondaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
    }

    private void updatePlaybackRow() {
        if (mVideo == null) {
            return;
        }
        if (mPlaybackControlsRow.getItem() != null) {
            Video item = (Video) mPlaybackControlsRow.getItem();
            item.setTitle(mVideo.getTitle());
            item.setDescription(mVideo.getDescription());
        }
        mPlaybackControlsRowTarget = new PicassoPlaybackControlsRowTarget(mPlaybackControlsRow);
        updateVideoImage(getThumbURI(mVideo));
        mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
        int duration = getDuration();
        mPlaybackControlsRow.setTotalTime(duration);
        mPlaybackControlsRow.setCurrentTime(0);
        mPlaybackControlsRow.setBufferedProgress(0);
        Log.d(TAG, "setTotalTime(getDuration()=" + duration + ")");
    }

    private static URI getThumbURI(Video video) {
        try {
            return new URI(video.getThumbUrl());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private void addOtherRows() {
        ObjectAdapter rowContents = new CursorObjectAdapter((new SinglePresenterSelector(new CardPresenter())));
        VideoDataManager manager = new VideoDataManager(getActivity(), getLoaderManager(), VideoItemContract.VideoItem.buildDirUri(), rowContents );
        manager.startDataLoading();

        HeaderItem headerItem = new HeaderItem(0, "You may also like", null);
        mRowsAdapter.add(new ListRow(headerItem, manager.getItemList()));
    }

    private int getUpdatePeriod() {
        if (getView() == null || mPlaybackControlsRow.getTotalTime() <= 0) {
            return DEFAULT_UPDATE_PERIOD;
        }
        return Math.max(UPDATE_PERIOD, mPlaybackControlsRow.getTotalTime() / getView().getWidth());
    }

    private void startProgressAutomation() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                int updatePeriod = getUpdatePeriod();
                int currentTime = mPlaybackControlsRow.getCurrentTime() + updatePeriod;
                int totalTime = mPlaybackControlsRow.getTotalTime();
                mPlaybackControlsRow.setCurrentTime(currentTime);
                mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);

                if (totalTime > 0 && totalTime <= currentTime) {
                    next();
                }
                mHandler.postDelayed(this, updatePeriod);
            }
        };
        mHandler.postDelayed(mRunnable, getUpdatePeriod());
    }

    private void next() {
        int currentIndex = getCurrentIndex();
        if (++currentIndex >= mItems.size()) {
            currentIndex = 0;
        }
        setVideoIndex(currentIndex);
    }

    private void prev() {
        int currentIndex = getCurrentIndex();
        if (--currentIndex< 0) {
            currentIndex = mItems.size() - 1;
        }
        setVideoIndex(currentIndex);
    }

    private void setVideoIndex(int itemIndex) {
        mCurrentIndex = itemIndex;
        mVideo = mItems.get(mCurrentIndex);
        if (mPlayPauseAction.getIndex() == PlayPauseAction.PLAY) {
            mCallback.onFragmentPlayPause(mVideo, 0, false);
        } else {
            mCallback.onFragmentPlayPause(mVideo, 0, true);
        }
        updatePlaybackRow();
    }

    private void fastForward() {
        Log.d(TAG, "current time: " + mPlaybackControlsRow.getCurrentTime());
        updateRapidFfRwClickTracker();
        int currentTime = mPlaybackControlsRow.getCurrentTime() + getFfRwSpeed();
        if( currentTime > (int) mDuration ) {
            currentTime = (int) mDuration;
        }
        mCallback.onFragmentFfwRwd(mVideo, currentTime);
        mPlaybackControlsRow.setCurrentTime(currentTime);
        mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);
    }

    private void fastRewind() {
        Log.d(TAG, "current time: " + mPlaybackControlsRow.getCurrentTime());
        updateRapidFfRwClickTracker();
        int currentTime = mPlaybackControlsRow.getCurrentTime() - getFfRwSpeed();
        if( currentTime < 0 || currentTime > (int) mDuration ) {
            currentTime = 0;
        }
        mCallback.onFragmentFfwRwd(mVideo, currentTime);
        mPlaybackControlsRow.setCurrentTime(currentTime);
        mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);
    }

    private void stopProgressAutomation() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    public void onStop() {
        stopProgressAutomation();
        super.onStop();
    }

    static class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {
        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            viewHolder.getTitle().setText(((Video) item).getTitle());
            viewHolder.getSubtitle().setText(((Video) item).getDescription());
        }
    }

    public static class PicassoPlaybackControlsRowTarget implements Target {
        PlaybackControlsRow mPlaybackControlsRow;

        public PicassoPlaybackControlsRowTarget(PlaybackControlsRow playbackControlsRow) {
            mPlaybackControlsRow = playbackControlsRow;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            Drawable bitmapDrawable = new BitmapDrawable(sActivity.getResources(), bitmap);
            mPlaybackControlsRow.setImageDrawable(bitmapDrawable);
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            mPlaybackControlsRow.setImageDrawable(drawable);
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            // Do nothing, default_background mManager has its own transitions
        }
    }

    protected void updateVideoImage(URI uri) {
        Picasso.with(sActivity)
                .load(uri.toString())
                .resize(convertDpToPixel(sActivity, CARD_WIDTH),
                        convertDpToPixel(sActivity, CARD_HEIGHT))
                .into(mPlaybackControlsRowTarget);
    }

    private static int convertDpToPixel(Context ctx, int dp) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private class OnActionListener implements OnActionClickedListener {
        public void onActionClicked(Action action) {
            if (action.getId() == mPlayPauseAction.getId()) {
                if (mPlayPauseAction.getIndex() == PlayPauseAction.PLAY) {
                    startProgressAutomation();
                    setFadingEnabled(true);
                    mCallback.onFragmentPlayPause(mVideo,
                            mPlaybackControlsRow.getCurrentTime(), true);
                } else {
                    stopProgressAutomation();
                    setFadingEnabled(false);
                    mCallback.onFragmentPlayPause(mVideo,
                            mPlaybackControlsRow.getCurrentTime(), false);
                }
            } else if (action.getId() == mSkipNextAction.getId()) {
                next();
            } else if (action.getId() == mSkipPreviousAction.getId()) {
                prev();
            } else if (action.getId() == mFastForwardAction.getId()) {
                fastForward();
            } else if (action.getId() == mRewindAction.getId()) {
                fastRewind();
            }
            if (action instanceof PlaybackControlsRow.MultiAction) {
                ((PlaybackControlsRow.MultiAction) action).nextIndex();
                notifyChanged(action);
            }
        }
    }

    private void updateRapidFfRwClickTracker() {
        if (null != mClickTrackingTimer) {
            mClickCount++;
            mClickTrackingTimer.cancel();
        } else {
            mClickCount = 0;
        }
        Log.i(TAG, "RW/FF click count=" + mClickCount + ", speed=" + getFfRwSpeed());
        mClickTrackingTimer = new Timer();
        mClickTrackingTimer.schedule(new RapidFfRwClickTrackerTask(), CLICK_TRACKING_DELAY);
    }

    private class RapidFfRwClickTrackerTask extends TimerTask {
        @Override
        public void run() {
            mClickTrackingHandler.post(new Runnable() {
                @Override
                public void run() {
                    mClickCount = 0;
                    mClickTrackingTimer = null;
                }
            });
        }
    }

    private int getFfRwSpeed() {
        // This works well for short videos (< 5 minutes).
        // Longer content should probably increase the speed more rapidly.
        return 10000 + mClickCount * 5000;
    }
}
