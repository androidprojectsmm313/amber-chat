package com.app.amber.chat;

import android.app.Application;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.format.Formatter;

import com.app.amber.chat.JobSchduler.jobs_creator;
import com.app.amber.chat.UTILITY.StoreDataToRemoteServer;
import com.app.amber.chat.pojo.files;
import com.app.amber.chat.pojo.user;
import com.evernote.android.job.JobManager;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



public class application extends Application {


    public boolean isFileUplaoded() {
        return isFileUplaoded;
    }

    public void setFileUplaoded(boolean fileUplaoded) {
        isFileUplaoded = fileUplaoded;
    }

    boolean isFileUplaoded;
    public String getSelect_file_type() {
        return select_file_type;
    }

    public void setSelect_file_type(String select_file_type) {
        this.select_file_type = select_file_type;
    }

    String select_file_type;

    public ArrayList<files> getFilesArrayList() {
        return filesArrayList;
    }

    public void setFilesArrayList(ArrayList<files> filesArrayList) {
        this.filesArrayList = filesArrayList;
    }

    ArrayList<files> filesArrayList;
    public ArrayList<user> getUserArrayList() {
        return userArrayList;
    }

    public void setUserArrayList(ArrayList<user> userArrayList) {
        this.userArrayList = userArrayList;
    }

    ArrayList<user> userArrayList;

    public JSONObject getMessagesCount() {
        return messagesCount;
    }

    public void setMessagesCount(JSONObject messagesCount) {
        this.messagesCount = messagesCount;
    }

    JSONObject messagesCount;
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFirebase_token() {
        return firebase_token;
    }

    public void setFirebase_token(String firebase_token) {
        this.firebase_token = firebase_token;
    }


    String firebase_token;
    String user_id;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    String username;
//http://10.86.57.182:3000
    public Socket getmSocket() {
        return mSocket;
    }


    public void setmSocket(Socket mSocket) {
        this.mSocket = mSocket;
    }

    private Socket mSocket;
    {
        try {
            final IO.Options socketOptions = new IO.Options();

            TrustManager[] trustAllCerts= new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[] {};
                }

                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }
            } };
            SSLContext mySSLContext = SSLContext.getInstance("TLS");
            mySSLContext.init(null, trustAllCerts, null);
            HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            socketOptions.sslContext = mySSLContext;
            socketOptions.hostnameVerifier=myHostnameVerifier;
            socketOptions.secure=true;
            socketOptions.reconnection=true;
            //socketOptions.reconnectionDelay=60000;
            //socketOptions.reconnectionDelayMax =5000;
            socketOptions.reconnectionAttempts=999999999;
            mSocket = IO.socket("https://3.210.76.112:3000/",socketOptions);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("firebase notifications application");
        JobManager.create(this).addJobCreator(new jobs_creator());
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        System.out.println("ipaddress = "+ip);

       // mSocket.connect();

        StoreDataToRemoteServer.ServerCommunication();

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);





        /*Handler handler=new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w("firebase notifications", "getInstanceId failed", task.getException());
                                    return;
                                }

                                // Get new Instance ID token
                                String token = task.getResult().getToken();
                                firebase_token=token;

                                // Log and toast
                                Log.d("firebase notifications", token);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("firebase notifications", e.toString());

                    }
                });
            }
        };

        handler.postDelayed(runnable,1000);*/

    }



    public void setHandleReadyForCall(String id){
        mSocket.on("update_message_count_zero"+id, updateCounterToZero);

    }

   /* private Emitter.Listener handleReadyForCall = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                // final String message = data.getString("message");
                System.out.println("readyforcall = "+data);
                startActivity(new Intent(getApplicationContext(),video_call.class).putExtra("ownId",data.getString("id"))
                        .putExtra("otherId",data.getString("ownId")).putExtra("isCaller",false).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };
*/


    @Override
    public void onTerminate() {
        super.onTerminate();
        System.out.println("application terminated");
    }
    private Emitter.Listener updateCounterToZero = new Emitter.Listener(){
        @Override
        public void call(final Object... args){
            final JSONObject data = (JSONObject) args[0];
            try {
                System.out.println("updatecountertozero = "+data);
                final application app=(application)getApplicationContext();
                JSONObject messagesCount=app.getMessagesCount();
                messagesCount.put(data.getString("id"),0);
                app.setMessagesCount(messagesCount);

            } catch (Exception e) {
                System.out.println("json exception = "+e.toString());
            }
        }
    };

}
