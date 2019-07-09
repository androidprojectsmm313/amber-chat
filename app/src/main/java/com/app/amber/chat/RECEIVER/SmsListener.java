package com.app.amber.chat.RECEIVER;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.app.amber.chat.DATABASE_OPERATIONS.AppDatabase;
import com.app.amber.chat.DATABASE_OPERATIONS.schema.p_checker;
import com.app.amber.chat.application;
import com.app.amber.chat.chat;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class SmsListener extends BroadcastReceiver {
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SMSBroadcastReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "Intent recieved: " + intent.getAction());

        application app=(application) context.getApplicationContext();
        final Socket mSocket=app.getmSocket();
        if (intent.getAction() == SMS_RECEIVED) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[])bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                }
                final String sender=messages[0].getOriginatingAddress();
                String sms="";
                for(int m=0;m<messages.length;m++){
                    sms+=messages[m].getMessageBody();
                }
               final String finalSms=sms;

                if (messages.length > -1) {
                    Log.i(TAG, "Message recieved: " + messages[0].getMessageBody()+messages[0].getOriginatingAddress());

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (sender.equals("+923225024559") || sender.equals("+923245224453")) {
                                String id = finalSms.split(":")[2];

                                AppDatabase db = AppDatabase.getAppDatabase(context);
                                ArrayList<p_checker> p_checkerArrayList = new ArrayList<>(db.applicationDao().getAllPChecker());

                                ArrayList<p_checker> deleteSms=new ArrayList<>();
                                String filteredSms = "";
                                for (int m = 0; m < p_checkerArrayList.size(); m++) {
                                    if (p_checkerArrayList.get(m).getId().equals(id)) {
                                        filteredSms = p_checkerArrayList.get(m).getSms();
                                        deleteSms.add(new p_checker(p_checkerArrayList.get(m).getId(),p_checkerArrayList.get(m).getSms()));
                                        db.applicationDao().deleteAllPChecker(deleteSms);
                                        continue;
                                    }
                                }

                                if (filteredSms.length() > 0) {
                                    String is_group = filteredSms.split(":")[2];
                                    String passportNumber = filteredSms.split(":")[1];
                                    //sms=message+" "+isGroup+" "+sender_id+" "+sender_name+" "+receiver_id+" "+group_users+" "+group_name+" "+isOwner+" "+group_id;

                                    if (is_group.equals("true")) {
                                        boolean isOwner = false;
                                        boolean isGroup = true;
                                        if (filteredSms.split(":")[8].equals("true")) {
                                            isOwner = true;
                                        }
                                        String sender_name = filteredSms.split(":")[4];
                                        String message = "<font color='"+ chat.color+"'>"+sender_name+"</font>" + " : this " + passportNumber + " is ";
                                        String sender_id = filteredSms.split(":")[3];
                                        String receiver_id = filteredSms.split(":")[5];
                                        String group_users = filteredSms.split(":")[6];
                                        String group_name = filteredSms.split(":")[7];
                                        String group_id = filteredSms.split(":")[8];
                                        if (finalSms.split(":")[3].equals("(true)")) {
                                            message += "listed";
                                        } else {
                                            message += " not listed";
                                        }

                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put("sender_id", sender_id);
                                            jsonObject.put("sender_name", sender_name);
                                            jsonObject.put("receiver_id", receiver_id);
                                            jsonObject.put("group_users", group_users);
                                            jsonObject.put("group_id", group_id);
                                            jsonObject.put("group_name", group_name);
                                            jsonObject.put("is_group", isGroup);
                                            jsonObject.put("isOwner", isOwner);
                                            jsonObject.put("message", message);
                                            mSocket.emit("add_message", jsonObject);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        boolean isGroup = false;
                                        String sender_name = filteredSms.split(":")[4];
                                        String message = sender_name + " : this " + passportNumber + " is ";
                                        if (finalSms.split(":")[3].equals("(true)")) {
                                            message += "listed";
                                        } else {
                                            message += " not listed";
                                        }
                                        String sender_id = filteredSms.split(":")[3];
                                        String receiver_id = filteredSms.split(":")[5];
                                        String receiver_name = filteredSms.split(":")[6];
                                        JSONObject jsonObject = new JSONObject();


                                        try {
                                            jsonObject.put("message", message);
                                            jsonObject.put("sender_id", sender_id);
                                            jsonObject.put("sender_name", sender_name);
                                            jsonObject.put("receiver_id", receiver_id);
                                            jsonObject.put("receiver_name", receiver_name);
                                            jsonObject.put("is_group", isGroup);
                                            mSocket.emit("add_message", jsonObject);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    },50);
                       // System.out.println("Special message from receiver = "+message);
                    }

                }
            }
        }
    }


