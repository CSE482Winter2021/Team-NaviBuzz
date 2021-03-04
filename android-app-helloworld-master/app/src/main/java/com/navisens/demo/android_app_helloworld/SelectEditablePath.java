package com.navisens.demo.android_app_helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.navisens.demo.android_app_helloworld.database_obj.Path;
import com.navisens.demo.android_app_helloworld.database_obj.PathDatabase;
import com.navisens.demo.android_app_helloworld.database_obj.PathPoint;
import com.navisens.demo.android_app_helloworld.utils.Utils;

import java.util.List;

public class SelectEditablePath extends AppCompatActivity {
    List<Path> paths;
    PathDatabase db;
    public static SelectEditablePath curr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_path);
        db = Utils.setupDatabase(getApplicationContext());
        curr = this;

        this.getSupportActionBar().hide();

        initPathsList();
    }

    private void addCardView() {
        Context context = getApplicationContext();
        final LinearLayout pathList = findViewById(R.id.path_list);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        cardParams.setMargins(30, 15, 30, 15);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.gravity = Gravity.CENTER_VERTICAL;
        for (final Path p : paths) {
            final CardView c = new CardView(context);
            c.setLayoutParams(cardParams);
            c.setMinimumHeight(200);
            c.setContentPadding(50, 50, 50, 50);
            c.setId((int) p.pid);

            LinearLayout l = new LinearLayout(context);
            l.setOrientation(LinearLayout.VERTICAL);
            TextView t = new TextView(context);
            t.setId((int) p.pid);
            t.setLayoutParams(textParams);
            t.setTextSize(20);
            t.setText(p.name);
            t.setTypeface(null, Typeface.BOLD);
            t.setTextColor(getResources().getColor(R.color.flatBlack));
            l.addView(t);
            c.addView(l);

            c.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startNewActivity(EditPathActivity.class, p.pid);
                }
            });

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pathList.addView(c);
                }
            });

        }
    }

    private void initPathsList() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                paths = db.getPathDao().getAll();
                addCardView();
            }
        });
    }

    private void startNewActivity(Class activity, long pid) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("currentPath", pid);
        startActivity(intent);
    }
}