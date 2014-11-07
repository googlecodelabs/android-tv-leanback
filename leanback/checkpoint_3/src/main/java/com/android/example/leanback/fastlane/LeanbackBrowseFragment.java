package com.android.example.leanback.fastlane;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.CursorObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SinglePresenterSelector;
import android.view.View;

import com.android.example.leanback.R;
import com.android.example.leanback.data.Video;
import com.android.example.leanback.data.VideoDataManager;
import com.android.example.leanback.data.VideoItemContract;

import java.io.Serializable;


/**
 * A simple {@link Fragment} subclass.
 */
public class LeanbackBrowseFragment extends BrowseFragment {

    private ArrayObjectAdapter mRowsAdapter;

    private static final String[] HEADERS = new String[]{
        "Featured", "Popular","Editor's choice"
    };


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    public void init() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mRowsAdapter);

        setBrandColor(getResources().getColor(R.color.primary));
        setBadgeDrawable(getResources().getDrawable(R.drawable.filmi));


        for (int position = 0; position < HEADERS.length; position++) {
            ObjectAdapter rowContents = new CursorObjectAdapter((new SinglePresenterSelector(new CardPresenter())));
            VideoDataManager manager = new VideoDataManager(getActivity(), getLoaderManager(), VideoItemContract.VideoItem.buildDirUri(), rowContents );
            manager.startDataLoading();

            HeaderItem headerItem = new HeaderItem(position, HEADERS[position], null);
            mRowsAdapter.add(new ListRow(headerItem, manager.getItemList()));
        }


        setOnItemViewClickedListener(getDefaultItemViewClickedListener());

    }

    protected OnItemViewClickedListener getDefaultItemViewClickedListener() {
        return new OnItemViewClickedListener() {

            @Override
            public void onItemClicked(Presenter.ViewHolder viewHolder, Object o, RowPresenter.ViewHolder viewHolder2, Row row) {

                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                intent.putExtra(Video.INTENT_EXTRA_VIDEO, (Serializable)o);
                startActivity(intent);

            }
        };
    }
}
