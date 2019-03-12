package com.example.chancharwei.dailyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class MonitorReceiver extends BroadcastReceiver {
    private static final String TAG = MonitorReceiver.class.getName();
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d(TAG,"Byron check trigger alarm "+context.getClass()+" intent = "+intent.getClass());

        //triggerAlarmManager(context);
        startBackgroundService(context);
    }
    private void startBackgroundService(Context context){
        Intent startServiceIntent = new Intent(context,BackgroundService.class);
        startServiceIntent.putExtra("clients","MonitorReceiver");
        context.startService(startServiceIntent);
    }


}
