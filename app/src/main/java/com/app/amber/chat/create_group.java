package com.app.amber.chat;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.amber.chat.pojo.user;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;


public class create_group extends AppCompatActivity {

    TextView error_msg;
    ListView list;
    ArrayList<user> userArrayList;
    EditText group_name;
    private Socket mSocket;
    ProgressBar progressBar;
    ArrayList<String> convertedGroupUsers;
    create_group_adapter customAdapter;
    boolean shouldUpdate;
    String group_users;
    String group_id;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        System.out.println("create group oncreate called");
        list =(ListView) findViewById(R.id.list_item);
        error_msg=(TextView) findViewById(R.id.error_msg);
        group_name=(EditText) findViewById(R.id.group_name);
        progressBar=(ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        error_msg.setVisibility(View.INVISIBLE);
        application app=(application) getApplicationContext();
        userArrayList=app.getUserArrayList();
        String username=app.getUsername();
         id=app.getUser_id();
        mSocket=app.getmSocket();
        convertedGroupUsers=new ArrayList<>();
        if(mSocket!=null)
        {
            if(!mSocket.connected())
            {
                //mSocket.disconnect();
                //mSocket.connect();
            }
            mSocket.on("create_group", handleGroupCreated);
            mSocket.on("remove_group_chat"+id, handleDeleteGroup);

        }

         group_users=  getIntent().getExtras().getString("group_users","");
        String groupname_update=  getIntent().getExtras().getString("group_name","");
         group_id=  getIntent().getExtras().getString("group_id","");
        shouldUpdate=  getIntent().getExtras().getBoolean("should_update",false);


        if(shouldUpdate){
            group_name.setHint(groupname_update);
            //group_name.setVisibility(View.GONE);
        }

        Gson gson = new Gson(); // Or use new GsonBuilder().create();


        if(group_users.length()>0)
        try {
            convertedGroupUsers = gson.fromJson(group_users, ArrayList.class); // deserializes json into target2
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(userArrayList!=null && userArrayList.size()>0){
            ArrayList<user> updatedList;
            if(convertedGroupUsers.size()>0){
                updatedList=filterUserFromGroup(userArrayList,convertedGroupUsers);
            }else{
                updatedList=filterUserFromGroup(userArrayList);
            }

             customAdapter = new create_group_adapter(getApplicationContext(),updatedList,groupname_update,
                    username,id,group_name,mSocket,progressBar,shouldUpdate,convertedGroupUsers,group_id);

            list.setAdapter(customAdapter);
        }else{
            error_msg.setVisibility(View.VISIBLE);
        }
    }


    public ArrayList<user> filterUserFromGroup(ArrayList<user> userArrayList,ArrayList<String> groupUsers){
        ArrayList<user> updatedUsers=new ArrayList<>();
        for(int i=0;i<userArrayList.size();i++){
            if(!userArrayList.get(i).isGroup()){
                user u=userArrayList.get(i);
                if(groupUsers.indexOf(u.getId())>=0){
                    u.setExist(true);
                }else{
                    u.setExist(false);
                }
                updatedUsers.add(u);
            }
        }
        return updatedUsers;
    }

    public ArrayList<user> filterUserFromGroup(ArrayList<user> userArrayList){
        ArrayList<user> updatedUsers=new ArrayList<>();
        for(int i=0;i<userArrayList.size();i++){
            if(!userArrayList.get(i).isGroup()){
                user u=userArrayList.get(i);
                u.setExist(false);
                updatedUsers.add(u);
            }
        }
        return updatedUsers;
    }




    private Emitter.Listener handleGroupCreated = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                // final String message = data.getString("message");
                System.out.println("group created  = "+data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            application app=(application) getApplicationContext();
                            //createOffer();
                            String username=app.getUsername();
                            String id=app.getUser_id();
                            progressBar.setVisibility(View.INVISIBLE);

                            startActivity(new Intent(create_group.this,users.class).putExtra("id",id).putExtra("username",username)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            //finish();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };



    private Emitter.Listener handleDeleteGroup = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                // final String message = data.getString("message");
                System.out.println("group delete  = "+data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            //createOffer();
                            String username=getIntent().getExtras().getString("username");
                            String id=getIntent().getExtras().getString("id");
                            progressBar.setVisibility(View.INVISIBLE);

                            startActivity(new Intent(create_group.this,users.class).putExtra("id",id).putExtra("username",username)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_group, menu);

        MenuItem dlt = menu.findItem(R.id.delete);

        if(!shouldUpdate){
            dlt.setVisible(false);
        }

        return true;
    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.create:
                customAdapter.createUpdateGroup();
                break;

            case R.id.delete:
                progressBar.setVisibility(View.VISIBLE);
                customAdapter.deleteGroup(group_users,group_id,id);
                break;

        }

        return true;
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }




}
