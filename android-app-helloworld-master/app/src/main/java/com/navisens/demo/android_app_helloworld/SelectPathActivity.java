package com.navisens.demo.android_app_helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.widget.TextView;


import com.navisens.demo.android_app_helloworld.database_obj.Path;
import com.navisens.demo.android_app_helloworld.database_obj.PathDatabase;
import com.navisens.demo.android_app_helloworld.database_obj.PathPoint;
import com.navisens.demo.android_app_helloworld.utils.Utils;

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
        LayoutInflater inflater = getLayoutInflater();
        final LinearLayout pathList = findViewById(R.id.path_list);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        cardParams.setMargins(30, 15, 30, 15);

        for (final Path p : paths) {
            final CardView c = (CardView) inflater.inflate(R.layout.path_card_dist, null);
            c.setLayoutParams(cardParams);
            c.setId((int) p.pid);
            c.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startNewActivity(ReplayPathActivity.class, p.pid);
                }
            });

            TextView name = c.findViewById(R.id.pathName);
            name.setText(p.name);

            final TextView dist = c.findViewById(R.id.dist);
            if (cachedDistances.containsKey(p.pid)) {
                dist.setText("~ " + cachedDistances.get(p.pid) + " meters away");
            }
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