package com.example.chancharwei.dailyapp.data;


import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class ExchangeRatePerDayRecord {
    private static final String TAG = ExchangeRatePerDayRecord.class.getName();
    private SQLiteDatabase db;
    public static final String TABLE_NAME = "exchangeRateTable";
    private static final String KEY_ID = "_id";
    private static final String DATETIME_COLUMN = "DateTime";
    private static final String TYPE_OF_CURRENCY = "Currency";
    private static final String CASHRATE_BUY_MAX = "CashRateBuyMax";
    private static final String CASHRATE_BUY_MIN = "CashRateBuyMin";
    private static final String CASHRATE_SELL_MAX = "CashRateSellMax";
    private static final String CASHRATE_SELL_MIN = "CashRateSellMin";
    private static final String SPOTRATE_BUY_MAX = "SpotRateBuyMax";
    private static final String SPOTRATE_BUY_MIN = "SpotRateBuyMin";
    private static final String SPOTRATE_SELL_MAX = "SpotRateSellMax";
    private static final String SPOTRATE_SELL_MIN = "SpotRateSellMin";

    public static final String CREATE_TABLE =
            "CREATE TABLE if not exists " +TABLE_NAME+ " (" +
                    KEY_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + //0
                    DATETIME_COLUMN+ " TEXT NOT NULL, " +  //1
                    TYPE_OF_CURRENCY+ " TEXT NOT NULL, " +  //2
                    CASHRATE_BUY_MAX+ " REAL, "+ //3
                    CASHRATE_BUY_MIN+ " REAL, "+ //4
                    CASHRATE_SELL_MAX+ " REAL, "+ //5
                    CASHRATE_SELL_MIN+ " REAL, "+ //6
                    SPOTRATE_BUY_MAX+ " REAL, "+ //7
                    SPOTRATE_BUY_MIN+ " REAL, "+ //8
                    SPOTRATE_SELL_MAX+ " REAL, "+ //9
                    SPOTRATE_SELL_MIN+ " REAL)"; //10

    public ExchangeRatePerDayRecord(Context context, boolean isRead){
        db = MyDBHelper.getDataBase(context,"exchangeRate.db",TABLE_NAME,isRead);
    }

    public void close(){
        if(db!=null && db.isOpen()){
            Log.d(TAG,"Byron database close");
            db.close();
        }
    }
    public long insert(ExchangeRateTableData data){
        ContentValues cv = setDataToDataBase(data);
        long id = db.insert(TABLE_NAME,null,cv);
        data.setId(id);
        return id;
    }

    public boolean update(ExchangeRateTableData data){
        ContentValues cv = setDataToDataBase(data);
        String where = KEY_ID + "=" + data.getId();
        return db.update(TABLE_NAME,cv,where,null) > 0;
    }

    public ExchangeRateTableData queryByID(long id){
        String where = KEY_ID + "=" + id;
        Cursor cursor = db.query(TABLE_NAME,null,where,null,
                null,null,null,null);

        if(cursor.getCount()!=0){
            cursor.moveToFirst();
        }else{
            Log.e(TAG+".queryByID"," no match data in database");
            return null;
        }
        if(cursor.getLong(0) != id){
            Log.d(TAG+".queryByID","get wrong data from cursor");
            return null;
        }else{
            return getDataFromDataBase(cursor);
        }
    }

    public ExchangeRateTableData queryByDateAndCurrency(String dateTime,String currency){

        String where = DATETIME_COLUMN + "='" + dateTime + "' and " + TYPE_OF_CURRENCY + "='" + currency +"'";
        Log.d(TAG+".queryByDateAndCurrency","Byron check dateTime = "+dateTime+", currency = "+currency);
        Cursor cursor = db.query(TABLE_NAME,null,where,null,
                    null,null,null,null);

        if(cursor.getCount() != 0){
            cursor.moveToFirst();
        }else{
            Log.e(TAG+".queryByDateAndCurrency"," no match data in database");
            return null;
        }
        Log.d(TAG,"Byron get count = "+cursor.getCount());
        return getDataFromDataBase(cursor);
    }

    public ArrayList<ExchangeRateTableData> queryDateOrCurrency(String dateTime,String currency){
        String where;
        if(dateTime != null){
            where = DATETIME_COLUMN + "='" + dateTime +"'";
        }else if(currency != null){
            where = TYPE_OF_CURRENCY + "='" + currency +"'";
        }else{
            return null;
        }

        Cursor cursor = db.query(TABLE_NAME,null,where,null,
                null,null,null,null);

        ArrayList<ExchangeRateTableData> dataList = new ArrayList<>();
        if(cursor.getCount()!=0){
            cursor.moveToFirst();
        }else{
            Log.e(TAG+".queryAllData"," no match data in database");
            return null;
        }
        Log.d(TAG,"count = "+cursor.getCount()+" position = "+cursor.getPosition());
        do{
            dataList.add(getDataFromDataBase(cursor));
            //Log.d(TAG,"position in while = "+cursor.getPosition());
        }while(cursor.moveToNext());
        return dataList;
    }

    public ArrayList<ExchangeRateTableData> queryAllData(){
        Cursor cursor = db.query(TABLE_NAME,null,null,null,
                null,null,null,null);

        ArrayList<ExchangeRateTableData> dataList = new ArrayList<>();
        if(cursor.getCount()!=0){
            cursor.moveToFirst();
        }else{
            Log.e(TAG+".queryAllData"," no match data in database");
            return null;
        }
        Log.d(TAG,"count = "+cursor.getCount()+" position = "+cursor.getPosition());
        do{
            dataList.add(getDataFromDataBase(cursor));
            //Log.d(TAG,"position in while = "+cursor.getPosition());
        }while(cursor.moveToNext());
        Log.d(TAG,"dataList size = "+dataList.get(90).getDateTime());
        return dataList;
    }

    private ExchangeRateTableData getDataFromDataBase(Cursor cursor){
        ExchangeRateTableData data = new ExchangeRateTableData();
        data.setId(cursor.getLong(0));
        data.setDateTime(cursor.getString(1));
        data.setCurrency(cursor.getString(2));
        data.setCashRateBuyMax(cursor.getDouble(3));
        data.setCashRateBuyMin(cursor.getDouble(4));
        data.setCashRateSellMax(cursor.getDouble(5));
        data.setCashRateSellMin(cursor.getDouble(6));
        data.setSpotRateBuyMax(cursor.getDouble(7));
        data.setSpotRateBuyMin(cursor.getDouble(8));
        data.setSpotRateSellMax(cursor.getDouble(9));
        data.setSpotRateSellMin(cursor.getDouble(10));
        return data;
    }

    private ContentValues setDataToDataBase(ExchangeRateTableData data){
        ContentValues cv = new ContentValues();
        cv.put(DATETIME_COLUMN,data.getDateTime());
        cv.put(TYPE_OF_CURRENCY,data.getCurrency());
        cv.put(CASHRATE_BUY_MAX,data.getCashRateBuyMax());
        cv.put(CASHRATE_BUY_MIN,data.getCashRateBuyMin());
        cv.put(CASHRATE_SELL_MAX,data.getCashRateSellMax());
        cv.put(CASHRATE_SELL_MIN,data.getCashRateSellMin());
        cv.put(SPOTRATE_BUY_MAX,data.getSpotRateBuyMax());
        cv.put(SPOTRATE_BUY_MIN,data.getSpotRateBuyMin());
        cv.put(SPOTRATE_SELL_MAX,data.getSpotRateSellMax());
        cv.put(SPOTRATE_SELL_MIN,data.getSpotRateSellMin());
        return cv;
    }

    private long findKey(String dateTime,String currency){
        String[] columns = new String[2];
        columns[0] = DATETIME_COLUMN;
        columns[1] = TYPE_OF_CURRENCY;
        Cursor cursor = db.query(TABLE_NAME,columns,null,null,
                null,null,null,null);

        if(cursor.getCount()!=0){
            cursor.moveToFirst();
        }else{
            Log.e(TAG+".findKey"," no match data in database");
            return -1;
        }

        do{
            if(dateTime.equals(cursor.getString(1)) && currency.equals(cursor.getString(2))){
                Log.d(TAG,"get Correct ID ("+cursor.getLong(0)+" from dateTime = "+dateTime+" currency = "+currency);
                return cursor.getLong(0);
            }
        }while(cursor.moveToNext());

        return -1;

    }

}
