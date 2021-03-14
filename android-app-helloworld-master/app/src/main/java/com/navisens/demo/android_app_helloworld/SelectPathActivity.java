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
    private static Map<Long, CardView> cachedPathCards;
    public static LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT);

    List<Path> paths;
    Map<Long, TextView> dists;
    Map <Long, PathPoint> startingPoints;
    PathDatabase db;
    boolean startList = true;
    Location currLocation;
    public static SelectPathActivity curr;
    LayoutInflater inflater;
    LinearLayout pathList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_path);
        curr = this;
        db = Utils.setupDatabase(getApplicationContext());
        if (cachedDistances == null) {
            cachedDistances = new HashMap<Long, Integer>();
        }
        if (cachedPathCards == null) {
            cachedPathCards = new HashMap<Long, CardView>();
        }
        inflater = getLayoutInflater();
        pathList = findViewById(R.id.path_list);
        dists = new HashMap<Long, TextView>();
        startingPoints = new HashMap<Long, PathPoint>();
        cardParams.setMargins(30, 15, 30, 15);


        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                currLocation = location;
                if (startList) {
                    initPathsList(pathList);
                    startList = false;
                } else {
                    updateDists();
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
    }

    private void addCardView(final LinearLayout pathList) {
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
            dists.put(p.pid, dist);
            if (cachedDistances.containsKey(p.pid)) {
                dist.setText("~ " + cachedDistances.get(p.pid) + " meters away");
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pathList.addView(c);
                }
            });

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    PathPoint start = null;
                    if (startingPoints.containsKey(p.pid)) {
                        start = startingPoints.get(p.pid);
                    } else {
                        start = db.getPathPointDao().getFirstPointByPathId(p.pid);
                        startingPoints.put(p.pid, start);
                    }
                    if (start != null) {
                        final int d = (int) Math.round(Utils.estimateDistanceBetweenTwoPoints(new PathPoint(currLocation.getLatitude(), currLocation.getLongitude()), start));
                        if (!cachedDistances.containsKey(p.pid) || d <= 10 || Math.abs(cachedDistances.get(p.pid) - d) >= 5 ) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dist.setText("~ " + d + " meters away");
                                }
                            });
                            cachedDistances.put(p.pid, d);
                        }
                    }
                }
            });
        }
    }

    private void initPathsList(final LinearLayout pathList) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                paths = db.getPathDao().getAll();
                addCardView(pathList);
            }
        });
    }

    private void startNewActivity(Class activity, long pid) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("currentPath", pid);
        startActivity(intent);
    }

    private void updateDists() {
        for (long pid : dists.keySet()) {
            final TextView dist = dists.get(pid);
            final PathPoint start = startingPoints.get(pid);

            if (start != null) {
                final int d = (int) Math.round(Utils.estimateDistanceBetweenTwoPoints(new PathPoint(currLocation.getLatitude(), currLocation.getLongitude()), start));
                if (!cachedDistances.containsKey(pid) || d <= 10 || Math.abs(cachedDistances.get(pid) - d) >= 5) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dist.setText("~ " + d + " meters away");
                        }
                    });
                    cachedDistances.put(pid, d);
                }
            }
        }
    }
}