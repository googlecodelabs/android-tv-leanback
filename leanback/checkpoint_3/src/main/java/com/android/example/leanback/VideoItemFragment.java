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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.example.leanback.data.Video;
import com.android.example.leanback.data.VideoDataManager;
import com.android.example.leanback.data.VideoItemContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

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


    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private MovieAdapter mAdapter;
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

        mAdapter = new MovieAdapter(getActivity(),
                android.R.layout.two_line_list_item, null, new String[]{VideoItemContract.VideoItem._ID, VideoItemContract.VideoItem.TITLE, VideoItemContract.VideoItem.THUMB_IMG_URL, VideoItemContract.VideoItem.CONTENT_URL, VideoItemContract.VideoItem.YEAR}, null);
        mGridView.setAdapter(mAdapter);


        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                CursorWrapper c = (CursorWrapper) ((SimpleCursorAdapter) parent.getAdapter()).getItem(position);
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
        ((SimpleCursorAdapter) mGridView.getAdapter()).swapCursor(cursor);
        mGridView.setVisibility(View.VISIBLE);
        mGridView.smoothScrollToPosition(0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        ((SimpleCursorAdapter) mGridView.getAdapter()).swapCursor(null);
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


    public class MovieAdapter extends SimpleCursorAdapter {

        final List<Video> videoList = new ArrayList<Video>();
        VideoDataManager.VideoItemMapper mapper;

        public MovieAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (mapper == null) {
                mapper = new VideoDataManager.VideoItemMapper();
                mapper.bindColumns(getCursor());
            }
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.video_card, null);
            }


            Cursor c = getCursor();
            c.moveToPosition(position);
            Video video = mapper.bind(c);
            videoList.add(video);

            ((TextView) v.findViewById(R.id.info_text)).setText(video.getTitle());

            ImageView imageView = (ImageView) v.findViewById(R.id.info_image);
            Picasso.with(getActivity()).load(video.getThumbUrl()).into(imageView);

            Button button = (Button) v.findViewById(R.id.play_button);
            button.setTag(position);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Video video = videoList.get((Integer) view.getTag());
                    Intent intent = new Intent(view.getContext(), VideoDetailsActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable(Video.INTENT_EXTRA_VIDEO, video);
                    intent.putExtras(b);
                    view.getContext().startActivity(intent);
                }
            });
            return v;
        }

        @Override
        public Cursor swapCursor(Cursor c) {
            videoList.clear();
            return super.swapCursor(c);
        }
    }



}
