package com.example.chancharwei.dailyapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDBHelper extends SQLiteOpenHelper{
    private static final String TAG = MyDBHelper.class.getName();
    private static final String EXCHANGERATE_DATABASE_NAME = "exchangeRate.db";
    private static final int VERSION_EXCHANGERATE = 1;
    private static String tableName = "";

    public MyDBHelper(Context context, String dataBaseName, SQLiteDatabase.CursorFactory factory, int version) {
        super(context,dataBaseName,factory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG,"onCreate");
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(TAG,"onUpgrade");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.d(TAG,"onOpen");
        createTable(db);
        super.onOpen(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        Log.d(TAG,"onConfigure");
        super.onConfigure(db);
    }

    public synchronized static SQLiteDatabase getDataBase(Context context, String dataBaseName, String tableName, boolean isRead){
        Log.d(TAG,"tableName = "+tableName);
        MyDBHelper.tableName = tableName;
        SQLiteDatabase database = null;
        int version = mappingDataBaseVersion(dataBaseName);
        if(isRead){
            database = new MyDBHelper(context,dataBaseName,null,version).getReadableDatabase();
        }else{
            database = new MyDBHelper(context,dataBaseName,null,version).getWritableDatabase();
        }
        return database;
    }

    private void createTable(SQLiteDatabase db){
        if(MyDBHelper.tableName.equals(ExchangeRatePerDayRecord.TABLE_NAME)){
            db.execSQL(ExchangeRatePerDayRecord.CREATE_TABLE);
            Log.d(TAG,"check createTable ExchangeRatePerDayRecord command = "+ExchangeRatePerDayRecord.CREATE_TABLE);
        }else if(MyDBHelper.tableName.equals(ExchangeRateMonitorRecord.TABLE_NAME)){
            db.execSQL(ExchangeRateMonitorRecord.CREATE_TABLE);
            Log.d(TAG,"check createTable ExchangeRateMonitorRecord command = "+ExchangeRateMonitorRecord.CREATE_TABLE);
        }
    }

    private static int mappingDataBaseVersion(String dataBaseName){
        if(dataBaseName.equals(EXCHANGERATE_DATABASE_NAME)){
            return VERSION_EXCHANGERATE;
        }

        return -1;
    }

}
