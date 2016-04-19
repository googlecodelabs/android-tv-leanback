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

import android.net.Uri;

public class VideoItemContract {
    public static final String CONTENT_AUTHORITY = "com.android.example.leanback";
    public static final Uri BASE_URI = Uri.parse("content://com.android.example.leanback");

    public interface VideoItemColumns {
        /**
         * Type: INTEGER PRIMARY KEY AUTOINCREMENT
         */
        String _ID = "_id";
        /**
         * Type: TEXT NOT NULL DEFAULT 'Movies'
         */
        String CATEGORY = "category";
        /**
         * Type: TEXT NOT NULL DEFAULT ''
         */
        String TITLE = "title";
        /**
         * Type: TEXT NOT NULL
         */
        String CONTENT_URL = "content_url";
        /**
         * Type: TEXT NOT NULL
         */
        String DESCRIPTION = "description";
        /**
         * Type: INTEGER DEFAULT 0
         */
        String RATING = "rating";
        /**
         * Type: TEXT
         */
        String THUMB_IMG_URL = "thumb_img_url";
        /**
         * Type: TEXT
         */
        String TAGS = "tags";
        /**
         * Type: INTEGER DEFAULT 2000
         */
        String YEAR = "year";
    }

    public static class VideoItem implements VideoItemColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.android.example.leanback.video_item";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.android.example.leanback.video_item";
        public static final String DEFAULT_SORT = TITLE + " ASC";

        /**
         * Matches: /videoitem/
         */
        public static Uri buildDirUri() {
            return BASE_URI.buildUpon().appendPath("videoitem").build();
        }

        /**
         * Matches: /videoitem/[_id]/
         */
        public static Uri buildItemUri(long _id) {
            return BASE_URI.buildUpon().appendPath("videoitem").appendPath(Long.toString(_id)).build();
        }
    }

    private VideoItemContract() {
    }
}
