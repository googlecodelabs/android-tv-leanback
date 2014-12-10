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

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.example.leanback.PlayerActivity;
import com.android.example.leanback.R;
import com.android.example.leanback.data.Video;
import com.android.example.leanback.data.VideoDataManager;
import com.android.example.leanback.data.VideoItemContract;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by anirudhd on 11/2/14.
 */
public class RecommendationsService extends IntentService {

    private static final String TAG = "RecommendationsService";
    private static final int MAX_RECOMMENDATIONS = 3;
    public static final String EXTRA_BACKGROUND_IMAGE_URL = "background_image_url";

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;


    public RecommendationsService() {
        super("RecommendationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ContentProviderClient client = getContentResolver().acquireContentProviderClient(VideoItemContract.VideoItem.buildDirUri());
        try {
            Cursor cursor = client.query(VideoItemContract.VideoItem.buildDirUri(), VideoDataManager.PROJECTION, null, null, VideoItemContract.VideoItem.DEFAULT_SORT);

            VideoDataManager.VideoItemMapper mapper = new VideoDataManager.VideoItemMapper();
            mapper.bindColumns(cursor);

            NotificationManager mNotificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            Log.d(TAG, mNotificationManager == null ? "It's null" : mNotificationManager.toString());

            int count = 1;


            while (cursor.moveToNext() && count <= MAX_RECOMMENDATIONS) {


                Video video = mapper.bind(cursor);
                PendingIntent pendingIntent = buildPendingIntent(video);
                Bundle extras = new Bundle();
                extras.putString(EXTRA_BACKGROUND_IMAGE_URL, video.getThumbUrl());

                Bitmap image = Picasso.with(getApplicationContext())
                        .load(video.getThumbUrl())
                        .resize(VideoDetailsFragment.dpToPx(DETAIL_THUMB_WIDTH, getApplicationContext()),
                                VideoDetailsFragment.dpToPx(DETAIL_THUMB_HEIGHT, getApplicationContext()))
                        .get();

                Notification notification = new NotificationCompat.BigPictureStyle(
                        new NotificationCompat.Builder(getApplicationContext())
                                .setContentTitle(video.getTitle())
                                .setContentText(video.getDescription())
                                .setPriority(4)
                                .setLocalOnly(true)
                                .setOngoing(true)
                                .setColor(getApplicationContext().getResources().getColor(R.color.primary))
                                        // .setCategory(Notification.CATEGORY_RECOMMENDATION)
                                .setCategory("recommendation")
                                .setLargeIcon(image)
                                .setSmallIcon(R.drawable.ic_stat_f)
                                .setContentIntent(pendingIntent)
                                .setExtras(extras))
                        .build();

                mNotificationManager.notify(count, notification);
                count++;
            }

            cursor.close();
        } catch (RemoteException re) {
            Log.e(TAG, "Cannot query VideoItems", re);
        } catch (IOException re) {
            Log.e(TAG, "Cannot download thumbnail", re);
        } finally {
            client.release();
        }
    }

    private PendingIntent buildPendingIntent(Video video) {
        Intent detailsIntent = new Intent(this, PlayerActivity.class);
        detailsIntent.putExtra(Video.INTENT_EXTRA_VIDEO, video);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(VideoDetailsActivity.class);
        stackBuilder.addNextIntent(detailsIntent);
        // Ensure a unique PendingIntents, otherwise all recommendations end up with the same
        // PendingIntent
        detailsIntent.setAction(Long.toString(video.getId()));

        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }
}
