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
import com.navisens.demo.android_app_helloworld.utils.Utils;

import net.gotev.speech.Speech;

import java.util.List;
import java.util.ArrayList;

public class SelectPathActivity extends AppCompatActivity {
    List<Path> paths;
    LinearLayout pathList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_path);

        pathList = findViewById(R.id.path_list);
        paths = new ArrayList<Path>();
        initPathsList();
        Context context = null;
        for (Path p : paths) {
            /*CardView c = new CardView(context);
            TextView t = new TextView(context);
            t.append(p.name);
            c.addView(t);
            c.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // set selected path as the current path somehow
                    
                    startNewActivity(ReplayPathActivity.class);
                }
            });*/
        }
    }

    private void initPathsList() {}

    private void startNewActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }
}