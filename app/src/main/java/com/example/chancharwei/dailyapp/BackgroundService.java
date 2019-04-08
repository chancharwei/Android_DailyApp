package com.example.chancharwei.dailyapp;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.chancharwei.dailyapp.data.ExchangeRateMonitorData;
import com.example.chancharwei.dailyapp.data.ExchangeRateMonitorRecord;
import com.example.chancharwei.dailyapp.data.ExchangeRatePerDayRecord;
import com.example.chancharwei.dailyapp.data.ExchangeRateTableData;
import com.example.chancharwei.dailyapp.utilies.ExchangeRateHTMLUtility;
import com.example.chancharwei.dailyapp.utilies.NetworkUtility;
import com.example.chancharwei.dailyapp.utilies.NotificationUtily;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class BackgroundService extends IntentService {
    private static final String TAG = BackgroundService.class.getName();
    private static final String TAIWAN_BANK_EXCHANGERATE = "https://rate.bot.com.tw/xrt?Lang=zh-TW";
    private SharedPreferences preference;
    ExchangeRateHTMLUtility exchangeRateHTMLUtility;
    ExchangeRatePerDayRecord exchangeRatePerDayRecord;
    ExchangeRateMonitorRecord exchangeRateMonitorRecord;
    public BackgroundService() {
        super("BackgroundService");
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        preference = getSharedPreferences(ExchangeRateActivity.exchangeRateSharedPreference, MODE_PRIVATE);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        if(exchangeRatePerDayRecord != null){
            exchangeRatePerDayRecord.close();
        }
        if(exchangeRateMonitorRecord != null){
            exchangeRateMonitorRecord.close();
        }

        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            exchangeRateHTMLUtility = new ExchangeRateHTMLUtility();
            exchangeRateHTMLUtility.parsingHTMLData(TAIWAN_BANK_EXCHANGERATE);
            try {
                URL exchangeRateURL = new URL(TAIWAN_BANK_EXCHANGERATE);
                if (NetworkUtility.HttpCheckStatusWithURL(exchangeRateURL)) {
                    exchangeRateHTMLUtility = new ExchangeRateHTMLUtility();
                    exchangeRateHTMLUtility.parsingHTMLData(TAIWAN_BANK_EXCHANGERATE);

                    if(exchangeRateHTMLUtility.getCurrencyTitle()!=null && exchangeRateHTMLUtility.getCurrencyInfo() != null){
                        recordExchangeRate(exchangeRateHTMLUtility.getTransformDoneDataList());
                    }
                }else {
                    Log.e(TAG, "Network Status Wrong");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Log.d(TAG,"Byron check intentService trigger -> "+intent.getExtras().getString("clients"));
            triggerAlarmManager();
        }else{
            Log.d(TAG,"get client intent is null");
        }

    }

    private void recordExchangeRate(ArrayList<Object> dataList){
        exchangeRatePerDayRecord = new ExchangeRatePerDayRecord(this,false);
        exchangeRateMonitorRecord = new ExchangeRateMonitorRecord(this,false);
        long updateRowID = -1,updateStartRowID = -1,updateEndRowID = -1;
        boolean setNewDataForNewDay;
        String dateTime = "";
        String clockTime = "";
        if(exchangeRateHTMLUtility.getDateTime() != null){
            dateTime = exchangeRateHTMLUtility.getDateTime()[0];
            clockTime = exchangeRateHTMLUtility.getDateTime()[1];
        }

        Log.d(TAG,"lastDateTime = "+preference.getString("recordDateTime","yyyy/MM/dd")+" dateTime = "+dateTime);
        if(preference.getString("recordDateTime","yyyy/MM/dd").equals(dateTime)){
            setNewDataForNewDay = false;
            if(preference.getString("recordClockTime","hh:mm").equals(clockTime)){
                Log.i(TAG,"same data with time ("+dateTime+","+clockTime+")");
                return;
            }else{
                if(exchangeRateHTMLUtility.getDateTime() != null){
                    preference.edit().putString("recordClockTime",clockTime).commit();
                }
            }
        }else{
            setNewDataForNewDay = true;
            preference.edit().putString("recordDateTime",dateTime).commit();
        }

        if(!setNewDataForNewDay){
            updateStartRowID = preference.getLong("singleDayStartRowID", (long) -1);
            updateEndRowID = preference.getLong("singleDayEndRowID", (long) -1);
            updateRowID = updateStartRowID;
        }

        int numDataEachCurrency = ExchangeRateHTMLUtility.PARSE_NUM_DATA_EACH_CURRENCY;
        for(int currency=0;currency<exchangeRateHTMLUtility.getNumOfTypeCurrency();currency++){
            ExchangeRateTableData data = new ExchangeRateTableData();
            data.setDateTime(dateTime);

            for(int j=currency*numDataEachCurrency;j<currency*numDataEachCurrency+numDataEachCurrency;j++){
                data.assignMappingData(j,j%numDataEachCurrency,dataList);
            }


            findMonitorEXRate(data,exchangeRateMonitorRecord);

            if(setNewDataForNewDay){
                long rowID = exchangeRatePerDayRecord.insert(data);
                if(currency == 0){
                    preference.edit().putLong("singleDayStartRowID",rowID).commit();
                }else if(currency == exchangeRateHTMLUtility.getNumOfTypeCurrency()-1){
                    preference.edit().putLong("singleDayEndRowID",rowID).commit();
                }
                preference.edit().putLong("perDateDataBase_"+currency,rowID).commit();
                Log.d(TAG,"get("+currency+") id = "+rowID);
            }else{
                Log.d(TAG,"updateRowID = "+updateRowID+", updateEndRowID = "+updateEndRowID);
                if(updateRowID<=updateEndRowID) {
                    ExchangeRateTableData originalData = exchangeRatePerDayRecord.queryByID(updateRowID);
                    boolean needUpdate = data.updateWithNewData(originalData);
                    if(needUpdate){
                        Log.d(TAG,"needUpdate updateRowID = "+updateRowID);
                        boolean updateDataSuccess = exchangeRatePerDayRecord.update(originalData);
                        if(!updateDataSuccess){
                            Log.w(TAG,"update this row failed, rowID("+originalData.getId()+")");
                        }
                    }else{
                        Log.d(TAG,"no need to update DataBase id("+updateRowID+") currency ("+originalData.getCurrency()+")");
                    }

                    updateRowID++;
                }
            }

        }
    }

    private void findMonitorEXRate(ExchangeRateTableData data,ExchangeRateMonitorRecord exchangeRateMonitorRecord){
        ArrayList<ExchangeRateMonitorData>[] monitorList = exchangeRateMonitorRecord.queryByCurrency(data.getCurrency());
        ArrayList<ExchangeRateMonitorData> nowCurrencyList = monitorList[0];
        ArrayList<ExchangeRateMonitorData> targetCurrencyList = monitorList[1];

        for(ExchangeRateMonitorData listData : nowCurrencyList){
            boolean needNotify = false;
            double now_exchangeRate = 0;
            if(listData.getTypeOfExchangeRate().equals(listData.CashRateName)){
                if(data.getCashRateBuyMax() > listData.getExpectedExchangeRate()){ //data.getCashRateBuyMax == data.getCashRateBuyMin
                    needNotify = true;
                    now_exchangeRate = data.getCashRateBuyMax();
                }
            }else{

                if(data.getSpotRateBuyMax() > listData.getExpectedExchangeRate()){ //data.getSpotRateBuyMax == data.getSpotRateBuyMin
                    needNotify = true;
                    now_exchangeRate = data.getSpotRateBuyMax();
                }
            }
            Log.d(TAG,"Byron check needNotify = "+needNotify+" getNotifyDone = "+listData.getNotifyDone());
            if(needNotify && listData.getNotifyDone() == 0){
                notifyUser(listData.getNowCurrency(),listData.getTypeOfExchangeRate(),"Sell",listData.getExpectedExchangeRate(),now_exchangeRate);
                listData.setNotifyDone(1);
                exchangeRateMonitorRecord.updateByID(listData.getId(),listData);
            }

        }
        for(ExchangeRateMonitorData listData : targetCurrencyList){
            boolean needNotify = false;
            double now_exchangeRate = 0;
            if(listData.getExpectedExchangeRate() == -1){
                //not monitor this item
                continue;
            }
            if(listData.getTypeOfExchangeRate().equals(listData.CashRateName)){
                if(data.getCashRateSellMax() < listData.getExpectedExchangeRate()){ //data.getCashRateSellMax == data.getCashRateSellMin
                    needNotify = true;
                    now_exchangeRate = data.getCashRateSellMax();
                }
            }else{
                if(data.getSpotRateSellMax() < listData.getExpectedExchangeRate()){ //data.getSpotRateSellMax == data.getSpotRateSellMin
                    needNotify = true;
                    now_exchangeRate = data.getSpotRateSellMax();
                }
            }
            if(needNotify && listData.getNotifyDone() == 0){
                notifyUser(listData.getTargetCurrency(),listData.getTypeOfExchangeRate(),"Buy",listData.getExpectedExchangeRate(),now_exchangeRate);
                listData.setNotifyDone(1);
                exchangeRateMonitorRecord.updateByID(listData.getId(),listData);
            }

        }
        return;
    }

    private void notifyUser(String monitorCurrency,String typeOfMonitorExXRate,String buyOrSell,double targetExRate,double nowExRate){
        NotificationUtily.remindExchangeRate(this,monitorCurrency,typeOfMonitorExXRate,buyOrSell,targetExRate,nowExRate);
    }



    public void triggerAlarmManager(){
        Intent intent = new Intent();
        intent.setClass(this,MonitorReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+AlarmManager.INTERVAL_FIFTEEN_MINUTES,pendingIntent);
    }

}
