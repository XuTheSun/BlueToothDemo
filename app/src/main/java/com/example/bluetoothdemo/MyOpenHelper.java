package com.example.bluetoothdemo;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by 关旭 on 2018/3/13.
 */

public class MyOpenHelper extends SQLiteOpenHelper {
    public static final String CREATE_TABLE =
            "create table Preload(" +
                    "name varchar(50) Primary Key," +
                    "angle Integer," +
                    "stops Integer," +
                    "focus Integer," +
                    "shots Integer," +
                    "camera varchar(50)," +
                    "continue Integer," +
                    "return Integer," +
                    "direction Integer)";

    public MyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL(CREATE_TABLE);
            Log.d("Debug", "创建数据库成功");
        } catch (SQLiteException se) {
            Log.d("Debug", "创建数据库失败");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
