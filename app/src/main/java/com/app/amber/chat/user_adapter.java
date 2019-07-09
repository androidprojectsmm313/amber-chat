package com.app.amber.chat;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.app.amber.chat.pojo.user;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class user_adapter implements ListAdapter {
    ArrayList<user> arrayList;

    String username,id;
    Context context;
    public user_adapter(Context context, ArrayList<user> arrayList,String username,String id) {
        this.arrayList=arrayList;
        this.username=username;this.id=id;
        this.context=context;
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
        final boolean isGroup=arrayList.get(position).isGroup();

        final application app=(application) context;

        if(convertView==null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView=layoutInflater.inflate(R.layout.user_item, null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            final TextView tittle=convertView.findViewById(R.id.text);
            final ImageView image_status=convertView.findViewById(R.id.status_image);
            if(status){
                image_status.setImageResource(R.drawable.online);
            }else{
                image_status.setImageResource(R.drawable.offline);
            }


            if(isGroup){
                image_status.setVisibility(View.INVISIBLE);
                tittle.setText(message);
            }else{
                JSONObject jsonObject=app.getMessagesCount();
                if(!jsonObject.isNull(arrayList.get(position).getId())){
                    try {
                        if(jsonObject.getInt(arrayList.get(position).getId())>0){
                            tittle.setText(message+" ("+jsonObject.getInt(arrayList.get(position).getId())+")");
                        }else{
                            tittle.setText(message);
                        }
                    } catch (JSONException e) {
                        tittle.setText(message);
                        e.printStackTrace();
                    }
                }else{
                    tittle.setText(message);
                }
            }
            tittle.setTag(position);
            tittle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index= (int) v.getTag();
                    user u=arrayList.get(index);
                    System.out.println("item clcked = "+v.getTag());
                    if(u.isGroup()){
                        String groupUsers = new Gson().toJson(u.getGroup_users());
                        context.startActivity(new Intent(context,chat.class).putExtra("sender_id",app.getUser_id()).putExtra("sender_name",app.getUsername())
                                .putExtra("group_id",arrayList.get(index).getId()).putExtra("group_name",arrayList.get(index).getUsername())
                                .putExtra("isGroup",u.isGroup()).putExtra("group_users",groupUsers).putExtra("isOwner",u.isOwner())
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        //context.finish();

                    }else{
                        context.startActivity(new Intent(context,chat.class).putExtra("sender_id",app.getUser_id()).putExtra("sender_name",app.getUsername())
                                .putExtra("receiver_id",arrayList.get(index).getId()).putExtra("receiver_name",arrayList.get(index).getUsername())
                        .putExtra("isGroup",u.isGroup()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
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
}
