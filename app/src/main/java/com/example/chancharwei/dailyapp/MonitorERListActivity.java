package com.example.chancharwei.dailyapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.chancharwei.dailyapp.data.ExchangeRateMonitorData;
import com.example.chancharwei.dailyapp.data.ExchangeRateMonitorRecord;

import java.util.ArrayList;

public class MonitorERListActivity extends AppCompatActivity {
    private static final String TAG = MonitorERListActivity.class.getName();
    ExchangeRateMonitorRecord exchangeRateMonitorRecord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_erlist);
        exchangeRateMonitorRecord = new ExchangeRateMonitorRecord(this,false);
        ArrayList<ExchangeRateMonitorData> monitorDataList = exchangeRateMonitorRecord.querAllData();
        for(ExchangeRateMonitorData data : monitorDataList){
            Log.d(TAG,"Start >>>>>");
            Log.d(TAG,"getId = "+data.getId());
            Log.d(TAG,"getNowCurrency = "+data.getNowCurrency());
            Log.d(TAG,"getTargetCurrency = "+data.getTargetCurrency());
            Log.d(TAG,"getTypeOfExchangeRate = "+data.getTypeOfExchangeRate());
            Log.d(TAG,"getExpectedExchangeRate = "+data.getExpectedExchangeRate());
            Log.d(TAG,"END >>>>>>");
        }
    }

}
