package com.example.duri.finalproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private final WebSocketConnection socket = new WebSocketConnection();
    private int status;

    TextView document;
    StringBuilder textData = new StringBuilder("");
    String textDataFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        SeekBar seek = (SeekBar) findViewById(R.id.seekBar);
        seek.setOnSeekBarChangeListener(this);
        document = (TextView) findViewById(R.id.textView2);

        document.setText(textData);
        textDataFinal = textData.toString();
        live(findViewById(R.id.button2));

        try {
            //use ws://10.0.2.2:8080 for localhost
            //"ws://classroom1.cs.unc.edu:5050" for CS server
            socket.connect("ws://classroom1.cs.unc.edu:5050", new WebSocketHandler() {
                @Override
                public void onOpen() {
                    Log.v("WEBSOCKETS", "Connected to server.");
                    getWholeDocument("10eVBvPyYNHGE_xOOoIGfKYkIIrDpg9tUfn8FuSyVKIA");
                }

                @Override
                public void onTextMessage(String payload) {
                    if (payload.equals("handshake")) {
                        socket.sendTextMessage("teacher");
                    } else if (payload.equals("documentNotFound")) {
                        //TODO: Handle this error
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
                //Note: 0 is making progress, 1 is facing difficulty
                setCurrentStatus(jObject.getInt("status"));
            } else if (jObject.has("insertCommands")) {
                //save these in the database and use them for the live mode
                //This identifies the document and therefore the student. We should have a database query that allows us to
                //query with the documentId and receive the student's name in return.
                jObject.getString("documentId");
                JSONArray insertCommands = jObject.getJSONArray("insertCommands");
                for (int i = 0; i < insertCommands.length(); i++) {
                    JSONObject insertCommandObject = insertCommands.getJSONObject(i);
                    insertCommandObject.getLong("timeStamp");
                    String content = insertCommandObject.getString("content");
                    Log.v("WEBSOCKETS", content);
//                    document.append(insertCommandObject.getString("content"));
                    int insertIdx = insertCommandObject.getInt("index");
                    System.out.println("trying to insert at: " + insertIdx);
                    System.out.println("length: " + textData.length());
                    if (insertIdx > textData.length()) {
                        textData.append(content);
                    } else {
                        textData.insert(insertIdx - 1, content);
                    }
                    document.setText(textData);
                }

                JSONArray deleteCommands = jObject.getJSONArray("deleteCommands");
                //See above note on documentId
                jObject.getString("documentId");
                for (int i = 0; i < deleteCommands.length(); i++) {
                    JSONObject deleteCommandObject = deleteCommands.getJSONObject(i);
                    deleteCommandObject.getLong("timeStamp");

                    int start = deleteCommandObject.getInt("startIndex");
                    int end = deleteCommandObject.getInt("endIndex");
                    System.out.println("start delete: " + start);
                    System.out.println("end delete: " + end);
                    try {
                        textData.delete(start - 1, end);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println(textData);
                    document.setText(textData);

                }
            } else if (jObject.has("wholeDocument")) {
                String wholeDoc = jObject.getString("wholeDocument");
                Log.v("wholeDoc: ", wholeDoc);
                textData = new StringBuilder(wholeDoc);
                document.setText(textData);
            } else if (jObject.has("substring")) {
                Log.v("Return String: ", jObject.getString("endOfDoc"));
            } else if (jObject.has("beginningOfDoc")) {
                Log.v("BEGINNING OF DOC: ", jObject.getString("beginningOfDoc"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        document.setText(textDataFinal.substring(0, (int) Math.ceil(progress * .10 * textDataFinal.length())));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void live(View view) {
        findViewById(R.id.seekBar).setVisibility(View.GONE);
        Button history = (Button) findViewById(R.id.button);
        view.setBackgroundColor(Color.GREEN);
        history.setBackgroundResource(android.R.drawable.btn_default);
    }

    public void history(View view) {
        findViewById(R.id.seekBar).setVisibility(View.VISIBLE);
        Button live = (Button) findViewById(R.id.button2);
        view.setBackgroundColor(Color.GREEN);
        live.setBackgroundResource(android.R.drawable.btn_default);
    }

    class myWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl("https://docs.google.com/document/d/1iCHQpzQQJmhA67N8kKT4ry4m4wpyUShfD98h_LcxjjM/edit?pref=2&pli=1");
            return true;
        }
    }

    //These are all going to return asynchronously, so plan accordingly
    //TODO: Might be useful to handle the error that occurs if the socket is null (aka teacher not connected to server). Could show a toast.
    public void getWholeDocument(String documentId) {
        if (socket != null) {
            socket.sendTextMessage("{type: wholeDocument, documentId: " + documentId + " }");
        }
    }

    public void getDocumentFromBeginning(String documentId, int percentage) {
        if (socket != null) {
            socket.sendTextMessage("{type: documentFromBeginning, documentId: " + documentId + ", percentage: " + percentage + " }");
        }
    }

    public void getDocumentSubstring(String documentId, int index, int substringLength) {
        if (socket != null) {
            socket.sendTextMessage("{type: documentSubstring, documentId: " + documentId + ", index: " + index + ", substringLength: " + substringLength + " }");
        }
    }

}
