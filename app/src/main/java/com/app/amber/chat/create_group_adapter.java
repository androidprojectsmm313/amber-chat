package com.app.amber.chat;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.amber.chat.pojo.user;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


class create_group_adapter implements ListAdapter {
    ArrayList<user> arrayList;
    String username,id,group_id,group_name_update;
    Context context;
    EditText group_name;
    ArrayList<String> selectedUsersList,prevUsers;
    Socket mSocket;
    ProgressBar progressBar;
    boolean shouldUpdate;

    public create_group_adapter(final Context context, ArrayList<user> arrayList, final String group_name_update, final String username, final String id, final EditText group_name,
                                final Socket mSocket, final ProgressBar progressBar, final boolean shouldUpdate, final ArrayList<String> prevUsers, final String group_id) {
        this.arrayList=arrayList;
        this.username=username;
        this.id=id;
        this.context=context;
        this.group_name=group_name;
        this.mSocket=mSocket;
        this.shouldUpdate=shouldUpdate;
        this.progressBar=progressBar;
        this.prevUsers=prevUsers;
        this.group_id=group_id;
        this.group_name_update=group_name_update;
        selectedUsersList=new ArrayList<>();
        if(shouldUpdate)
        selectedUsersList.addAll(prevUsers);

    }
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
    @Override
    public boolean isEnabled(int position) {
        return true;
    }
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }
    @Override
    public int getCount() {
        return arrayList.size();
    }
    @Override
    public Object getItem(int position) {
        return position;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public boolean hasStableIds() {
        return false;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String message=arrayList.get(position).getUsername();
        boolean status=arrayList.get(position).getStatus();
        String last_login=arrayList.get(position).getLast_login();
        boolean isExist=arrayList.get(position).isExist();

        if(convertView==null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView=layoutInflater.inflate(R.layout.create_group_item, null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            final TextView tittle=convertView.findViewById(R.id.text);
            final CheckBox checkBox=convertView.findViewById(R.id.select_user);

            tittle.setText(message);
            checkBox.setTag(position);
            checkBox.setChecked(isExist);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int index= (int) buttonView.getTag();
                    if(isChecked){
                        selectedUsersList.add(arrayList.get(index).getId());
                    }else{
                        selectedUsersList.remove(arrayList.get(index).getId());
                    }
                    System.out.println("checked change calleed "+index+"  "+isChecked);
                }
            });
        }
        return convertView;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getViewTypeCount() {
        return arrayList.size();
    }
    @Override
    public boolean isEmpty() {
        return false;
    }


    public void createUpdateGroup(){
        System.out.println("creatre update");


        progressBar.setVisibility(View.VISIBLE);
        System.out.println("clicked");
        if (!shouldUpdate){
            if (!group_name.getText().toString().isEmpty() && selectedUsersList.size() > 0) {
                for (int i = 0; i < selectedUsersList.size(); i++)
                    System.out.println("selected users = " + selectedUsersList.get(i));

                JSONObject jsonObject = new JSONObject();
                try {
                    selectedUsersList.add(id);
                    String jsonArrayList = new Gson().toJson(selectedUsersList);
                    String jsonPrevArrayList = new Gson().toJson(prevUsers);

                    jsonObject.put("name", group_name.getText().toString());
                    jsonObject.put("group_users", jsonArrayList);
                    jsonObject.put("prev_users", jsonPrevArrayList);
                    jsonObject.put("created_by", id);
                    jsonObject.put("message", username + " added you in a group " + group_name.getText().toString());

                    if(!mSocket.connected()){
                        //mSocket.connect();
                    }
                    mSocket.emit("create_group", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println("group name = " + group_name.getText().toString() + " user_group = " + "" + " created_by = " + username);
            } else {
                if (selectedUsersList.size() < 1) {
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(context, "Please add atleast 1 user in group!", Toast.LENGTH_LONG).show();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, "Group name cant be empty", Toast.LENGTH_LONG).show();
                }
                System.out.println("Group name cant be empty");
            }
        }else{

            if (selectedUsersList.size() > 1) {
                for (int i = 0; i < selectedUsersList.size(); i++)
                    System.out.println("selected users = " + selectedUsersList.get(i));


                JSONObject jsonObject = new JSONObject();
                String jsonPrevArrayList = new Gson().toJson(prevUsers);

                try {
                    selectedUsersList.add(id);
                    String jsonArrayList = new Gson().toJson(selectedUsersList);
                    jsonObject.put("name", group_name.getText().toString());
                    jsonObject.put("group_users", jsonArrayList);
                    jsonObject.put("prev_users", jsonPrevArrayList);
                    jsonObject.put("created_by", id);
                    jsonObject.put("update", shouldUpdate);
                    jsonObject.put("group_id", group_id);
                    jsonObject.put("group_name", group_name_update);
                    jsonObject.put("message", username + " added you in a group " + group_name.getText().toString());

                    application app=(application) context.getApplicationContext();
                    ArrayList<user> tempArrayList=app.getUserArrayList();
                    for(int p=0;p<app.getUserArrayList().size();p++){
                        if(tempArrayList.get(p).getId().equals(group_id)){
                            tempArrayList.get(p).setGroup_users(selectedUsersList);
                        }
                    }
                    app.setUserArrayList(tempArrayList);

                    if(!mSocket.connected()){
                        //mSocket.connect();
                    }
                    mSocket.emit("create_group", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println("group name = " + group_name.getText().toString() + " user_group = " + "" + " created_by = " + username);
            } else {
                if (selectedUsersList.size() <=1) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, "Please add atleast 1 user in group!", Toast.LENGTH_LONG).show();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(context, "Group name cant be empty", Toast.LENGTH_LONG).show();
                }
                System.out.println("Group name cant be empty");
            }

        }
    }


    public void deleteGroup(String groupUsers,String groupId,String id){
        try{
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("group_id",groupId);
            jsonObject.put("group_users",groupUsers);
            jsonObject.put("user_id",id);
            mSocket.emit("delete_group",jsonObject);
        }catch(Exception e){

        }

    }
}
