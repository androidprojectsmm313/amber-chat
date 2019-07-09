package com.app.amber.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.amber.chat.UTILITY.StoreDataToRemoteServer;
import com.app.amber.chat.pojo.message;
import com.github.clans.fab.FloatingActionButton;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.api.GoogleApiClient;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class chat extends AppCompatActivity  implements LocationListener {
    ListView list;
    ArrayList<message> chatMessages;
    FloatingActionButton share_location,share_file;
    EditText message_edt;ImageButton send,record,capture_pic;
    private Socket mSocket;
    final String TAG = "GPS";
    MediaRecorder recorder;
    boolean isOwner=false;
    public static String color="";
    LocationManager locationManager;
    ProgressBar progressBar;


    boolean isRecordingStarted=false;
    String voiceMesasgePath="";
    /*{
        try {
            mSocket = IO.socket("http://3.210.76.112:9000");
        } catch (URISyntaxException e) {}
    }*/
     boolean isGroup;
    android.app.AlertDialog dialog;
    Handler locationHandler;
    Runnable locationRunnable;
    Location lastLocation;
    GoogleApiClient gac;
    Date d1,d2;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000;
    String sender_id;
    String group_id;
    Bundle extras;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        System.out.println("user name from chat = "+getIntent().getExtras().getString("sender_name"));
        chatMessages=new ArrayList<>();
        //share_location=(FloatingActionButton) findViewById(R.id.share_location);
        //share_file=(FloatingActionButton) findViewById(R.id.share_file);
        list = findViewById(R.id.chat_item);

        extras=getIntent().getExtras();


        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(chat.this);
        builder.setTitle("Upload file confirmation?");
// Add the buttons
        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                new uploadfile(voiceMesasgePath).execute();

                dialog.dismiss();
                System.out.println("upload file");
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                progressBar.setVisibility(View.INVISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                dialog.dismiss();
                System.out.println("dont");
            }
        });




        message_edt=(EditText) findViewById(R.id.message);
        send=(ImageButton) findViewById(R.id.send);
        record=(ImageButton) findViewById(R.id.record);
        capture_pic=(ImageButton) findViewById(R.id.capture_pic);

        progressBar=(ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        application app=(application) getApplicationContext();
        mSocket=app.getmSocket();


        final int Capture_Camera_PERMISSION_ALL = 10;
        final String[] Capture_Camera_PERMISSIONS = {
                Manifest.permission.CAMERA

        };



        if(app.getUsername()==null || getIntent().getExtras().getString("sender_name")==null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }



        if(app.getUsername()==null){
            app.setUsername(getIntent().getExtras().getString("sender_name"));
        }

        if(app.getUser_id()==null){
            app.setUser_id(getIntent().getExtras().getString("sender_id"));
        }
        final int LOCATION_PERMISSION_ALL = 2;
        final String[] LOCATION_PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION

        };

        /*if(!hasPermissions(this, LOCATION_PERMISSIONS)){
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, LOCATION_PERMISSION_ALL);
        }
        else{
            locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
            try {
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, false);
                Location location = locationManager.getLastKnownLocation(provider);
                Log.d(TAG, provider);
                Log.d(TAG, location == null ? "NO LastLocation" : location.toString());

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }*/


        isGroup=getIntent().getExtras().getBoolean("isGroup");
        isOwner=getIntent().getExtras().getBoolean("isOwner");
        sender_id=getIntent().getExtras().getString("sender_id");




        if(isGroup){
            String group_name=getIntent().getExtras().getString("group_name");
            chat.this.setTitle(group_name);
        }else{
            String receiver_name=getIntent().getExtras().getString("receiver_name");
            chat.this.setTitle(receiver_name);
        }
        if(mSocket!=null)
        {
            if(!mSocket.connected())
            {
                mSocket.off("delete_message", handelDeleteMessage);
                mSocket.off("update_message_status"+app.getUser_id(), updateCounterToZero);
                if(!isGroup){
                    mSocket.off("message_recieved"+sender_id, handleReceiveMessage);
                    mSocket.off("get_messages_response", handelGetAllMessages);

                }else{
                    group_id=getIntent().getExtras().getString("group_id");
                    mSocket.off("message_recieved"+group_id, handleReceiveMessage);
                    mSocket.off("get_messages_response", handelGetAllMessages);
                    mSocket.off("get_assigned_color", getAssignedColor);
                    mSocket.off("remove_group_chat"+sender_id);
                }
                //mSocket.disconnect();
                //mSocket.connect();
            }




            mSocket.on("delete_message", handelDeleteMessage);
            mSocket.on("update_message_status"+app.getUser_id(), updateCounterToZero);

            if(!isGroup){
                mSocket.on("message_recieved"+sender_id, handleReceiveMessage);
                mSocket.on("get_messages_response", handelGetAllMessages);

            }else{
                group_id=getIntent().getExtras().getString("group_id");
                mSocket.on("message_recieved"+group_id, handleReceiveMessage);
                mSocket.on("get_messages_response", handelGetAllMessages);
                mSocket.on("get_assigned_color", getAssignedColor);
                mSocket.on("remove_group_chat"+sender_id, removeGroup);
            }


            JSONObject  jsonObject=new JSONObject();
            JSONObject  assignedColor=new JSONObject();

            try {
                if(!isGroup){
                    String receiver_id=getIntent().getExtras().getString("receiver_id");
                    jsonObject.put("sender_id",sender_id);
                    jsonObject.put("receiver_id",receiver_id);
                    jsonObject.put("is_group",isGroup);
                }else{
                    String receiver_id=getIntent().getExtras().getString("group_id");
                    assignedColor.put("group_id",receiver_id);
                    assignedColor.put("user_id",sender_id);
                    jsonObject.put("sender_id",sender_id);
                    jsonObject.put("receiver_id",receiver_id);
                    jsonObject.put("is_group",isGroup);

                    if(!mSocket.connected()){
                        //mSocket.connect();
                    }
                    mSocket.emit("get_assigned_color",assignedColor);
                }

                if(!mSocket.connected()){
                    //mSocket.connect();
                }
                mSocket.emit("get_messages",jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        capture_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!chat.hasPermissions(chat.this, Capture_Camera_PERMISSIONS)){
                    ActivityCompat.requestPermissions(chat.this, Capture_Camera_PERMISSIONS, Capture_Camera_PERMISSION_ALL);
                }else{


                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 21);

                }
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(message_edt.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please enter message",Toast.LENGTH_LONG).show();
                }else{
                    System.out.println("sending message = "+message_edt.getText().toString());
                    JSONObject jsonObject=new JSONObject();
                    try {
                        if(isGroup){
                            if(color.length()>0){
                                jsonObject.put("notify_msg",getIntent().getExtras().getString("sender_name")+" : "+message_edt.getText().toString());

                                jsonObject.put("message",
                                        "<font color='"+color+"'>"+getIntent().getExtras().getString("sender_name")+"</font>"+" : "+message_edt.getText().toString());

                            }else{
                                jsonObject.put("message",getIntent().getExtras().getString("sender_name")+" : "+message_edt.getText().toString());

                            }
                            application app=(application) getApplicationContext();
                            jsonObject.put("sender_id",getIntent().getExtras().getString("sender_id"));
                            jsonObject.put("sender_name",getIntent().getExtras().getString("sender_name"));
                            jsonObject.put("receiver_id",getIntent().getExtras().getString("group_id"));
                            jsonObject.put("group_users",getIntent().getExtras().getString("group_users"));
                            jsonObject.put("group_id",getIntent().getExtras().getString("group_id"));
                            jsonObject.put("group_name",getIntent().getExtras().getString("group_name"));
                            jsonObject.put("is_group",isGroup);
                            jsonObject.put("isOwner",isOwner);

                        }else{
                            jsonObject.put("message",getIntent().getExtras().getString("sender_name")+" : "+message_edt.getText().toString());
                            jsonObject.put("sender_id",getIntent().getExtras().getString("sender_id"));
                            jsonObject.put("sender_name",getIntent().getExtras().getString("sender_name"));
                            jsonObject.put("receiver_id",getIntent().getExtras().getString("receiver_id"));
                            jsonObject.put("receiver_name",getIntent().getExtras().getString("receiver_name"));
                            jsonObject.put("is_group",isGroup);
                        }

                        if(!mSocket.connected()){
                            //mSocket.connect();
                        }

                        String msg=message_edt.getText().toString();
                        if(msg.trim().split(":").length==2 && (msg.trim().split(":")[0].equals("#amberw")
                                ||msg.trim().split(":")[0].equals("#amberlp")
                        ||msg.trim().split(":")[0].equals("#amberp"))){
                            jsonObject.put("type","p_checker");
                            mSocket.emit("p_checker",jsonObject);
                            System.out.println("special messages");
                        }else{
                            mSocket.emit("add_message",jsonObject);
                        }
                        message_edt.setText("");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });




        record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        progressBar.setVisibility(View.VISIBLE);
                        System.out.println("start recording");
                        voiceMesasgePath = getFilename("mp3");
                        startRecording(voiceMesasgePath);
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED

                        if (getDateDiff(d1, new Date(), TimeUnit.SECONDS) > 0) {
                            stopRecording();

                            Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!isRecordingStarted) {
                                                dialog = builder.create();
                                                dialog.show();
                                            }
                                        }
                                    });
                                }
                            };
                            handler.postDelayed(runnable, 500);
                            System.out.println("stop recoridng");
                            return true; // if you want to handle the touch event
                        }else {

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            stopRecording();

                                                            Handler handler = new Handler();
                                                            Runnable runnable = new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            if (!isRecordingStarted) {
                                                                                //new uploadfile(voiceMesasgePath).execute();
                                                                                dialog = builder.create();
                                                                                dialog.show();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            };
                                                            handler.postDelayed(runnable, 500);                                                        }


                                    });

                                }
                            },1000);
                            //Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT).show();
                        }
                }
                return false;
            }
        });

        /*chat_adapter customAdapter = new chat_adapter(this, arrayList);
        list.setAdapter(customAdapter);*/



        /*share_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkLocaltionEnable(chat.this);
                if(lastLocation==null){


                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, chat.this);

                    if (locationManager != null) {
                        lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }else {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, chat.this);

                        if (locationManager != null) {
                            lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }

                    }
                }

                if(locationHandler!=null && locationRunnable!=null)
                locationHandler.removeCallbacks(locationRunnable);

                getLastLoation();


              //  System.out.println("send location to user = "+lastLocation);
            }
        });

        share_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileManager();
            }
        });*/


    }


    private Emitter.Listener removeGroup = new Emitter.Listener(){
        @Override
        public void call(final Object... args) {
            final JSONObject data = (JSONObject) args[0];
            try {
                System.out.println("remove group response from chat = " + data+"   "+group_id);
                final String idToRemove = data.getString("id");
                if (isGroup && idToRemove.equals(group_id)) {
                    finish();
                }
            }
            catch (Exception e){
            e.printStackTrace();
            }
        };
    };

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }


    private Emitter.Listener handelGetAllMessages = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            final application app=(application)getApplicationContext();
            try {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(data.getBoolean("success")){
                                JSONObject updateMessagesCount=new JSONObject();
                                if(!isGroup){
                                    String receiver_id=getIntent().getExtras().getString("receiver_id");
                                    updateMessagesCount.put("update_user_id",app.getUser_id());
                                    updateMessagesCount.put("other_user_id",receiver_id);
                                    mSocket.emit("update_message_count_zero",updateMessagesCount);
                                }
                                JSONArray jsonArray=data.getJSONArray("messages");
                                String sender_id=getIntent().getExtras().getString("sender_id");

                                for(int i=0;i<jsonArray.length();i++){
                                    JSONObject jsonObject=jsonArray.getJSONObject(i);

                                    if((jsonObject.has("type") && jsonObject.isNull("type")
                                            || jsonObject.getString("type").equals("p_checker")     || jsonObject.getString("type").equals("message") || jsonObject.getString("type").equals("safecity") || jsonObject.getString("type").equals("location")) && (jsonObject.has("msg") &&
                                            !jsonObject.isNull("msg") && !jsonObject.getString("msg").isEmpty())){
                                        boolean is_sender=false;
                                        if(jsonObject.getString("sender_id").toString().equals(sender_id.toString())){
                                            is_sender=true;
                                        }else{
                                            is_sender=false;
                                        }
                                        if(jsonObject.isNull("type") || jsonObject.getString("type").equals("message") || jsonObject.getString("type").equals("p_checker")){
                                            chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),null,null,is_sender,jsonObject.getString("date_created"),
                                                    jsonObject.getBoolean("is_read")));
                                        }else if(!jsonObject.isNull("type")){
                                                chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),jsonObject.getString("type"),null,is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                        }
                                    }else{
                                        if(jsonObject.has("type") && !jsonObject.isNull("type") &&
                                                jsonObject.getString("type").equals("file")){
                                            boolean is_sender=false;
                                            if(jsonObject.getString("sender_id").toString().equals(sender_id.toString())){
                                                is_sender=true;
                                            }else{
                                                is_sender=false;
                                            }
                                            if(jsonObject.has("path") && !jsonObject.isNull("path")){
                                                System.out.println("path = "+jsonObject.getString("path"));
                                                if(jsonObject.getString("path").endsWith(".jpg")
                                                        || jsonObject.getString("path").endsWith(".png")
                                                        || jsonObject.getString("path").endsWith(".jpeg")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"image",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type image");
                                                }else if(jsonObject.getString("path").endsWith(".doc")
                                                        || jsonObject.getString("path").endsWith(".docx")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"document",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type document");
                                                }else if(jsonObject.getString("path").endsWith(".ppt")
                                                        || jsonObject.getString("path").endsWith(".pptx")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"powerpoint",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type poweerpoint");
                                                }else if(jsonObject.getString("path").endsWith(".xls")
                                                        || jsonObject.getString("path").endsWith(".xlsx")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"xls",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type xls");
                                                }else if(jsonObject.getString("path").endsWith(".txt")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"text",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type text");
                                                }else if(jsonObject.getString("path").endsWith(".pdf")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"pdf",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type pdf");
                                                }else if(jsonObject.getString("path").endsWith(".csv")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"csv",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type csv");
                                                }else if(jsonObject.getString("path").endsWith(".aac")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"aac",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type aac");
                                                }else if(jsonObject.getString("path").endsWith(".amr")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"amr",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type aac");
                                                }else if(jsonObject.getString("path").endsWith(".m4a")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"m4a",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type aac");
                                                }else if(jsonObject.getString("path").endsWith(".opus")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"opus",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type aac");
                                                }else if(jsonObject.getString("path").endsWith(".wav")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"wav",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type aac");
                                                }else if(jsonObject.getString("path").endsWith(".mp3")){
                                                    chatMessages.add(new message(jsonObject.getString("id"),jsonObject.getString("msg"),"mp3",
                                                            "http://3.210.76.112:9000/"+jsonObject.getString("path"),is_sender,jsonObject.getString("date_created"),jsonObject.getBoolean("is_read")));
                                                    System.out.println("message is of some othe type mp3");
                                                }
                                            }
                                        }
                                    }
                                }
                                if(chatMessages.size()>0){
                                    chat_adapter customAdapter = new chat_adapter(chat.this, chatMessages,chat.this,mSocket);
                                    list.setAdapter(customAdapter);
                                }

                            }else{

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };




    private Emitter.Listener handelDeleteMessage = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (data.has("position") && data.getInt("position")>=0) {
                                chatMessages.remove(data.getInt("position"));
                                chat_adapter customAdapter = new chat_adapter(chat.this, chatMessages,chat.this,mSocket);
                                list.setAdapter(customAdapter);
                            }
                        }catch (Exception e) {
                            System.out.println("json exception = "+e.toString());
                        }
                }});

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };


    public void addNewMessage(message message){
        chatMessages.add(message);
        chat_adapter customAdapter = new chat_adapter(this, chatMessages,chat.this,mSocket);
        list.setAdapter(customAdapter);
    }

    private Emitter.Listener handleReceiveMessage = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
           final JSONObject data = (JSONObject) args[0];
            try {
                final application app = (application) getApplicationContext();
                final String message = data.getString("message");
                final String message_sender_id = data.getString("sender_id");

                final String sender_id = getIntent().getExtras().getString("sender_id");
                boolean isChecker = false;

                    if (data.has("type") && data.getString("type").equals("p_checker")) {
                        isChecker = true;
                    }

                    final boolean finalIsChecker = isChecker;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                boolean is_sender = false;
                                if (data.getString("sender_id").toString().equals(sender_id.toString())) {
                                    is_sender = true;
                                } else {
                                    if (!isGroup && !finalIsChecker && (sender_id.toString()).equals(message_sender_id.toString())) {
                                        JSONObject updateCounter = new JSONObject();
                                        updateCounter.put("update_user_id", app.getUser_id());
                                        updateCounter.put("other_user_id", data.getString("sender_id"));
                                        mSocket.emit("update_message_count_zero", updateCounter);
                                    }
                                    is_sender = false;
                                }
                                if (!data.has("type") || data.isNull("type")) {
                                    addNewMessage(new message(data.getString("id"), data.getString("message"), null, null, is_sender,
                                            data.getString("date_created"), false));
                                } else if (data.has("type") && data.getString("type").equals("location") || data.getString("type").equals("safecity")) {
                                    addNewMessage(new message(data.getString("id"), data.getString("message"), data.getString("type"), null, is_sender,
                                            data.getString("date_created"), false));
                                } else if (data.has("type") && data.getString("type").equals("p_checker")) {
                                    addNewMessage(new message(data.getString("id"), data.getString("message"), null, null, is_sender,
                                            data.getString("date_created"), true));
                                } else {
                                    if (data.has("type") && !data.isNull("type") &&
                                            data.getString("type").equals("file")) {
                                        if (data.has("path") && !data.isNull("path")) {
                                            if (data.getString("path").endsWith(".jpg")
                                                    || data.getString("path").endsWith(".png")
                                                    || data.getString("path").endsWith(".jpeg")) {

                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "image",
                                                        "http://3.210.76.112:9000/" + data.getString("path"), is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type image");
                                            } else if (data.getString("path").endsWith(".doc")
                                                    || data.getString("path").endsWith(".docx")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "document",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type document");
                                            } else if (data.getString("path").endsWith(".ppt")
                                                    || data.getString("path").endsWith(".pptx")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "powerpoint",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type poweerpoint");
                                            } else if (data.getString("path").endsWith(".xls")
                                                    || data.getString("path").endsWith(".xlsx")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "xls",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type xls");
                                            } else if (data.getString("path").endsWith(".txt")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "text",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type text");
                                            } else if (data.getString("path").endsWith(".pdf")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "pdf",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type pdf");
                                            } else if (data.getString("path").endsWith(".csv")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "csv",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type csv");
                                            }else if (data.getString("path").endsWith(".aac")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "aac",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type csv");
                                            }else if (data.getString("path").endsWith(".amr")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "amr",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type csv");
                                            }else if (data.getString("path").endsWith(".m4a")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "m4a",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type csv");
                                            }else if (data.getString("path").endsWith(".opus")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "opus",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type csv");
                                            }else if (data.getString("path").endsWith(".wav")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "wav",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type csv");
                                            } else if (data.getString("path").endsWith(".mp3")) {
                                                addNewMessage(new message(data.getString("id"), data.getString("message"), "mp3",
                                                        "http://3.210.76.112:9000/" + data.getString("path"),
                                                        is_sender, data.getString("date_created"), false));
                                                System.out.println("message is of some othe type mp3");
                                            }
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            }catch (JSONException e) {
                System.out.println("json exception = "+e.toString());
            }

        }

    };


    private Emitter.Listener getAssignedColor = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                // final String message = data.getString("message");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            color=data.getString("color");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };

    private Emitter.Listener updateCounterToZero = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                System.out.println("updatecountertozero = "+data);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateMessagesRead();
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };



    void updateMessagesRead(){
        for(int i=0;i<chatMessages.size();i++){
            if(chatMessages.get(i).isIs_sender())
            chatMessages.get(i).setIs_read(true);
        }
        if(chatMessages.size()>0) {
            chat_adapter customAdapter = new chat_adapter(this, chatMessages, chat.this, mSocket);
            list.setAdapter(customAdapter);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

       /* if(mSocket!=null)
        {
            if(!mSocket.connected())
            {
                mSocket.disconnect();
                mSocket.connect();
                // mSocket.on("validate_user_response", handleValidateResponse);
                // mSocket.on("register_user_response", handleRegisterResponse);

            }
        }
        */
    }


    public void openFileManager(){
        Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getDownloadCacheDirectory().getPath().toString());
        chooser.addCategory(Intent.CATEGORY_OPENABLE);
        chooser.setType( "*/*");
// startActivity(chooser);
        try {
            startActivityForResult(chooser, 1);
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==1){
            Uri uri = data.getData();
            String uriString = uri.toString();
            String path=getRealPathFromURI(getApplicationContext(),uri);
            boolean isCorrectFormat=false;

            if(path==null){
                try {
                    path = getPath(getApplicationContext(), uri);
                    if(path==null){
                        path=getRealPathFromURI_API11to18(getApplicationContext(),uri);
                        if(path==null){
                            path=getRealPathFromURI_API19(getApplicationContext(),uri);
                            if(path==null){
                                path=getRealPathFromURI_BelowAPI11(getApplicationContext(),uri);



                                String id = DocumentsContract.getDocumentId(uri);
                                InputStream inputStream = getContentResolver().openInputStream(uri);
                                File file = new File(getCacheDir().getAbsolutePath()+"/"+id);
                                writeFile(inputStream, file);
                                String filePath = file.getAbsolutePath();
                                System.out.println("file path = "+filePath);
                            }
                        }
                    }
                    System.out.println("file path = "+path);
                }catch(Exception e){
                    System.out.println("exception = "+e.toString());
                }
            }


            if(path!=null){
                if(path.endsWith(".png")){
                    isCorrectFormat=true;
                }else if(path.endsWith(".jpg")){
                    isCorrectFormat=true;

                }else if(path.endsWith(".jpeg")){
                    isCorrectFormat=true;

                }else if(path.endsWith(".docx")){
                    isCorrectFormat=true;

                }else if(path.endsWith(".doc")){
                    isCorrectFormat=true;

                }else if(path.endsWith(".pdf")){
                    isCorrectFormat=true;

                }else if(path.endsWith(".ppt") || uriString.endsWith(".pptx")){
                    isCorrectFormat=true;

                }else if(path.endsWith(".xls") || uriString.endsWith(".xlsx")){
                    isCorrectFormat=true;

                }else if(path.endsWith(".txt")){
                    isCorrectFormat=true;

                }else if(path.endsWith(".csv")){
                    isCorrectFormat=true;
                }else{
                    Toast.makeText(getApplicationContext(),"File not supported",Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(getApplicationContext(),"File not supported",Toast.LENGTH_LONG).show();
            }

            if(isCorrectFormat){
                final int PERMISSION_ALL = 1;
                final String[] PERMISSIONS = {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };

                if(!hasPermissions(this, PERMISSIONS)){
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                }else{
                    new uploadfile(path).execute();
                }
                /*JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("message","");
                    jsonObject.put("sender_id",getIntent().getExtras().getString("sender_id"));
                    jsonObject.put("receiver_id",getIntent().getExtras().getString("receiver_id"));
                    jsonObject.put("path",path);
                    mSocket.emit("add_message",jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }

            //getRealPathFromURI(getApplicationContext(),uri);
        }else if(resultCode==RESULT_OK && requestCode==21){
            try {

                progressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                String path = getFilename("png");
                File f = new File(path);
                f.createNewFile();

//Convert bitmap to byte array

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();


                new uploadfile(path).execute();


            }catch(Exception e){

                progressBar.setVisibility(View.INVISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                e.printStackTrace();
            }

        }
    }




    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation=location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    class uploadfile extends AsyncTask<Void,Void,Void>{

        String path;
        uploadfile(String path){
            this.path=path;
        }
        @Override
        protected Void doInBackground(Void... voids) {

            String receiver_id=getIntent().getExtras().getString("receiver_id");
            String msg=getIntent().getExtras().getString("sender_name") + "";
            String notificatioMessage=getIntent().getExtras().getString("sender_name") + " : uploaded a new file";
            if(isGroup) {
                receiver_id=getIntent().getExtras().getString("group_id");
                if (color.length() > 0) {
                    msg= "<font color='" + color + "'>" + getIntent().getExtras().getString("sender_name") + "</font>" + "";

                } else {
                    msg=getIntent().getExtras().getString("sender_name") + "";
                }
            }
            String groupUsers="";
            if(isGroup){
                groupUsers=getIntent().getExtras().getString("group_users");
            }
            StoreDataToRemoteServer.uploadFile(getIntent().getExtras().getString("sender_id"),receiver_id,path,getApplicationContext(),msg,
                    notificatioMessage,groupUsers);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.INVISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            JSONObject jsonObject=new JSONObject();
            try {
                if(isGroup){
                    application app=(application) getApplicationContext();
                    jsonObject.put("sender_id",app.getUser_id());
                    jsonObject.put("sender_name",app.getUsername());
                    jsonObject.put("group_id",getIntent().getExtras().getString("group_id"));
                    jsonObject.put("group_name",getIntent().getExtras().getString("group_name"));
                    jsonObject.put("isGroup",true);
                    jsonObject.put("group_users",getIntent().getExtras().getString("group_users"));
                    jsonObject.put("isOwner",isOwner);
                    jsonObject.put("message",app.getUsername()+" "+" uploaded a new file");
                    mSocket.emit("file_notification_broadcast",jsonObject);
                }else{
                    application app=(application) getApplicationContext();
                    jsonObject.put("sender_id",app.getUser_id());
                    jsonObject.put("sender_name",app.getUsername());
                    jsonObject.put("receiver_id",getIntent().getExtras().getString("receiver_id"));
                    jsonObject.put("receiver_name",getIntent().getExtras().getString("receiver_name"));
                    jsonObject.put("isGroup",false);
                    jsonObject.put("message",app.getUsername()+" "+" uploaded a new file");
                    mSocket.emit("file_notification_broadcast",jsonObject);

                }
            }catch(Exception e){

            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // openFileManager();
                    //readContacts();read permission granted
                    //sendUserDataToSrver();

                    extras.putString("color",color);
                    application app=(application) getApplicationContext();
                    app.setFileUplaoded(false);

                    startActivity(new Intent(chat.this,select_file_type_activity.class).putExtras(extras));
                    //finish();

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        new AlertDialog.Builder(this).
                                setTitle("File sharing Permission").
                                setMessage("You need to grant read contacts permission to use this feature. Retry and grant it !").show();
                    } else {
                        new AlertDialog.Builder(this).
                                setTitle("File sharing Permission").
                                setMessage("You denied read external storage permission." +
                                        " So, the feature will be disabled. To enable it" +
                                        ", go on settings and " +
                                        "grant read phone state permission for the application")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //openSETTING();
                                        extras.putString("color",color);
                                        application app=(application) getApplicationContext();
                                        app.setFileUplaoded(false);

                                        startActivity(new Intent(chat.this,select_file_type_activity.class).putExtras(extras));
                                       // finish();

                                    }
                                })
                                .setNegativeButton("Canel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(getApplicationContext(), "Application will not work correctly without this permission.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .show();
                    }
                }
                break;
            case 2:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
                    try {
                        Criteria criteria = new Criteria();
                        String provider = locationManager.getBestProvider(criteria, false);
                        Location location = locationManager.getLastKnownLocation(provider);
                        Log.d(TAG, provider);
                        Log.d(TAG, location == null ? "NO LastLocation" : location.toString());
                        checkLocaltionEnable(chat.this);
                        if(lastLocation==null){
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    0,
                                    0, chat.this);
                            if (locationManager != null) {
                                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                if(lastLocation==null && locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)
                                && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                                    locationManager.requestLocationUpdates(
                                            LocationManager.NETWORK_PROVIDER,
                                            MIN_TIME_BW_UPDATES,
                                            MIN_DISTANCE_CHANGE_FOR_UPDATES, chat.this);

                                    if (locationManager != null) {
                                        lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                    }
                                }
                            }else {
                                if(lastLocation==null && locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)
                                        && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                                    locationManager.requestLocationUpdates(
                                            LocationManager.NETWORK_PROVIDER,
                                            MIN_TIME_BW_UPDATES,
                                            MIN_DISTANCE_CHANGE_FOR_UPDATES, chat.this);

                                    if (locationManager != null) {
                                        lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                    }
                                }

                            }
                        }

                        if(locationHandler!=null && locationRunnable!=null)
                            locationHandler.removeCallbacks(locationRunnable);
                        getLastLoation();


                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        new AlertDialog.Builder(this).
                                setTitle("Location sharing permission").
                                setMessage("You need to grant location  permission to send location. Retry and grant it !").show();
                    } else {
                        new AlertDialog.Builder(this).
                                setTitle("Location Sharing Permission").
                                setMessage("You denied  permission." +
                                        " So, the feature will be disabled. To enable it" +
                                        ", go on settings and " +
                                        "grant read phone state permission for the application")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        openSETTING();
                                    }
                                })
                                .setNegativeButton("Canel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(getApplicationContext(), "Location sharing will not work correctly without this permission.", Toast.LENGTH_LONG).show();
                                    }
                                }).show();
                    }
                }
                break;
            case 3:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("");
                    //readContacts();read permission granted
                    //sendUserDataToSrver();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO)) {
                        new AlertDialog.Builder(this).
                                setTitle("Video calling Permission").
                                setMessage("You need to camera  permission to use video calling. Retry and grant it !").show();
                    } else {
                        new AlertDialog.Builder(this).
                                setTitle("Video calling Permission").
                                setMessage("You denied  permission." +
                                        " So, the feature will be disabled. To enable it" +
                                        ", go on settings and " +
                                        "grant read phone state permission for the application")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        openSETTING();
                                    }
                                })
                                .setNegativeButton("Canel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(getApplicationContext(), "Application will not work correctly without this permission.", Toast.LENGTH_LONG).show();
                                    }
                                }).show();
                    }
                }
                break;
            case 10:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("");
                    //readContacts();read permission granted
                    //sendUserDataToSrver();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA)) {
                        new AlertDialog.Builder(this).
                                setTitle("Capture Image Permission").
                                setMessage("You need to camera  permission to use capture image permission. Retry and grant it !").show();
                    } else {
                        new AlertDialog.Builder(this).
                                setTitle("Capture Image Permission").
                                setMessage("You denied  permission." +
                                        " So, the feature will be disabled. To enable it" +
                                        ", go on settings and " +
                                        "grant read phone state permission for the application")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        openSETTING();
                                    }
                                })
                                .setNegativeButton("Canel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(getApplicationContext(), "This feature will not work without this permission.", Toast.LENGTH_LONG).show();
                                    }
                                }).show();
                    }
                }
                break;
        }
    }


    public void openSETTING(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

    }








    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat, menu);
        if(isGroup) {
            MenuItem item = menu.findItem(R.id.video_call);
            MenuItem audio = menu.findItem(R.id.audio_call);

            item.setVisible(false);
            audio.setVisible(false);
        }
        MenuItem allCameras = menu.findItem(R.id.all_cameras);
        allCameras.setVisible(false);
        String group_name=getIntent().getExtras().getString("group_name");
        if(isGroup && group_name!=null && (group_name.equals("Safe") || group_name.equals("Safe-RED")
                || group_name.equals("Dip")|| group_name.equals("Dip-RED"))){
            allCameras.setVisible(true);
        }
        if(!isOwner){
            MenuItem item = menu.findItem(R.id.add_new_member);
            item.setVisible(false);
        }
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.video_call:
                final int PERMISSION_ALL = 3;
                final String[] PERMISSIONS = {
                        Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO

                };

                if(!hasPermissions(this, PERMISSIONS)){
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                }else{
                    if(!isGroup){
                        final String receiver_name=getIntent().getExtras().getString("receiver_name");
                        final String sender_name=getIntent().getExtras().getString("sender_name");

                        startActivity(new Intent(getApplicationContext(),video_call.class).putExtra("ownId",sender_name)
                                .putExtra("otherId",receiver_name).putExtra("isCaller",true).putExtra("is_audio",false));
                        finish();

                    }else{

                    }
                }

                finish();
                break;
            case R.id.audio_call:
                final int PERMISSION_AUDIO_ALL = 4;
                final String[] PERMISSIONS_AUDIO = {
                        Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO

                };

                if(!hasPermissions(this, PERMISSIONS_AUDIO)){
                    ActivityCompat.requestPermissions(this, PERMISSIONS_AUDIO, PERMISSION_AUDIO_ALL);
                }else{
                    if(!isGroup){
                        final String receiver_name=getIntent().getExtras().getString("receiver_name");
                        final String sender_name=getIntent().getExtras().getString("sender_name");

                        startActivity(new Intent(getApplicationContext(),video_call.class).putExtra("ownId",sender_name)
                                .putExtra("otherId",receiver_name).putExtra("isCaller",true).putExtra("is_audio",true));
                        finish();

                    }else{

                    }
                }

                finish();
                break;
            case R.id.attachemnt:

               /* final int FILE_PERMISSION_ALL = 1;
                final String[] FILE_PERMISSIONS = {
                        Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

                if(!hasPermissions(this, FILE_PERMISSIONS)){
                    ActivityCompat.requestPermissions(this, FILE_PERMISSIONS, FILE_PERMISSION_ALL);
                }else{
                    openFileManager();
                }*/

                final int FILE_PERMISSION_ALL = 1;
                final String[] FILE_PERMISSIONS = {
                        Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

                if(!hasPermissions(this, FILE_PERMISSIONS)){
                    ActivityCompat.requestPermissions(this, FILE_PERMISSIONS, FILE_PERMISSION_ALL);
                }else{
                    extras.putString("color",color);
                    application app=(application) getApplicationContext();
                    app.setFileUplaoded(false);
                    startActivity(new Intent(chat.this,select_file_type_activity.class).putExtras(extras));
                    }
                break;
            case R.id.location:
                System.out.println("localtion clicked");
                final int LOCATION_PERMISSION_ALL = 2;
                final String[] LOCATION_PERMISSIONS = {
                        Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION

                };

                if(!hasPermissions(this, LOCATION_PERMISSIONS)){
                    ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, LOCATION_PERMISSION_ALL);
                }else{
                    locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
                    try {
                        Criteria criteria = new Criteria();
                        String provider = locationManager.getBestProvider(criteria, false);
                        Location location = locationManager.getLastKnownLocation(provider);
                        Log.d(TAG, provider);
                        Log.d(TAG, location == null ? "NO LastLocation" : location.toString());
                        checkLocaltionEnable(chat.this);
                        if(lastLocation==null){
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    0,
                                    0, chat.this);
                            if (locationManager != null) {
                                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                               /* if(lastLocation==null && locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)
                                        && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                                    locationManager.requestLocationUpdates(
                                            LocationManager.NETWORK_PROVIDER,
                                            MIN_TIME_BW_UPDATES,
                                            MIN_DISTANCE_CHANGE_FOR_UPDATES, chat.this);

                                    if (locationManager != null) {
                                        lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                    }
                                }*/
                            }else {
                               /* if(lastLocation==null && locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)
                                        && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                                    locationManager.requestLocationUpdates(
                                            LocationManager.NETWORK_PROVIDER,
                                            MIN_TIME_BW_UPDATES,
                                            MIN_DISTANCE_CHANGE_FOR_UPDATES, chat.this);

                                    if (locationManager != null) {
                                        lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                    }
                                }*/

                            }
                        }

                        if(locationHandler!=null && locationRunnable!=null)
                            locationHandler.removeCallbacks(locationRunnable);
                        getLastLoation();


                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.add_new_member:
                String id=getIntent().getExtras().getString("sender_id");
                String name=getIntent().getExtras().getString("sender_name");
                startActivity(new Intent(chat.this,create_group.class).
                        putExtra("group_users",getIntent().getExtras().getString("group_users")).putExtra("id",id).
                        putExtra("name",name).putExtra("should_update",true).
                        putExtra("group_name",getIntent().getExtras().getString("group_name")).putExtra("should_update",true)
                        .putExtra("group_id",getIntent().getExtras().getString("group_id")));
                //finish();
                break;
            case R.id.all_cameras:
                startActivity(new Intent(chat.this, display_urls.class).putExtra("url",
                        "http://3.210.76.112:3001/static/AllMaps.html"));

        }
        return true;
    }



    public void checkLocaltionEnable(final Context context){
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                    .setMessage("Turn on device location to use this feature")
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).show();
        }
    }



    public void getLastLoation(){
         locationHandler=new Handler();
        locationRunnable=new Runnable() {
            @Override
            public void run() {
                System.out.println("getlocation is running  "+lastLocation);
                if (locationManager != null) {
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                    if(lastLocation==null){
                    locationHandler.postDelayed(this,200);
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject jsonObject=new JSONObject();
                            try {
                                if(isGroup){
                                    jsonObject.put("message",getIntent().getExtras().getString("sender_name")+" : "+lastLocation.getLatitude()+","+lastLocation.getLongitude());
                                    jsonObject.put("sender_id",getIntent().getExtras().getString("sender_id"));
                                    jsonObject.put("receiver_id",getIntent().getExtras().getString("group_id"));
                                    jsonObject.put("group_users",getIntent().getExtras().getString("group_users"));
                                    jsonObject.put("lat",lastLocation.getLatitude());
                                    jsonObject.put("lng",lastLocation.getLongitude());
                                    jsonObject.put("is_group",isGroup);

                                    jsonObject.put("type","location");
                                }else{
                                    jsonObject.put("message",getIntent().getExtras().getString("sender_name")+" : "+lastLocation.getLatitude()+","+lastLocation.getLongitude());
                                    jsonObject.put("sender_id",getIntent().getExtras().getString("sender_id"));
                                    jsonObject.put("receiver_id",getIntent().getExtras().getString("receiver_id"));
                                    jsonObject.put("is_group",isGroup);
                                    jsonObject.put("lat",lastLocation.getLatitude());
                                    jsonObject.put("lng",lastLocation.getLongitude());
                                    jsonObject.put("type","location");
                                }
                                if(!mSocket.connected()){
                                   // mSocket.connect();
                                }

                                mSocket.emit("add_message",jsonObject);
                                message_edt.setText("");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    System.out.println("send location to server = "+lastLocation);
                }
            }
        };
        locationHandler.postDelayed(locationRunnable,0);
    }




    public String getPath(Context context, Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{ split[1] };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }



    public static String getRealPathFromURI_API19(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }



    void writeFile(InputStream in, File file) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if ( out != null ) {
                    out.close();
                }
                in.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }


    private String getFilename(String ext) {
        String filepath = getApplicationContext().getExternalCacheDir().getAbsolutePath();
        File file = new File(filepath,"/recordings");

        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + "."+ext);
    }




    public  void startRecording(String path) {


        System.out.println("path  = "+path);

        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(path);

        recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mr, int what, int extra) {

            }
        });
        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
            }
        });

        try
        {
            recorder.prepare();
            recorder.start();
            d1=new Date();
            isRecordingStarted=true;
        }
        catch (IllegalStateException e)
        {
            if(recorder!=null)
            {
                recorder.reset();
                recorder.release();
                recorder=null;
            }

            // startRecording(path,rec,recording,time_to_record);
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private void stopRecording() {

        if (null != recorder && isRecordingStarted)
        {
            isRecordingStarted=false;
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("onbackpressedcalled");
        application app=(application)getApplicationContext();
        mSocket.off("delete_message", handelDeleteMessage);
        mSocket.off("update_message_status"+app.getUser_id(), updateCounterToZero);

        if(!isGroup){
            mSocket.off("message_recieved"+app.getUser_id(), handleReceiveMessage);
            mSocket.off("get_messages_response", handelGetAllMessages);

        }else{
            String group_id=getIntent().getExtras().getString("group_id");
            mSocket.off("message_recieved"+group_id, handleReceiveMessage);
            mSocket.off("get_messages_response", handelGetAllMessages);
            mSocket.off("get_assigned_color", getAssignedColor);
        }        finish();

    }


    @Override
    protected void onPause() {
        super.onPause();

        System.out.println("onpause called");
        /*application app=(application) getApplicationContext();
        mSocket.off("delete_message", handelDeleteMessage);
        mSocket.off("update_message_status"+app.getUser_id(), updateCounterToZero);
        if(!isGroup){
            mSocket.off("message_recieved"+sender_id, handleReceiveMessage);
            mSocket.off("get_messages_response", handelGetAllMessages);

        }else{
            String group_id=getIntent().getExtras().getString("group_id");
            mSocket.off("message_recieved"+group_id, handleReceiveMessage);
            mSocket.off("get_messages_response", handelGetAllMessages);
            mSocket.off("get_assigned_color", getAssignedColor);
        }*/
      //  finish();
    }

    @Override
    protected void onStop() {
        super.onStop();

        System.out.println("ontopcalled");
       /* application app=(application) getApplicationContext();
        mSocket.off("delete_message", handelDeleteMessage);
        mSocket.off("update_message_status"+app.getUser_id(), updateCounterToZero);
        if(!isGroup){
            mSocket.off("message_recieved"+sender_id, handleReceiveMessage);
            mSocket.off("get_messages_response", handelGetAllMessages);

        }else{
            String group_id=getIntent().getExtras().getString("group_id");
            mSocket.off("message_recieved"+group_id, handleReceiveMessage);
            mSocket.off("get_messages_response", handelGetAllMessages);
            mSocket.off("get_assigned_color", getAssignedColor);
        }*/
        //finish();
    }
}
