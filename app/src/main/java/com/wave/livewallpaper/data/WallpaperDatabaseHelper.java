package com.wave.livewallpaper.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/* loaded from: classes4.dex */
public class WallpaperDatabaseHelper extends SQLiteOpenHelper {
    private static String DATABASE_NAME = "wallpaper";
    public static final int DATABASE_VERSION = 1;
    private static final String TAG = "WallpaperDatabaseHelper";
    private static String TABLE_NAME = "prefs";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY,key TEXT UNIQUE,value TEXT)";

    public static class KeyValueTable implements BaseColumns {
        public static final String KEY = "key";
        public static final String VALUE = "value";
    }

    public WallpaperDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
    }

    public void closeDbConnection() {
        close();
    }

    public String getValue(String str) {
        try {
            Cursor cursorQuery = getReadableDatabase().query(TABLE_NAME, null, "key = ?", new String[]{str}, null, null, null);
            if (cursorQuery == null || cursorQuery.getCount() <= 0 || !cursorQuery.moveToFirst()) {
                return "";
            }
            String string = cursorQuery.getString(cursorQuery.getColumnIndex("value"));
            cursorQuery.close();
            return string;
        } catch (Exception e2) {
            Log.d(TAG, e2.getMessage(), e2);
            return "";
        }
    }

    public void insertValue(String str, String str2) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("key", str);
        contentValues.put("value", str2);
        writableDatabase.insertWithOnConflict(TABLE_NAME, null, contentValues, 5);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase sQLiteDatabase) throws SQLException {
        sQLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i2, int i3) {
    }
}
