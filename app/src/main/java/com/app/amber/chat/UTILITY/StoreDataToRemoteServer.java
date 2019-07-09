package com.app.amber.chat.UTILITY;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class StoreDataToRemoteServer {
    static String responseOfServer;

    private static HttpClient mHttpClient;


    public static void ServerCommunication() {
       /* HttpParams params = new BasicHttpParams();

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("https",
                SSLSocketFactory.getSocketFactory(), 443));
        SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);
*/
       try {
           KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
           trustStore.load(null, null);

           MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
           sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

           HttpParams params = new BasicHttpParams();
           HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
           HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

           SchemeRegistry registry = new SchemeRegistry();
           registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
           registry.register(new Scheme("https", sf, 443));

           ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);


           mHttpClient = new DefaultHttpClient(ccm, params);
       } catch (IOException e) {
           e.printStackTrace();
       } catch (CertificateException e) {
           e.printStackTrace();
       } catch (NoSuchAlgorithmException e) {
           e.printStackTrace();
       } catch (UnrecoverableKeyException e) {
           e.printStackTrace();
       } catch (KeyStoreException e) {
           e.printStackTrace();
       } catch (KeyManagementException e) {
           e.printStackTrace();
       }
    }



    public static void uploadFile(String sender_id, String receiver_id, String path,Context context,String message,String notificationMessage,String groupUsers)
    {
        try {

                System.out.println("original length image "+new File(path).length());
                File f = new File(path); // Your image file
                String file_name=path.substring(path.lastIndexOf("/")+1);

            String uniqueID = sender_id+'_'+receiver_id+'_'+UUID.randomUUID().toString();


            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = context.getResources().openRawResource(
                    context.getResources().getIdentifier("spylatest",
                            "raw", context.getPackageName()));
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
            final SSLContext sslContextcontext = SSLContext.getInstance("TLS");
            sslContextcontext.init(null, tmf.getTrustManagers(), null);



            HttpPost httppost = new HttpPost("https://3.210.76.112:3000/upload/files");
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            multipartEntity.addPart("path", new StringBody(path));
            multipartEntity.addPart("sender_id", new StringBody(sender_id));
            multipartEntity.addPart("receiver_id", new StringBody(receiver_id));
            multipartEntity.addPart("file_name", new StringBody(uniqueID+'_'+file_name));
            multipartEntity.addPart("message", new StringBody(message));
            multipartEntity.addPart("group_users", new StringBody(groupUsers));
            multipartEntity.addPart("notification_message", new StringBody(notificationMessage));
            multipartEntity.addPart("avatar", new FileBody(f));
            httppost.setEntity(multipartEntity);
            mHttpClient.execute(httppost, new FileUploadResponse());
        } catch (Exception e) {
            System.out.println("exception = "+e.toString());
            if (e.getLocalizedMessage().contains("open failed")) {
                System.out.println("filenotfoundexception");
            } else if (e.getLocalizedMessage().contains("Connection to")) {
                System.out.println("connection error");

            }
            Log.e("123", e.getLocalizedMessage());
        }
    }


    private static class FileUploadResponse implements ResponseHandler<Object> {
        private FileUploadResponse() {
        }

        public Object handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            String responseString = EntityUtils.toString(response.getEntity());
            System.out.println("file sended successfilly");
            return responseString;
        }
    }


   public static class DownloadFileFromURL extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;
        Context context;
        String url,file_name;
        public DownloadFileFromURL(Context context,String url,String file_name){
            this.context=context;
            this.url=url;
            this.file_name=file_name;
        }


        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Downloading file. Please wait...");
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... voids) {
            int count;
            try {
                URL url = new URL(this.url);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);


                createDirIfNotExists("downloads");
                // Output stream
                OutputStream output = new FileOutputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/Downloads/"+file_name);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            if(progressDialog!=null){
                progressDialog.hide();
                progressDialog.dismiss();
            }

        }

    }


    public static void showDialog(ProgressDialog progressDialog, Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Downloading file. Please wait...");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
        progressDialog.show();

    }

    public static void createDirIfNotExists(String path) {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,"/"+path);

        if(!file.exists()){
            file.mkdirs();
        }
    }

}
