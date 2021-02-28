package com.navisens.demo.android_app_helloworld;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.widget.TextView;


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
        initPathsList();
        Context context = pathList.getContext();
        for (final Path p : paths) {
            CardView c = new CardView(context);
            TextView t = new TextView(context);
            t.append(p.name);
            c.addView(t);
            c.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // set selected path as the current path somehow

                    startNewActivity(ReplayPathActivity.class, p.pid);
                }
            });*/
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
        } else {
            paths = Utils.getUserPaths(db);
        }
    }

    private void startNewActivity(Class activity, int pid) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("currentPath", pid);
        startActivity(intent);
    }
}