package com.example.chancharwei.dailyapp;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.chancharwei.dailyapp.utilies.ReminderTask;

public class ExchangeRateReminderService extends IntentService{
    private static final String TAG = ExchangeRateReminderService.class.getName();

    public ExchangeRateReminderService(){
        super("DisplayNotification");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        ReminderTask.executeTask(this,action);
    }
}
