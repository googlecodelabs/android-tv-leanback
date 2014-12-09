package com.android.example.leanback.search;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SearchOpenHelper extends SQLiteOpenHelper {

    private final Context context;

    public SearchOpenHelper(Context context) {
        super(context, "search.db", null, 0x00000010);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (AbstractContract.Table table : AbstractContract.tables(UniversalSearchContract.class)) {
            table.onCreate(db);
        }
        MockData.insert(context, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (AbstractContract.Table table : AbstractContract.tables(UniversalSearchContract.class)) {
            table.onUpgrade(db, oldVersion, newVersion);
        }
    }
}
