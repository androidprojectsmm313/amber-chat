package com.app.amber.chat.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.app.amber.chat.MainActivity;
import com.app.amber.chat.R;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.net.URISyntaxException;


public class MessagesNotification extends FirebaseMessagingService {


    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.8.131:3000");
        } catch (URISyntaxException e) {}
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        System.out.println("message received = "+remoteMessage.getMessageId()+remoteMessage.getData().get("msg"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showNotificationForOreo(remoteMessage);
        }else{
            showNotificationOtherThanOreo(remoteMessage);
        }

    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        System.out.println("fiebase notifications = "+s);

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotificationForOreo(RemoteMessage remoteMessage){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "my_channel_01";
        CharSequence name = "Message";
        String description = remoteMessage.getData().get("msg");
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name,importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManager.createNotificationChannel(mChannel);
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notifyID = 1;
        String CHANNEL_ID = "my_channel_01";
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("New Message")
                .setContentText(description)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(CHANNEL_ID)
                .build();
        mNotificationManager.notify(notifyID, notification);

    }

    public void showNotificationOtherThanOreo(RemoteMessage remoteMessage){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        String description = remoteMessage.getData().get("msg");
        Notification n  = new Notification.Builder(this)
                .setContentTitle("New Message")
                .setContentText(description)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, n);

    }
}
