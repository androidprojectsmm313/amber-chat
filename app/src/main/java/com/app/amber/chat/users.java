package com.app.amber.chat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.amber.chat.DATABASE_OPERATIONS.AppDatabase;
import com.app.amber.chat.pojo.user;
import com.app.amber.chat.service.socket_events_listener;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class users extends AppCompatActivity {
    ListView list;
    ArrayList<user> user_list;
    TextView error_msg;
    Button contacts,group;
    private Socket mSocket;
    EditText search_users_edt;


    /*{
        try {
            mSocket = IO.socket("http://192.168.8.104:3000");
        } catch (URISyntaxException e) {}
    }*/


    boolean isSelectedGroup;

    public static boolean isAlreadyGathered=false;
    private Toolbar mTopToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        System.out.println("users oncreate called ");
        mTopToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(mTopToolbar);

        search_users_edt=(EditText) findViewById(R.id.search);


        search_users_edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("search users = "+s);
                application app=(application) getApplicationContext();
                if(s.toString().length()>0){
                    if(user_list!=null && user_list.size()>0){
                        ArrayList<user> filteredList=filterByUserGroup(user_list,isSelectedGroup);
                        ArrayList<user> tempArrayList=searchByName(filteredList,s.toString());
                        if(tempArrayList.size()>0){
                            user_adapter customAdapter = new user_adapter(getApplicationContext(),tempArrayList,app.getUsername(),app.getUser_id());
                            list.setAdapter(customAdapter);
                        }else{
                            list.setAdapter(null);
                        }
                    }
                }else{
                    ArrayList<user> filteredList=filterByUserGroup(user_list,isSelectedGroup);
                    if(filteredList.size()>0){
                        user_adapter customAdapter = new user_adapter(getApplicationContext(),filteredList,app.getUsername(),app.getUser_id());
                        list.setAdapter(customAdapter);
                    }else{
                        list.setAdapter(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        user_list=new ArrayList<>();
        list = findViewById(R.id.list_item);
        error_msg=findViewById(R.id.error_msg);
        contacts=findViewById(R.id.contacts);
        group=findViewById(R.id.group);

        isAlreadyGathered=false;
        application app=(application) getApplicationContext();
        app.setUserArrayList(new ArrayList<user>());
        socket_events_listener.isBusy=false;
        isSelectedGroup=false;
        contacts.setEnabled(false);
        group.setEnabled(false);
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String username=getIntent().getExtras().getString("username");
                final String id=getIntent().getExtras().getString("id");

                ArrayList<user> filteredList=new ArrayList<>();
                for(int i=0;i<user_list.size();i++){
                    if(!user_list.get(i).isGroup()){
                        filteredList.add(user_list.get(i));
                    }
                }


                isSelectedGroup=false;

                if(filteredList.size()>0){
                    user_adapter customAdapter = new user_adapter(getApplicationContext(),filteredList,username,id);
                    list.setAdapter(customAdapter);
                }else{
                    list.setAdapter(null);
                    if(isSelectedGroup){
                        error_msg.setText("No Group Found");
                        error_msg.setVisibility(View.INVISIBLE);
                    }else{
                        error_msg.setText("No User Found");
                        error_msg.setVisibility(View.INVISIBLE);
                    }
                }
                contacts.setBackgroundColor(getResources().getColor(R.color.contacts_selected));
                group.setBackgroundColor(getResources().getColor(R.color.group));
            }
        });


        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String username=getIntent().getExtras().getString("username");
                final String id=getIntent().getExtras().getString("id");

                isSelectedGroup=true;
                ArrayList<user> filteredList=new ArrayList<>();
                for(int i=0;i<user_list.size();i++){
                    if(user_list.get(i).isGroup()){
                        filteredList.add(user_list.get(i));
                    }
                }

                if(filteredList.size()>0){
                    user_adapter customAdapter = new user_adapter(getApplicationContext(),filteredList,username,id);
                    list.setAdapter(customAdapter);
                }else{
                    list.setAdapter(null);
                    if(isSelectedGroup){
                        error_msg.setText("No Group Found");
                        error_msg.setVisibility(View.INVISIBLE);
                    }else{
                        error_msg.setText("No User Found");
                        error_msg.setVisibility(View.INVISIBLE);
                    }

                }
                group.setBackgroundColor(getResources().getColor(R.color.group_selected));
                contacts.setBackgroundColor(getResources().getColor(R.color.contacts));
            }
        });

        error_msg.setVisibility(View.INVISIBLE);
        String username=getIntent().getExtras().getString("username");
        String id=getIntent().getExtras().getString("id");

        mSocket=app.getmSocket();

        if(mSocket!=null)
        {
            if(mSocket.connected())
            {
                error_msg.setText("Gathering data ..");
                error_msg.setVisibility(View.VISIBLE);
            }else{
                error_msg.setText("Gathering data ..");
                error_msg.setVisibility(View.VISIBLE);
                //mSocket.disconnect();
                //mSocket.connect();

            }

            mSocket.off("get_all_users_response");
            mSocket.off("update_user_status");
            mSocket.off("add_new_user");
            mSocket.off("logout");
            mSocket.off("add_new_group"+id);
            mSocket.off("remove_group_user"+id);
            mSocket.off("increment_messages_count"+id);

            mSocket.on("logout", logOutUser);
            if(!isAlreadyGathered){
                mSocket.on("get_all_users_response", handleGetAllUserResponse);
                error_msg.setVisibility(View.VISIBLE);
            }else{
                error_msg.setVisibility(View.INVISIBLE);
                contacts.setEnabled(true);
                group.setEnabled(true);
                contacts.setBackgroundColor(getResources().getColor(R.color.contacts_selected));
            }
           mSocket.on("update_user_status", handleUpdaeUserStatus);
            mSocket.on("add_new_user", addNewUserEvent);
            mSocket.on("add_new_group"+id, addNewGroup);
            mSocket.on("remove_group_user"+id, removeGroup);
            mSocket.on("increment_messages_count"+id, incrementMessagesCount);
            app.setHandleReadyForCall(id);
        }


        JSONObject  jsonObject=new JSONObject();
        try {
            jsonObject.put("username",username);
            jsonObject.put("id",id);

            if(!mSocket.connected()){
                //mSocket.connect();
            }
            mSocket.emit("get_all_users",jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        final int PERMISSION_ALL = 1;
        final String[] PERMISSIONS = {
                Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.MODIFY_PHONE_STATE

        };

        if(!chat.hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

    }



    private Emitter.Listener handleGetAllUserResponse = new Emitter.Listener(){
        @Override
        public void call(final Object... args) {
            if (!users.isAlreadyGathered) {
                final JSONObject data = (JSONObject) args[0];
                try {
                    System.out.println("get all users response = " + data);
                    users.isAlreadyGathered = true;
                    final application app = (application) getApplicationContext();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (!data.isNull("messages_count")) {
                                    app.setMessagesCount(data.getJSONObject("messages_count"));
                                } else {
                                    app.setMessagesCount(new JSONObject());
                                }
                                error_msg.setVisibility(View.INVISIBLE);
                                if (data.getBoolean("success")) {
                                    JSONArray jsonArray = data.getJSONArray("user");

                                    if (jsonArray.length() > 0) {
                                        mSocket.off("get_all_users_response");
                                    }

                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        if (jsonObject.getBoolean("isGroup")) {
                                            JSONArray arr = jsonObject.getJSONArray("group_users");

                                            ArrayList<String> group_users = new ArrayList<String>();
                                            for (int j = 0; j < arr.length(); j++) {
                                                group_users.add(arr.getString(j));
                                            }
                                            //convert json string to object
                                            if (!jsonObject.isNull("username")) {
                                                user_list.add(new user(jsonObject.getString("id"), jsonObject.getString("username"),
                                                        false, "", true, group_users, jsonObject.getBoolean("isOwner")));
                                            }
                                        } else {

                                            if (!jsonObject.isNull("username")) {
                                                if (jsonObject.isNull("messages_count")) {
                                                    user_list.add(new user(jsonObject.getString("id"), jsonObject.getString("username"),
                                                            jsonObject.getBoolean("status"), jsonObject.getString("last_login"),
                                                            new JSONObject()));
                                                } else {
                                                    user_list.add(new user(jsonObject.getString("id"), jsonObject.getString("username"),
                                                            jsonObject.getBoolean("status"), jsonObject.getString("last_login"),
                                                            jsonObject.getJSONObject("messages_count")));
                                                }
                                            }
                                        }
                                    }

                                    if (user_list.size() < 1) {
                                        error_msg.setVisibility(View.VISIBLE);
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                String username = getIntent().getExtras().getString("username");
                                                String id = getIntent().getExtras().getString("id");
                                                application app = (application) getApplicationContext();
                                                app.setUserArrayList(user_list);
                                                ArrayList<user> filteredList = new ArrayList<>();
                                                for (int i = 0; i < user_list.size(); i++) {
                                                    if (!user_list.get(i).isGroup()) {
                                                        filteredList.add(user_list.get(i));
                                                    }
                                                }

                                                if (filteredList.size() > 0) {
                                                    user_adapter customAdapter = new user_adapter(getApplicationContext(), filteredList, username, id);
                                                    list.setAdapter(customAdapter);
                                                } else {
                                                    list.setAdapter(null);
                                                    if (isSelectedGroup) {
                                                        error_msg.setText("No Group Found");
                                                        error_msg.setVisibility(View.INVISIBLE);
                                                    } else {
                                                        error_msg.setText("No User Found");
                                                        error_msg.setVisibility(View.INVISIBLE);
                                                    }
                                                }


                                                contacts.setEnabled(true);
                                                group.setEnabled(true);
                                                contacts.setBackgroundColor(getResources().getColor(R.color.contacts_selected));

                                            }
                                        });
                                    }


                                } else {

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (Exception e) {
                    System.out.println("json exception = " + e.toString());
                }
            }
        }
    };



    private Emitter.Listener handleUpdaeUserStatus = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                System.out.println("update user status111 = "+data);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {


                            if(data.has("user_id") && !data.isNull("user_id") && data.has("username") &&
                                    !data.isNull("username") && data.has("status") && !data.isNull("status") && data.has("last_login")
                                    && !data.isNull("last_login")){

                                for(int i=0;i<user_list.size();i++){
                                    if(user_list.get(i).getId().toString().equals(data.getString("user_id"))){
                                        user u =user_list.get(i);
                                        u.setStatus(data.getBoolean("status"));
                                        user_list.set(i,u);

                                        if(user_list.size()>0){
                                            ArrayList<user> updateList=new ArrayList<>();
                                            if(!isSelectedGroup){
                                                for(int j=0;j<user_list.size();j++){
                                                    if(!user_list.get(j).isGroup()){
                                                        updateList.add(user_list.get(j));
                                                    }
                                                }
                                                application app=(application)getApplicationContext();
                                                user_adapter customAdapter = new user_adapter(getApplicationContext(),updateList,app.getUsername(),app.getUser_id());
                                                list.setAdapter(customAdapter);
                                                System.out.println("status updated successfully");
                                            }
                                        }
                                        continue;
                                    }
                                }
                                /*user u=new user(data.getString("user_id"),data.getString("username"),data.getBoolean("status"),
                                        data.getString("last_login"));
                                String username=getIntent().getExtras().getString("username");
                                String id=getIntent().getExtras().getString("id");

                                if(!data.getString("username").equals(username) && !data.getString("user_id").equals(id))
                                updateUsersList(user_list,u);*/
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };

    private Emitter.Listener addNewUserEvent = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                if(!data.isNull("user") && data.has("user")) {
                    final JSONObject user = data.getJSONObject("user");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (user.has("id") && !user.isNull("user_id") && user.has("username") &&
                                        !user.isNull("username") && user.has("status") && !user.isNull("status")
                                        && user.has("last_login")
                                        && !user.isNull("last_login")) {
                                    /*user u = new user(data.getString("user_id"), data.getString("username"), data.getBoolean("status"),
                                            data.getString("last_login"));
                                    String username=getIntent().getExtras().getString("username");
                                    String id=getIntent().getExtras().getString("id");

                                    if(!data.getString("username").equals(username) && !data.getString("user_id").equals(id))
                                        updateUsersList(user_list,u);*/

                                   // addNewUserIfNotExist(u);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };




    private Emitter.Listener logOutUser = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                if(data.getBoolean("success")){
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // this code will be executed after 2 seconds
                            AppDatabase db = AppDatabase.getAppDatabase(getApplicationContext());
                            String uniqueID = UUID.randomUUID().toString();

                            final ArrayList<com.app.amber.chat.DATABASE_OPERATIONS.schema.user> usersList = new ArrayList<>(db.applicationDao().getAllUsers());
                            if(usersList.size()>0) {
                                db.applicationDao().deleteAllUsers(usersList);
                            }


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(users.this,MainActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                    //finish();
                                }
                            });
                        }},50);
                }else{

                }
            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };


    private Emitter.Listener addNewGroup = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                System.out.println("add new group response = "+data);
                JSONArray arr = data.getJSONArray("group_users");
                ArrayList<String> group_users = new ArrayList<String>();
                for(int j = 0; j < arr.length(); j++){
                    group_users.add(arr.getString(j));
                }
                final user u = new user(data.getString("group_id"),data.getString("name"),
                        false,"",true,group_users,data.getBoolean("isOwner"));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUsersList(user_list,u);
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception add new group = "+e.toString());
            }
        }
    };


    private Emitter.Listener incrementMessagesCount = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                System.out.println("increment message count = "+data);
                final application app=(application)getApplicationContext();
                JSONObject messagesCount=app.getMessagesCount();
                if(messagesCount.isNull(data.getString("id"))){
                    messagesCount.put(data.getString("id"),1);
                }else{
                    int count=messagesCount.getInt(data.getString("id"))+1;
                    messagesCount.put(data.getString("id"),count);
                }
                app.setMessagesCount(messagesCount);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<user> updateList=new ArrayList<>();
                        if(!isSelectedGroup){
                            for(int j=0;j<user_list.size();j++){
                                if(!user_list.get(j).isGroup()){
                                    updateList.add(user_list.get(j));
                                }
                            }
                        }

                        if(updateList.size()>0){
                            user_adapter customAdapter = new user_adapter(getApplicationContext(),updateList,app.getUsername(),app.getUser_id());
                            list.setAdapter(customAdapter);
                        }
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };





    private Emitter.Listener removeGroup = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                System.out.println("remove group response = "+data);
                final String idToRemove=data.getString("id");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(int p=0;p<user_list.size();p++){
                            if(user_list.get(p).getId().equals(idToRemove)){
                                user_list.remove(p);
                                application app=(application)getApplicationContext();
                                app.setUserArrayList(user_list);
                                if(isSelectedGroup){
                                    String username=getIntent().getExtras().getString("username");
                                    String id=getIntent().getExtras().getString("id");
                                    ArrayList<user> updateList=new ArrayList<>();
                                    for(int j=0;j<user_list.size();j++){
                                        if(user_list.get(j).isGroup()){
                                            updateList.add(user_list.get(j));
                                        }
                                    }

                                    if(updateList.size()>0){
                                        user_adapter customAdapter = new user_adapter(getApplicationContext(),updateList,username,id);
                                        list.setAdapter(customAdapter);
                                    }else{
                                        list.setAdapter(null);
                                        if(isSelectedGroup){
                                            error_msg.setText("No Group Found");
                                            error_msg.setVisibility(View.INVISIBLE);
                                        }else{
                                            error_msg.setText("No User Found");
                                            error_msg.setVisibility(View.INVISIBLE);
                                        }
                                    }

                                }
                            }
                        }
                    }
                });
            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };

    public void updateUsersList(ArrayList<user> usersList,user u){
        for(int i=0;i<usersList.size();i++){
            if(u.getId().toString().equals(usersList.get(i).getId().toString())){
                usersList.set(i,u);
                String username=getIntent().getExtras().getString("username");
                String id=getIntent().getExtras().getString("id");

                ArrayList<user> updateList=new ArrayList<>();
                if(isSelectedGroup){
                    for(int j=0;j<user_list.size();j++){
                        if(user_list.get(j).isGroup()){
                            updateList.add(user_list.get(j));
                        }
                    }
                }else{
                    for(int j=0;j<user_list.size();j++){
                        if(!user_list.get(j).isGroup()){
                            updateList.add(user_list.get(j));
                        }
                    }
                }

                if(updateList.size()>0){
                    user_adapter customAdapter = new user_adapter(getApplicationContext(),updateList,username,id);
                    list.setAdapter(customAdapter);
                }else{
                    list.setAdapter(null);
                    if(isSelectedGroup){
                        error_msg.setText("No Group Found");
                        error_msg.setVisibility(View.INVISIBLE);
                    }else{
                        error_msg.setText("No User Found");
                        error_msg.setVisibility(View.INVISIBLE);
                    }
                }
                return;
            }
        }

        usersList.add(u);

        ArrayList<user> updateList=new ArrayList<>();
        if(isSelectedGroup){
            for(int j=0;j<user_list.size();j++){
                if(user_list.get(j).isGroup()){
                    updateList.add(user_list.get(j));
                }
            }
        }else{
            for(int j=0;j<user_list.size();j++){
                if(!user_list.get(j).isGroup()){
                    updateList.add(user_list.get(j));
                }
            }
        }
        String username=getIntent().getExtras().getString("username");
        String id=getIntent().getExtras().getString("id");

        if(updateList.size()>0){
            user_adapter customAdapter = new user_adapter(getApplicationContext(),updateList,username,id);
            list.setAdapter(customAdapter);
        } else{
            list.setAdapter(null);
            if(isSelectedGroup){
                error_msg.setText("No Group Found");
                error_msg.setVisibility(View.INVISIBLE);
            }else{
                error_msg.setText("No User Found");
                error_msg.setVisibility(View.INVISIBLE);
            }
        }

        application app=(application)getApplicationContext();
        app.setUserArrayList(usersList);
        return;
    }



    public void addNewUserIfNotExist(user u){
        String username=getIntent().getExtras().getString("username");
        String id=getIntent().getExtras().getString("id");
        boolean isUserExist=false;
        for(int i=0;i<user_list.size();i++){
            if(user_list.get(i).getUsername().equals(u.getUsername())
                    && user_list.get(i).getId().toString().equals(u.getId().toString())){
                isUserExist=true;
            }
        }

        if(!isUserExist){
            user_list.add(u);

            if(user_list.size()>0){
                user_adapter customAdapter = new user_adapter(getApplicationContext(),user_list,username,id);
                list.setAdapter(customAdapter);
            }else{
                if(isSelectedGroup){
                    error_msg.setText("No Group Found");
                    error_msg.setVisibility(View.INVISIBLE);
                }else{
                    list.setAdapter(null);
                    error_msg.setText("No User Found");
                    error_msg.setVisibility(View.INVISIBLE);
                }
            }
        }


    }

    protected void onResume() {
        super.onResume();
        System.out.println("on resume called");
        socket_events_listener.isBusy=false;
        ArrayList<user> updateList=new ArrayList<>();
        application app=(application)getApplicationContext();
        isSelectedGroup=false;
        if(app.getUserArrayList()!=null && app.getUserArrayList().size()>0){
            System.out.println("userlist = "+user_list.size());
            user_list=app.getUserArrayList();
        }

        if(isSelectedGroup){
            for(int j=0;j<user_list.size();j++){
                if(user_list.get(j).isGroup()){
                    updateList.add(user_list.get(j));
                }
            }
        }else{
            for(int j=0;j<user_list.size();j++){
                if(!user_list.get(j).isGroup()){
                    updateList.add(user_list.get(j));
                }
            }
        }
        System.out.println("userlist = "+user_list.size());
        System.out.println("updatelist = "+updateList.size());
        String username=app.getUsername();
        String id=app.getUser_id();
        if(updateList.size()>0){
           // list.setAdapter(null);
            user_adapter customAdapter = new user_adapter(getApplicationContext(),updateList,username,id);
            list.setAdapter(customAdapter);
        } else{
            list.setAdapter(null);

            if(!isAlreadyGathered) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    jsonObject.put("id", id);

                    if (!mSocket.connected()) {
                        //mSocket.connect();
                    }
                    mSocket.emit("get_all_users", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            /*if(isSelectedGroup){
                error_msg.setText("No Group Found");
                error_msg.setVisibility(View.INVISIBLE);
            }else{
                error_msg.setText("No User Found");
                error_msg.setVisibility(View.INVISIBLE);
            }*/
        }
        /*if(mSocket!=null)
        {
            if(!mSocket.connected())
            {
                mSocket.disconnect();
                mSocket.connect();
                // mSocket.on("validate_user_response", handleValidateResponse);
                // mSocket.on("register_user_response", handleRegisterResponse);

            }
        }*/

      /*  String username=getIntent().getExtras().getString("username");
        String id=getIntent().getExtras().getString("id");

        JSONObject  jsonObject=new JSONObject();
        try {
            jsonObject.put("username",username);
            jsonObject.put("id",id);
            mSocket.emit("get_all_users",jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/



    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //startActivity(new Intent(users.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        application app=(application)getApplicationContext();

        final String username=app.getUsername();
        MenuItem item = menu.findItem(R.id.login);
        item.setTitle("Logged in as "+username);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.create_group:

                String username=getIntent().getExtras().getString("username");
                String id=getIntent().getExtras().getString("id");
                startActivity(new Intent(getApplicationContext(),create_group.class).putExtra("id",id).putExtra("username",username));
                //finish();
                break;
            case R.id.log_out:

                /*new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // this code will be executed after 2 seconds
                        AppDatabase db = AppDatabase.getAppDatabase(getApplicationContext());
                        String uniqueID = UUID.randomUUID().toString();

                        final ArrayList<com.app.amber.chat.DATABASE_OPERATIONS.schema.user> usersList = new ArrayList<>(db.applicationDao().getAllUsers());
                        if(usersList.size()>0) {
                            db.applicationDao().deleteAllUsers(usersList);
                        }


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(users.this,MainActivity.class));
                                //finish();
                            }
                        });
                    }},50);*/
                application app=(application) getApplicationContext();
            JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("user_id",app.getUser_id());
                    mSocket.emit("logout_user",jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("");
                    //readContacts();read permission granted
                    //sendUserDataToSrver();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.MODIFY_PHONE_STATE) ) {
                        new AlertDialog.Builder(this).
                                setTitle("Video calling Permission").
                                setMessage("You need to camera  permission to use this feature. Retry and grant it !").show();
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
        }
    }
    public void openSETTING(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

    }

    ArrayList<user> searchByName(ArrayList<user> userArrayList,String userNameToFind){
        ArrayList<user> tempArrayList=new ArrayList<>();

        for(int i=0;i<userArrayList.size();i++){
            if(userArrayList.get(i).getUsername().contains(userNameToFind)){
                tempArrayList.add(userArrayList.get(i));
            }
        }
        return  tempArrayList;
    }



    ArrayList<user> filterByUserGroup(ArrayList<user> userArrayList,boolean isGroup){


        ArrayList<user> filteredList=new ArrayList<>();
        if(isGroup) {
            for (int i = 0; i < userArrayList.size(); i++) {
                if (userArrayList.get(i).isGroup()) {
                    filteredList.add(userArrayList.get(i));
                }
            }
        }else{
            for (int i = 0; i < userArrayList.size(); i++) {
                if (!userArrayList.get(i).isGroup()) {
                    filteredList.add(userArrayList.get(i));
                }
            }
        }

        return  filteredList;
    }

}
