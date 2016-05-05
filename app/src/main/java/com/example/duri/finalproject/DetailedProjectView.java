package com.example.duri.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DetailedProjectView extends AppCompatActivity {
    private int buttonIdCounter = 20;
    private HashMap<String, String> studentsMapping = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getAndDisplayStudents();
    }

    private void goToStudent(View v) {
        if (v instanceof Button) {
            Button b = (Button) v;
            String studentChosen = b.getText().toString();
            //TODO: intent here to open detailed project view
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("studentChosen", studentsMapping.get(studentChosen));
            intent.putExtra("projectChosen", getIntent().getStringExtra("projectChosen"));
            startActivity(intent);
        }
    }

    private void getAndDisplayStudents() {
        JsonArrayRequest jsArrRequest = new JsonArrayRequest
                (Request.Method.GET, "http://comp156.cs.unc.edu/comp790/all_students.php", null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray projectList) {
                        for (int i = 0; i<projectList.length(); i++) {
                            try {
                                LinearLayout layout = (LinearLayout) findViewById(R.id.student_list);
                                JSONObject anObj = (JSONObject) projectList.get(i);
                                String fullName = anObj.getString("firstName")+" "+anObj.getString("lastName");
                                String id = anObj.getString("studentID");
                                studentsMapping.put(fullName,id);
                                addNewStudent(fullName, layout);

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
    private void addNewStudent(String student, LinearLayout layout) {
        LinearLayout aStudent = new LinearLayout(this);
        aStudent.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(aStudent);
        final Button button = new Button(this);
        button.setId(buttonIdCounter++);
        button.setText(student);
        button.setTag(R.string.StudentName, student);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(params);
        aStudent.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToStudent(v);
            }
        });
    }

}
