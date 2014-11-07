package com.android.example.leanback.fastlane;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.CursorObjectAdapter;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.SinglePresenterSelector;
import android.widget.Toast;

import com.android.example.leanback.PlayerActivity;
import com.android.example.leanback.R;
import com.android.example.leanback.data.Video;
import com.android.example.leanback.data.VideoDataManager;
import com.android.example.leanback.data.VideoItemContract;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 */
public class VideoDetailsFragment extends DetailsFragment {

    private Video selectedVideo;
    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private static final int ACTION_PLAY = 1;
    private static final int ACTION_WATCH_LATER = 2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedVideo = (Video) getActivity().getIntent().getSerializableExtra(Video.INTENT_EXTRA_VIDEO);
        new DetailRowBuilderTask().execute(selectedVideo);
    }

    private class DetailRowBuilderTask extends AsyncTask<Video, Integer, DetailsOverviewRow> {

        @Override
        protected DetailsOverviewRow doInBackground(Video... videos) {
            DetailsOverviewRow row = new DetailsOverviewRow(videos[0]);
            Bitmap poster = null;
            try {
                poster = Picasso.with(getActivity())
                        .load(videos[0].getThumbUrl())
                        .resize(dpToPx(DETAIL_THUMB_WIDTH, getActivity().getApplicationContext()),
                                dpToPx(DETAIL_THUMB_HEIGHT, getActivity().getApplicationContext()))
                        .centerCrop()
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            row.setImageBitmap(getActivity(), poster);
            row.addAction(new Action(ACTION_PLAY, getResources().getString(
                    R.string.action_play)));
            row.addAction(new Action(ACTION_WATCH_LATER, getResources().getString(R.string.action_watch_later)));
            return row;

        }

        @Override
        protected void onPostExecute(DetailsOverviewRow detailRow) {
            ClassPresenterSelector ps = new ClassPresenterSelector();
            DetailsOverviewRowPresenter dorPresenter =
                    new DetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
            // set detail background and style
            dorPresenter.setBackgroundColor(getResources().getColor(R.color.primary));
            dorPresenter.setStyleLarge(true);
            dorPresenter.setOnActionClickedListener(new OnActionClickedListener() {
                @Override
                public void onActionClicked(Action action) {
                    if (action.getId() == ACTION_PLAY) {
                        Intent intent = new Intent(getActivity(), PlayerActivity.class);
                        intent.putExtra(Video.INTENT_EXTRA_VIDEO, (Serializable)selectedVideo);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ps.addClassPresenter(DetailsOverviewRow.class, dorPresenter);
            ps.addClassPresenter(ListRow.class,
                    new ListRowPresenter());

            /** bonus code for adding related items to details fragment **/
            // <START>
            ArrayObjectAdapter adapter = new ArrayObjectAdapter(ps);
            adapter.add(detailRow);

            String subcategories[] = {
                    "You may also like"
            };

            CursorObjectAdapter rowAdapter = new CursorObjectAdapter(new SinglePresenterSelector(new CardPresenter()));
            VideoDataManager manager  = new VideoDataManager(getActivity(),getLoaderManager(), VideoItemContract.VideoItem.buildDirUri(),rowAdapter);
            manager.startDataLoading();
            HeaderItem header = new HeaderItem(0, subcategories[0], null);
            adapter.add(new ListRow(header, rowAdapter));
            setAdapter(adapter);
            // <END>
        }

    }

    // Utility method for converting dp to pixels
    public static int dpToPx(int dp, Context ctx) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

}
