package com.navisens.demo.android_app_helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class SelectPathActivity extends AppCompatActivity {
    private static Map<Long, Integer> cachedDistances;
    List<Path> paths;
    PathDatabase db;
    boolean startList = true;
    Location currLocation;
    public static SelectPathActivity curr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_path);
        curr = this;
        db = Utils.setupDatabase(getApplicationContext());
        paths = new ArrayList<Path>();
        if (cachedDistances == null) {
            cachedDistances = new HashMap<Long, Integer>();
        }

        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                currLocation = location;
                if (startList) {
                    initPathsList();
                    startList = false;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            // Todo: Handle this error condition
        }

//        this.getSupportActionBar().hide();
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
            c.setBackgroundColor(Color.WHITE);
            c.setContentPadding(50, 50, 50, 50);
            c.setId((int) p.pid);
            c.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startNewActivity(ReplayPathActivity.class, p.pid);
                }
            });

            LinearLayout l = new LinearLayout(context);
            c.addView(l);
            l.setOrientation(LinearLayout.VERTICAL);
            TextView t = new TextView(context);
            t.setId((int) p.pid);
            t.setLayoutParams(textParams);
            t.setTextSize(20);
            t.setText(p.name);
            t.setTypeface(null, Typeface.BOLD);
            t.setTextColor(getResources().getColor(R.color.flatBlack));
            l.addView(t);

            final TextView dist = new TextView(context);
            dist.setLayoutParams(textParams);
            dist.setTextColor(Color.BLACK);
            if (cachedDistances.containsKey(p.pid)) {
                dist.setText("~ " + cachedDistances.get(p.pid) + " meters away");
            }
            l.addView(dist);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pathList.addView(c);
                }
            });

            PathPoint start = db.getPathPointDao().getFirstPointByPathId(p.pid);
            if (start != null) {
                final int d = (int) Math.round(Utils.estimateDistanceBetweenTwoPoints(new PathPoint(currLocation.getLatitude(), currLocation.getLongitude()), start));
                if (!cachedDistances.containsKey(p.pid) || cachedDistances.get(p.pid) != d) {
                    cachedDistances.put(p.pid, d);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dist.setText("~ " + d + " meters away");
                        }
                    });
                }
            }
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