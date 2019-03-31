package com.example.chancharwei.dailyapp.utilies;

import android.content.Context;
import android.util.Log;

public class ReminderTask {
    private static final String TAG = ReminderTask.class.getName();
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss-notification";
    public static void executeTask(Context context, String action){
        Log.d(TAG ,"Byron check executeTask action = "+action);
        if(ACTION_DISMISS_NOTIFICATION.equals(action)){
            NotificationUtily.clearAllNotifications(context);
        }
    }
}
