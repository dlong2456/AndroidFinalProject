package com.example.duri.finalproject;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SeekBar;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    private final WebSocketConnection socket = new WebSocketConnection();
    private int status;


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

        SeekBar seek = (SeekBar) findViewById(R.id.seekBar);
        seek.setOnSeekBarChangeListener(this);

        WebView web = (WebView) findViewById(R.id.webView);
        web.setWebViewClient(new WebViewClient());
        
        web.loadUrl("https://docs.google.com/document/d/1Pe2CivDGHMg5jGVRUFjcNZOzYx4I0Mkvc-GN25k6p9A/edit?pref=2&pli=1");

        try {
            //use ws://10.0.2.2:8080 for localhost
            //"ws://classroom1.cs.unc.edu:5050" for CS server
            socket.connect("ws://classroom1.cs.unc.edu:5050", new WebSocketHandler() {
                @Override
                public void onOpen() {
                    Log.v("WEBSOCKETS", "Connected to server.");
                }

                @Override
                public void onTextMessage(String payload) {
                    if (payload.equals("handshake")) {
                        socket.sendTextMessage("teacher");
                    } else {
                        parseJSON(payload);
                    }
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.v("WEBSOCKETS", "Connection lost " + reason);
                }
            });
        } catch (WebSocketException wse) {
            Log.d("WEBSOCKETS", wse.getMessage());
        }

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

    public int getCurrentStatus() {
        return status;
    }

    public void setCurrentStatus(int status) {
        this.status = status;
    }

    private void parseJSON(String data) {
        try {
            JSONObject jObject = new JSONObject(data);
            if (jObject.has("status")) {
                //TODO: save this in the database and use them for the live mode and project view
                //Note: 0 is making progress, 1 is facing difficulty
                setCurrentStatus(jObject.getInt("status"));
            } else if (jObject.has("insertCommands")) {
                //save these in the database and use them for the live mode
                //This identifies the document and therefore the student. We should have a database query that allows us to
                //query with the documentId and receive the student's name in return.
                jObject.getString("documentId");
                JSONArray insertCommands = jObject.getJSONArray("insertCommands");
                for (int i = 0; i < insertCommands.length(); i++) {
                    //TODO: save each of these in the database and use to update live mode
                    JSONObject insertCommandObject = insertCommands.getJSONObject(i);
                    insertCommandObject.getLong("timeStamp");
                    insertCommandObject.getString("content");
                    insertCommandObject.getInt("index");
                }
            } else if (jObject.has("deleteCommands")) {
                JSONArray deleteCommands = jObject.getJSONArray("deleteCommands");
                //See above note on documentId
                jObject.getString("documentId");
                for (int i = 0; i < deleteCommands.length(); i++) {
                    //TODO: save each of these in the database and use to update live mode
                    JSONObject deleteCommandObject = deleteCommands.getJSONObject(i);
                    deleteCommandObject.getLong("timeStamp");
                    deleteCommandObject.getInt("endIndex");
                    deleteCommandObject.getInt("startIndex");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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


}
