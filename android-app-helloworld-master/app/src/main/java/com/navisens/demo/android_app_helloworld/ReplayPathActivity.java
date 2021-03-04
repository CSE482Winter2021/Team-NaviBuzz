package com.navisens.demo.android_app_helloworld;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.navisens.demo.android_app_helloworld.database_obj.PathDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.navisens.demo.android_app_helloworld.database_obj.PathPoint;
import com.navisens.demo.android_app_helloworld.utils.Constants;
import com.navisens.demo.android_app_helloworld.utils.Utils;
import com.navisens.motiondnaapi.MotionDna;
import com.navisens.motiondnaapi.MotionDnaSDK;
import com.navisens.motiondnaapi.MotionDnaSDKListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class ReplayPathActivity extends AppCompatActivity implements MotionDnaSDKListener, OnMapReadyCallback {
    long pid;
    LinearLayout instructionList;
    List<PathPoint> pathPoints;
    PathDatabase db;
    Map<PathPoint, CardView> pointCards;
    boolean removeCardFlag;

    MotionDnaSDK motionDnaSDK;
    TextView reportStatusTextView;
    TextView receiveMotionDnaTextView;
    Button startReplayBtn;
    Button pauseReplayBtn;
    Button confirmLandmarkBtn;
    LocationManager manager;
    Location initialGPSLocation;
    TextToSpeech ttobj;
    int currPathCounter;
    GoogleMap map;
    boolean startMap = false;
    PathPoint currLocation;
    PathPoint lastLocation;
    MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_path);
        db = Utils.setupDatabase(getApplicationContext());
        currLocation = new PathPoint(0, 0);
        lastLocation = new PathPoint(0, 0);
        Bundle bundle = getIntent().getExtras();
        pid = getIntent().getLongExtra("currentPath");
        currPathCounter = 0;

        this.getSupportActionBar().hide();
        startReplayBtn = findViewById(R.id.start_path_btn);
        receiveMotionDnaTextView = findViewById(R.id.receiveMotionDnaTextView);
        pauseReplayBtn = findViewById(R.id.pause_path_btn);
        pauseReplayBtn.setEnabled(false);
        confirmLandmarkBtn = findViewById(R.id.confirm_landmark);
        confirmLandmarkBtn.setEnabled(false);
        // testing code
//        confirmLandmarkBtn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                instructionList.removeViewAt(0);
//                CardView c = (CardView) instructionList.getChildAt(0);
//                c.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
//
//                LinearLayout l = (LinearLayout) c.getChildAt(0);
//                int children = l.getChildCount();
//                for (int i = 0; i < children; i++) {
//                    TextView t = (TextView) l.getChildAt(i);
//                    t.setTextColor(Color.WHITE);
//                }
//            }
//        });


        ActivityCompat.requestPermissions(this,MotionDnaSDK.getRequiredPermissions()
                , Constants.REQUEST_MDNA_PERMISSIONS);

        // pull list of pathPoints from database, PathPointDao.getPathById(pid)
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
//                 SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                 Gson gson = new Gson();
//                 System.out.println("taking out pid " + pid);
//                 String jsonText = sp.getString("path " + pid, null);
//                 List<PathPoint> path = new ArrayList<PathPoint>(Arrays.asList(gson.fromJson(jsonText, PathPoint[].class)));
//                 pathPoints = path;
              
                pathPoints = db.getPathPointDao().getByPathId(pid);
                System.out.println("path points are: " + pathPoints.size());
                initCardList();
            }
        });

        manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
  
        // mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                initialGPSLocation = location;
//                 if (startMap) {
//                     startMap();
//                     startMap = false;
//                 }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    private void startMap() {
        mapFragment.getMapAsync(this);
    }

    private void initCardList() {
        pointCards = new HashMap<PathPoint, CardView>();
        instructionList = findViewById(R.id.instruction_list);
        final Context context = instructionList.getContext();

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        cardParams.setMargins(30, 15, 30, 15);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        // textParams.gravity = Gravity.CENTER_VERTICAL;

        for (final PathPoint p : pathPoints) {
            if (p.instruction != null || p.landmark != null) {
                final CardView c = new CardView(context);
                c.setLayoutParams(cardParams);
                c.setMinimumHeight(200);
                c.setContentPadding(50, 50, 50, 50);
                c.setId((int) p.pid);
                LinearLayout l = new LinearLayout(context);
                l.setOrientation(LinearLayout.VERTICAL);
                if (p.landmark != null) {
                    System.out.println(p.landmark);
                    TextView t = new TextView(context);
                    t.setText(p.landmark);
                    t.setId((int) p.pid);
                    t.setLayoutParams(textParams);
                    t.setTextSize(20);
                    t.setTypeface(null, Typeface.BOLD);
                    t.setTextColor(Color.BLACK);
                    l.addView(t);
                }
                if (p.instruction != null) {
                    System.out.println(p.instruction);
                    TextView t = new TextView(context);
                    t.setText(p.instruction);
                    t.setId((int) p.pid);
                    t.setLayoutParams(textParams);
                    t.setTextSize(20);
//                    t.setTypeface(null, Typeface.BOLD);
                    t.setTextColor(Color.BLACK);
                    l.addView(t);
                }
                c.addView(l);
                pointCards.put(p, c);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        instructionList.addView(c);
                    }
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (MotionDnaSDK.checkMotionDnaPermissions(this)) // permissions already requested
        {
            startReplayBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startReplayPath();
                }
            });
            pauseReplayBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    pauseReplayPath();
                }
            });
        }
    }

    public void startReplayPath() {
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            motionDnaSDK = new MotionDnaSDK(this.getApplicationContext(), this);
            motionDnaSDK.startForegroundService();
            HashMap<String, Object> config = new HashMap<String, Object>();
            config.put("gps", false);
            motionDnaSDK.start(Constants.NAVISENS_DEV_KEY, config);
            motionDnaSDK.setGlobalPosition(initialGPSLocation.getLatitude(), initialGPSLocation.getLongitude());
            //double heading = initialGPSLocation.getBearing() < 180 ? initialGPSLocation.getBearing() + 180 : initialGPSLocation.getBearing() - 180;
            motionDnaSDK.setGlobalHeading(initialGPSLocation.getBearing());

            startReplayBtn.setEnabled(false);
            startReplayBtn.setBackgroundTintList(ColorStateList.valueOf(0x00000000));
            startReplayBtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            pauseReplayBtn.setEnabled(true);
            pauseReplayBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            pauseReplayBtn.setTextColor(Color.WHITE);
        } else {
            // service error, GPS is not on
        }
    }

    public void pauseReplayPath() {
        startReplayBtn.setEnabled(true);
        startReplayBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
        startReplayBtn.setTextColor(Color.WHITE);
        pauseReplayBtn.setEnabled(false);
        pauseReplayBtn.setBackgroundTintList(ColorStateList.valueOf(0x00000000));
        pauseReplayBtn.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    public void receiveMotionDna(MotionDna motionDna) {
        String str = "Navisens MotionDnaSDK Estimation:\n";

        currLocation.latitude = motionDna.getLocation().global.latitude;
        currLocation.longitude = motionDna.getLocation().global.longitude;

        double diffBetween = Utils.estimateDistanceBetweenTwoPoints(currLocation, lastLocation);

        if (diffBetween > 3  || lastLocation.longitude == 0) {
            double distanceBetweenPoints = Utils.estimateDistanceBetweenTwoPoints(pathPoints.get(currPathCounter), currLocation);
            System.out.println("in this block");
//             runOnUiThread(new Runnable() {
//                 @Override
//                 public void run() {
//                     map.clear();
//                     for (int i = 0; i < pathPoints.size(); i++) {
//                         PathPoint p = pathPoints.get(i);
//                         int color = i < currPathCounter ? Color.MAGENTA : Color.BLACK;
//                         map.addCircle(new CircleOptions()
//                                 .center(new LatLng(p.latitude, p.longitude))
//                                 .radius(0.5)
//                                 .strokeColor(color)
//                                 .fillColor(color));
//                         if (p.landmark != null && !p.landmark.equals("")) {
//                             map.addCircle(new CircleOptions()
//                                     .center(new LatLng(p.latitude, p.longitude))
//                                     .radius(0.2)
//                                     .strokeColor(Color.BLUE)
//                                     .fillColor(Color.BLUE));
//                         }

//                         if (p.instruction != null && !p.instruction.equals("")) {
//                             map.addCircle(new CircleOptions()
//                                     .center(new LatLng(p.latitude, p.longitude))
//                                     .radius(0.2)
//                                     .strokeColor(Color.GREEN)
//                                     .fillColor(Color.GREEN));
//                         }
//                     }
//                     map.addCircle(new CircleOptions()
//                             .center(new LatLng(currLocation.latitude, currLocation.longitude))
//                             .radius(1)
//                             .strokeColor(Color.BLUE)
//                             .fillColor(Color.BLUE));
//                 }
//             });

            lastLocation = new PathPoint(currLocation);
            if (distanceBetweenPoints < 4) {
                if (removeCardFlag) {
                  confirmLandmarkBtn.setEnabled(false);
                  instructionList.removeViewAt(0);
                }
              
                currPathCounter++;
                PathPoint currPathPoint = pathPoints.get(currPathCounter);
                double distanceToNextPoint = Utils.estimateDistanceBetweenTwoPoints(currPathPoint, currLocation);
                double headingBetweenPoints = Math.abs(Utils.getHeadingBetweenGPSPoints(currPathPoint, currLocation) - 180);

                // Todo: Add unit customization
                final String instructionStr = "walk straight " + Math.round(distanceToNextPoint) + " meters";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       ttobj.speak(instructionStr, TextToSpeech.QUEUE_ADD, null);
                    }
                });

                // For simplicity I'm going to assume they can only set 1 instruction or 1 landmark per point for now
                /*if (!currPathPoint.instruction.equals("") && !currPathPoint.landmark.equals("")) {
                    throw new AssertionError("Can't have an instruction and a landmark (for now)");
                }*/
                
                // TODO: These are instructions for the *next* point, giving them here feels wrong
                String customizedInstruction = currPathPoint.instruction;
                if (customizedInstruction != null && !customizedInstruction.equals("")) {
                    ttobj.speak("An instruction has been set here " + customizedInstruction, TextToSpeech.QUEUE_ADD, null);
                }

                String landmarkStr = currPathPoint.landmark;
                if (landmarkStr != null && !landmarkStr.equals("")) {
                    ttobj.speak("There is a recorded landmark here called " + landmarkStr + " please confirm", TextToSpeech.QUEUE_ADD, null);
                }
              
                // TODO: check that this is happeneing at the correct time
                if (pointCards.containsKey(currPathPoint)) {
                    CardView c = pointCards.get(currPathPoint);
                    c.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    int children = c.getChildCount();
                    for (int i = 0; i < children; i++) {
                        TextView t = (TextView) c.getChildAt(i);
                        t.setTextColor(Color.WHITE);
                    }
                    removeCardFlag = true;
                    if (currPathPoint.landmark != null) {
                        confirmLandmarkBtn.setEnabled(true);
                    }
                }

            }
            printDebugInformation(motionDna, str);
            // Todo: Add landmark confirmation
        } else if (currPathCounter >= pathPoints.size()) { // you're at the destination
            ttobj.speak("You are at your destination", TextToSpeech.QUEUE_ADD, null);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.clear();
                }
            });
            //motionDnaSDK.stop();
        } else {
            // User is not near starting point, we should probably handle this error condition in SelectPathActivity, so this else block shouldn't be possible in that case
        }
    }

    @Override
    public void reportStatus(final MotionDnaSDK.Status status, final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //reportStatusTextView.append(String.format(Locale.US,"Status: %s Message: %s\n",status.toString(),s));
            }
        });
        /*switch (status) {
            case AuthenticationFailure:
                System.out.println("Error: Authentication Failed " + s);
                break;
            case AuthenticationSuccess:
                System.out.println("Status: Authentication Successful " + s);
                break;
            case ExpiredSDK:
                System.out.println("Status: SDK expired " + s);
                break;
            case PermissionsFailure:
                System.out.println("Status: permissions not granted " + s);
                break;
            case MissingSensor:
                System.out.println("Status: sensor missing " + s);
                break;
            case SensorTimingIssue:
                System.out.println("Status: sensor timing " + s);
                break;
            case Configuration:
                System.out.println("Status: configuration " + s);
                break;
            case None:
                System.out.println("Status: None " + s);
                break;
            default:
                System.out.println("Status: Unknown " + s);
        }*/
    }

    protected void onDestroy() {
        // Shuts downs the MotionDna Core
        if (motionDnaSDK != null) {
            motionDnaSDK.stop();
        }

        super.onDestroy();
    }

    private void printDebugInformation(MotionDna motionDna, String str) {
        str += MotionDnaSDK.SDKVersion() + "\n";
        str += "Lat: " + currLocation.latitude + " Lon: " + currLocation.longitude + "\n";
        MotionDna.CartesianLocation location = motionDna.getLocation().cartesian;
        str += String.format(Locale.US," (%.2f, %.2f, %.2f)\n",location.x, location.y, location.z);
        str += "Hdg: " + motionDna.getLocation().global.heading +  " \n";
        str += "motionType: " + Objects.requireNonNull(motionDna.getClassifiers().get("motion")).prediction.label + "\n";

        str += "Predictions (BETA): \n\n";
        HashMap<String, MotionDna.Classifier> classifiers =  motionDna.getClassifiers();
        for (Map.Entry<String, MotionDna.Classifier> entry : classifiers.entrySet()) {
            str += String.format("Classifier: %s\n",entry.getKey());
            str += String.format(Locale.US,"\tcurrent prediction: %s confidence: %.2f\n",entry.getValue().prediction.label, entry.getValue().prediction.confidence);
            str += "\tprediction stats:\n";

            for (Map.Entry<String, MotionDna.PredictionStats> statsEntry : entry.getValue().statistics.entrySet()) {
                str += String.format(Locale.US,"\t%s",statsEntry.getKey());
                str += String.format(Locale.US,"\t duration: %.2f\n",statsEntry.getValue().duration);
                str += String.format(Locale.US,"\t distance: %.2f\n",statsEntry.getValue().distance);
            }
            str += "\n";
        }


        final String fstr = str;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receiveMotionDnaTextView.setText(fstr);
            }
        });
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     * just add a marker near Africa.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //gps = Utils.getLastKnownLocation(manager);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(initialGPSLocation.getLatitude(), initialGPSLocation.getLongitude()), 20f, 0, 0)));
                }
            });
        }
    }
}
