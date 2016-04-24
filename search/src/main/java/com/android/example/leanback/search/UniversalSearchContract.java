package com.android.example.leanback.search;

import android.app.SearchManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class UniversalSearchContract extends AbstractContract {
    /*package*/ static final String AUTHORITY = "com.android.example.leanback.search";
    /*package*/ static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    protected UniversalSearchContract() { }

    public static class Video extends Table {
        /*package*/ static final String TABLE_NAME = "video";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, TABLE_NAME);

        public static final String ID = _ID;
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String IMAGE = "image";
        public static final String YEAR = "year";
        public static final String DURATION = "duration";
        public static final String PRICE_RENT = "price_rent";
        public static final String PRICE_BUY = "price_buy";

        @Override
        /*package*/ String name() {
            return TABLE_NAME;
        }

        @Override
        void onCreate(SQLiteDatabase db) {
            Ddl.builder()
                    .create(Ddl.Type.TABLE, name())
                    .columnDef(
                            ID, TYPE_PRIMARY_KEY,
                            TITLE, TYPE_TEXT,
                            DESCRIPTION, TYPE_TEXT,
                            IMAGE, TYPE_TEXT,
                            YEAR, TYPE_INTEGER,
                            DURATION, TYPE_INTEGER,
                            PRICE_RENT, TYPE_INTEGER,
                            PRICE_BUY, TYPE_INTEGER
                    )
                    .execSql(db);
        }

        @Override
        void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Ddl.builder().drop(Ddl.Type.TABLE, name()).execSql(db);
        }

    }

    public static class VideoFts extends Table {
        /*package*/ static final String TABLE_NAME = "fts";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, TABLE_NAME);

        public static final String ID = "docid";
        public static final String FTS_TITLE = "FTS_" + Video.TITLE;
        public static final String FTS_DESCRIPTION = "FTS_" + Video.DESCRIPTION;

        @Override
        /*package*/ String name() {
            return TABLE_NAME;
        }

        @Override
        void onCreate(SQLiteDatabase db) {
            Ddl.builder()
                .create(Ddl.Type.VIRTUAL_TABLE, name()).append(" USING fts3")
                .columnDef(
                        FTS_TITLE, TYPE_TEXT,
                        FTS_DESCRIPTION, TYPE_TEXT
                )
                .execSql(db);
        }

        @Override
        void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Ddl.builder().drop(Ddl.Type.VIRTUAL_TABLE, name()).execSql(db);
        }

    }

    public static class SearchView extends Table {
        /*package*/ static final String NAME = "search";
        public static final Uri CONTENT_URI = BASE_URI.buildUpon()
                .appendPath(NAME).appendPath(SearchManager.SUGGEST_URI_PATH_QUERY).build();

        @Override
        String name() {
            return NAME;
        }

        @Override
        void onCreate(SQLiteDatabase db) {
            Ddl.builder()
                    .create(Ddl.Type.VIEW, name())
                    .columnList(Video.TABLE_NAME + ".*", VideoFts.FTS_TITLE)
                    .from(Video.TABLE_NAME)
                    .join(VideoFts.TABLE_NAME,
                            Video.TABLE_NAME + "." + Video.ID
                                    + "=" + VideoFts.TABLE_NAME + "." + VideoFts.ID)
                    .execSql(db);
        }

        @Override
        void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Ddl.builder().drop(Ddl.Type.VIEW, name()).execSql(db);
        }
    }
}
// EOF

