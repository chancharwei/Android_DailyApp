package com.example.chancharwei.dailyapp.utilies;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.chancharwei.dailyapp.ExchangeRateActivity;
import com.example.chancharwei.dailyapp.ExchangeRateReminderService;
import com.example.chancharwei.dailyapp.R;

public class NotificationUtily {
    private static final String TAG = NotificationUtily.class.getName();
    private static final int EXCHANGERATE_ACHIEVE_REQUESTION = 5;
    private static final int EXCHANGERATE_REMINDER_NOTIFICATION_ID = 1138;
    private static final int ACTION_IGNORE_PENDING_INTENT_ID = 14;
    private static final String EXCHANGERATE_REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";

    public static void remindExchangeRate(Context context,String monitorCurrency,String typeOfMonitorExXRate,String BuyOrSell,double targetExRate,double nowExRate){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel mChannel = new NotificationChannel(
                    EXCHANGERATE_REMINDER_NOTIFICATION_CHANNEL_ID,
                    "primary",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,EXCHANGERATE_REMINDER_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setSmallIcon(R.drawable.ic_coin2)
                .setLargeIcon(largeIcon(context))
                .setContentTitle("exchangeRate achieve")
                //.setContentText(monitorCurrency+"("+typeOfMonitorExXRate+") : "+"Now ("+nowExRate+") <-> Monitor ("+targetExRate+")")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(monitorCurrency+"("+typeOfMonitorExXRate+" "+BuyOrSell+") : "+"Now ("+nowExRate+") <-> Monitor ("+targetExRate+")"))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .addAction(ignoreReminderAction(context))
                .setAutoCancel(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(EXCHANGERATE_REMINDER_NOTIFICATION_ID,notificationBuilder.build());
    }

    public static void clearAllNotifications(Context context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private static Action ignoreReminderAction(Context context){
        Intent ignoreReminderIntent = new Intent(context,ExchangeRateReminderService.class);
        ignoreReminderIntent.setAction(ReminderTask.ACTION_DISMISS_NOTIFICATION);
        PendingIntent ignoreReminderPendingIntent = PendingIntent.getService(
                context,
                ACTION_IGNORE_PENDING_INTENT_ID,
                ignoreReminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Action ignoreReminderAction = new Action(0,"OK!! I Know",ignoreReminderPendingIntent);
        return ignoreReminderAction;
    }


    private static PendingIntent contentIntent(Context context){
        Intent startActivityIntent = new Intent(context, ExchangeRateActivity.class);
        return PendingIntent.getActivity(
                context,
                EXCHANGERATE_ACHIEVE_REQUESTION,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_coin2);
        return largeIcon;
    }
}
