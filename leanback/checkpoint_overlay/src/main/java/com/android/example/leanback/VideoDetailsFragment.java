package com.android.example.leanback;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.example.leanback.data.Video;
import com.squareup.picasso.Picasso;

/**
 * Created by anirudhd on 11/5/14.
 */
public class VideoDetailsFragment extends Fragment {
    private View mView;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static VideoDetailsFragment newInstance(String tag) {
        VideoDetailsFragment f = new VideoDetailsFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("TAG", tag);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_details, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Picasso.with(getActivity()).load(mVideo.getThumbUrl()).transform(BlurTransform.getInstance(this.getActivity())).fit().into((ImageView) mView.findViewById(R.id.image_view));
        ((TextView)mView.findViewById(R.id.movie_info_title)).setText(mVideo.getTitle());
        ((TextView)mView.findViewById(R.id.movie_info_text)).setText(mVideo.getDescription());
        mView.findViewById(R.id.movie_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("video", mVideo);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    public Video getmVideo() {
        return mVideo;
    }

    public void setmVideo(Video mVideo) {
        this.mVideo = mVideo;
    }

    private Video mVideo;

}
