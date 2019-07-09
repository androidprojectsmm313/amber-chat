package com.app.amber.chat;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.app.amber.chat.DATABASE_OPERATIONS.AppDatabase;
import com.app.amber.chat.DATABASE_OPERATIONS.schema.user;
import com.app.amber.chat.JobSchduler.service_watcher;
import com.app.amber.chat.service.socket_events_listener;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.mklimek.sslutilsandroid.SslUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoTrack;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    EditText user_name,password;Button register,login;
    private SurfaceViewRenderer localView,remoteView;
    AudioTrack localAudioTrack;VideoTrack localVideoTrack;
    private Socket mSocket;
    PeerConnection  localPeer;
    PeerConnection remotePeer;
    boolean isOfferAdded=false;
    JobManager mJobManager;
    ProgressBar progressBar;
    RelativeLayout relativeLayout;
    /*{
        try {
            mSocket = IO.socket("http://192.168.8.104:3000");
        } catch (URISyntaxException e) {}
    }*/
    PeerConnectionFactory peerConnectionFactory;
    SurfaceViewRenderer remoteVideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user_name=(EditText) findViewById(R.id.user_name);
        password=(EditText) findViewById(R.id.password);
        //register=(Button) findViewById(R.id.register);
        login=(Button) findViewById(R.id.login);
        progressBar=(ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        relativeLayout=(RelativeLayout) findViewById(R.id.relative_layout);
        application app=(application) getApplicationContext();
        mSocket=app.getmSocket();
        relativeLayout.setVisibility(View.INVISIBLE);
        mJobManager = JobManager.instance();







        mJobManager.cancelAllForTag(service_watcher.TAG);

        app.setUserArrayList(new ArrayList<com.app.amber.chat.pojo.user>());

        System.out.println("signing certificate");
        /*try {
            signCertificate();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }*/

        String filepath = getApplicationContext().getExternalCacheDir().getAbsolutePath();
        File file = new File(filepath,"/recordings");
        deleteAllRecoridngs(file);


        Handler h=new Handler();
        Runnable r=new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(mSocket!=null)
                        {
                            mSocket.off();
                            mSocket.off("validate_user_response");
                            mSocket.off("register_user_response");
                            mSocket.off("ready_for_call");
                            mSocket.off("message_recieved_notification");
                            mSocket.disconnect();
                            mSocket.connect();
                            mSocket.on("validate_user_response", handleValidateResponse);
                            mSocket.on("register_user_response", handleRegisterResponse);
                        }


                        final AppDatabase db=AppDatabase.getAppDatabase(getApplicationContext());


                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // this code will be executed after 2 seconds
                                AppDatabase db=AppDatabase.getAppDatabase(getApplicationContext());
                                String uniqueID = UUID.randomUUID().toString();

                                final ArrayList<user> usersList=new ArrayList<>(db.applicationDao().getAllUsers());
                                if(usersList.size()>0) {
                                    final String id = usersList.get(0).threadId;
                                    final String username = usersList.get(0).user_name;

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isMyServiceRunning(socket_events_listener.class, getApplicationContext())) {
                                                stopService(new Intent(getApplicationContext(), socket_events_listener.class));
                                            }
                                            // startService(new Intent(getApplicationContext(),socket_events_listener.class));
                                            int restartService = new JobRequest.Builder(service_watcher.TAG)
                                                    .startNow()
                                                    .build()
                                                    .schedule();
                                            progressBar.setVisibility(View.INVISIBLE);
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                            login.setClickable(true);
                                            //register.setClickable(true);
                                            application app = (application) getApplicationContext();
                                            app.setUser_id(id);
                                            app.setUsername(username);



                                            String firebase_token=app.getFirebase_token();
                                            JSONObject jsonObject=new JSONObject();
                                            try {
                                                jsonObject.put("user_id",id);
                                                jsonObject.put("username",username);
                                                if(firebase_token!=null){
                                                    jsonObject.put("firebase_token",firebase_token);

                                                    if(!mSocket.connected()){
                                                        //mSocket.disconnect();

                                                        mSocket.off("validate_user_response");
                                                        mSocket.off("register_user_response");
                                                        mSocket.off("ready_for_call");
                                                        mSocket.off("message_recieved_notification");

                                                        //mSocket.connect();
                                                    }
                                                    mSocket.emit("update_firebase_token",jsonObject);
                                                }else{
                                                    System.out.println("fiebase token is null "+firebase_token);
                                                }

                                                if(!mSocket.connected()){
                                                    //mSocket.disconnect();
                                                    mSocket.off("validate_user_response");
                                                    mSocket.off("register_user_response");
                                                    mSocket.off("ready_for_call");
                                                    mSocket.off("message_recieved_notification");

                                                    //mSocket.connect();
                                                }
                                                mSocket.emit("update_status",jsonObject);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }


                                            startActivity(new Intent(MainActivity.this,
                                                    users.class).putExtra("username", username).putExtra("id", id)
                                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                            finish();
                                        }
                                    });
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            relativeLayout.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            }
                        }, 50);
                        // app.setHandleReadyForCall("abdullah");

        /*startActivity(new Intent(getApplicationContext(),video_call.class).putExtra("ownId","hamza")
                .putExtra("otherId","abdullah").putExtra("isCaller",true));*/


       /* register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mSocket.connected()) {
                    progressBar.setVisibility(View.VISIBLE);
                    login.setClickable(false);
                    register.setClickable(false);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    if (user_name.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please enter username", Toast.LENGTH_LONG).show();
                    } else if (password.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_LONG).show();
                    } else {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("username", user_name.getText().toString());
                            jsonObject.put("password", password.getText().toString());
                            mSocket.emit("register_user", jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //startActivity(new Intent(MainActivity.this,chat.class).putExtra("user_name",user_name.getText().toString()));
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Plase try again later.Not connected with network!", Toast.LENGTH_SHORT).show();
                }
            }
        });*/

                        login.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mSocket.connected()) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    login.setClickable(false);
                                    //register.setClickable(false);

                                    if (user_name.getText().toString().isEmpty()) {
                                        Toast.makeText(getApplicationContext(), "Pleas enter username", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        login.setClickable(true);
                                    } else if (password.getText().toString().isEmpty()) {
                                        Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        login.setClickable(true);

                                    } else {

                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put("username", user_name.getText().toString());
                                            jsonObject.put("password", password.getText().toString());

                                            if(!mSocket.connected()){
                                                //mSocket.connect();
                                            }
                                            mSocket.emit("validate_user", jsonObject);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        //startActivity(new Intent(MainActivity.this,chat.class).putExtra("user_name",user_name.getText().toString()));
                                    }
                                }
                                else{
                                   //mSocket.connect();
                                    Toast.makeText(getApplicationContext(), "Plase try again later.Not connected with network", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    login.setClickable(true);

                                }
                            }
                        });


                    }
                });


            }
        };
        h.postDelayed(r,50);
    }




    private Emitter.Listener handleValidateResponse = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(data.getBoolean("success")){


                                JSONObject user=data.getJSONObject("user");
                                final String id=user.getString("id");
                                final String username=user.getString("username");
                                application app=(application) getApplicationContext() ;
                                app.setUser_id(id);

                                String firebase_token=app.getFirebase_token();
                                JSONObject jsonObject=new JSONObject();
                                jsonObject.put("user_id",id);
                                jsonObject.put("username",username);
                                if(firebase_token!=null){
                                    jsonObject.put("firebase_token",firebase_token);

                                    if(!mSocket.connected()){
                                       // mSocket.connect();
                                    }
                                    mSocket.emit("update_firebase_token",jsonObject);
                                }else{
                                    System.out.println("fiebase token is null "+firebase_token);
                                }

                                if(!mSocket.connected()){
                                    //mSocket.connect();
                                }
                                mSocket.emit("update_status",jsonObject);
                                Toast.makeText(getApplicationContext(),"Valid user",Toast.LENGTH_LONG).show();

                                if(isMyServiceRunning(socket_events_listener.class,getApplicationContext())){
                                    stopService(new Intent(getApplicationContext(),socket_events_listener.class));
                                }


                                //app.setHandleReadyForCall(user_name.getText().toString());

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        // this code will be executed after 2 seconds
                                        AppDatabase db=AppDatabase.getAppDatabase(getApplicationContext());
                                        String uniqueID = UUID.randomUUID().toString();

                                        ArrayList<user> usersList=new ArrayList<>(db.applicationDao().getAllUsers());
                                        if(usersList.size()>0) {
                                            db.applicationDao().deleteAllUsers(usersList);
                                        }
                                            db.applicationDao().insertUser(new user(user_name.getText().toString(),password.getText().toString(),id));
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(isMyServiceRunning(socket_events_listener.class,getApplicationContext())){
                                                        stopService(new Intent(getApplicationContext(),socket_events_listener.class));
                                                    }
                                                    // startService(new Intent(getApplicationContext(),socket_events_listener.class));
                                                    int restartService = new JobRequest.Builder(service_watcher.TAG)
                                                            .startNow()
                                                            .build()
                                                            .schedule();
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                    login.setClickable(true);
                                                    //register.setClickable(true);
                                                    application app=(application)getApplicationContext();
                                                    app.setUser_id(id);
                                                    app.setUsername(username);
                                                    startActivity(new Intent(MainActivity.this,
                                                            users.class).putExtra("username",username).putExtra("id",id));
                                                    finish();

                                                }
                                            });
                                    }
                                }, 100);




                            }else{
                                Toast.makeText(getApplicationContext(),data.getString("err_msg"),Toast.LENGTH_LONG).show();
                                login.setClickable(true);
                                //register.setClickable(true);

                                progressBar.setVisibility(View.INVISIBLE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(),"Invalid username or password",Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            login.setClickable(true);
                            //register.setClickable(true);

                            e.printStackTrace();
                        }

                    }
                });

            } catch (Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                System.out.println("json exception = "+e.toString());
            }
        }
    };
    private Emitter.Listener handleRegisterResponse = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
               // final String message = data.getString("message");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            if(data.getBoolean("success")){


                                JSONObject user=data.getJSONObject("user");
                                final String id=user.getString("id");
                                final String username=user.getString("username");

                                application app=(application) getApplicationContext();
                                String firebase_token=app.getFirebase_token();
                                JSONObject jsonObject=new JSONObject();
                                jsonObject.put("user_id",id);
                                jsonObject.put("username",username);
                                if(firebase_token!=null){
                                    jsonObject.put("firebase_token",firebase_token);


                                    if(!mSocket.connected()){
                                        //mSocket.connect();
                                    }
                                    mSocket.emit("update_firebase_token",jsonObject);
                                }else{
                                    System.out.println("fiebase token is null "+firebase_token);
                                }

                                if(!mSocket.connected()){
                                    //mSocket.connect();
                                }
                                mSocket.emit("update_status",jsonObject);
                                Toast.makeText(getApplicationContext(),"user registered succesfully",Toast.LENGTH_LONG).show();

                                final AppDatabase db=AppDatabase.getAppDatabase(getApplicationContext());
                                final String uniqueID = UUID.randomUUID().toString();




                                //app.setHandleReadyForCall(user_name.getText().toString());
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        // this code will be executed after 2 seconds
                                        ArrayList<user> usersList=new ArrayList<>(db.applicationDao().getAllUsers());
                                        if(usersList.size()>0) {
                                            db.applicationDao().deleteAllUsers(usersList);
                                        }
                                            db.applicationDao().insertUser(new user(user_name.getText().toString(),password.getText().toString(),id));



                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    if(isMyServiceRunning(socket_events_listener.class,getApplicationContext())){
                                                        stopService(new Intent(getApplicationContext(),socket_events_listener.class));
                                                    }

                                                    //startService(new Intent(getApplicationContext(),socket_events_listener.class));

                                                    int restartService = new JobRequest.Builder(service_watcher.TAG)
                                                            .startNow()
                                                            .build()
                                                            .schedule();
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                                    login.setClickable(true);
                                                    //register.setClickable(true);

                                                    application app=(application)getApplicationContext();
                                                    app.setUser_id(id);
                                                    app.setUsername(username);

                                                    startActivity(new Intent(MainActivity.this,users.class)
                                                            .putExtra("username",username).putExtra("id",id));
                                                    finish();

                                                }
                                            });
                                    }
                                }, 100);



                            }else{
                                Toast.makeText(getApplicationContext(),"user name already exist",Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                login.setClickable(true);
                                //register.setClickable(true);

                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(),"error occurs while registering user",Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            login.setClickable(true);
                            //register.setClickable(true);

                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
                progressBar.setVisibility(View.INVISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                login.setClickable(true);
                //register.setClickable(true);

            }
        }
    };




    @Override
    protected void onResume() {
        super.onResume();


        if(mSocket!=null){
            //mSocket.connect();
        }
        /*if(mSocket!=null){
            mSocket.disconnect();
            mSocket.connect();
        }*/
    }



    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        videoCapturer = createCameraCapturer(new Camera1Enumerator(false));
        return videoCapturer;
    }
    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // Trying to find a front facing camera!
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // We were not able to find a front cam. Look for other cameras
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }


    private boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    void deleteAllRecoridngs(File fileOrDirectory) {
        if(fileOrDirectory.exists() && fileOrDirectory.listFiles()!=null) {
            if (fileOrDirectory.isDirectory())
                for (File child : fileOrDirectory.listFiles())
                    deleteAllRecoridngs(child);
            fileOrDirectory.delete();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void signCertificate() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, KeyManagementException {

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = getResources().openRawResource(
                getResources().getIdentifier("spylatest",
                        "raw", getPackageName()));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

// Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);



// Example send http request
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://192.168.42.169:3000");
                    HttpsURLConnection urlConnection =
                            (HttpsURLConnection)url.openConnection();
                    urlConnection.setSSLSocketFactory(context.getSocketFactory());
                    InputStream in = urlConnection.getInputStream();


                    System.out.println("reponse from server  "+convertStreamToString(in));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        },50);

      /*  CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509");

// From https://www.washington.edu/itconnect/security/ca/load-der.crt
        InputStream caInput = getResources().openRawResource(R.raw.server);

        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

// Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return hv.verify("https://192.168.42.71:3000", session);
                }
            };

// Tell the URLConnection to use a SocketFactory from our SSLContext
        URL url = new URL("https://192.168.42.71:3000/");
            final HttpsURLConnection urlConnection =
                    (HttpsURLConnection)url.openConnection();
            urlConnection.setHostnameVerifier(hostnameVerifier);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    InputStream in = urlConnection.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        },50);
        //copyInputStreamToOutputStream(in, System.out);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }*/
    }


    String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
