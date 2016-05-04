package com.example.duri.finalproject;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class SummaryProjectView extends AppCompatActivity {
    TextView t5;
    TextView t7;
    TextView t9;
    String text = "This paper will build on eco-critical interpretations of Shakespeare’s work by\n" +
            "\n" +
            "discussing the convergence of ecology and performance in Druid Theater’s recent\n" +
            "\n" +
            "production DruidShakespeare, an adaptation by Mark O’Rowe of Shakespeare’s\n" +
            "\n" +
            "Richard II, Henry IV (Parts I and II), and Henry V. The site-specific nature of the show\n" +
            "\n" +
            "and the use of natural elements such as earth, water, and fire in performance\n" +
            "\n" +
            "contribute to an eco-critical interpretation of Shakespeare’s history plays.\n" +
            "\n" +
            "Specifically, this paper will investigate how the site-specific";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PieChart chart = (PieChart) findViewById(R.id.view);
        List<Entry> entries = new ArrayList<Entry>();
        List<Integer> colors = new ArrayList<Integer>();
        colors.add(R.color.Green);
        colors.add(R.color.Red);

        Entry entry1 = new Entry(75,1);
        Entry entry2 = new Entry(25,2);
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
        text = text.replace("\n"," ");
        t5 = (TextView) findViewById(R.id.textView5);
        t7 = (TextView) findViewById(R.id.textView7);
        t9 = (TextView) findViewById(R.id.textView9);

        demoNewsFeed();
    }
    int i=0;
    int j=100;
    int k=200;
    private void demoNewsFeed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int start = i%((text.length()-30));
                            int end = (i+30)%(text.length());
                            if(start >=0 && end >= 0 && start<text.length() && end<text.length()) {
                                t5.setText(text.substring(start, end));
                            }
                            i++;
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int start = j%((text.length()-30));
                            int end = (j+30)%(text.length());
                            if(start >=0 && end >= 0 && start<text.length() && end<text.length()) {
                                t7.setText(text.substring(start, end));
                            }
                            j++;
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int start = k%((text.length()-30));
                            int end = (k+30)%(text.length());
                            if(start >=0 && end >= 0 && start<text.length() && end<text.length()) {
                                t9.setText(text.substring(start, end));
                            }
                            k++;
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


}
