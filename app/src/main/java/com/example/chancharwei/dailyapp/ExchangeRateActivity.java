package com.example.chancharwei.dailyapp;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.chancharwei.dailyapp.utilies.ExchangeRateHTMLUtility;
import com.example.chancharwei.dailyapp.utilies.NetworkUtility;
import com.example.chancharwei.dailyapp.utilies.WeatherJsonUtility;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ExchangeRateActivity extends AppCompatActivity {
    final static String TAG = ExchangeRateActivity.class.getName();
    private static final String TAIWAN_BANK_EXCHANGERATE = "https://rate.bot.com.tw/xrt?Lang=zh-TW";

    ExchangeRateHTMLUtility exchangeRateHTMLUtility;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //scheduleMonitorExchangeRateJob();
        startThread();
    }
    //TODO Test
    public void startThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL exchangeRateURL = new URL(TAIWAN_BANK_EXCHANGERATE);
                    if (NetworkUtility.HttpCheckStatusWithURL(exchangeRateURL)) {
                        exchangeRateHTMLUtility = new ExchangeRateHTMLUtility();
                        exchangeRateHTMLUtility.parsingHTMLData(TAIWAN_BANK_EXCHANGERATE);

                        if(exchangeRateHTMLUtility.getCurrencyTitle()!=null && exchangeRateHTMLUtility.getCurrencyInfo() != null){
                                //save to database
                            showAndRecord(exchangeRateHTMLUtility.getCurrencyTitle(),exchangeRateHTMLUtility.getCurrencyInfo());
                        }
                    }else {
                        Log.e(TAG, "Network Status Wrong");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showAndRecord(String[] currencyTitle,String[] currencyInfo){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.exchangerate,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        Intent featureIntent = new Intent();
        switch (itemThatWasClickedId) {
            case android.R.id.home:
                featureIntent.setClass(this, MainActivity.class);
                startActivity(featureIntent);
                finish();
                break;
            case R.id.weather:
                featureIntent.setClass(this,WeatherActivity.class);
                startActivity(featureIntent);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return true;

    }
}
