package com.android.example.leanback.search;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContext;
import android.util.Log;

import java.io.File;

public class UniversalSearchContractTest extends AndroidTestCase {

    private final static String TAG = UniversalSearchContractTest.class.getSimpleName();

    private void log(String msg) {
        Log.i(getClass().getSimpleName(), msg);
    }

    private static final String DATABASE = "test.db";

    private void assertTables(final UniversalSearchContract.Table[] tables) {
        assertNotNull(tables);
        assertEquals(3, tables.length);
    }

    private class MockContext2 extends MockContext {

        @Override
        public Resources getResources() {
            return getContext().getResources();
        }

        @Override
        public File getDir(String name, int mode) {
            // name the directory so the directory will be separated from
            // one created through the regular Context
            return getContext().getDir("mockcontext2_" + name, mode);
        }

        @Override
        public Context getApplicationContext() {
            return this;
        }
    }

    private SQLiteDatabase createTables(final boolean delete, final UniversalSearchContract.Table... tables) {
        SQLiteDatabase db = null;
        final String filenamePrefix = "test.";
        RenamingDelegatingContext targetContextWrapper = new
                RenamingDelegatingContext(
                new MockContext2(), // The context that most methods are
                //delegated to
                getContext(), // The context that file methods are delegated to
                filenamePrefix);
        final Context context = new IsolatedContext(super.getContext().getContentResolver(), targetContextWrapper);
        try {
            db = context.openOrCreateDatabase(DATABASE, 1, null);
            for (final UniversalSearchContract.Table table : tables) {
                table.onCreate(db);
                log("Table " + table + " onCreate successful");
            }
            return db;
        } finally {
            if (null != db && delete) {
                db.close();
                context.deleteDatabase(DATABASE);
            }
        }
    }

    public void testTables() {
        final UniversalSearchContract.Table[] tables = AbstractContract.tables(UniversalSearchContract.class);
        assertTables(tables);
    }

    public void testTablesDdl() {
        final UniversalSearchContract.Table[] tables = AbstractContract.tables(UniversalSearchContract.class);
        assertTables(tables);
        try {
            createTables(true, tables);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testMockData() {
        final UniversalSearchContract.Table[] tables = AbstractContract.tables(UniversalSearchContract.class);
        assertTables(tables);
        SQLiteDatabase db = null;
        try {
            db = createTables(false, tables);
            MockData.insert(getContext(), db);
        } catch (Exception e) {
            Log.e(TAG, "Cannot test mock data", e);
            fail(e.getMessage());
        } finally {
            if (null != db) {
                db.close();
                getContext().deleteDatabase(DATABASE);
            }
        }
    }
}
// EOF
