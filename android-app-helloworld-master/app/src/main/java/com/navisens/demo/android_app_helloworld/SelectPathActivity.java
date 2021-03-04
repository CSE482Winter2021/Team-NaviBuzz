package com.navisens.demo.android_app_helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.widget.TextView;


import com.google.gson.Gson;
import com.google.android.material.resources.TextAppearance;
import com.navisens.demo.android_app_helloworld.database_obj.Path;
import com.navisens.demo.android_app_helloworld.database_obj.PathDatabase;
import com.navisens.demo.android_app_helloworld.database_obj.PathPoint;
import com.navisens.demo.android_app_helloworld.utils.Utils;

import net.gotev.speech.Speech;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class SelectPathActivity extends AppCompatActivity {
    List<Path> paths;
    PathDatabase db;
    boolean startList = true;
    Location currLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_path);
        db = Utils.setupDatabase(getApplicationContext());
        paths = new ArrayList<Path>();


        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                currLocation = location;
                if (startList) {
                    initPathsList();
                    startList = false;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        this.getSupportActionBar().hide();
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
            t.setTextColor(Color.BLACK);
            l.addView(t);
          
//             SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//             Gson gson = new Gson();
//             System.out.println("taking out pid " + i);
//             String jsonText = sp.getString("path " + (i + 1), null);
//             List<PathPoint> path = new ArrayList<PathPoint>(Arrays.asList(gson.fromJson(jsonText, PathPoint[].class)));
//             t.append(paths.get(i).name+ "    ~ " + Math.round(Utils.estimateDistanceBetweenTwoPoints(new PathPoint(currLocation.getLatitude(), currLocation.getLongitude()), path.get(0))) + " meters away");
            
            TextView dist = new TextView(context);
            dist.setLayoutParams(textParams);
            List<PathPoint> path = db.getPathPointDao().getByPathId(p.pid);
            dist.setText("~ " + 
                         Math.round(Utils.estimateDistanceBetweenTwoPoints(new PathPoint(currLocation.getLatitude(), currLocation.getLongitude()), path.get(0))) + 
                         " meters away");
            dist.setTextColor(Color.BLACK);
            l.addView(dist);
            c.addView(l);

            c.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startNewActivity(ReplayPathActivity.class, p.pid);
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
//                 SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                 SharedPreferences.Editor prefsEditor = sp.edit();
//                 int pid = sp.getInt("pid", 0);
//                 List<Path> tmp = new ArrayList<Path>();
//                 for (int i = 1; i <= pid; i++) {
//                     tmp.add(new Path(i, "Path " + i));
//                 }
//                 paths = tmp;
              
                paths = db.getPathDao().getAll();
                // System.out.println("paths are " + paths.size());
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