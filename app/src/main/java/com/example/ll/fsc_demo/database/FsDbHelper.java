package com.example.ll.fsc_demo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ll on 4/7/15.
 */
public class FsDbHelper extends SQLiteOpenHelper {
    private static String DB_NAME = "fsc.db";
    private static int DB_VERSION = 2;

    public FsDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        FsParamTbl.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        FsParamTbl.onUpgrade(db, oldVersion, newVersion);
    }
}
