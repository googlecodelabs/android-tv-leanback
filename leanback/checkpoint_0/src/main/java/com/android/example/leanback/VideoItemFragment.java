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
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.android.example.leanback.data.Video;
import com.android.example.leanback.data.VideoDataManager;
import com.android.example.leanback.data.VideoItemContract;
import com.squareup.picasso.Picasso;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class VideoItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";


    static String[] PROJECTION = {
            VideoItemContract.VideoItemColumns._ID,
            VideoItemContract.VideoItemColumns.TITLE,
            VideoItemContract.VideoItemColumns.CATEGORY,
            VideoItemContract.VideoItemColumns.DESCRIPTION,
            VideoItemContract.VideoItemColumns.RATING,
            VideoItemContract.VideoItemColumns.YEAR,
            VideoItemContract.VideoItemColumns.THUMB_IMG_URL,
            VideoItemContract.VideoItemColumns.TAGS,
            VideoItemContract.VideoItemColumns.CONTENT_URL,
    };


    private OnFragmentInteractionListener mListener;

    private GridView mGridView;

    // TODO: Rename and change types of parameters
    public static VideoItemFragment newInstance(String param1) {
        VideoItemFragment fragment = new VideoItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        Log.d(VideoItemFragment.class.getName(), "Fragment created " + param1);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public VideoItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        manager = new VideoDataManager(getActivity(), getLoaderManager(), VideoItemContract.VideoItem.buildDirUri());
//        manager.startDataLoading();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_videoitem_list, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridview);

        mGridView.setAdapter(new MovieAdapter(getActivity()));


        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                CursorWrapper c = (CursorWrapper) parent.getAdapter().getItem(position);
                ((OnFragmentInteractionListener) getActivity()).onFragmentInteraction(c.getString(c.getColumnIndex(VideoItemContract.VideoItem._ID)), c.getString(c.getColumnIndex(VideoItemContract.VideoItem.CONTENT_URL)));
            }

        });
        getLoaderManager().initLoader(0, null, this);
        return rootView;


    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        Log.d(VideoItemFragment.class.getName(), "onAttach Called");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), VideoItemContract.VideoItem.buildDirUri(), PROJECTION, null, null, VideoItemContract.VideoItem.DEFAULT_SORT);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        ((CursorAdapter) mGridView.getAdapter()).swapCursor(cursor);
        mGridView.setVisibility(View.VISIBLE);
        mGridView.smoothScrollToPosition(0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        ((CursorAdapter) mGridView.getAdapter()).swapCursor(null);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id, String url);
    }


    private static class MovieAdapter extends ResourceCursorAdapter {

        private final VideoDataManager.VideoItemMapper mMapper = new VideoDataManager.VideoItemMapper();
        private final Picasso mPicasso;

        private final static View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(
                        new Intent(v.getContext(), VideoDetailsActivity.class)
                                .putExtra(Video.INTENT_EXTRA_VIDEO, (Video) v.getTag())
                );
            }
        };

        public MovieAdapter(Context context) {
            super(context, R.layout.video_card, null, 0);
            this.mPicasso = Picasso.with(context);
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            final Cursor old = super.swapCursor(newCursor);
            if (null != newCursor) {
                mMapper.bindColumns(newCursor);
            }
            return old;
        }

        @Override
        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
            if (null != cursor) {
                mMapper.bindColumns(cursor);
            }
        }

        public static class ViewHolder {
            public final TextView info;
            public final ImageView image;
            public final Button play;

            public ViewHolder(final View view) {
                this.info = (TextView) view.findViewById(R.id.info_text);
                this.image = (ImageView) view.findViewById(R.id.info_image);
                this.play = (Button) view.findViewById(R.id.play_button);
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = super.newView(context, cursor, parent);
            final ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
            holder.play.setOnClickListener(onClickListener);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final Video video = mMapper.bind(cursor);
            final ViewHolder holder = (ViewHolder) view.getTag();

            holder.info.setText(video.getTitle());
            mPicasso.load(video.getThumbUrl()).into(holder.image);

            holder.play.setTag(video);
        }
    }

}
