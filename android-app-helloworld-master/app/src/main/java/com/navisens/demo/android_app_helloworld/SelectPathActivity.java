package com.navisens.demo.android_app_helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.widget.TextView;


import com.google.android.material.resources.TextAppearance;
import com.navisens.demo.android_app_helloworld.database_obj.Path;
import com.navisens.demo.android_app_helloworld.database_obj.PathDatabase;
import com.navisens.demo.android_app_helloworld.utils.Utils;

import net.gotev.speech.Speech;

import java.util.List;
import java.util.ArrayList;

public class SelectPathActivity extends AppCompatActivity {
    private static final boolean TEST = true;
    List<Path> paths;
    LinearLayout pathList;
    PathDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_path);
        db = Utils.setupDatabase(getApplicationContext());

        pathList = findViewById(R.id.path_list);
        this.getSupportActionBar().hide();
        initPathsList();
    }

    private void addCardView() {
        Context context = getApplicationContext();
        for (final Path p : paths) {
            CardView c = new CardView(context);
            c.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            c.setMinimumHeight(200);
            c.setContentPadding(50, 50, 50, 50);
            c.setForegroundGravity(Gravity.CENTER_VERTICAL);
            c.setId((int) p.pid);

            TextView t = new TextView(context);
            t.setId((int) p.pid);
            t.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            t.setTextSize(20);
            t.setText(p.name);
            c.addView(t);
            pathList.addView(c);
            c.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // set selected path as the current path somehow

                    startNewActivity(ReplayPathActivity.class, p.pid);
                }
            });
        }
    }

    private void initPathsList() {
        if (TEST) {
            paths = new ArrayList<Path>();
            for (int i = 1; i <= 5; i++) {
                Path p = new Path();
                p.name = "Test Path " + i;
                p.pid = i;
                paths.add(p);
            }
            addCardView();
        } else {
            AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                    paths = db.getPathDao().getAll();
                    System.out.println("paths are " + paths.size());
                    addCardView();
                }
            });
        }
    }

    private void startNewActivity(Class activity, long pid) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("currentPath", pid);
        startActivity(intent);
    }
}