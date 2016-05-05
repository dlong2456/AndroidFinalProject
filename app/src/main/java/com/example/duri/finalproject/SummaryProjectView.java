package com.example.duri.finalproject;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class SummaryProjectView extends AppCompatActivity {
    private final WebSocketConnection socket = new WebSocketConnection();

    TextView[] studentFeeds = new TextView[3];
    TextView[] studentLabels = new TextView[3];
    int docGreenRedTotal=0;
    int greenTotal=0;
    private HashMap<String, String> documentMapping = new HashMap<>();
    Entry entry1, entry2;
    PieChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chart = (PieChart) findViewById(R.id.view);
        List<Entry> entries = new ArrayList<Entry>();
        List<Integer> colors = new ArrayList<Integer>();
        colors.add(R.color.Green);
        colors.add(R.color.Red);

        entry1 = new Entry(75,1);
        entry2 = new Entry(25,2);
        entries.add(entry1);
        entries.add(entry2);
        PieDataSet dataSet = new PieDataSet(entries,"");
        dataSet.setColors(new int[]{Color.GREEN, Color.RED});
        PieData data = new PieData(new String[]{"No difficulty", "Difficulty"});

        data.setValueTextColors(colors);

        data.setDataSet(dataSet);
        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.setDescription("");
        studentFeeds[0] = (TextView) findViewById(R.id.textView5);
        studentFeeds[1] = (TextView) findViewById(R.id.textView7);
        studentFeeds[2] = (TextView) findViewById(R.id.textView9);

        studentLabels[0] = (TextView) findViewById(R.id.textView4);
        studentLabels[1] = (TextView) findViewById(R.id.textView6);
        studentLabels[2] = (TextView) findViewById(R.id.textView8);
        getProjectDocuments();

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

    private void getProjectDocuments() {
        JsonArrayRequest jsArrRequest = new JsonArrayRequest
                (Request.Method.GET, "http://comp156.cs.unc.edu/comp790/document.php?projectID="+getIntent().getExtras().getString("projectChosen"), null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray documentList) {
                        for (int i = 0; i<documentList.length(); i++) {
                            try {
                                JSONObject anObj = (JSONObject) documentList.get(i);
                                String studentID = anObj.getString("studentID");
                                String docId = anObj.getString("documentID");
                                String docStatus = anObj.getString("docStatus");
                                switch (docStatus) {
                                    case "-1":
                                        break;
                                    case "0":
                                        docGreenRedTotal++; greenTotal++;
                                        break;
                                    case "1":
                                        docGreenRedTotal++;
                                        break;
                                }
                                documentMapping.put(docId, studentID);
                                System.out.println(documentMapping);
                                getAndDisplayStudents();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("greenTotal: "+greenTotal);
                        System.out.println("greenRedTotal: " + docGreenRedTotal);
                        entry1.setVal((greenTotal / (float) docGreenRedTotal) * 100);
                        entry2.setVal(100 - (greenTotal / (float) docGreenRedTotal) * 100);
                        chart.notifyDataSetChanged();
                        chart.invalidate();

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.getMessage());

                    }
                });
        ApplicationController.getInstance().addToRequestQueue(jsArrRequest);
    }

    int latestIndex;
    private void parseJSON(String data) {
        try {
            System.out.println(data);
            JSONObject jObject = new JSONObject(data);
            System.out.println(jObject.has("deleteCommands"));
            if (jObject.has("substring")) {
                String substring = jObject.getString("substring");
                Log.v("SUBSTRING: ", substring);
                String docId = jObject.getString("documentId");
                int studentId = Integer.parseInt(documentMapping.get(docId));
                studentFeeds[studentId-1].setText(substring);
            }else  if (jObject.has("insertCommands")) {
                JSONArray insertCommands = jObject.getJSONArray("insertCommands");
                JSONArray deleteCommands = jObject.getJSONArray("deleteCommands");
                    if(insertCommands.length()!=0) {
                        latestIndex = jObject.getJSONArray("insertCommands").getJSONObject(0).getInt("index");
                        String documentId = jObject.getString("documentId");
                        if(documentMapping.containsKey(documentId)){
                            getDocumentSubstring(documentId, latestIndex, 20);
                        }
                    }  else if(deleteCommands.length()!=0) {
                        latestIndex = jObject.getJSONArray("deleteCommands").getJSONObject(0).getInt("endIndex");
                        String documentId = jObject.getString("documentId");
                        if(documentMapping.containsKey(documentId)) {
                            getDocumentSubstring(documentId, latestIndex - 1, 20);
                        }
                    }
                }

            }
         catch (JSONException e) {
            System.err.println(data);
            e.printStackTrace();
        }
    }
    private void getAndDisplayStudents() {
        JsonArrayRequest jsArrRequest = new JsonArrayRequest
                (Request.Method.GET, "http://comp156.cs.unc.edu/comp790/all_students.php", null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray projectList) {
                        for (int i = 0; i<((projectList.length()<3)?projectList.length():3); i++) {
                            try {
                                JSONObject anObj = (JSONObject) projectList.get(i);
                                String fullName = anObj.getString("firstName")+" "+anObj.getString("lastName");
                                studentLabels[i].setText(fullName);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
    public void getDocumentSubstring(String documentId, int index, int substringLength) {
        if (socket.isConnected()) {
            socket.sendTextMessage("{type: documentSubstring, documentId: " + documentId + ", index: " + index + ", substringLength: " + substringLength + " }");
        }
        else {
            Toast t = new Toast(getApplicationContext());
            t.setText("Not connected!");
            t.show();
        }
    }
}
