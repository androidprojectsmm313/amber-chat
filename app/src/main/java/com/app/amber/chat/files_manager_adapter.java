package com.app.amber.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.amber.chat.UTILITY.StoreDataToRemoteServer;
import com.app.amber.chat.pojo.files;
import com.app.amber.chat.pojo.user;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


class files_manager_adapter implements ListAdapter {
    ArrayList<files> arrayList;
    Context context;
    Bundle extras;
    boolean isOwner=false;
    ProgressBar progressBar;
    Socket mSocket;
    Activity activity;
    public files_manager_adapter(final Context context, ArrayList<files> arrayList,Bundle extras,Socket mSocket,ProgressBar progressBar,Activity activity) {
        this.arrayList=arrayList;
        this.context=context;
        this.extras=extras;
        this.mSocket=mSocket;
        this.progressBar=progressBar;
        this.activity=activity;
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
        String file_name=arrayList.get(position).getFileName();
        String type=arrayList.get(position).getFileType();
        String modified_date=arrayList.get(position).getLastModidfied();
        if(convertView==null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView=layoutInflater.inflate(R.layout.file_manager_item, null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            LinearLayout file_manager_main_container=convertView.findViewById(R.id.file_manager_main_container);
            final ImageView file_type_imageview=convertView.findViewById(R.id.file_type);
            final TextView file_name_txt=convertView.findViewById(R.id.file_name);
            final TextView modified_date_txt=convertView.findViewById(R.id.modified_date);

            file_manager_main_container.setTag(arrayList.get(position));
            file_manager_main_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    files f=(files) v.getTag();
                    progressBar.setVisibility(View.VISIBLE);
                    new uploadfile(f.getFilePath()).execute();
                    //activity.finish();
                    System.out.println(f.getFilePath());
                }
            });
            if(type.equals("jpg")){
                file_type_imageview.setImageResource(R.drawable.jpg);
            }else if(type.equals("png")){
                file_type_imageview.setImageResource(R.drawable.png);
            }else if(type.equals("jpeg")){
                file_type_imageview.setImageResource(R.drawable.jpg);
            }else if(type.equals("docx")){
                file_type_imageview.setImageResource(R.drawable.docx);
            }else if(type.equals("xls")){
                file_type_imageview.setImageResource(R.drawable.xls);
            }else if(type.equals("ppt")){
                file_type_imageview.setImageResource(R.drawable.ppt);
            }else if(type.equals("pdf")){
                file_type_imageview.setImageResource(R.drawable.pdf);
            }else if(type.equals("txt")){
                file_type_imageview.setImageResource(R.drawable.txt);
            }else if(type.equals("csv")){
                file_type_imageview.setImageResource(R.drawable.csv);
            }else if(type.equals("aac")){
                file_type_imageview.setImageResource(R.drawable.aac);
            }else if(type.equals("amr")){
                file_type_imageview.setImageResource(R.drawable.amr);
            }else if(type.equals("m4a")){
                file_type_imageview.setImageResource(R.drawable.m4a);
            }else if(type.equals("opus")){
                file_type_imageview.setImageResource(R.drawable.opus);
            }else if(type.equals("wav")){
                file_type_imageview.setImageResource(R.drawable.wav);
            }

            if(file_name.length()>20){
                file_name_txt.setText(file_name.substring(0,20)+"..."+type);
            }else{
                file_name_txt.setText(file_name);
            }
            modified_date_txt.setText(modified_date);
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



    class uploadfile extends AsyncTask<Void,Void,Void> {

        String path;
        uploadfile(String path){
            this.path=path;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            boolean isGroup=extras.getBoolean("isGroup");
            String color=extras.getString("color");

            String receiver_id=extras.getString("receiver_id");
            String msg=extras.getString("sender_name") + "";
            String notificatioMessage=extras.getString("sender_name") + " : uploaded a new file";
            if(isGroup) {
                receiver_id=extras.getString("group_id");
                if (color.length() > 0) {
                    msg= "<font color='" + color + "'>" + extras.getString("sender_name") + "</font>" + "";

                } else {
                    msg=extras.getString("sender_name") + "";
                }
            }
            String groupUsers="";
            if(isGroup){
                groupUsers=extras.getString("group_users");
            }
            StoreDataToRemoteServer.uploadFile(extras.getString("sender_id"),receiver_id,path,context,msg,
                    notificatioMessage,groupUsers);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            boolean isGroup=extras.getBoolean("isGroup");
            isOwner=extras.getBoolean("isOwner");
            progressBar.setVisibility(View.INVISIBLE);
            JSONObject jsonObject=new JSONObject();
            try {
                if(isGroup){
                    application app=(application) context.getApplicationContext();
                    jsonObject.put("sender_id",app.getUser_id());
                    jsonObject.put("sender_name",app.getUsername());
                    jsonObject.put("group_id",extras.getString("group_id"));
                    jsonObject.put("group_name",extras.getString("group_name"));
                    jsonObject.put("isGroup",true);
                    jsonObject.put("group_users",extras.getString("group_users"));
                    jsonObject.put("isOwner",isOwner);
                    jsonObject.put("message",app.getUsername()+" "+" uploaded a new file");
                    mSocket.emit("file_notification_broadcast",jsonObject);
                }else{
                    application app=(application) context.getApplicationContext();
                    jsonObject.put("sender_id",app.getUser_id());
                    jsonObject.put("sender_name",app.getUsername());
                    jsonObject.put("receiver_id",extras.getString("receiver_id"));
                    jsonObject.put("receiver_name",extras.getString("receiver_name"));
                    jsonObject.put("isGroup",false);
                    jsonObject.put("message",app.getUsername()+" "+" uploaded a new file");
                    mSocket.emit("file_notification_broadcast",jsonObject);

                }

                //context.startActivity(new Intent(context,chat.class).putExtras(extras));

                application app=(application) context.getApplicationContext();
                app.setFileUplaoded(true);
                activity.finish();
            }catch(Exception e){

            }
        }
    }
}
