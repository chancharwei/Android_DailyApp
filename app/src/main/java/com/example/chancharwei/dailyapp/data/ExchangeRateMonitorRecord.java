package com.example.chancharwei.dailyapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.ArraySet;
import android.util.Log;

import java.util.ArrayList;

public class ExchangeRateMonitorRecord {
    private static final String TAG = ExchangeRateMonitorRecord.class.getName();
    private SQLiteDatabase db;
    public static final String TABLE_NAME = "exchangeRateMonitorTable";
    private static final String KEY_ID = "_id";
    private static final String NOW_CURRENCY = "NowCurrency";
    private static final String TARGET_CURRENCY = "TargetCurrency";
    private static final String TYPE_OF_EXCHANGERATE = "TypeOfExchangeRate";
    private static final String EXPECTED_EXCHANGERATE = "ExpectedExchangeRate";
    private static final String NOTIFY_DONE = "notifyDone";

    public static final String CREATE_TABLE =
            "CREATE TABLE if not exists " +TABLE_NAME+ " (" +
                    KEY_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + //0
                    NOW_CURRENCY+ " TEXT NOT NULL, " +  //1
                    TARGET_CURRENCY+ " TEXT NOT NULL, " +  //2
                    TYPE_OF_EXCHANGERATE+ " TEXT NOT NULL, " +
                    EXPECTED_EXCHANGERATE+ " REAL, " + //3
                    NOTIFY_DONE+ " REAL)"; //4
    public static final String DROP_TABLE=
            "DROP TABLE if exists "+TABLE_NAME+";";

    public ExchangeRateMonitorRecord(Context context, boolean isRead){
        db = MyDBHelper.getDataBase(context,"exchangeRate.db",TABLE_NAME,isRead);
    }


    private ContentValues setDataToDataBase(ExchangeRateMonitorData data){
        ContentValues cv = new ContentValues();
        cv.put(NOW_CURRENCY,data.getNowCurrency());
        cv.put(TARGET_CURRENCY,data.getTargetCurrency());
        cv.put(TYPE_OF_EXCHANGERATE,data.getTypeOfExchangeRate());
        cv.put(EXPECTED_EXCHANGERATE,data.getExpectedExchangeRate());
        cv.put(NOTIFY_DONE,data.getNotifyDone());
        return cv;
    }


    private ExchangeRateMonitorData getDataFromDataBase(Cursor cursor){
        ExchangeRateMonitorData data = new ExchangeRateMonitorData();
        data.setId(cursor.getLong(0));
        data.setNowCurrency(cursor.getString(1));
        data.setTargetCurrency(cursor.getString(2));
        data.setTypeOfExchangeRate(cursor.getString(3));
        data.setExpectedExchangeRate(cursor.getDouble(4));
        data.setNotifyDone(cursor.getInt(5));
        return data;
    }

    public long insert(ExchangeRateMonitorData data){
        ContentValues cv = setDataToDataBase(data);
        long id = db.insert(TABLE_NAME,null,cv);
        Log.d(TAG,"check insert ID = "+id);
        data.setId(id);
        return id;
    }

    public ExchangeRateMonitorData queryToCheckExistData(ExchangeRateMonitorData data){
        String where = NOW_CURRENCY + "='" + data.getNowCurrency() + "' and " + TARGET_CURRENCY + "='" + data.getTargetCurrency() +"' and "+ TYPE_OF_EXCHANGERATE + "='" + data.getTypeOfExchangeRate() +"'";
        Cursor cursor = db.query(TABLE_NAME,null,where,null,
                null,null,null,null);
        Log.d(TAG,"Byron check condition = "+where);
        Log.d(TAG,"Byron getCount = "+cursor.getCount());
        if(cursor.getCount() != 0){
            cursor.moveToFirst();
            return getDataFromDataBase(cursor);
        }else{
            return null;
        }
    }

    public ArrayList<ExchangeRateMonitorData> querAllData(){
        ArrayList<ExchangeRateMonitorData> dataList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME,null,null,
                null,null,null,null,null);
        Log.d(TAG,"Byron getCount = "+cursor.getCount());

        if(cursor.getCount()!=0){
            cursor.moveToFirst();
        }else{
            Log.e(TAG+".queryAllData"," no match data in database");
            return null;
        }
        Log.d(TAG,"count = "+cursor.getCount()+" position = "+cursor.getPosition());
        do{
            dataList.add(getDataFromDataBase(cursor));
        }while(cursor.moveToNext());
        Log.d(TAG,"dataList size = "+dataList.size());
        return dataList;
    }

    public boolean updateByID(long rowID,ExchangeRateMonitorData data){
        ContentValues cv = setDataToDataBase(data);
        Log.d(TAG,"Byron update row ID is "+rowID);
        String where = KEY_ID + "=" + rowID;
        return db.update(TABLE_NAME,cv,where,null) > 0;
    }

    public ExchangeRateMonitorData queryByID(Long rowID){
        String where = KEY_ID + "=" + rowID;
        Cursor cursor = db.query(TABLE_NAME,null,where,null,
                null,null,null,null);
        if(cursor.getCount()!=0){
            cursor.moveToFirst();
            return getDataFromDataBase(cursor);
        }else{
            return null;
        }
    }

    public ArrayList<ExchangeRateMonitorData>[] queryByCurrency(String currency){
        ArrayList<ExchangeRateMonitorData> nowCurrencyIDList = new ArrayList<ExchangeRateMonitorData>();
        ArrayList<ExchangeRateMonitorData> targetCurrencyIDList = new ArrayList<ExchangeRateMonitorData>();
        ArrayList<ExchangeRateMonitorData>[] dataList = new ArrayList[2];
        String where = NOW_CURRENCY + "='" + currency + "'";
        queryData(nowCurrencyIDList,where);
        where = TARGET_CURRENCY + "='" + currency + "'";
        queryData(targetCurrencyIDList,where);
        dataList[0] = nowCurrencyIDList;
        dataList[1] = targetCurrencyIDList;
        return dataList;
    }

    private void queryData(ArrayList<ExchangeRateMonitorData> dataList,String condition){
        Cursor cursor = db.query(TABLE_NAME,null,condition,null,
                null,null,null,null);
        if(cursor.getCount()!=0){
            cursor.moveToFirst();
            do{
                dataList.add(getDataFromDataBase(cursor));
            }while(cursor.moveToNext());
        }
    }

    public void close(){
        if(db!=null && db.isOpen()){
            Log.d(TAG,"Byron database close");
            db.close();
        }
    }
    public void deleteByID(long rowID){

    }

    public void deleteAllData(){
        //return db.delete(TABLE_NAME,null,null);
        db.execSQL(DROP_TABLE);
        return;
    }
}
