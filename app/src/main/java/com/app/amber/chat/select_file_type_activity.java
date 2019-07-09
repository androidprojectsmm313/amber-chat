package com.app.amber.chat;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.amber.chat.pojo.files;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class select_file_type_activity extends AppCompatActivity {

    Bundle extras;
    ListView listView;
    ProgressBar progressBar;
    TextView loader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file_type_activity);
        loader=(TextView) findViewById(R.id.loader);
        listView=(ListView) findViewById(R.id.list_view);
        application app=(application) getApplicationContext();
        progressBar=(ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        loader.setVisibility(View.VISIBLE);

        if(!app.isFileUplaoded) {
           new getFilesListTask().execute();
        }else{
            finish();
        }

    }


    int getFilesListCountByType(ArrayList<files> filesArrayList,String type){
        int count=0;
        for(int i=0;i<filesArrayList.size();i++){
            if(filesArrayList.get(i).getMain_type().equals(type)){
                count++;
            }
        }
        return count;
    }


    ArrayList<files> getFilesList(String rootPath,SimpleDateFormat dateFormat) {
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
                    if (getFilesList(file2.getAbsolutePath(),dateFormat) == null) {
                        file = rootFolder;
                        break;
                    }
                    fileList.addAll(getFilesList(file2.getAbsolutePath(),dateFormat));
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
                                "jpg", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Images"));
                    } else if (name.endsWith(".png")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "png", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Images"));
                    }else if (name.endsWith(".jpeg")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "jpeg", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Images"));
                    }else if (name.endsWith(".docx")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "docx", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Documents"));
                    }else if (name.endsWith(".xls")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "xls", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Documents"));
                    }else if (name.endsWith(".xlsx")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "xls", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Documents"));
                    }else if (name.endsWith(".ppt")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "ppt", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Documents"));
                    }else if (name.endsWith(".pdf")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "pdf", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Documents"));
                    }else if (name.endsWith(".doc")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "doc", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Documents"));
                    }else if (name.endsWith(".txt")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "txt", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Documents"));
                    }else if (name.endsWith(".csv")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "csv", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Documents"));
                    }else if (name.endsWith(".aac")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "aac", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Audios"));
                    }else if (name.endsWith(".amr")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "amr", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Audios"));
                    }else if (name.endsWith(".m4a")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "m4a", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Audios"));
                    }else if (name.endsWith(".opus")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "opus", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Audios"));
                    }else if (name.endsWith(".wav")) {
                        String absolutePath = file2.getAbsolutePath();
                        String name2 = file2.getName();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Integer.parseInt(String.valueOf(file2.length() / 1024)));
                        stringBuilder2.append("");
                        file = rootFolder;
                        String uniqueID = UUID.randomUUID().toString();
                        fileList.add(new files(absolutePath, name2,
                                "wav", stringBuilder2.toString(), dateFormat.format(new Date(file2.lastModified())).toString(),"Audios"));
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
    }
     class getFilesListTask extends AsyncTask<Void,Void,Void>{
         ArrayList<String> arrayList;
         ArrayList<Integer> countArrayList;
         ArrayList<files> filesArrayList;
         application app;
         @Override
         protected Void doInBackground(Void... voids) {

             app=(application)getApplicationContext();
             arrayList = new ArrayList<>();
             countArrayList = new ArrayList<>();

             extras = getIntent().getExtras();
             SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");


             filesArrayList = getFilesList(Environment.getExternalStorageDirectory() + "", dateFormat);

             app.setFilesArrayList(filesArrayList);


             countArrayList.add(getFilesListCountByType(filesArrayList, "Documents"));
             countArrayList.add(getFilesListCountByType(filesArrayList, "Images"));
             countArrayList.add(getFilesListCountByType(filesArrayList, "Audios"));

             arrayList.add("Documents");
             arrayList.add("Images");
             arrayList.add("Audios");

             return null;
         }

         @Override
         protected void onPostExecute(Void aVoid) {
             super.onPostExecute(aVoid);

             select_file_type_adapter customAdapter = new select_file_type_adapter(select_file_type_activity.this, arrayList, countArrayList, extras, select_file_type_activity.this);
             listView.setAdapter(customAdapter);
             progressBar.setVisibility(View.INVISIBLE);
             loader.setVisibility(View.INVISIBLE);

         }
     }

    @Override
    protected void onResume() {
        super.onResume();
        application app=(application) getApplicationContext();
        if(app.isFileUplaoded){
            finish();
        }

    }
}
