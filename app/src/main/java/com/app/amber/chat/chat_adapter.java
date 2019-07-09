package com.app.amber.chat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.amber.chat.UTILITY.StoreDataToRemoteServer;
import com.app.amber.chat.pojo.message;
import com.github.nkzawa.socketio.client.Socket;
import com.rygelouv.audiosensei.player.AudioSenseiPlayerView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



class chat_adapter implements ListAdapter {
    ArrayList<message> arrayList;
    Activity activity;
    Context context;
    Socket mSocket;
    public chat_adapter(Context context, ArrayList<message> arrayList,Activity activity,Socket mSocket) {
        this.arrayList=arrayList;
        this.context=context;
        this.activity=activity;
        this.mSocket=mSocket;
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
        String message=arrayList.get(position).getMessage();
        String type=arrayList.get(position).getType();
        String path=arrayList.get(position).getPath();
        String dateCreated=arrayList.get(position).getDate_created();
        boolean is_sender=arrayList.get(position).isIs_sender();
        boolean status=arrayList.get(position).getIs_read();

        if(convertView==null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);

            if(is_sender){
                convertView=layoutInflater.inflate(R.layout.chat_item_sender, null);

            }else{
                convertView=layoutInflater.inflate(R.layout.chat_item_receiver, null);
            }

            RelativeLayout file_layout=(RelativeLayout)convertView.findViewById(R.id.file_layout);
            RelativeLayout msg_layout=(RelativeLayout)convertView.findViewById(R.id.msg_layout);
            RelativeLayout audio_layout=(RelativeLayout)convertView.findViewById(R.id.audio_layout);
            TextView tittle=convertView.findViewById(R.id.message_text);
            TextView date_created=convertView.findViewById(R.id.date);
            TextView sender=convertView.findViewById(R.id.sender);
            TextView file_date=convertView.findViewById(R.id.file_date);

            final ImageButton delete_text=convertView.findViewById(R.id.delete_text);
            final ImageButton delete_file=convertView.findViewById(R.id.delete_file);
            final ImageButton delete_audio=convertView.findViewById(R.id.delete_audio);


            file_layout.setTag(position);
            msg_layout.setTag(position);
            audio_layout.setTag(position);


            if(arrayList.get(position).isIs_sender()){
                delete_text.setTag(position);
                delete_file.setTag(position);
                delete_audio.setTag(position);

                delete_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos=(Integer) v.getTag();
                        String id=arrayList.get(pos).getId();
                        System.out.println("delete msg with id  = "+id);
                        JSONObject jsonObject=new JSONObject();
                        try {
                            jsonObject.put("position", pos);
                            jsonObject.put("msg_id", id);
                            mSocket.emit("delete_message",jsonObject );
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });


                delete_audio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos=(Integer) v.getTag();
                        String id=arrayList.get(pos).getId();
                        System.out.println("delete msg with id  = "+id);
                        JSONObject jsonObject=new JSONObject();
                        try {
                            jsonObject.put("position", pos);
                            jsonObject.put("msg_id", id);
                            mSocket.emit("delete_message",jsonObject );
                        }catch (JSONException e){
                            e.printStackTrace();
                        }                }
                });

                delete_file.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos=(Integer) v.getTag();
                        String id=arrayList.get(pos).getId();
                        System.out.println("delete msg with id  = "+id);
                        JSONObject jsonObject=new JSONObject();
                        try {
                            jsonObject.put("position", pos);
                            jsonObject.put("msg_id", id);
                            mSocket.emit("delete_message",jsonObject );
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });


            }
            file_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos=(Integer) v.getTag();
                    if(arrayList.get(pos).isIs_sender())
                    delete_file.setVisibility(View.VISIBLE);
                    return true;
                }
            });


            msg_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos=(Integer) v.getTag();
                    if(arrayList.get(pos).isIs_sender())
                        delete_text.setVisibility(View.VISIBLE);
                    return true;
                }
            });

            audio_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos=(Integer) v.getTag();
                    if(arrayList.get(pos).isIs_sender())
                        delete_audio.setVisibility(View.VISIBLE);
                    return true;
                }
            });





            TextView audio_sender=convertView.findViewById(R.id.audio_sender);
            TextView audio_date=convertView.findViewById(R.id.audio_date);
            AudioSenseiPlayerView audioSenseiPlayerView=convertView.findViewById(R.id.audio_player);

            ImageView imageview=convertView.findViewById(R.id.imageview);
            final ImageView download=convertView.findViewById(R.id.download);

            file_layout.setVisibility(View.GONE);
            msg_layout.setVisibility(View.GONE);
            audio_layout.setVisibility(View.GONE);
            msg_layout.setTag(position);
            date_created.setText(dateCreated);

            if(type==null || type.equals("location") || type.equals("safecity") && message!=null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tittle.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    tittle.setText(Html.fromHtml(message));
                }
                if (is_sender){
                    ImageView status_msg = (ImageView) convertView.findViewById(R.id.status_msg);
                    if (status) {
                        status_msg.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_read_black_24dp));
                    } else {
                        status_msg.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_unread_black_24dp));
                    }
            }
                msg_layout.setVisibility(View.VISIBLE);
            }else{
                file_layout.setVisibility(View.VISIBLE);
                imageview.setTag(path);
                download.setTag(path);
                file_date.setText(dateCreated);
                ImageView status_file = (ImageView) convertView.findViewById(R.id.status_file);
                if(is_sender) {
                    if (status) {
                        status_file.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_read_black_24dp));
                    } else {
                        status_file.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_unread_black_24dp));
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    sender.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    sender.setText(Html.fromHtml(message));
                }
               // sender.setText(message);

                if(type.equals("image")){
                    System.out.println("path = "+path);
                    Picasso.get().load(path).into(imageview, new Callback() {
                        @Override
                        public void onSuccess() {
                            System.out.println("image loaded successfully");
                        }

                        @Override
                        public void onError(Exception e) {
                            System.out.println("exception "+e.toString());
                        }
                    });

                    //imageview.setImageResource(R.drawable.ic_image_black_24dp);
                }else if(type.equals("document")){
                    imageview.setImageResource(R.drawable.docx);
                }else if(type.equals("powerpoint")){
                    imageview.setImageResource(R.drawable.ppt);
                }else if(type.equals("xls")){
                    imageview.setImageResource(R.drawable.xls);
                }else if(type.equals("text")){
                    imageview.setImageResource(R.drawable.txt);
                }else if(type.equals("pdf")){
                    imageview.setImageResource(R.drawable.pdf);
                }else if(type.equals("csv")){
                    imageview.setImageResource(R.drawable.csv);
                }else if(type.equals("aac")){
                    imageview.setImageResource(R.drawable.aac);
                }else if(type.equals("amr")){
                    imageview.setImageResource(R.drawable.amr);
                }else if(type.equals("m4a")){
                    imageview.setImageResource(R.drawable.m4a);
                }else if(type.equals("opus")){
                    imageview.setImageResource(R.drawable.opus);
                }else if(type.equals("wav")){
                    imageview.setImageResource(R.drawable.wav);
                }else if(type.equals("mp3")){
                    file_layout.setVisibility(View.GONE);

                    if(is_sender){
                    ImageView status_audio=(ImageView) convertView.findViewById(R.id.status_audio);
                    if(status){
                        status_audio.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_read_black_24dp));
                    }else{
                        status_audio.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_unread_black_24dp));
                    }}

                    if(is_sender)
                    status_file.setVisibility(View.GONE);

                    audio_layout.setVisibility(View.VISIBLE);
                    audio_date.setText(dateCreated);
                    System.out.println("path = "+path);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        audio_sender.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT));
                    } else {
                        audio_sender.setText(Html.fromHtml(message));
                    }
                    audioSenseiPlayerView.setAudioTarget(path);


                }
            }


            msg_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos=(int)v.getTag();
                    if(arrayList.get(pos).getType()!=null){
                        if(arrayList.get(pos).getType().equals("location")){
                            context.startActivity(new Intent(context,display_urls.class).putExtra("url",
                                    "http://3.210.76.112:3001/static/"+arrayList.get(pos).getId()+".html"));
                        }else if(arrayList.get(pos).getType().equals("safecity")){
                            String m=arrayList.get(pos).getMessage();
                            String[] splitted=arrayList.get(pos).getMessage().split(" ");
                            if(arrayList.get(pos).getMessage().split(" ").length>=3) {
                                if(!m.split(" ")[3].trim().equals("")){
                                    context.startActivity(new Intent(context, display_urls.class).putExtra("url",
                                            "http://3.210.76.112:3001/static/" + arrayList.get(pos).getMessage().split(" ")[3] + ".html"));
                                }else{
                                    context.startActivity(new Intent(context, display_urls.class).putExtra("url",
                                            "http://3.210.76.112:3001/static/" + arrayList.get(pos).getMessage().split(" ")[5] + ".html"));

                                }

                                System.out.println("safecity" + arrayList.get(pos).getMessage().split(" ")[3]);
                            }
                            }
                    }

                    System.out.println("clicked = "+arrayList.get((int)v.getTag()).getType()+arrayList.get((int)v.getTag()).getId());

                }
            });

            imageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("image click = "+v.getTag());
                    openLinkInBrowser(v.getTag().toString());
                }
            });


            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("dowload clicked = "+v.getTag().toString());


                    final int PERMISSION_ALL = 2;
                    final String[] PERMISSIONS = {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    };

                    if(!chat.hasPermissions(context, PERMISSIONS)){
                        ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
                    }else{
                        String path=v.getTag().toString();
                        new StoreDataToRemoteServer.DownloadFileFromURL(context,v.getTag().toString(),
                                path.split("/")[path.split("/").length-1]).execute(v.getTag().toString());
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

    public void openLinkInBrowser(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);

    }



    public String convertToHtml(String htmlString) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<![CDATA[");
        stringBuilder.append(htmlString);
        stringBuilder.append("]]>");
        return stringBuilder.toString();
    }

}
