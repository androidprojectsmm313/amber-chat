package com.app.amber.chat.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.app.amber.chat.DATABASE_OPERATIONS.AppDatabase;
import com.app.amber.chat.DATABASE_OPERATIONS.schema.p_checker;
import com.app.amber.chat.DATABASE_OPERATIONS.schema.user;
import com.app.amber.chat.JobSchduler.service_watcher;
import com.app.amber.chat.R;
import com.app.amber.chat.application;
import com.app.amber.chat.chat;
import com.app.amber.chat.video_call;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Cipher;


public class socket_events_listener extends Service {
    private Vibrator vib;

    MediaPlayer mediaPlayer;

    public static MediaPlayer videoCallMediaPlayer;
    private Socket mSocket;

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    Handler handler;Runnable runnable;

    public static int endCallNotificationId=0;

    Context context;
    public static boolean isBusy=false;
    @Override
    public void onCreate() {
        super.onCreate();
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Service");

        System.out.println("service restart");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Chat is running")
                .setContentText("Chat is running")
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();





        startForeground(1, notification);

    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);

        System.out.println("service restart");




        application app=(application) getApplicationContext();
         mSocket=app.getmSocket();




        try {
             if (mSocket != null) {
                 if (!mSocket.connected()) {
                     //mSocket.disconnect();
                     //mSocket.connect();
                 }
                 new Timer().schedule(new TimerTask() {
                     @Override
                     public void run() {
                         // this code will be executed after 2 seconds
                         AppDatabase db = AppDatabase.getAppDatabase(getApplicationContext());
                         ArrayList<user> usersList = new ArrayList<>(db.applicationDao().getAllUsers());


                         if (usersList.size() > 0) {
                             //mSocket.off();
                             mSocket.off("ready_for_call" + usersList.get(0).user_name);
                             mSocket.off("message_recieved_notification" + usersList.get(0).threadId);
                             mSocket.off("p_checker" + usersList.get(0).threadId);
                             mSocket.off("end_call" + usersList.get(0).user_name);
                             mSocket.off("update_status");
                             mSocket.off("remove_group_service"+usersList.get(0).threadId);

                             mSocket.on("ready_for_call" + usersList.get(0).user_name, handleReadyForCall);
                             mSocket.on("message_recieved_notification" + usersList.get(0).threadId, handleReceiveMessage);
                             mSocket.on("p_checker" + usersList.get(0).threadId,pCheckerService);
                             mSocket.on("update_status",updateStatus);
                             mSocket.on("end_call" + usersList.get(0).user_name, cancelNotification);
                             mSocket.on("remove_group_service"+usersList.get(0).threadId, removeGroup);

                             try{
                                 JSONObject jsonObject=new JSONObject();
                                 jsonObject.put("user_id",usersList.get(0).threadId);
                                 jsonObject.put("username",usersList.get(0).user_name);
                                 mSocket.emit("update_status",jsonObject);

                             }catch(Exception e){

                             }

                         }


                     }
                 }, 500);


                // tryToReconnect();
             }
         }catch(Exception e){
             e.printStackTrace();
         }
        return START_STICKY;
    }



    private Emitter.Listener removeGroup = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                System.out.println("remove group response from service = "+data);
                final String idToRemove=data.getString("id");

                application app=(application)getApplicationContext();
                ArrayList<com.app.amber.chat.pojo.user> userArrayList=new ArrayList<>();
                for(int p=0;p<userArrayList.size();p++) {
                    if(userArrayList.get(p).getId().equals(idToRemove)){
                        app.setUserArrayList(userArrayList);
                    }}
            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };




    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "my_service_channelid";
        String channelName = "Chat is running";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    private Emitter.Listener updateStatus = new Emitter.Listener(){
        @Override
        public void call(final Object... args){


            try {
                final JSONObject data = (JSONObject) args[0];
                // final String message = data.getString("message");
                System.out.println("update status from service = "+data);
                byte[] encryptedText= Base64.decode(data.getString("encryptedText"), Base64.DEFAULT);

                InputStream inputStream = new ByteArrayInputStream(data.getString("publicKey").getBytes(Charset.forName("UTF-8")));


                System.out.println("decodedBase64Text = "+encryptedText);
                System.out.println("decodedKey = "+data.getString("publicKey"));
                System.out.println("decrypt Text = "+decryptRSA(getApplicationContext(),encryptedText,inputStream));
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        AppDatabase db = AppDatabase.getAppDatabase(getApplicationContext());
                        ArrayList<user> usersList = new ArrayList<>(db.applicationDao().getAllUsers());
                        if(usersList.size()>0){
                            JSONObject jsonObject=new JSONObject();
                            try {
                                jsonObject.put("user_id", usersList.get(0).threadId);
                                jsonObject.put("username",usersList.get(0).user_name);
                                mSocket.emit("update_status",jsonObject);
                            }catch(Exception e){

                            }

                        }
                    }
                },50);

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };


    private String decodeBase64(String coded){
        byte[] valueDecoded= new byte[0];
        try {
            valueDecoded = Base64.decode(coded.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new String(valueDecoded);
    }


    public  byte[] decryptRSA(Context mContext, byte[] message,InputStream is) throws Exception {

        // reads the public key stored in a file
        //InputStream is = mContext.getResources().openRawResource(R.raw.sm_public);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = br.readLine()) != null)
            lines.add(line);

        // removes the first and last lines of the file (comments)
        if (lines.size() > 1 && lines.get(0).startsWith("-----") && lines.get(lines.size()-1).startsWith("-----")) {
            lines.remove(0);
            lines.remove(lines.size()-1);
        }

        // concats the remaining lines to a single String
        StringBuilder sb = new StringBuilder();
        for (String aLine: lines)
            sb.append(aLine);
        String keyString = sb.toString();
        Log.d("log", "keyString:"+keyString);

        // converts the String to a PublicKey instance
        byte[] keyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(keyString.getBytes("utf-8"));
        Log.d("log", "keyString:"+keyString);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        Log.d("log", "keyString:"+keyString);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Log.d("log", "keyString:"+keyString);
        PublicKey key = keyFactory.generatePublic(spec);
        Log.d("log", "keyString:"+keyString);
        // decrypts the message
        byte[] dectyptedText = null;
        Cipher cipher = Cipher.getInstance("RSA");
        Log.d("log", "keyString:"+keyString);
        cipher.init(Cipher.DECRYPT_MODE, key);
        Log.d("log", "keyString:"+keyString);
        dectyptedText = cipher.doFinal(message);
        Log.d("log", "keyString:"+keyString);
        String str = new String(dectyptedText); // for UTF-8 encoding
        for(int i=0;i<str.length();i++){
            System.out.println((int)(str.charAt(i))+" "+str.charAt(i));
        }
        String decryptedString=str.replace(new String(Character.toChars(65533)), "").
                replace(new String(Character.toChars(0)),"");
        System.out.println("string = "+decryptedString);
        final byte[] dataToEncrypt = decryptedString.getBytes();


        final Cipher decCipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
        decCipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = decCipher.doFinal(dataToEncrypt);
        try {
            final String encryptedText = new String(Base64.encode(encryptedData, Base64.DEFAULT));
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("text",encryptedData);
            mSocket.emit("decryptMessage",jsonObject);
            System.out.println("base64encrypted data"+encryptedText.toString());
        }
        catch (Exception e) { e.printStackTrace(); }
        return dectyptedText;
    }





    private Emitter.Listener cancelNotification = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            System.out.println("end call from serice"+endCallNotificationId);
            cancelNotification(getApplicationContext(),endCallNotificationId);
        }
    };


    private Emitter.Listener handleReadyForCall = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            mWakeLock.acquire();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    mWakeLock.release();
                }
            },1000);

            try {
                // final String message = data.getString("message");
                System.out.println("readyforcall = "+data);
                if(!socket_events_listener.isBusy){
                    socket_events_listener.isBusy=true;
                    Intent intent=new Intent(getApplicationContext(),video_call.class).putExtra("ownId",data.getString("id"))
                            .putExtra("is_audio",data.getBoolean("is_audio"))
                            .putExtra("otherId",data.getString("ownId")).putExtra("isCaller",false)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    endCallNotificationId=data.getInt("notify_id");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showNotificationForOreoWithoutIntent(data.getString("ownId")+" is calling ..", intent,endCallNotificationId,true);
                    } else {
                        showNotificationOtherThanOreoWithoutIntent(data.getString("ownId")+" is calling ..", intent,endCallNotificationId,true);
                    }
                    videoCallMediaPlayer=MediaPlayer.create(getApplicationContext(),R.raw.callring);
                    vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    try {
                        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, 15, 0);
                    }catch (SecurityException e){
                        e.printStackTrace();
                    }
                    videoCallMediaPlayer.start();
                    videoCallMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            videoCallMediaPlayer.seekTo(0);
                            videoCallMediaPlayer.start();

                        }
                    });
                    mWakeLock.acquire();

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mWakeLock.release();
                        }
                    },1000);
                    startActivity(intent);


                }else{
                    System.out.println("busy");
                }

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };


    private Emitter.Listener pCheckerService = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            mWakeLock.acquire();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    mWakeLock.release();
                }
            },1000);

            try {
                // final String message = data.getString("message");
                System.out.println("send_sms"+data);

                String sms="",message="",receiver_id,receiver_name,group_users,group_id,group_name,is_group,isOwner,sender_id,sender_name,type;
                boolean isGroup=false;
                if(data.has("is_group") && data.getBoolean("is_group")){
                    isGroup=data.getBoolean("is_group");

                }
                if(isGroup){
                    sms="";
                    message=data.getString("message").split(":")[1].trim()+":"+data.getString("message").split(":")[2].trim();
                    //sender_id="eb8d807d-1a6b-436a-8b88-f33933e3efd6"; //internet
                    sender_id="63ce0680-d26f-45a3-bf15-c016af3472cb";//withoutinternet

                    sender_name="p_checker";
                    receiver_id=data.getString("receiver_id");
                    group_users=data.getString("group_users");
                    group_name=data.getString("group_name");
                    isOwner=data.getString("isOwner");
                    group_id=data.getString("group_id");
                    sms=message+":"+isGroup+":"+sender_id+":"+sender_name+":"+receiver_id+":"+group_users+":"+group_name+":"+isOwner+":"+group_id;
                }else{
                    message=data.getString("message").split(":")[1].trim()+":"+data.getString("message").split(":")[2].trim();
                    sender_id=data.getString("receiver_id");
                    sender_name=data.getString("receiver_name");
                    receiver_id=data.getString("sender_id");
                    receiver_name=data.getString("sender_name");
                    sms=message+":"+isGroup+":"+sender_id+":"+sender_name+":"+receiver_id+":"+receiver_name;
                }
                String uniqueID = UUID.randomUUID().toString();
                String filteredSms=message+":"+uniqueID;
                insertSMS(sms,uniqueID);
                System.out.println("sms = "+sms);
                sendSMS("03245224453",filteredSms);
            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };



    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }


    public void insertSMS(final String sms,final String uniqueID){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // this code will be executed after 2 seconds
                AppDatabase db=AppDatabase.getAppDatabase(getApplicationContext());
                db.applicationDao().insertPChecker(new p_checker(uniqueID,sms));
            }
        }, 50);

    }

    private Emitter.Listener handleReceiveMessage = new Emitter.Listener(){
        @Override
        public void call(final Object... args){


            mediaPlayer=MediaPlayer.create(getApplicationContext(),R.raw.notificationring);
            vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vib.vibrate(2000);
            try {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
            }catch (SecurityException e){
                e.printStackTrace();
            }
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                }
            });
            mWakeLock.acquire();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    mWakeLock.release();
                }
            },1000);

            JSONObject data = (JSONObject) args[0];
            try {
                System.out.println("notification received = "+data.toString());
                if(!data.has("message")){
                    data=data.getJSONObject("data");
                }


                final JSONObject finalData = data;
                final JSONObject finalData1 = data;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // this code will be executed after 2 seconds
                        AppDatabase db=AppDatabase.getAppDatabase(getApplicationContext());
                        String uniqueID = UUID.randomUUID().toString();

                        final ArrayList<user> usersList=new ArrayList<>(db.applicationDao().getAllUsers());
                        if(usersList.size()>0) {
                            final String id = usersList.get(0).threadId;
                            final String username = usersList.get(0).user_name;
                            try {
                                Intent intent;
                                if (finalData.getBoolean("isGroup")) {
                                    String group_id = finalData.getString("group_id");
                                    String group_name = finalData.getString("group_name");
                                    boolean isGroup = finalData.getBoolean("isGroup");
                                    boolean isOwner = finalData.getBoolean("isOwner");
                                    String group_users = finalData.getString("group_users");

                                    intent = new Intent(getApplicationContext(), chat.class).putExtra("sender_id", id).putExtra("sender_name", username)
                                            .putExtra("group_id", group_id).putExtra("group_name", group_name)
                                            .putExtra("isGroup", isGroup).putExtra("group_users", group_users).putExtra("isOwner", isOwner)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                } else {
                                    String receiver_id = finalData.getString("sender_id");
                                    String receiver_name = finalData.getString("sender_name");
                                    boolean isGroup = finalData.getBoolean("isGroup");

                                    intent = new Intent(getApplicationContext(), chat.class).putExtra("sender_id", id).putExtra("sender_name", username)
                                            .putExtra("receiver_id", receiver_id).putExtra("receiver_name", receiver_name)
                                            .putExtra("isGroup", isGroup).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                }
                                 String message = finalData1.getString("message");

                                if(finalData1.has("notify_msg")){
                                    message=finalData1.getString("notify_msg");
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    showNotificationForOreo(message, intent,getID(),false);
                                } else {
                                    showNotificationOtherThanOreo(message, intent,getID(),false);
                                }
                            }catch (Exception e){
                                System.out.println("json exception = "+e.toString());

                            }
                        }
                    }}, 50);
            } catch (JSONException e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();

        System.out.println("ondestroy called");
        if(service_watcher.count==0){
            service_watcher.count++;
        }

        stopForeground(true);
        stopSelf();
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotificationForOreo(String remoteMessage,Intent intent,int notifyId,boolean isVideoCall){

        PendingIntent pIntent = PendingIntent.getActivity(this, getID(), intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "my_channel_01"+getID();
        CharSequence name = "Message";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name,importance);
        mChannel.setDescription(remoteMessage);
        mChannel.enableLights(true);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManager.createNotificationChannel(mChannel);
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("New Message")
                .setContentText(remoteMessage)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(id)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .build();
        mNotificationManager.notify(notifyId, notification);

    }

    public void showNotificationOtherThanOreo(String remoteMessage,Intent intent,int notifyId,boolean isVideoCall){
        PendingIntent pIntent = PendingIntent.getActivity(this, getID(), intent, 0);
        Notification n  = new Notification.Builder(this)
                .setContentTitle("New Message")
                .setContentText(remoteMessage)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notifyId, n);
        if(isVideoCall){
            endCallNotificationId=notifyId;
        }
    }




    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotificationForOreoWithoutIntent(String remoteMessage,Intent intent,int notifyId,boolean isVideoCall){

        PendingIntent pIntent = PendingIntent.getActivity(this, getID(), intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "my_channel_01"+getID();
        CharSequence name = "Message";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name,importance);
        mChannel.setDescription(remoteMessage);
        mChannel.enableLights(true);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManager.createNotificationChannel(mChannel);
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("New Message")
                .setContentText(remoteMessage)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(id)
                .setAutoCancel(true)
                .build();
        mNotificationManager.notify(notifyId, notification);

        if(isVideoCall){
            endCallNotificationId=notifyId;
        }




    }

    public void showNotificationOtherThanOreoWithoutIntent(String remoteMessage,Intent intent,int notifyId,boolean isVideoCall){
        PendingIntent pIntent = PendingIntent.getActivity(this, getID(), intent, 0);
        Notification n  = new Notification.Builder(this)
                .setContentTitle("New Message")
                .setContentText(remoteMessage)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notifyId, n);
        if(isVideoCall){
            endCallNotificationId=notifyId;
        }
    }


    private void acquireWakeLock() {
        try {
            mWakeLock.acquire();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseWakeLock() {
        try {
            mWakeLock.release();
        }
        catch (Exception e) {

        }
    }

    public static  void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }
    private final static AtomicInteger c = new AtomicInteger(0);
    public static int getID() {
        return c.incrementAndGet();
    }
    public static int createID(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        return id;
    }


    public static void StopRinging(){
        if(videoCallMediaPlayer!=null && videoCallMediaPlayer.isPlaying()){
            videoCallMediaPlayer.stop();
        }
    }



    public void tryToReconnect(){
        if(handler!=null){
            handler.removeCallbacks(runnable);
        }

        handler=new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                if(!mSocket.connected()){
                    //mSocket.disconnect();
                    //mSocket.connect();

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // this code will be executed after 2 seconds
                            AppDatabase db = AppDatabase.getAppDatabase(getApplicationContext());
                            ArrayList<user> usersList = new ArrayList<>(db.applicationDao().getAllUsers());


                            if (usersList.size() > 0) {
                                //mSocket.off();
                                mSocket.off("ready_for_call" + usersList.get(0).user_name);
                                mSocket.off("message_recieved_notification" + usersList.get(0).threadId);
                                mSocket.off("p_checker" + usersList.get(0).threadId);
                                mSocket.off("end_call" + usersList.get(0).user_name);
                                mSocket.off("update_status");

                                mSocket.on("ready_for_call" + usersList.get(0).user_name, handleReadyForCall);
                                mSocket.on("message_recieved_notification" + usersList.get(0).threadId, handleReceiveMessage);
                                mSocket.on("p_checker" + usersList.get(0).threadId,pCheckerService);
                                mSocket.on("update_status",updateStatus);
                                mSocket.on("end_call" + usersList.get(0).user_name, cancelNotification);

                                try{
                                    JSONObject jsonObject=new JSONObject();
                                    jsonObject.put("user_id",usersList.get(0).threadId);
                                    jsonObject.put("username",usersList.get(0).user_name);
                                    mSocket.emit("update_status",jsonObject);

                                }catch(Exception e){

                                }

                            }


                        }
                    }, 500);

                    System.out.println("socket is not connected");
                }else{
                    System.out.println("socket is connected");
                }
                handler.postDelayed(this,1000);
            }
        };
        handler.postDelayed(runnable,2000);
    }
}