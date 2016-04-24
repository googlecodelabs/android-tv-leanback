package com.android.example.leanback.search;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.util.Log;
import android.util.Pair;

public class SearchContentProviderTest extends ProviderTestCase2<SearchContentProvider> {

    private final static String TAG = SearchContentProviderTest.class.getSimpleName();

    public SearchContentProviderTest() {
        super(SearchContentProvider.class, UniversalSearchContract.AUTHORITY);
    }

    private Cursor getVideoCursor() {
        return getMockContentResolver().query(UniversalSearchContract.Video.CONTENT_URI,
                null, // projection[]
                null, // selection
                null, // selectionArgs[]
                null // sortOrder
        );
    }

    private Pair<Long, String> getMiddleRow() {
        final Cursor cursor = getVideoCursor();
        assertNotNull(cursor);
        assertTrue(cursor.getCount() > 0);
        assertTrue(cursor.moveToPosition(cursor.getCount() / 2));
        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(UniversalSearchContract.Video.ID));
        final String title = cursor.getString(cursor.getColumnIndexOrThrow(UniversalSearchContract.Video.TITLE));
        cursor.close();
        Log.d(TAG, "Sample row: " + id + " " + title);
        return new Pair<>(id, title);
    }

    public void testMockData() {
        final Cursor cursor = getVideoCursor();
        assertNotNull(cursor);
        assertTrue(cursor.getCount() > 0);
        cursor.close();
    }

    public void testVideoQueryAll() {
        final Cursor cursor = getVideoCursor();
        assertNotNull(cursor);
        // test complete iteration, unlike testMockData
        assertTrue(cursor.moveToFirst());
        int count = 0;
        do {
            count++;

        } while (cursor.moveToNext());
        assertTrue(count == cursor.getCount());
        cursor.close();
    }

    public void testVideoQueryById() {
        final Pair<Long, String> data = getMiddleRow();
        final Cursor cursor = getMockContentResolver().query(
                ContentUris.withAppendedId(UniversalSearchContract.Video.CONTENT_URI, data.first),
                null, // projection[]
                null, // selection
                null, // selectionArgs[]
                null // sortOrder
        );
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(data.first.longValue(),
                cursor.getLong(cursor.getColumnIndexOrThrow(UniversalSearchContract.Video.ID)));
        assertEquals(data.second,
                cursor.getString(cursor.getColumnIndexOrThrow(UniversalSearchContract.Video.TITLE)));
        cursor.close();
    }

    public void testFtsQuery() {
        final Pair<Long, String> data = getMiddleRow();
        final Cursor cursor = getMockContentResolver().query(
                Uri.withAppendedPath(UniversalSearchContract.SearchView.CONTENT_URI, data.second.toLowerCase()),
                null, // projection[]
                null, // selection
                null, // selectionArgs[]
                null // sortOrder
        );
        assertNotNull("Cursor not null", cursor);
        assertTrue("Result set not empty", cursor.getCount() > 0);
        boolean found = false;
        if (cursor.moveToFirst()) {
            final int idx = cursor.getColumnIndexOrThrow(UniversalSearchContract.Video.ID);
            do {
                if (data.first == cursor.getLong(idx)) {
                    found = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        assertTrue("Row with id " + data.first + " in the result set", found);
    }
}
// EOF
