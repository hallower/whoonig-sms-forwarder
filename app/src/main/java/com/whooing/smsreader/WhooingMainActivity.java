package com.whooing.smsreader;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WhooingMainActivity extends AppCompatActivity {

    public WebView whooingWebview;

    private static final String WHOOING_CONNECT_URL = "https://whooing.com/sms_app";
    private static final String WHOOING_UA_STRING = "whooingSMS";
    private final Context WhooingAlertContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whooing_main);
        whooingWebview = (WebView) findViewById(R.id.whooingwebview);

        WebSettings settings = whooingWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        //settings.setAppCachePath(dir.getPath());
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);

        StringBuilder userAgent = new StringBuilder(settings.getUserAgentString());
        userAgent.append(";" + WHOOING_UA_STRING);
        settings.setUserAgentString(userAgent.toString());

        whooingWebview.setWebViewClient(new WebViewClient());
        whooingWebview.addJavascriptInterface(new WebAppInterface(this), "whooingWrapperFunc");
        whooingWebview.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(WhooingAlertContext).setTitle("후잉SMS").setMessage(message).setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .setCancelable(false)
                        .create().show();
                //return super.onJsAlert(view, url, message, result);
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(WhooingAlertContext).setTitle("후잉SMS").setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.cancel();
                                    }
                                })
                        .setCancelable(false)
                        .create().show();
                //return super.onJsAlert(view, url, message, result);
                return true;
                //return super.onJsConfirm(view, url, message, result);
            }
        });

        whooingWebview.loadUrl(WHOOING_CONNECT_URL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        whooingWebview.reload();
    }

    public JSONArray ReadSMS(String getaddress, Long startdate, Long enddate) throws JSONException {

        Log.d("로그데이터", "incomming data ");
        Uri WhooingSMS = Uri.parse("content://sms/inbox");
        ContentResolver cr = getContentResolver();
        JSONArray JSA = new JSONArray();

        Cursor c = null;
        String[] PROJECTION = {"_id", "thread_id", "address", "person", "date", "body"};
        String WHERE1 = "address = " + getaddress;
        String WHERE = "(date BETWEEN " + startdate + " AND " + enddate + ") AND (" + WHERE1 + ")";

        c = cr.query(WhooingSMS, PROJECTION, WHERE, null, "date ASC");
        Log.d("hello data incomming "," data call one? : "+WHERE);
        Log.d("hello data incomming "," data call two? : "+WHERE1);
        Log.d("hello data incomming ","c data ? : " + c.getCount());

        if(c.getCount() > 0) {
            while (c.moveToNext()) {
                JSONObject WhooingJsonObject = new JSONObject();
                WhooingJsonObject.put("body", c.getString(5));
                WhooingJsonObject.put("datetime", c.getString(4));
                Log.d("로그데이터", "=======================================================");
                Log.d("로그데이터", "id : " + c.getString(0));
                Log.d("로그데이터", "address : " + c.getString(2));
                Log.d("로그데이터", "person : " + c.getString(3));
                Log.d("로그데이터", "date : " + c.getString(4));
                Log.d("로그데이터", "body : " + c.getString(5));
                //Log.d("로그데이터", "parmes : " + params.toString());
                Log.d("로그데이터", "=======================================================");
                JSA.put(WhooingJsonObject);
            }
            Log.d("로그데이터", "=======================================================");
            Log.d("로그데이터", "JSA : " + JSA.toString());
            Log.d("로그데이터", "=======================================================");
        } else{
            Log.d("로그데이터", "no data ");
        }
        return JSA;
    }

    private class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public String loadSMS(String phone_number, String start_timestamp, String end_timestamp) throws JSONException {
            Log.d("whooing?", "OK !!! : " + phone_number + " start_timestamp : " + start_timestamp + " end_timestamp : " + end_timestamp);
            return ReadSMS(phone_number, Long.valueOf(start_timestamp), Long.valueOf(end_timestamp)).toString();
        }
    }
}
