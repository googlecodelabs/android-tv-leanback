package com.android.example.leanback.search;

import android.test.AndroidTestCase;

public class DdlTest extends AndroidTestCase {

    public void testViewDdl() {
        final String required = "CREATE VIEW search AS SELECT video.*,FTS_title FROM video JOIN fts ON video._id=fts.docid";
        final String sql = AbstractContract.Ddl.builder()
                .create(AbstractContract.Ddl.Type.VIEW, UniversalSearchContract.SearchView.NAME)
                .columnList(UniversalSearchContract.Video.TABLE_NAME + ".*", UniversalSearchContract.VideoFts.FTS_TITLE)
                .from(UniversalSearchContract.Video.TABLE_NAME)
                .join(UniversalSearchContract.VideoFts.TABLE_NAME,
                        UniversalSearchContract.Video.TABLE_NAME + "." + UniversalSearchContract.Video.ID
                        + "=" + UniversalSearchContract.VideoFts.TABLE_NAME + "." + UniversalSearchContract.VideoFts.ID)
                .build();
        System.out.println(sql);
        assertEquals(required.toLowerCase(), sql.toLowerCase());
    }

    public void testBasicDdl() {
        final String sql1 = AbstractContract.Ddl.builder()
                .create(AbstractContract.Ddl.Type.TABLE, "test1")
                .columnDef("one", "text", "two", "integer")
                .build();
        assertEquals("create table test1(one text,two integer)", sql1.toLowerCase());
        final String sql2 = AbstractContract.Ddl.builder()
                .create(AbstractContract.Ddl.Type.VIEW, "v")
                .columnList("*")
                .from("test1")
                .join("test1", "id=_id")
                .build();
        assertEquals("create view v as select * from test1 join test1 on id=_id", sql2.toLowerCase());
    }
}
// EOF
