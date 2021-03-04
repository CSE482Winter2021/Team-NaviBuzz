package com.navisens.demo.android_app_helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
//import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.widget.TextView;


import com.google.gson.Gson;
import com.navisens.demo.android_app_helloworld.database_obj.Path;
import com.navisens.demo.android_app_helloworld.database_obj.PathDatabase;
import com.navisens.demo.android_app_helloworld.database_obj.PathPoint;
import com.navisens.demo.android_app_helloworld.utils.Utils;

import net.gotev.speech.Speech;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class SelectPathActivity extends AppCompatActivity {
    private static final boolean TEST = false;
    List<Path> paths;
    LinearLayout pathList;
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

        pathList = findViewById(R.id.path_list);
        this.getSupportActionBar().hide();
    }

    private void addCardView() {
        Context context = getApplicationContext();
        for (int i = paths.size() - 1; i >=0; i--) {
            final CardView c = new CardView(context);
            c.setMinimumHeight(200);
            c.setContentPadding(0, 5, 0, 0);
            TextView t = new TextView(context);
            t.setTextSize(20);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();
            System.out.println("taking out pid " + i);
            String jsonText = sp.getString("path " + (i + 1), null);
            List<PathPoint> path = new ArrayList<PathPoint>(Arrays.asList(gson.fromJson(jsonText, PathPoint[].class)));
            t.append(paths.get(i).name+ "    ~ " + Math.round(Utils.estimateDistanceBetweenTwoPoints(new PathPoint(currLocation.getLatitude(), currLocation.getLongitude()), path.get(0))) + " meters away");
            c.addView(t);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    pathList.addView(c, 0);
                }
            });
            final int co = i;
            c.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // set selected path as the current path somehow
                    // Todo: If they are too far away from the path don't let them select it

                    startNewActivity(ReplayPathActivity.class, (int) paths.get(co).pid);
                }
            });
        }
    }

    private void initPathsList() {
        if (TEST) {
            /*paths = new ArrayList<Path>();
            for (int i = 1; i <= 5; i++) {
                Path p = new Path();
                p.name = "Test Path " + i;
                p.pid = i;
                paths.add(p);
            }*/
        } else {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    /*paths = db.getPathDao().getAll();
                    System.out.println("paths are " + paths.size());*/
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor prefsEditor = sp.edit();
                    int pid = sp.getInt("pid", 0);
                    List<Path> tmp = new ArrayList<Path>();
                    for (int i = 1; i <= pid; i++) {
                        tmp.add(new Path(i, "Path " + i));
                    }
                    paths = tmp;

                    addCardView();
                }
            });
        }
    }

    private void startNewActivity(Class activity, int pid) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("currentPath", pid);
        startActivity(intent);
    }
}