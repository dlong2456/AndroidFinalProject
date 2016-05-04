package com.example.duri.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailedProjectView extends AppCompatActivity {
    private int buttonIdCounter = 20;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        LinearLayout layout = (LinearLayout) findViewById(R.id.student_list);
        ArrayList<String> studentList = getStudents();
        for(String student: studentList) {
            LinearLayout aStudent = new LinearLayout(this);
            aStudent.setOrientation(LinearLayout.HORIZONTAL);
            layout.addView(aStudent);
            final Button button = new Button(this);
            button.setId(buttonIdCounter++);
            button.setText(student);
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

    private void goToStudent(View v) {
        if (v instanceof Button) {
            Button b = (Button) v;
            String studentChosen = b.getText().toString();
            //TODO: intent here to open detailed project view
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("studentChose", studentChosen);
            startActivity(intent);
        }
    }

    private ArrayList<String> getStudents() {
        //TODO: query the database here. This just returns a dummy array right now.
        ArrayList<String> students = new ArrayList<String>();
        students.add("Andrew");
        students.add("Duri");
        students.add("Kevin");
        return students;
    }

}
