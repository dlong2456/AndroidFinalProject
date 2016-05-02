package com.example.duri.finalproject;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getChangeLog();

        SeekBar seek = (SeekBar) findViewById(R.id.seekBar);
        seek.setOnSeekBarChangeListener(this);

        WebView web = (WebView) findViewById(R.id.webView);
        web.setWebViewClient(new WebViewClient());
        
        web.loadUrl("https://docs.google.com/document/d/1Pe2CivDGHMg5jGVRUFjcNZOzYx4I0Mkvc-GN25k6p9A/edit?pref=2&pli=1");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    //Web request working, but not with this URL
    //It works on Chrome on my desktop, but not on Android - getting a 401 error
    //I think it is because it thinks I am not logged in (although I am)
    //Download rest client on android device and test it out
    //Also try logging in with Google Drive
    public void getChangeLog() {
        final Map<String, String> mHeaders = new ArrayMap<String, String>();
        mHeaders.put("x-same-domain", "1");
        Log.v("duriTest", "get change log");
        final String URL = "https://docs.google.com/document/u/1/d/1zG6ud0GscccmH_GvMAH4SohiCVo8jAeDtsVCMxs9YYI/revisions/load?id=1zG6ud0GscccmH_GvMAH4SohiCVo8jAeDtsVCMxs9YYI&start=341&end=497&smv=0&token=AC4w5Vh826EW31PUXCe0O4ie7OrCvj5AgQ%3A1462032263630";
        //final String URL = "https://docs.google.com/document/u/1/d/1zG6ud0GscccmH_GvMAH4SohiCVo8jAeDtsVCMxs9YYI/revisions/load?id=1zG6ud0GscccmH_GvMAH4SohiCVo8jAeDtsVCMxs9YYI&start=341&end=497&smv=0&token=AC4w5Vh826EW31PUXCe0O4ie7OrCvj5AgQ%3A1462032263630";
        // pass second argument as "null" for GET requests
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("Response: ", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.v("Error: ", error.networkResponse.statusCode);
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    VolleyLog.e("Error: ", "Timeout or connection");
                } else if (error instanceof AuthFailureError) {
                    VolleyLog.e("Error: ", "Auth");
                } else if (error instanceof ServerError) {
                    VolleyLog.e("Error: ", "Server");
                } else if (error instanceof NetworkError) {
                    VolleyLog.e("Error: ", "Network");
                } else if (error instanceof ParseError) {
                    VolleyLog.e("Error: ", "Parse");
                }
            }
        }
        ){
//            // could be any class that implements Map
            @Override
            public Map<String, String> getHeaders() {
                return mHeaders;
            };
        };

        // add the request object to the queue to be executed
        ApplicationController.getInstance().addToRequestQueue(req);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        System.out.println("Yo new value!: "+ progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
    class myWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl("https://docs.google.com/document/d/1Pe2CivDGHMg5jGVRUFjcNZOzYx4I0Mkvc-GN25k6p9A/edit?pref=2&pli=1");
            return true;
        }
    }

//    getChangelog: function() {
//        var regmatch = location.href.match(/^(https:\/\/docs\.google\.com.*?\/document\/d\/)/)
//        var baseUrl = regmatch[1]
//        var docId = draftback.getDocId()
//        var loadUrl = baseUrl + docId + "/revisions/load?id=" + docId + "&start=1&end=" + parseInt(('' + draftback.revisionCount).replace(/,/g, '')) + "&token=" + draftback.token
//
//        $.ajax({
//                type: "get",
//                url: loadUrl,
//                headers: {"x-same-domain": 1},
//        error: function(response, error_type, error) {
//            var res = response.responseText
//            chrome.runtime.sendMessage({msg: 'changelog', docId: draftback.getDocId(), changelog: res}, function(response) {});
//        },
//        success: function(response) {
//            console.log(response)
//        }
//        })
//    }
//})
}
