package com.example.chancharwei.dailyapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDBHelper extends SQLiteOpenHelper{
    private static final String TAG = MyDBHelper.class.getName();
    private static final String EXCHANGERATE_DATABASE_NAME = "exchangeRate.db";
    private static SQLiteDatabase databaseExchangeRate;
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

    public static SQLiteDatabase getDataBase(Context context, String dataBaseName, String tableName, boolean isRead){
        MyDBHelper.tableName = tableName;
        SQLiteDatabase database = mappingDataBase(dataBaseName);
        int version = mappingDataBaseVersion(dataBaseName);
        Log.d(TAG,"Byron check dataBase null ? ("+(database == null)+"), open ? ("+(database == null? "false":database.isOpen())+") version = "+version);
        if((database == null || !database.isOpen()) && version != -1){
            if(isRead){
                database = new MyDBHelper(context,dataBaseName,null,version).getReadableDatabase();
            }else{
                database = new MyDBHelper(context,dataBaseName,null,version).getWritableDatabase();
            }
            setDataBase(dataBaseName,database);
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
    private static SQLiteDatabase mappingDataBase(String dataBaseName){
        if(dataBaseName.equals(EXCHANGERATE_DATABASE_NAME)){
            return databaseExchangeRate;
        }
        return null;
    }

    private static int mappingDataBaseVersion(String dataBaseName){
        if(dataBaseName.equals(EXCHANGERATE_DATABASE_NAME)){
            return VERSION_EXCHANGERATE;
        }

        return -1;
    }

    private static void setDataBase(String dataBaseName,SQLiteDatabase database){
        if(dataBaseName.equals(EXCHANGERATE_DATABASE_NAME)){
            databaseExchangeRate = database;
        }
    }
}
