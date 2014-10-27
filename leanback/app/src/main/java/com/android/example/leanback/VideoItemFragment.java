package com.android.example.leanback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.example.leanback.data.VideoDataManager;
import com.android.example.leanback.data.VideoItemContract;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class VideoItemFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private  ProgressBar progressView;
    private VideoDataManager manager;

    // TODO: Rename and change types of parameters
    public static VideoItemFragment newInstance(String param1) {
        VideoItemFragment fragment = new VideoItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        Log.d(VideoItemFragment.class.getName(),"Fragment created " + param1);
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

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

        manager = new VideoDataManager(getActivity(), getLoaderManager(), VideoItemContract.VideoItem.buildDirUri());
        manager.startDataLoading();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videoitem_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        progressView = (ProgressBar) view.findViewById(R.id.progress);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mRecyclerView.setAdapter(mAdapter);

        // TODO: Change Adapter to display your content
        mAdapter = new VideoAdapter(manager.getVideos(),getActivity());
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mAdapter.getItemCount() == 0) {
                    progressView.setVisibility(View.VISIBLE);
                } else {
                    progressView.setVisibility(View.GONE);
                }
            }
        });
        return view;
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
        Log.d(VideoItemFragment.class.getName(),"onAttach Called");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
