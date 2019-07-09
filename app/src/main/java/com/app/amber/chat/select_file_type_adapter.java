package com.app.amber.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.app.amber.chat.pojo.files;

import java.util.ArrayList;


class select_file_type_adapter implements ListAdapter {
    ArrayList<String> arrayList;ArrayList<Integer> countArrayList;
    Context context;
    Activity activity;
    Bundle extras;
    public select_file_type_adapter(final Context context, ArrayList<String> arrayList, ArrayList<Integer> countArrayList, Bundle extras, Activity activity) {
        this.arrayList=arrayList;
        this.context=context;
        this.countArrayList=countArrayList;
        this.activity=activity;
        this.extras=extras;
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
        String item=arrayList.get(position);
        int count=countArrayList.get(position);

        if(convertView==null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView=layoutInflater.inflate(R.layout.select_file_type_item, null);
            LinearLayout linearLayout=(LinearLayout) convertView.findViewById(R.id.main_layout);
            linearLayout.setTag(item);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("clicked = "+v.getTag().toString());
                    application app=(application) context.getApplicationContext();
                    app.setSelect_file_type(v.getTag().toString());
                    context.startActivity(new Intent(context,file_manager.class).putExtras(extras));
                    //activity.finish();
                }
            });
            final TextView type_name=convertView.findViewById(R.id.type);
            final TextView type_count=convertView.findViewById(R.id.count);


            type_name.setText(item);
            type_count.setText(count+"");
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
