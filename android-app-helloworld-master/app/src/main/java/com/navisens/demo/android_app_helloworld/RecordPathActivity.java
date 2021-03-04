package com.navisens.demo.android_app_helloworld;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.AlphabeticIndex;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.navisens.demo.android_app_helloworld.database_obj.Path;
import com.navisens.demo.android_app_helloworld.database_obj.PathDatabase;
import com.navisens.demo.android_app_helloworld.database_obj.PathPoint;
import com.navisens.demo.android_app_helloworld.utils.Constants;
import com.navisens.demo.android_app_helloworld.utils.ErrorState;
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
import java.util.UUID;

/*
 * For complete documentation on the MotionDnaSDK API
 * Please go to the following link:
 * https://github.com/navisens/NaviDocs/blob/master/API.Android.md
 */
public class RecordPathActivity extends AppCompatActivity implements MotionDnaSDKListener, OnMapReadyCallback {
    MotionDnaSDK motionDnaSDK;
    TextView receiveMotionDnaTextView;
    TextView reportStatusTextView;
    Button startPathBtn;
    Button stopPathBtn;
    Button recordInstructionBtn;
    Button seeDebugText;
    EditText landmarkName;
    EditText instructionString;
    GoogleMap map;
    PathDatabase db;
    Button recordLandmarkBtn;
    long pathId;
    String pathName;
    List<PathPoint> currPath = new ArrayList<PathPoint>();
    LocationManager manager;
    PathPoint lastLocation;
    PathPoint currLocation;
    boolean startMap = true;
    Location initialGPSLocation;
    MapFragment mapFragment;
    Context context;
    boolean isDebugToggled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_path);
        db = Utils.setupDatabase(getApplicationContext());
        receiveMotionDnaTextView = findViewById(R.id.receiveMotionDnaTextView);
        receiveMotionDnaTextView.setVisibility(View.INVISIBLE);
        reportStatusTextView = findViewById(R.id.reportStatusTextView);
        startPathBtn = findViewById(R.id.start_record_btn);
        landmarkName = findViewById(R.id.landmark_name);
        instructionString = findViewById(R.id.instruction_text);
        recordInstructionBtn = findViewById(R.id.record_instruction);
        stopPathBtn = findViewById(R.id.stop_record_btn);
        recordLandmarkBtn = findViewById(R.id.record_landmark);
        seeDebugText = findViewById(R.id.see_debug_text);
        context = getApplicationContext();

        LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(R.layout.path_name_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(RecordPathActivity.this);
        builder.setTitle("Input Path Title")
            .setView(v)
            .setPositiveButton("Set Name", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {}
            });

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pathName = ((EditText) v.findViewById(R.id.path_name)).getText().toString();
                System.out.println(pathName);
                if (pathName.length() > 0) {
                    dialog.dismiss();
                    initPath(pathName);
                }
            }
        });

        recordLandmarkBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recordLandmark();
            }
        });
        
        this.getSupportActionBar().hide();
        
        recordInstructionBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recordInstruction();
            }
        });
      
        seeDebugText.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                isDebugToggled = !isDebugToggled;
                if (isDebugToggled) {
                    receiveMotionDnaTextView.setVisibility(View.VISIBLE);
                    recordInstructionBtn.setVisibility(View.INVISIBLE);
                    instructionString.setVisibility(View.INVISIBLE);
                    landmarkName.setVisibility(View.INVISIBLE);
                    startPathBtn.setVisibility(View.INVISIBLE);
                    stopPathBtn.setVisibility(View.INVISIBLE);
                    recordInstructionBtn.setVisibility(View.INVISIBLE);
                    recordLandmarkBtn.setVisibility(View.INVISIBLE);
                } else {
                    receiveMotionDnaTextView.setVisibility(View.INVISIBLE);
                    recordInstructionBtn.setVisibility(View.VISIBLE);
                    instructionString.setVisibility(View.VISIBLE);
                    landmarkName.setVisibility(View.VISIBLE);
                    startPathBtn.setVisibility(View.VISIBLE);
                    stopPathBtn.setVisibility(View.VISIBLE);
                    recordInstructionBtn.setVisibility(View.VISIBLE);
                    recordLandmarkBtn.setVisibility(View.VISIBLE);
                }
            }
        });
      
        stopPathBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopRecordingPath();
            }
        });
        stopPathBtn.setEnabled(false);
        landmarkName.setEnabled(false);
        instructionString.setEnabled(false);
        recordLandmarkBtn.setEnabled(false);
        recordInstructionBtn.setEnabled(false);
        // Requests app
        ActivityCompat.requestPermissions(this, MotionDnaSDK.getRequiredPermissions()
                , Constants.REQUEST_MDNA_PERMISSIONS);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
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

    private void initPath(String name) {
        // Generate a new entry to the path table
        final Path p = new Path();
        p.name = name;
        // TODO: replace with path name gotten from dialogue
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pathId = db.getPathDao().insertPath(p);
                lastLocation = new PathPoint(0, 0, pathId);
                currLocation = new PathPoint(0, 0, pathId);
            }
        });
        // TODO: Make sure to delete the is path if there is a failure but we need to path id
        lastLocation = new PathPoint(0, 0, pathId);
        currLocation = new PathPoint(0, 0, pathId);
    }

    private void startMap() {
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (MotionDnaSDK.checkMotionDnaPermissions(this)) // permissions already requested
        {
            startPathBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    startRecordingPath();
                }
            });
        }
    }

    public void startRecordingPath() {
        //mapFragment.getMapAsync(this);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            motionDnaSDK = new MotionDnaSDK(this.getApplicationContext(), this);
            motionDnaSDK.startForegroundService();
            HashMap<String, Object> config = new HashMap<String, Object>();
            config.put("gps", false);
            motionDnaSDK.start(Constants.NAVISENS_DEV_KEY, config);
            motionDnaSDK.setGlobalPosition(initialGPSLocation.getLatitude(), initialGPSLocation.getLongitude());
            //double heading = initialGPSLocation.getBearing() < 180 ? initialGPSLocation.getBearing() + 180 : initialGPSLocation.getBearing() - 180;
            motionDnaSDK.setGlobalHeading(initialGPSLocation.getBearing());
            stopPathBtn.setEnabled(true);
            landmarkName.setEnabled(true);
            instructionString.setEnabled(true);
            recordInstructionBtn.setEnabled(true);
            recordLandmarkBtn.setEnabled(true);
            startPathBtn.setEnabled(false);
        } else {
            // service error, GPS is not on
        }
    }

    public void stopRecordingPath() {
        motionDnaSDK.stop();
        AsyncTask.execute(new Runnable() {
              @Override
              public void run() {
                  db.getPathPointDao().addPathPoints(currPath);
                  System.out.println(currPath);
//                   List<Path> paths = db.getPathDao().getAll();
//                   System.out.println("paths are " + paths.size());
                  currPath.clear();
              }
          });
//         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//         SharedPreferences.Editor prefsEditor = sp.edit();
//         int pid = sp.getInt("pid", 0) + 1;
//         prefsEditor.putInt("pid", 1);

//         Gson gson = new Gson();
//         List<PathPoint> textList = new ArrayList<PathPoint>(currPath);
//         String jsonText = gson.toJson(textList);
//         System.out.println("putting in pid " + pid);
//         prefsEditor.putString("path " + 1, jsonText);
//         prefsEditor.apply();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                map.clear();
            }
        });
        stopPathBtn.setEnabled(false);
        landmarkName.setEnabled(false);
        instructionString.setEnabled(false);
        recordInstructionBtn.setEnabled(false);
        recordLandmarkBtn.setEnabled(false);
        startPathBtn.setEnabled(true);
        finish();
//         startNewActivity(SavePathActivity.class, pathId);
    }

    @Override
    public void receiveMotionDna(MotionDna motionDna) {
        String str = "Navisens MotionDnaSDK Estimation:\n";

        currLocation.latitude = motionDna.getLocation().global.latitude;
        currLocation.longitude = motionDna.getLocation().global.longitude;

        double diffBetween = Utils.estimateDistanceBetweenTwoPoints(currLocation, lastLocation);

        // Update location history if necessary
        if (diffBetween > 3  || lastLocation.longitude == 0) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    map.clear();
                    for (PathPoint p : currPath) {
                        map.addCircle(new CircleOptions()
                                .center(new LatLng(p.latitude, p.longitude))
                                .radius(0.5)
                                .strokeColor(Color.BLACK)
                                .fillColor(Color.BLACK));
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
            currPath.add(new PathPoint(currLocation.latitude, currLocation.longitude, pathId));
            lastLocation = new PathPoint(currLocation);

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(currLocation.latitude, currLocation.longitude), 20f, 0, 0)));
                }
            });
        }

        printDebugInformation(motionDna, str);
    }


    // Todo: Propagate these error conditions to the user through speech/text
    @Override
    public void reportStatus(final MotionDnaSDK.Status status, final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reportStatusTextView.append(String.format(Locale.US, "Status: %s Message: %s\n", status.toString(), s));
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

    // Helper to print some diagnostics about Navisens
    private void printDebugInformation(MotionDna motionDna, String str) {
        str += MotionDnaSDK.SDKVersion() + "\n";
        str += "Lat: " + motionDna.getLocation().global.latitude + " Lon: " + motionDna.getLocation().global.longitude + "\n";
        MotionDna.CartesianLocation location = motionDna.getLocation().cartesian;
        str += String.format(Locale.US, " (%.2f, %.2f, %.2f)\n", location.x, location.y, location.z);
        str += "Hdg: " + motionDna.getLocation().global.heading + " \n";
        str += "motionType: " + Objects.requireNonNull(motionDna.getClassifiers().get("motion")).prediction.label + "\n";

        str += "Predictions (BETA): \n\n";
        HashMap<String, MotionDna.Classifier> classifiers = motionDna.getClassifiers();
        for (Map.Entry<String, MotionDna.Classifier> entry : classifiers.entrySet()) {
            str += String.format("Classifier: %s\n", entry.getKey());
            str += String.format(Locale.US, "\tcurrent prediction: %s confidence: %.2f\n", entry.getValue().prediction.label, entry.getValue().prediction.confidence);
            str += "\tprediction stats:\n";

            for (Map.Entry<String, MotionDna.PredictionStats> statsEntry : entry.getValue().statistics.entrySet()) {
                str += String.format(Locale.US, "\t%s", statsEntry.getKey());
                str += String.format(Locale.US, "\t duration: %.2f\n", statsEntry.getValue().duration);
                str += String.format(Locale.US, "\t distance: %.2f\n", statsEntry.getValue().distance);
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

    protected ErrorState recordLandmark() {
        if (currPath.isEmpty()) {
            return new ErrorState("An unknown error occurred, please try again", false);
        }

        String landmark = landmarkName.getText().toString();
        System.out.println(landmark);
        landmarkName.setText("");
        landmarkName.clearFocus();
        currPath.get(currPath.size() - 1).landmark = landmark;
        return new ErrorState("Success", true);
    }

    protected ErrorState recordInstruction() {
        if (currPath.isEmpty()) {
            return new ErrorState("An unknown error occurred, please try again", false);
        }

        String instruction = instructionString.getText().toString();
        System.out.println(instruction);
        instructionString.setText("");
        instructionString.clearFocus();
        currPath.get(currPath.size() - 1).instruction = instruction;
        return new ErrorState("Success", true);
    }

    protected void onDestroy() {
        //TODO: delete path by id if it exists and there is an error
        // Shuts downs the MotionDna Core
        if (motionDnaSDK != null) {
            motionDnaSDK.stop();
        }
        super.onDestroy();
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

    private void startNewActivity(Class activity, long pid) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("currentPath", pid);
        startActivity(intent);
    }
}
