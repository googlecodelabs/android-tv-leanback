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

import android.content.Context;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.util.Log;

import com.android.example.leanback.R;
import com.android.example.leanback.data.Video;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    private final Context mContext;

    private DetailsDescriptionPresenter() {
        super();
        this.mContext = null;
    }

    public DetailsDescriptionPresenter(Context ctx) {
        super();
        this.mContext = ctx;
    }

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Video video = (Video) item;

        if (video != null) {
            Log.d("Presenter", String.format("%s, %s, %s", video.getTitle(), video.getThumbUrl(), video.getDescription()));
            viewHolder.getTitle().setText(video.getTitle());
            viewHolder.getSubtitle().setText(String.format(mContext.getString(R.string.rating), video.getRating()));
            viewHolder.getBody().setText(video.getDescription());
        }
    }
}
