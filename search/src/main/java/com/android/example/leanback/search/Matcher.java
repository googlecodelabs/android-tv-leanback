package com.android.example.leanback.search;

import android.content.UriMatcher;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.LinkedList;

/**
 * Created by dmalykhanov on 11/27/14.
 */
public class Matcher {

    public static interface Segment {
        String table();
        String type();
        String[] projection(String... projection);
        String selection(String selection);
        String[] selectionArgs(Uri uri, String... selectionArgs);
    }

    public static abstract class CommonSegment implements Segment {
        protected final String table;

        protected CommonSegment(String table) {
            this.table = table;
        }

        @Override
        public String table() {
            return table;
        }

        @Override
        public String type() {
            return "vnd.android.cursor.dir/vnd.com.android.example.leanback." + table;
        }

        @Override
        public String[] projection(String... projection) {
            return projection;
        }

        @Override
        public String selection(String selection) {
            return selection;
        }

        @Override
        public String[] selectionArgs(Uri uri, String... selectionArgs) {
            return selectionArgs;
        }
    }

    protected static class TableSegment extends CommonSegment {

        public TableSegment(String table) {
            super(table);
        }

    }

    protected static class RowSegment extends CommonSegment {

        public RowSegment(String table) {
            super(table);
        }

        @Override
        public String selection(String selection) {
            return DatabaseUtils.concatenateWhere(selection, BaseColumns._ID + "=?");
        }

        @Override
        public String[] selectionArgs(Uri uri, String... selectionArgs) {
            return DatabaseUtils.appendSelectionArgs(selectionArgs,
                    new String[] { uri.getLastPathSegment() });
        }
    }

    private final UriMatcher uriMatcher;
    private final Segment[] segments;

    protected Matcher(UriMatcher uriMatcher, Segment... segments) {
        this.uriMatcher = uriMatcher;
        this.segments = segments;
    }

    public Segment match(Uri uri) {
        final int id = uriMatcher.match(uri);
        if (UriMatcher.NO_MATCH != id) {
            return segments[id];
        } else {
            throw new IllegalArgumentException("Unsupported uri " + uri);
        }
    }

    public static class Builder {
        private final String authority;
        private final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        private final LinkedList<Segment> segments = new LinkedList<>();

        public Builder(String authority) {
            this.authority = authority;
        }

        public Builder add(final String path, final Segment segment) {
            matcher.addURI(authority, path, segments.size());
            segments.add(segment);
            return this;
        }

        public Builder table(String path, AbstractContract.Table table) {
            return add(path, new TableSegment(table.name()));
        }

        public Builder row(String path, AbstractContract.Table table) {
            return add(path + "/#", new RowSegment(table.name()));
        }

        public Matcher build() {
            return new Matcher(matcher, segments.toArray(new Segment[segments.size()]));
        }
    }
}
