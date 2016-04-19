
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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.List;

public abstract class AbstractVideoItemProvider extends ContentProvider {
    private SQLiteOpenHelper mOpenHelper;

    interface Tables {
        String VIDEO_ITEM = "video_item";
    }

    interface Views {

    }

    private static final int VIDEOITEM = 0;
    private static final int VIDEOITEM__ID = 1;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = VideoItemContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "videoitem", VIDEOITEM);
        matcher.addURI(authority, "videoitem/#", VIDEOITEM__ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new VideoDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case VIDEOITEM:
                return VideoItemContract.VideoItem.CONTENT_TYPE;
            case VIDEOITEM__ID:
                return VideoItemContract.VideoItem.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final SelectionBuilder builder = buildQuerySelection(uri);
        return builder.where(selection, selectionArgs).query(db, projection, sortOrder);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case VIDEOITEM: {
                final long _id = db.insertOrThrow(Tables.VIDEO_ITEM, null, values);
                return VideoItemContract.VideoItem.buildItemUri(_id);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSelection(uri);
        return builder.where(selection, selectionArgs).update(db, values);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSelection(uri);
        return builder.where(selection, selectionArgs).delete(db);
    }

    private SelectionBuilder buildQuerySelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final List<String> paths = uri.getPathSegments();
        final int match = sUriMatcher.match(uri);
        switch (match) {

            default: {
                return buildSelection(uri, match, builder);
            }
        }
    }

    private SelectionBuilder buildSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        return buildSelection(uri, match, builder);
    }

    private SelectionBuilder buildSelection(Uri uri, int match, SelectionBuilder builder) {
        final List<String> paths = uri.getPathSegments();
        switch (match) {
            case VIDEOITEM: {
                return builder.table(Tables.VIDEO_ITEM);
            }
            case VIDEOITEM__ID: {
                final String _id = paths.get(1);
                return builder.table(Tables.VIDEO_ITEM).where(VideoItemContract.VideoItem._ID + "=?", _id);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }
}
