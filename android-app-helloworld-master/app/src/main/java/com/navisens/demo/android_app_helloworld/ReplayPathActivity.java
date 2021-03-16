package com.navisens.demo.android_app_helloworld;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButtonToggleGroup;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class ReplayPathActivity extends AppCompatActivity implements MotionDnaSDKListener, OnMapReadyCallback {
    private static final boolean TEST = false;
    private static final boolean DEBUG = false;

    long pid;
    LinearLayout instructionList;
    List<PathPoint> pathPoints;
    PathDatabase db;
    Map<Long, CardView> pointCards;
    boolean removeCardFlag;

    MotionDnaSDK motionDnaSDK;
    TextView reportStatusTextView;
    TextView receiveMotionDnaTextView;
    Button startReplayBtn;
    Button pauseReplayBtn;
    MaterialButtonToggleGroup toggleGroup;
    Button confirmLandmarkBtn;
    LocationManager manager;
    Location initialGPSLocation;
    boolean isGpsUnderThreshold = true;
    TextToSpeech ttobj;
    int currPathCounter;
    boolean hasInitNavisensLocation = false;
    GoogleMap map;
    boolean startMap = true;
    boolean inNavigation = false;
    boolean navigationPaused = false;
    PathPoint currLocation;
    PathPoint lastLocation;
    MapFragment mapFragment;
    long frtime;
    long elapsedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_path);
        db = Utils.setupDatabase(getApplicationContext());
        currLocation = new PathPoint(0, 0);
        lastLocation = new PathPoint(0, 0);
        pid = getIntent().getLongExtra("currentPath", 0);
        currPathCounter = 0;
        pointCards = new HashMap<Long, CardView>();

//        this.getSupportActionBar().hide();
        toggleGroup = findViewById(R.id.btn_container);
        toggleGroup.uncheck(R.id.start_path_btn);
        toggleGroup.check(R.id.start_path_btn);
        startReplayBtn = findViewById(R.id.start_path_btn);
        reportStatusTextView = findViewById(R.id.reportStatusTextView);
        receiveMotionDnaTextView = findViewById(R.id.receiveMotionDnaTextView);
        pauseReplayBtn = findViewById(R.id.pause_path_btn);
        confirmLandmarkBtn = findViewById(R.id.confirm_landmark);

        startReplayBtn.setEnabled(false);
        pauseReplayBtn.setEnabled(false);
        confirmLandmarkBtn.setEnabled(false);

        if (!DEBUG) {
            reportStatusTextView.setVisibility(View.INVISIBLE);
            receiveMotionDnaTextView.setVisibility(View.INVISIBLE);
        }

        ActivityCompat.requestPermissions(this,MotionDnaSDK.getRequiredPermissions()
                , Constants.REQUEST_MDNA_PERMISSIONS);

        // pull list of pathPoints from database, PathPointDao.getPathById(pid)
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
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

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (location.getAccuracy() > Constants.MAX_ALLOWABLE_DISTANCE) {
                    isGpsUnderThreshold = false;
                }
                initialGPSLocation = location;
                if (startMap) {
                    startMap();
                    startMap = false;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            // Todo: Handle this error condition
        }
    }

    private void startMap() {
        mapFragment.getMapAsync(this);
    }

    private void initCardList() {
        LayoutInflater inflater = getLayoutInflater();
        instructionList = findViewById(R.id.instruction_list);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        cardParams.setMargins(30, 15, 30, 15);

        for (int i = 0; i < pathPoints.size(); i++) {
            final PathPoint p = pathPoints.get(i);
            if (p.instruction != null && p.landmark != null) {
                final CardView c = (CardView) inflater.inflate(R.layout.path_point_double_card, null);
                c.setLayoutParams(cardParams);
                c.setId((int) p.pid);

                TextView landmark = c.findViewById(R.id.landmark);
                landmark.setText(p.landmark);

                TextView instruction = c.findViewById(R.id.instruction);
                instruction.setText(p.instruction);

                if (i == 0) {
                    c.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    landmark.setTextColor(Color.WHITE);
                    instruction.setTextColor(Color.WHITE);
                    removeCardFlag = true;
                }

                pointCards.put(p.ppid, c);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        instructionList.addView(c);
                    }
                });

            } else if (p.landmark != null || p.instruction != null) {
                String waypoint = "";
                if (p.landmark != null) waypoint = p.landmark;
                else waypoint = p.instruction;

                final CardView c = (CardView) inflater.inflate(R.layout.path_point_single_card, null);
                c.setLayoutParams(cardParams);
                c.setId((int) p.pid);

                TextView w = c.findViewById(R.id.waypoint);
                w.setText(waypoint);
                if (p.landmark != null) w.setTypeface(null, Typeface.BOLD);

                if (i == 0) {
                    c.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    w.setTextColor(Color.WHITE);
                    removeCardFlag = true;
                }

                pointCards.put(p.ppid, c);
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
            startReplayBtn.setEnabled(true);
            startReplayBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!inNavigation) {
                        inNavigation = true;
                        startReplayPath();
                    } else {
                        navigationPaused = false;
                    }

                }
            });
            pauseReplayBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    pauseReplayPath();
                }
            });
            confirmLandmarkBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // TODO: add landmark confirmation code
                    confirmLandmarkBtn.setEnabled(false);
                    instructionList.removeViewAt(0);
                    removeCardFlag = false;
                }
            });
            toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                @Override
                public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                    if (checkedId == R.id.start_path_btn) {
                        startReplayBtn.setEnabled(false);
                        startReplayBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.antiqueWhite)));
                        startReplayBtn.setTextColor(getResources().getColor(R.color.flatBlack));
                        pauseReplayBtn.setEnabled(true);
                        pauseReplayBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                        pauseReplayBtn.setTextColor(Color.WHITE);
                        if (removeCardFlag) {
                            confirmLandmarkBtn.setEnabled(true);
                            String landmarkStr = pathPoints.get(0).landmark;
                            if (landmarkStr != null && !landmarkStr.equals("")) {
                                ttobj.speak("There is a recorded landmark here called " + landmarkStr + " please confirm", TextToSpeech.QUEUE_ADD, null);
                            }
                        }
                        System.out.println("click on start button, checked = " + isChecked);
                    } else {
                        startReplayBtn.setEnabled(true);
                        startReplayBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                        startReplayBtn.setTextColor(Color.WHITE);
                        pauseReplayBtn.setEnabled(false);
                        pauseReplayBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.antiqueWhite)));
                        pauseReplayBtn.setTextColor(getResources().getColor(R.color.flatBlack));
                        confirmLandmarkBtn.setEnabled(false);
                        System.out.println("click on stop button, checked = " + isChecked);
                    }
                }
            });
        }
    }

    public void startReplayPath() {
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            motionDnaSDK = new MotionDnaSDK(this.getApplicationContext(), this);
            motionDnaSDK.startForegroundService();
            // Todo: A zip exception occasionally gets thrown and crashes the app on starting motionDna
            motionDnaSDK.start(Constants.NAVISENS_DEV_KEY);
        } else {
            // service error, GPS is not on
        }
    }

    public void pauseReplayPath() {
        navigationPaused = true;
    }

    @Override
    public void receiveMotionDna(MotionDna motionDna) {
        if (!navigationPaused) {
            frtime = System.nanoTime();
            if (elapsedTime == 0) {
                // First instruction will play in 8-6 seconds
                elapsedTime = frtime - (6 * Constants.ONESEC_NANOS);
            }

            String str = "Navisens MotionDnaSDK Estimation:\n";

            currLocation.latitude = motionDna.getLocation().global.latitude;
            currLocation.longitude = motionDna.getLocation().global.longitude;

            if (!hasInitNavisensLocation) {
                hasInitNavisensLocation = true;
                double heading = motionDna.getLocation().global.heading;
                motionDnaSDK.setGlobalPositionAndHeading(currLocation.latitude, currLocation.longitude, heading);
            }

            double diffBetween = Utils.estimateDistanceBetweenTwoPoints(currLocation, lastLocation);

            if (diffBetween > 2 || lastLocation.longitude == 0) {
                double distanceBetweenPoints = Utils.estimateDistanceBetweenTwoPoints(pathPoints.get(currPathCounter), currLocation);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.clear();
                        for (int i = 0; i < pathPoints.size(); i++) {
                            PathPoint p = pathPoints.get(i);
                            int color = i < currPathCounter ? Color.MAGENTA : Color.BLACK;
                            map.addCircle(new CircleOptions()
                                    .center(new LatLng(p.latitude, p.longitude))
                                    .radius(0.5)
                                    .strokeColor(color)
                                    .fillColor(color));
                            if (p.landmark != null && !p.landmark.equals("")) {
                                map.addCircle(new CircleOptions()
                                        .center(new LatLng(p.latitude, p.longitude))
                                        .radius(0.2)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.BLUE));
                            }

                            if (p.instruction != null && !p.instruction.equals("")) {
                                map.addCircle(new CircleOptions()
                                        .center(new LatLng(p.latitude, p.longitude))
                                        .radius(0.2)
                                        .strokeColor(Color.GREEN)
                                        .fillColor(Color.GREEN));
                            }
                        }
                        map.addCircle(new CircleOptions()
                                .center(new LatLng(currLocation.latitude, currLocation.longitude))
                                .radius(1)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.BLUE));
                    }
                });

                lastLocation = new PathPoint(currLocation);
                if (distanceBetweenPoints < 3) {
                    if (removeCardFlag) {
                        confirmLandmarkBtn.setEnabled(false);
                        instructionList.removeViewAt(0);
                        removeCardFlag = false;
                    }

                    PathPoint currPathPoint = pathPoints.get(currPathCounter);

                    String customizedInstruction = currPathPoint.instruction;
                    if (customizedInstruction != null && !customizedInstruction.equals("")) {
                        ttobj.speak("An instruction has been set here " + customizedInstruction, TextToSpeech.QUEUE_ADD, null);
                    }

                    String landmarkStr = currPathPoint.landmark;
                    if (landmarkStr != null && !landmarkStr.equals("")) {
                        ttobj.speak("There is a recorded point of interest here called " + landmarkStr + " please confirm", TextToSpeech.QUEUE_ADD, null);
                    }

                    currPathCounter++;
                    if (currPathCounter >= pathPoints.size()) {
                        completeNavigationPath();
                        return;
                    }

                    // For simplicity I'm going to assume they can only set 1 instruction or 1 landmark per point for now
                /*if (!currPathPoint.instruction.equals("") && !currPathPoint.landmark.equals("")) {
                    throw new AssertionError("Can't have an instruction and a landmark (for now)");
                }*/

                    // TODO: check that this is happeneing at the correct time
                    if (pointCards.containsKey(currPathPoint)) {
                        System.out.println("current path point has card");
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
            } else {
                // User is not near starting point, we should probably handle this error condition in SelectPathActivity, so this else block shouldn't be possible in that case
            }

            if ((frtime - elapsedTime) > (8*Constants.ONESEC_NANOS)) {
                elapsedTime = frtime;
                double distanceToNextPoint = Utils.estimateDistanceBetweenTwoPoints(pathPoints.get(currPathCounter), currLocation);
                double headingBetweenPoints = Utils.getHeadingBetweenGPSPoints(currLocation, pathPoints.get(currPathCounter));
                double distanceToTurn = Utils.getHeadingTurnDegrees(motionDna.getLocation().global.heading, headingBetweenPoints);

                // Todo: Add unit customization
                String orientationInstr = "";
                if (Math.abs(distanceToTurn) < 10) {
                    orientationInstr += "Walk straight ";
                } else {
                    orientationInstr += distanceToTurn < 0 ? "Turn left " : "Turn right ";
                    orientationInstr += Math.round(Math.abs(distanceToTurn)) + " degrees and walk ";
                }
                final String instructionStr = orientationInstr + Math.round(distanceToNextPoint) + " meters";

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ttobj.speak(instructionStr, TextToSpeech.QUEUE_ADD, null);
                    }
                });
            }
        }
    }

    private void completeNavigationPath() {
        ttobj.speak("You are at your destination, returning to home screen", TextToSpeech.QUEUE_ADD, null);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                motionDnaSDK.stop();
                Intent intent = new Intent(getApplicationContext(), StartingScreen.class);
                startActivity(intent);
            }
        }, 1000);
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
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                receiveMotionDnaTextView.setText(fstr);
//            }
//        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(initialGPSLocation.getLatitude(), initialGPSLocation.getLongitude()), 20f, 0, 0)));
                }
            });
        }
    }
}
