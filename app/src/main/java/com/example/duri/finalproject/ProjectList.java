package com.example.duri.finalproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ProjectList extends AppCompatActivity {

    private int buttonIdCounter = 0;
    private HashMap<String, String>  projectsMapping = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getAndDisplayProjects();

    }

    private void addNewProject(String name, LinearLayout rows) {
        LinearLayout aProject = new LinearLayout(this);
        aProject.setOrientation(LinearLayout.HORIZONTAL);
        rows.addView(aProject);
        TextView projectName = new TextView(this);
        projectName.setText(name);
        projectName.setWidth(450);
        aProject.addView(projectName);
        final Button button = new Button(this);
        button.setWidth(22);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        button.setId(buttonIdCounter);
        button.setText("Overview");
        button.setTag(R.string.ProjectName, name);
        buttonIdCounter++;
        final Button button2 = new Button(this);
        button2.setId(buttonIdCounter);
        button2.setText("Detail View");
        button2.setWidth(22);
        button2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        button2.setTag(R.string.ProjectName,name);

        //SET CLICK LISTENER
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToProjectOverview(v);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToProjectDetailed(v);
            }
        });
        //SET LAYOUT PARAMETERS
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(RelativeLayout.ALIGN_PARENT_RIGHT,
//                    LinearLayout.LayoutParams.MATCH_PARENT);
//            button.setLayoutParams(params);
//            button2.setLayoutParams(params);
        //ADD BUTTON TO GRID
        aProject.addView(button);
        aProject.addView(button2);
    }

    private void getAndDisplayProjects() {

        JsonArrayRequest jsArrRequest = new JsonArrayRequest
                (Request.Method.GET, "http://comp156.cs.unc.edu/comp790/all_projects.php", null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray projectList) {
                        for (int i = 0; i<projectList.length(); i++) {
                            try {
                                LinearLayout layout = (LinearLayout) findViewById(R.id.project_list);
                                JSONObject anObj = (JSONObject) projectList.get(i);
                                String name = anObj.getString("name");
                                String id = (anObj.getString("assignmentID"));
                                projectsMapping.put(name,id);
                                addNewProject(name,layout);

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

    public void addProject(View v) {
        //TODO: add project to the database here. Right now this just creates a button.
        Log.v("duriTest", "add project");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Project Name");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LinearLayout layout = (LinearLayout) findViewById(R.id.project_list);
                addNewProject(input.getText().toString(),layout);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void goToProjectOverview(View v) {
        //This should always be true but checking just in case
        if (v instanceof Button) {
            Button b = (Button) v;
            String projectChosen = (String) b.getTag(R.string.ProjectName);
            //TODO: intent here to open detailed project view
            Intent intent = new Intent(this, SummaryProjectView.class);
            intent.putExtra("projectChosen", projectsMapping.get(projectChosen));
            startActivity(intent);
        }
    }
    private void goToProjectDetailed(View v) {
        //This should always be true but checking just in case
        if (v instanceof Button) {
            Button b = (Button) v;
            String projectChosen = (String) b.getTag(R.string.ProjectName);
            //TODO: intent here to open detailed project view
            Intent intent = new Intent(this, DetailedProjectView.class);
            intent.putExtra("projectChosen", projectsMapping.get(projectChosen));
            intent.putExtra("projectMapping", projectsMapping);
//            System.out.println(projectChosen);
//            System.out.println(projectsMapping);
//            System.out.println("projectChosen: "+ projectsMapping.get(projectChosen));
            startActivity(intent);
        }
    }

}
