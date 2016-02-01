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
    private String whooingUrl;
    public ArrayList<JSONObject> mDataArray = new ArrayList<>();
    public Map<String, Object> Userdata = new HashMap<String, Object>();

    private static final String WHOOING_CONNECT_URL = "https://whooing.com/sms_app";
    //private static final String WHOOING_CONNECT_URL = "http:///202.68.228.212/sms_app";
    private static final String WHOOING_UA_STRING = "whooingSMS";
    private static final String ID_LAST_URL = "WHO_LAST_URL";

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
        whooingWebview.setWebViewClient(new WebViewClientClass());
        whooingWebview.addJavascriptInterface(new WebAppInterface(this), "whooingWrapperFunc");
        final Context WhooingAlertContext = this;
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
        if (savedInstanceState == null) {
            SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
            whooingUrl = pref.getString(ID_LAST_URL,WHOOING_CONNECT_URL);
            //whooingUrl = pref.getString(ID_LAST_URL, WHOOING_CONNECT_DEV_URL);
            whooingWebview.loadUrl(whooingUrl);
        } else {
            whooingWebview.restoreState(savedInstanceState);
        }

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
      //  Map<String, Object> params = new HashMap<String, Object>();
        JSONObject JO = new JSONObject();
        JSONArray JSA = new JSONArray(mDataArray);

        Cursor c = null;
        String[] PROJECTION = {"_id", "thread_id", "address", "person", "date", "body"};
        String WHERE1 = "address = " + getaddress;
       // String WHERE = "(date BETWEEN " + startdate + " AND " + enddate +") AND ("+ WHERE1+")";
        String WHERE = "(date BETWEEN " + startdate + " AND "
         				+ enddate + ") AND (" + WHERE1 + ")";

        c = cr.query(WhooingSMS, PROJECTION, WHERE, null, "date ASC");
       // c = getContentResolver().query(WhooingSMS, new String[] { "_id", "thread_id", "address", "person", "date", "body" },null, null, "date DESC");
        Log.d("hello data incomming "," data call one? : "+WHERE);
        Log.d("hello data incomming "," data call two? : "+WHERE1);
        Log.d("hello data incomming ","c data ? : " + c.getCount());
        //TODO Arraylist 작성
        mDataArray.clear();
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
            Log.d("로그데이터", "mDataArray : " + mDataArray.toString());
            Log.d("로그데이터", "JSA : " + JSA.toString());
            Log.d("로그데이터", "=======================================================");
        }
        else{
            Log.d("로그데이터", "no data ");
/*            mDataArray.clear();
           *//* JO.put("body", "");
            JO.put("datetime","");*//*
            mDataArray.add(JO);*/
        }
       // return Userdata;
        return JSA;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_whooing_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class WebViewClientClass extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO : limit webview's content to show only vinovel.com
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            //  llSplash.setVisibility(View.GONE);
        }
    }

    private class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void changeBaseURL(String base_url) {
            if(!URLUtil.isValidUrl(base_url)){
                return;
            }
            Toast.makeText(mContext, "BaseURL will be changed with " + base_url, Toast.LENGTH_SHORT).show();
            //DEFAULT_PAGE_URL = base_url;
            // TODO : make a handler functio to manage webview.
            //webview.loadUrl(DEFAULT_PAGE_URL);
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void setPortraitLayout() {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        @JavascriptInterface
        public void setLandscapeLayout() {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        @JavascriptInterface
        public void openWebBrowser(String url){
            if(!URLUtil.isValidUrl(url)){
                return;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }

        @JavascriptInterface
        public String loadSMS(String phone_number, String start_timestamp, String end_timestamp) throws JSONException {
            Log.d("whooing?", "OK !!! : " + phone_number + " start_timestamp : " + start_timestamp + " end_timestamp : " + end_timestamp);
            //ReadSMS("15773321", "1439342430113", "1439176988864");
            return ReadSMS(phone_number, Long.valueOf(start_timestamp), Long.valueOf(end_timestamp)).toString();
        }

 /*       @JavascriptInterface
        public String readSMS(){
           // return mDataArray.toString();
        }*/
    }
}
