package com.app.amber.chat;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.app.amber.chat.pojo.files;
import com.github.nkzawa.socketio.client.Socket;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class file_manager extends AppCompatActivity {
    ProgressBar progressBar;

    ListView listView;
    Bundle extras;
    Socket mSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        listView=(ListView) findViewById(R.id.list_view);
        extras=getIntent().getExtras();


        progressBar=(ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        application app=(application) getApplicationContext();
        String select_file_type=app.getSelect_file_type();
        ArrayList<files> filesArrayList=app.getFilesArrayList();
        mSocket=app.getmSocket();
        ArrayList<files> selectedFilesArrayList=getFilesListByType(filesArrayList,select_file_type);
        files_manager_adapter customAdapter = new files_manager_adapter(file_manager.this, selectedFilesArrayList,extras,mSocket,progressBar,file_manager.this);
        listView.setAdapter(customAdapter);

    }


    ArrayList<files> getFilesListByType(ArrayList<files> filesArrayList,String type){
        ArrayList<files> tempArrayList=new ArrayList<>();
        for(int i=0;i<filesArrayList.size();i++){
            if(filesArrayList.get(i).getMain_type().equals(type)){
                tempArrayList.add(filesArrayList.get(i));
            }
        }
        return tempArrayList;
    }


    /*ArrayList<files> getFilesList(String rootPath) {
        ArrayList<files> fileList = new ArrayList();
        try {
            File rootFolder = new File(rootPath);
            File[] files2 = rootFolder.listFiles();
            int length = files2.length;
            int i = 0;
            while (i < length) {
                File file;
                File file2 = files2[i];
                if (file2.isDirectory()) {
                    if (getFilesList(file2.getAbsolutePath()) == null) {
                        file = rootFolder;
                        break;
                    }
                    fileList.addAll(getFilesList(file2.getAbsolutePath()));
                    file = rootFolder;
                } else {
                    String name = file2.getName();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(".");
                    if (name.endsWith(".jpg")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "jpg", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    } else if (name.endsWith(".png")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "png", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".jpeg")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "jpeg", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".docx")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "docx", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".xls")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "xls", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".xlsx")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "xls", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".ppt")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "ppt", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".pdf")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "pdf", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".doc")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "doc", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".txt")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "txt", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".csv")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "csv", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".aac")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "aac", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".amr")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "amr", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".m4a")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "m4a", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".opus")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "opus", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }else if (name.endsWith(".wav")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "wav", stringBuilder2.toString(), new Date(file2.lastModified()).toString()));
                    }  else {
                        file = rootFolder;
                    }
                }
                i++;
                rootFolder = file;
                String str2 = rootPath;
            }
            return fileList;
        } catch (Exception e) {
            Exception e2 = e;
            PrintStream printStream = System.out;
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("exception = ");
            stringBuilder3.append(e2);
            printStream.println(stringBuilder3.toString());
            return null;
        }
    }*/






}
