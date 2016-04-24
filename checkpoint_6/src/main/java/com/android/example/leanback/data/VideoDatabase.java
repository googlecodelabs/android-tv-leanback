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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VideoDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "video.db";
    private static final int DATABASE_VERSION = 1;
    private static final String[] insert = new String[]{"INSERT INTO [video_item] ([description], [title], [category], [rating], [thumb_img_url], [year], [tags], [content_url]) VALUES ( 'Big Buck Bunny tells the story of a giant rabbit with a heart bigger than himself. When one sunny day three rodents rudely harass him, something snaps... and the rabbit ain''t no bunny anymore! In the typical cartoon tradition he prepares the nasty rodents a comical revenge.', 'Big Buck Bunny', 'Movie', 5, 'https://lh6.googleusercontent.com/-RTTiaMb7IcY/VExRPqrDFII/AAAAAAAA8aw/2MzPEG7GCUU/s512/BBBPortrait.jpg', 2000, 'latest, featured', 'http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool''s%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4');",
            "INSERT INTO [video_item] ( [description], [title], [category], [rating], [thumb_img_url], [year], [tags], [content_url]) VALUES ('Sintel is a short computer animated film by the Blender Institute, part of the Blender Foundation', 'Sintel', 'Movie', 0, 'https://lh3.googleusercontent.com/-Udmq7sHMJCU/VEx9sd7dWkI/AAAAAAAA8bg/AQV8plCbG1E/s512/Sintel.jpg', 2000, 'latest, featured', 'http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool''s%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4');",
            "INSERT INTO [video_item] ([description], [title], [category], [rating], [thumb_img_url], [year], [tags], [content_url]) VALUES ('Terminator 2: The judgement day', 'Terminator 2', 'Movies', 0, 'https://lh3.googleusercontent.com/-Wwil8MdyaMM/VEx9sEaqwaI/AAAAAAAA8bk/bLqK4re9G8s/s350/Terminator2.jpg', 2000, 'latest, featured', 'http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool''s%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4');",
            "INSERT INTO [video_item] ( [description], [title], [category], [rating], [thumb_img_url], [year], [tags], [content_url]) VALUES ( 'With the help of Lieutenant Jim Gordon and District Attorney Harvey Dent, Batman sets out to destroy organized crime in Gotham for good. ', 'The Dark Knight', 'Movies', 0, 'https://lh6.googleusercontent.com/-NOG2i8LBnh8/VEx9sCUVhSI/AAAAAAAA8bc/-KVKtI9WHfE/s512/darkknight.jpg', 2000, 'latest, featured', 'http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool''s%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4');",
            "INSERT INTO [video_item] ([description], [title], [category], [rating], [thumb_img_url], [year], [tags], [content_url]) VALUES ( 'Man of Steel is a 2013 superhero film based on the DC Comics character Superman, co-produced by Legendary Pictures and Syncopy Films, distributed by Warner Bros. ', 'Man of Steel', 'Movies', 0, 'https://lh6.googleusercontent.com/-I7MExse6TQs/VEx9smoHkZI/AAAAAAAA8bo/FYxFcLvCDbQ/s326/manofsteel.jpeg', 2000, 'latest, featured', 'http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool''s%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4');",
            "INSERT INTO [video_item] ( [description], [title], [category], [rating], [thumb_img_url], [year], [tags], [content_url]) VALUES ( 'The Amazing Spider-Man is a 2012 American superhero film based on the Marvel Comics character Spider-Man and sharing the title of the character longest-running comic book of the same name ', 'The Amazing Spider-Man', 'Movies', 0, 'https://lh5.googleusercontent.com/-6CDHWU3xeIw/VEx9MSOfvyI/AAAAAAAA8bI/TkdMwotVm4U/s512/spiderman.jpg', 2000, 'latest, featured', 'http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool''s%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4');"};

    public VideoDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE " + AbstractVideoItemProvider.Tables.VIDEO_ITEM + " ("
                + VideoItemContract.VideoItemColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + VideoItemContract.VideoItemColumns.DESCRIPTION + " TEXT NOT NULL,"
                + VideoItemContract.VideoItemColumns.TITLE + " TEXT NOT NULL DEFAULT '',"
                + VideoItemContract.VideoItemColumns.CATEGORY + " TEXT NOT NULL DEFAULT 'Movies',"
                + VideoItemContract.VideoItemColumns.RATING + " INTEGER DEFAULT 0,"
                + VideoItemContract.VideoItemColumns.THUMB_IMG_URL + " TEXT,"
                + VideoItemContract.VideoItemColumns.YEAR + " INTEGER DEFAULT 2000,"
                + VideoItemContract.VideoItemColumns.TAGS + " TEXT,"
                + VideoItemContract.VideoItemColumns.CONTENT_URL + " TEXT NOT NULL"
                + ")";
        Log.d("VideoDatabase", sql);

        db.execSQL(sql);
        inserData(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AbstractVideoItemProvider.Tables.VIDEO_ITEM);
        onCreate(db);
    }

    public void inserData(SQLiteDatabase db) {
        for (String record : insert) {
            db.execSQL(record);
        }

    }
}
