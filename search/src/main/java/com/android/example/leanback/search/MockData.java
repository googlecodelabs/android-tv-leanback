package com.android.example.leanback.search;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

public class MockData {
    private MockData() { }

    private static Reader getMockDataReader(final Context context) throws IOException {
        Reader reader;
        try {
            reader = new InputStreamReader(context.getAssets().open("movies.csv"), "UTF-8");
        } catch (UnsupportedOperationException e) {
            reader = new StringReader(
                    //title,year,duration,rent,buy,image,description
                    "The Terminator,1984,6420000,0.0,0.0,x,I'll be back\n" +
                    "Maleficent,2014,5820000,1.0,1.99,y,Magic\n" +
                    "How to Train Your Dragon 2,2014,6300000,2.0,1.11,z,Dragon Fly!\n" +
                    "Transformers: Age of Extinction,2014,9900000,3.0,3.33,z,Get them all\n" +
                    "Transformers: Dark of the Moon,2011,9240000,0.99,1.44,v,A mysterious event from Earth's past...\n" +
                    "Star Wars,1977,7260000,0.55,0.88,b,A long time ago in a galaxy far far away...\n" +
                    "Cortana,2001,6420000,0.0,0.0,n,Fake Entry\n"
            );
        }
        return reader;
    }

    public static void insert(final Context context, final SQLiteDatabase db) {
        BufferedReader reader = null;
        try {
            Log.d("MockData", "Inserting into " + db);
            reader = new BufferedReader(getMockDataReader(context));
            String tmp;
            ContentValues values = new ContentValues();
            int count = 0;
            while (null != (tmp = reader.readLine())) {
                //title,year,duration,rent,buy,image,description
                final String[] fields = tmp.split(",");
                Log.i("MockData", fields.length + tmp);
                if (fields.length < 7) continue;
                values.clear();
                count++;
                values.put(UniversalSearchContract.VideoFts.ID, count);
                values.put(UniversalSearchContract.VideoFts.FTS_TITLE, fields[0]);
                values.put(UniversalSearchContract.VideoFts.FTS_DESCRIPTION, fields[6]);
                db.insert(UniversalSearchContract.VideoFts.TABLE_NAME, null, values);
                values.clear();
                values.put(UniversalSearchContract.Video.ID, count);
                values.put(UniversalSearchContract.Video.TITLE, fields[0]);
                values.put(UniversalSearchContract.Video.YEAR, fields[1]);
                values.put(UniversalSearchContract.Video.DURATION, fields[2]);
                values.put(UniversalSearchContract.Video.PRICE_RENT, fields[3]);
                values.put(UniversalSearchContract.Video.PRICE_BUY, fields[4]);
                values.put(UniversalSearchContract.Video.IMAGE, fields[5]);
                values.put(UniversalSearchContract.Video.DESCRIPTION, fields[6]);
                db.insert(UniversalSearchContract.Video.TABLE_NAME, null, values);
            }
        } catch (IOException e) {
            Log.e(MockData.class.getSimpleName(),
                    "Cannot insert mock data", e);
        } finally {
            if (null != reader) {
                try { reader.close(); } catch (Exception ignore) { }
            }
        }
    }
}
// EOF
