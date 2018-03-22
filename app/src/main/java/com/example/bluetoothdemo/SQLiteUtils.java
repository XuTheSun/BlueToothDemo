package com.example.bluetoothdemo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/**
 * Created by 关旭 on 2018/3/11.
 */

public class SQLiteUtils{

    public static String tableName = "Preload";

    private static SQLiteUtils instance = new SQLiteUtils();

    public static SQLiteUtils getInstance(){
        return instance;
    }

    private SQLiteUtils() {}

    public void insert(SQLiteDatabase database,String name, int angle, int stops, int focus, int shots, String camera, boolean continues, boolean returns, boolean direction) {
        int continuesInt = continues ? 1 : 0;
        int returnsInt = returns ? 1 : 0;
        int directionInt = direction ? 1 : 0;
        database.execSQL("insert into "+tableName+" (name, angle, stops, focus, shots, camera, continue, return, direction) " +
                "values ('"+name+"',"+angle+","+stops+","+focus+","+shots+",'"+camera+"',"+continuesInt+","+returnsInt+","+directionInt+")");
        Log.e("Debug","成功添加数据。");
    }

    public void delete(SQLiteDatabase database, String name) {
        database.execSQL("delete from "+tableName+" where name ='"+name+"'");
        Log.e("Debug","成功删除数据。");
    }

    public  Cursor getData(SQLiteDatabase database){
        return database.rawQuery("select * from "+tableName, null);
    }

    public void update(SQLiteDatabase database,String name, int angle, int stops, int focus, int shots,String camera, boolean continues, boolean returns, boolean direction){
        int continuesInt = continues ? 1 : 0;
        int returnsInt = returns ? 1 : 0;
        int directionInt = direction ? 1 : 0;
        database.execSQL("update "+tableName+" set angle = "+angle +","+
                " stops="+stops +","+
                " focus="+focus +","+
                " shots=" +shots+","+
                " camera='" +camera+"'" +","+
                " continue=" +continuesInt+","+
                " return=" +returnsInt+","+
                " direction="+directionInt +
                " where name='"+name+"'");
        Log.e("Debug","成功更新数据。");
    }


}
