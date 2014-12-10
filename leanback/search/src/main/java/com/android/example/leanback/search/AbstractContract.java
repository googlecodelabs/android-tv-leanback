package com.android.example.leanback.search;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.android.example.leanback.search.BuildConfig;

import java.util.LinkedList;

public class AbstractContract {
    protected static final String _ID = BaseColumns._ID;

    protected static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY";
    protected static final String TYPE_INTEGER = "INTEGER";
    protected static final String TYPE_TEXT = "TEXT";

    /*package*/ static class Ddl {
        public static enum Type {
            TABLE("TABLE"),
            VIRTUAL_TABLE("VIRTUAL TABLE"),
            VIEW("VIEW");

            public final String token;

            private Type(final String token) {
                this.token = token;
            }
        }

        private static Ddl instance = null;

        public static Ddl builder() {
            return null == instance ? (instance = new Ddl()) : instance;
        }

        public static void reset() {
            instance = null;
        }

        private final StringBuilder sql;

        private Ddl() {
            this(256);
        }

        private Ddl(final int bufferSize) {
            sql = new StringBuilder(bufferSize);
        }

        @Override
        public String toString() {
            return sql.toString();
        }

        public String build() {
            final String statement = sql.toString();
            sql.setLength(0);
            return statement;
        }

        public void execSql(final SQLiteDatabase db) {
            db.execSQL(build());
        }

        public Ddl append(final CharSequence string) {
            sql.append(string);
            return this;
        }

        /*public Ddl append(final CharSequence... strings) {
            for (final CharSequence s : strings) {
                sql.append(s);
            }
            return this;
        }*/

        public Ddl create(final Type type, final String name) {
            sql.append("CREATE ").append(type.token).append(' ').append(name);
            if (Type.VIEW == type) {
                sql.append(" AS SELECT");
            }
            return this;
        }

        public Ddl drop(final Type type, final String name) {
            sql.append("DROP ")
                    .append(Type.VIRTUAL_TABLE == type ? Type.TABLE.token : type.token)
                    .append(" IF EXISTS ").append(name);
            return this;
        }

        public Ddl columnDef(final String... columns) {
            if (BuildConfig.DEBUG) {
                if (0 != (columns.length % 2)) {
                    throw new IllegalArgumentException("Column def array mus have even number of entries");
                }
            }
            final int size = columns.length;
            sql.append('(');
            for (int i=0; i<size; i+=2) {
                sql.append(columns[i]).append(' ').append(columns[i + 1]).append(',');
            }
            if (size > 0) {
                sql.setLength(sql.length() - 1);
            }
            sql.append(')');
            return this;
        }

        public Ddl columnList(final String... columns) {
            final int size = columns.length;
            sql.append(' ');
            for (int i=0; i<size; i++) {
                sql.append(columns[i]).append(',');
            }
            if (size > 0) {
                sql.setLength(sql.length() - 1);
            }
            return this;
        }

        public Ddl from(final String name) {
            sql.append(" FROM ").append(name);
            return this;
        }

        public Ddl join(final String name, final String on) {
            sql.append(" JOIN ").append(name).append(" ON ").append(on);
            return this;
        }
    }

    /*package*/ static abstract class Table {
        /*package*/ Table() { }
        /*package*/ abstract String name();

        /*package*/ abstract void onCreate(SQLiteDatabase db);
        /*package*/ abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
    }

    /*package*/ static Table table(final Class<? extends Table> klass) {
        try {
            return klass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /*package*/ static Table[] tables(final Class<? extends AbstractContract> contract) {
        final LinkedList<Table> tables = new LinkedList<>();
        for (final Class<?> klass : contract.getClasses()) {
            if (Table.class.isAssignableFrom(klass)) {
                try {
                    if (klass.getSimpleName().endsWith("View")) {
                        tables.add((Table) klass.newInstance());
                    } else {
                        tables.addFirst((Table) klass.newInstance());
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(UniversalSearchContract.class.getSimpleName(),
                                "Cannot instantiate table " + klass, e);
                    }
                }
            }
        }
        return tables.toArray(new Table[tables.size()]);
    }

    protected AbstractContract() { }

}
// EOF
