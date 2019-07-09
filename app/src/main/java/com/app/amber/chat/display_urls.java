package com.app.amber.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class display_urls extends AppCompatActivity {
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_urls);
        webView=(WebView) findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if(getIntent().getExtras().getString("url")!=null && getIntent().getExtras().getString("url").length()>0){
            System.out.println("url = "+getIntent().getExtras().getString("url"));
            webView.loadUrl(getIntent().getExtras().getString("url"));
        }else {
        finish();
        }
    }
}
