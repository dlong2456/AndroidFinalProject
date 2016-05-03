package com.example.duri.finalproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class ProjectList extends AppCompatActivity {

    private int buttonIdCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);
        LinearLayout layout = (LinearLayout) findViewById(R.id.project_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ArrayList<String> projectList = getProjects();
        for (int i = 0; i<projectList.size(); i++) {
            final Button button = new Button(this);
            button.setId(buttonIdCounter);
            button.setText(projectList.get(buttonIdCounter));
            buttonIdCounter++;
            //SET CLICK LISTENER
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    goToProject(v);
                }
            });
            //SET LAYOUT PARAMETERS
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            button.setLayoutParams(params);
            //ADD BUTTON TO GRID
            layout.addView(button);
        }

    }

    private ArrayList<String> getProjects() {
        //TODO: query the database here. This just returns a dummy array right now.
        ArrayList<String> projects = new ArrayList<String>();
        projects.add("Social Security Paper");
        projects.add("American Gov Paper");
        projects.add("Presidents Paper");
        return projects;
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
            final Button button = new Button(getApplicationContext());
            button.setId(buttonIdCounter);
            button.setText(input.getText().toString());
            buttonIdCounter++;
            //SET CLICK LISTENER
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    goToProject(v);
                }
            });
            //SET LAYOUT PARAMETERS
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            button.setLayoutParams(params);
            //ADD BUTTON TO GRID
            layout.addView(button);
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

    private void goToProject(View v) {
        //This should always be true but checking just in case
        if (v instanceof Button) {
            Button b = (Button) v;
            String projectChosen = b.getText().toString();
            //TODO: intent here to open detailed project view
            Intent intent = new Intent(this, DetailedProjectView.class);
            intent.putExtra("projectChosen", projectChosen);
            startActivity(intent);
        }
    }

}
