package com.example.duri.finalproject;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private final WebSocketConnection socket = new WebSocketConnection();
    private int status;

    private boolean live = false;
    private String documentId="";
    private SeekBar seek;

    private TextView document;
    private StringBuilder textData = new StringBuilder("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        seek = (SeekBar) findViewById(R.id.seekBar);
        seek.setOnSeekBarChangeListener(this);
        seek.setVisibility(View.GONE);
        document = (TextView) findViewById(R.id.textView2);
        document.setMovementMethod(new ScrollingMovementMethod());
        document.setText(textData);
        setDocumentIdAndBegin();

    }


    private void connectToSocket() {
        try {
            //use ws://10.0.2.2:8080 for localhost
            //"ws://classroom1.cs.unc.edu:5050" for CS server
            socket.connect("ws://classroom1.cs.unc.edu:5050", new WebSocketHandler() {
                @Override
                public void onOpen() {
                    Log.v("WEBSOCKETS", "Connected to server.");
                    live(findViewById(R.id.button2));

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
    private void setDocumentIdAndBegin() {
        String projectId = getIntent().getStringExtra("projectChosen");
        String studentId = getIntent().getStringExtra("studentChosen");
        System.out.println("http://comp156.cs.unc.edu/comp790/document.php?projectID="+projectId+"&studentID="+studentId);
        JsonArrayRequest jsArrRequest = new JsonArrayRequest
                (Request.Method.GET, "http://comp156.cs.unc.edu/comp790/document.php?projectID="+projectId+"&studentID="+studentId, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray documentList) {
                        try {
                            JSONObject anObj = (JSONObject) documentList.get(0);
                            documentId = anObj.getString("documentID");
                            System.out.println("current doc: "+documentId);
                            connectToSocket();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.getMessage());

                    }
                });
        ApplicationController.getInstance().addToRequestQueue(jsArrRequest);
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
                System.out.println(data);
                //Note: 0 is making progress, 1 is facing difficulty
                //TODO: save this in the database
                setCurrentStatus(jObject.getInt("status"));
            } else if (jObject.has("insertCommands")) { // this means insert or delete command
                if(!live) return;
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
                    getDocumentSubstring(documentId, insertIdx, 20);
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
                Log.v("SUBSTRING: ", jObject.getString("substring"));
            } else if (jObject.has("beginningOfDoc")) {
                String partialDoc = jObject.getString("beginningOfDoc");
                Log.v("BEGINNING OF DOC: ", partialDoc);
                textData = new StringBuilder(partialDoc);
                document.setText(textData);
            } else if (jObject.has("statusGivenPercentage")) {
                String status = jObject.getString("statusGivenPercentage");
                Log.v("STATUS GIVEN PERCENTAGE", status);
                switch (status) {
                    case "-1":
                        seek.setBackgroundColor(Color.GRAY);
                        break;
                    case "0":
                        seek.setBackgroundColor(Color.GREEN);
                        break;
                    case "1":
                        seek.setBackgroundColor(Color.RED);
                        break;
                }
            }
        } catch (JSONException e) {
            System.err.println(data);
            e.printStackTrace();
        }

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        getDocumentFromBeginning(documentId,progress);
        getStatusGivenPercentage(documentId, progress);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void live(View view) {
        live=true;
        findViewById(R.id.seekBar).setVisibility(View.GONE);
        Button history = (Button) findViewById(R.id.button);
        view.setBackgroundColor(Color.GREEN);
        history.setBackgroundResource(android.R.drawable.btn_default);
        if(socket.isConnected()) getWholeDocument(documentId);

    }

    public void history(View view) {
        live=false;
        findViewById(R.id.seekBar).setVisibility(View.VISIBLE);
        Button live = (Button) findViewById(R.id.button2);
        view.setBackgroundColor(Color.GREEN);
        live.setBackgroundResource(android.R.drawable.btn_default);
        getDocumentFromBeginning(documentId, ((SeekBar) findViewById(R.id.seekBar)).getProgress());
        getStatusGivenPercentage(documentId, ((SeekBar) findViewById(R.id.seekBar)).getProgress());
    }

    //These are all going to return asynchronously, so plan accordingly
    public void getWholeDocument(String documentId) {
        if (socket.isConnected()) {
            socket.sendTextMessage("{type: wholeDocument, documentId: " + documentId + " }");
        } else {
            Toast t = new Toast(getApplicationContext());
            t.setText("Not connected!");
            t.show();
        }
    }

    public void getDocumentFromBeginning(String documentId, int percentage) {
        if (socket.isConnected()) {
            socket.sendTextMessage("{type: documentFromBeginning, documentId: " + documentId + ", percentage: " + percentage + " }");
        } else {
            Toast t = new Toast(getApplicationContext());
            t.setText("Not connected!");
            t.show();
        }
    }

    public void getDocumentSubstring(String documentId, int index, int substringLength) {
        if (socket != null) {
            socket.sendTextMessage("{type: documentSubstring, documentId: " + documentId + ", index: " + index + ", substringLength: " + substringLength + " }");
        } else {
            throw new RuntimeException("Not connected!");
        }
    }

    public void getStatusGivenPercentage(String documentId, int percentage) {
        if (socket.isConnected()) {
            socket.sendTextMessage("{type: statusGivenPercentage, documentId: " + documentId + ", percentage: " + percentage + " }");
        } else {
            throw new RuntimeException("Not connected!");
        }
    }

}
