package com.android.example.leanback.search;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

public class SearchContentProvider extends ContentProvider {

    private final Matcher matcher;
    private SQLiteOpenHelper database;
    private ContentResolver resolver;

    private static String as(String colum, String alias) {
        return colum + " AS " + alias;
    }

    public SearchContentProvider() {
        final Matcher.Builder builder = new Matcher.Builder(UniversalSearchContract.AUTHORITY);
        for (final AbstractContract.Table table : AbstractContract.tables(UniversalSearchContract.class)) {
            builder.table(table.name(), table);
            builder.row(table.name(), table);
        }
        builder.add("search/search_suggest_query/*", new Matcher.CommonSegment(UniversalSearchContract.SearchView.NAME) {

            @Override
            public String[] projection(String... projection) {
                return new String[] {
                        as(UniversalSearchContract.Video.ID, BaseColumns._ID),
                        as(UniversalSearchContract.Video.TITLE, SearchManager.SUGGEST_COLUMN_TEXT_1),
                        as(UniversalSearchContract.Video.DESCRIPTION, SearchManager.SUGGEST_COLUMN_TEXT_2),
                        as(UniversalSearchContract.Video.IMAGE, SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE),
                        as("'video/*'", SearchManager.SUGGEST_COLUMN_CONTENT_TYPE),
                        as(UniversalSearchContract.Video.YEAR, SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR),
                        as(UniversalSearchContract.Video.DURATION, SearchManager.SUGGEST_COLUMN_DURATION),
                        as(UniversalSearchContract.Video.PRICE_BUY, SearchManager.SUGGEST_COLUMN_PURCHASE_PRICE),
                        as(UniversalSearchContract.Video.PRICE_RENT, SearchManager.SUGGEST_COLUMN_RENTAL_PRICE),

                };
            }

            @Override
            public String selection(String selection) {
                return DatabaseUtils.concatenateWhere(selection,
                        UniversalSearchContract.VideoFts.FTS_TITLE + " MATCH ?");
            }

            @Override
            public String[] selectionArgs(Uri uri, String... selectionArgs) {
                return DatabaseUtils.appendSelectionArgs(selectionArgs,
                        new String[] { uri.getLastPathSegment() });
            }
        });
        this.matcher = builder.build();
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        database = new SearchOpenHelper(context);
        resolver = context.getContentResolver();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return matcher.match(uri).type();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final Matcher.Segment segment = matcher.match(uri);
        final Cursor cursor = database.getReadableDatabase().query(
                segment.table(),
                segment.projection(projection),
                segment.selection(selection),
                segment.selectionArgs(uri, selectionArgs),
                null, null, null);
        cursor.setNotificationUri(resolver, uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final Matcher.Segment segment = matcher.match(uri);
        final long id = database.getWritableDatabase().insert(
                segment.table(),
                null,
                values);
        if ((-1L) != id) {
            resolver.notifyChange(uri, null);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final Matcher.Segment segment = matcher.match(uri);
        final int rows = database.getWritableDatabase().delete(
                segment.table(),
                segment.selection(selection),
                segment.selectionArgs(uri, selectionArgs));
        if (rows > 0) {
            resolver.notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final Matcher.Segment segment = matcher.match(uri);
        final int rows = database.getWritableDatabase().update(
                segment.table(),
                values,
                segment.selection(selection),
                segment.selectionArgs(uri, selectionArgs));
        if (rows > 0) {
            resolver.notifyChange(uri, null);
        }
        return rows;
    }
}
// EOF
