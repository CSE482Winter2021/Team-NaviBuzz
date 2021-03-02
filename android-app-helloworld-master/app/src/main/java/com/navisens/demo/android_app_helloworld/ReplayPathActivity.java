package com.navisens.demo.android_app_helloworld;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ReplayPathActivity extends AppCompatActivity implements MotionDnaSDKListener, OnMapReadyCallback {
    private static final boolean TEST = true;
    int pid;
    LinearLayout instructionList;
    List<PathPoint> pathPoints;
    PathPoint lastPoint;
    PathDatabase db;

    MotionDnaSDK motionDnaSDK;
    TextView reportStatusTextView;
    TextView receiveMotionDnaTextView;
    Button startReplayBtn;
    Button pauseReplayBtn;
    Button confirmLandmarkBtn;
    LocationManager manager;
    TextToSpeech ttobj;
    Location gps;
    int currPathCounter;
    GoogleMap map;
    PathPoint currLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_path);
        db = Utils.setupDatabase(getApplicationContext());
        Bundle bundle = getIntent().getExtras();
        pid = bundle.getInt("currentPath");
        currPathCounter = 0;
        instructionList = findViewById(R.id.instruction_list);
        this.getSupportActionBar().hide();
        startReplayBtn = findViewById(R.id.start_path_btn);
        receiveMotionDnaTextView = findViewById(R.id.receiveMotionDnaTextView);
        pauseReplayBtn = findViewById(R.id.pause_path_btn);
        pauseReplayBtn.setEnabled(false);
        //confirmLandmarkBtn = findViewById(R.id.confirm_landmark);
        ActivityCompat.requestPermissions(this,MotionDnaSDK.getRequiredPermissions()
                , Constants.REQUEST_MDNA_PERMISSIONS);

        // pull list of pathPoints from database, PathPointDao.getPathById(pid)
        initPathPoints();
        Context context = instructionList.getContext();
        for (final PathPoint p : pathPoints) {
            CardView c = new CardView(context);
            TextView t = new TextView(context);
            t.append(p.instruction);
            c.addView(t);
        }

        manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });

        lastPoint = pathPoints.get(0);
    }

    private void initPathPoints() {
        if (TEST) {
            pathPoints = new ArrayList<PathPoint>();
        } else {
            pathPoints = Utils.getPointsByPathIdFromDatabase(db, pid);
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
            motionDnaSDK.setGlobalPosition(gps.getLatitude(), gps.getLongitude());
            motionDnaSDK.setGlobalHeading(gps.getBearing());

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

        double distanceBetweenPoints = Utils.estimateDistanceBetweenTwoPoints(pathPoints.get(currPathCounter), currLocation);
        if (distanceBetweenPoints < 5) {
            currPathCounter++;
            PathPoint currPathPoint = pathPoints.get(currPathCounter);
            double distanceToNextPoint = Utils.estimateDistanceBetweenTwoPoints(currPathPoint, currLocation);
            double headingBetweenPoints = Utils.getHeadingBetweenGPSPoints(currPathPoint, currLocation);

            // Todo: Add unit customization
            String instructionStr = "Turn ";
            if (headingBetweenPoints < 0) {
                instructionStr += Math.round(headingBetweenPoints) + " degrees to the left and walk " + Math.round(distanceToNextPoint) + " meters" ;
            } else {
                instructionStr += Math.round(headingBetweenPoints) + " degrees to the right and walk " + Math.round(distanceToNextPoint) + " meters";
            }

            ttobj.speak(instructionStr, TextToSpeech.QUEUE_ADD, null);

            // For simplicity I'm going to assume they can only set 1 instruction or 1 landmark per point for now
            if (!currPathPoint.instruction.equals("") && !currPathPoint.landmark.equals("")) {
                throw new AssertionError("Can't have an instruction and a landmark (for now)");
            }

            String customizedInstruction = currPathPoint.instruction;
            if (!customizedInstruction.equals("")) {
                ttobj.speak(customizedInstruction, TextToSpeech.QUEUE_ADD, null);
            }

            String landmarkStr = currPathPoint.landmark;
            if (!customizedInstruction.equals("")) {
                ttobj.speak(landmarkStr, TextToSpeech.QUEUE_ADD, null);
            }
            printDebugInformation(motionDna, str);
        } else {
            // User is not near starting point, we should probably handle this error condition in SelectPathActivity, so this else block shouldn't be possible in that case
        }
    }

    @Override
    public void reportStatus(final MotionDnaSDK.Status status, final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reportStatusTextView.append(String.format(Locale.US,"Status: %s Message: %s\n",status.toString(),s));
            }
        });
        switch (status) {
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
        }
    }

    protected void onDestroy() {
        // Shuts downs the MotionDna Core
        motionDnaSDK.stop();
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
            gps = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(gps.getLatitude(), gps.getLongitude()), 20f, 0, 0)));
                }
            });
        }
    }
}
