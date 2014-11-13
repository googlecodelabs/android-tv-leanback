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

package com.android.example.leanback.data;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anirudhd on 10/25/14.
 *
 *
 */
public class VideoDataManager implements LoaderManager.LoaderCallbacks<Cursor> {
    protected Loader<Cursor> mDataLoader;
    protected Context mContext;
    protected LoaderManager mLoaderManager;
    protected int LOADER_ID;
    protected Uri mRowUri;

    public List<Video> getVideos() {
        return videos;
    }

    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    List<Video> videos;
    private final VideoItemMapper mMapper;


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

    public VideoDataManager(Context mContext, LoaderManager mLoaderManager, Uri mRowUri) {
        this.mLoaderManager = mLoaderManager;
        this.mRowUri = mRowUri;
        this.mContext = mContext;
        LOADER_ID = mRowUri.hashCode();
        mMapper = new VideoItemMapper();
        videos = new ArrayList<Video>();

    }

    public void startDataLoading() {
        if (mDataLoader == null) {
            mDataLoader = mLoaderManager.initLoader(LOADER_ID, null, this);
        } else {
            mLoaderManager.restartLoader(mDataLoader.getId(), null, this);

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(mContext, mRowUri, PROJECTION, null, null, VideoItemContract.VideoItem.DEFAULT_SORT);
    }

    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mMapper.bindColumns(cursor);
        while (cursor.moveToNext()) {
            videos.add(mMapper.bind(cursor));
        }
    }

    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    public static class VideoItemMapper {

        private int[] mColumnMap;
        private static final int ID = 0;
        private static final int TITLE = 1;
        private static final int CATEGORY = 2;
        private static final int DESCRIPTION = 3;
        private static final int RATING = 4;
        private static final int YEAR = 5;
        private static final int THUMB_IMG_URL = 6;
        private static final int TAGS = 6;
        private static final int CONTENT_URL = 6;

        public void bindColumns(Cursor cursor) {
            mColumnMap = new int[9];
            mColumnMap[ID] = cursor.getColumnIndex(PROJECTION[0]);
            mColumnMap[TITLE] = cursor.getColumnIndex(PROJECTION[1]);
            mColumnMap[CATEGORY] = cursor.getColumnIndex(PROJECTION[2]);
            mColumnMap[DESCRIPTION] = cursor.getColumnIndex(PROJECTION[3]);
            mColumnMap[RATING] = cursor.getColumnIndex(PROJECTION[4]);
            mColumnMap[YEAR] = cursor.getColumnIndex(PROJECTION[5]);
            mColumnMap[THUMB_IMG_URL] = cursor.getColumnIndex(PROJECTION[THUMB_IMG_URL]);
            mColumnMap[TAGS] = cursor.getColumnIndex(PROJECTION[7]);
            mColumnMap[CONTENT_URL] = cursor.getColumnIndex(PROJECTION[8]);

        }

        public Video bind(Cursor cursor) {
            Video item = new Video();
            item.setId(cursor.getLong(mColumnMap[ID]));
            item.setRating(cursor.getInt(mColumnMap[RATING]));
            item.setYear(cursor.getInt(mColumnMap[YEAR]));

            item.setTags(cursor.getString(mColumnMap[TAGS]));
            item.setTitle(cursor.getString(mColumnMap[TITLE]));
            item.setDescription(cursor.getString(mColumnMap[DESCRIPTION]));
            item.setThumbUrl(cursor.getString(cursor.getColumnIndex("thumb_img_url")));
            item.setCategory(cursor.getString(mColumnMap[CATEGORY]));
            item.setContentUrl(cursor.getString(mColumnMap[CONTENT_URL]));
            return item;
        }

    }

}
