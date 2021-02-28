package com.navisens.demo.android_app_helloworld;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.navisens.demo.android_app_helloworld.database_obj.CoordinatePoint;
import com.navisens.demo.android_app_helloworld.database_obj.Landmark;
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
public class RecordPathActivity extends AppCompatActivity implements MotionDnaSDKListener {
    MotionDnaSDK motionDnaSDK;
    TextView receiveMotionDnaTextView;
    TextView reportStatusTextView;
    Button startPathBtn;
    Button stopPathBtn;
    Button recordLandmarkBtn;
    TextView landmarkNameTextView;
    List<CoordinatePoint> currPath = new ArrayList<CoordinatePoint>();
    CoordinatePoint lastLocation;
    CoordinatePoint currLocation;
    double lastCumulativeDistanceTraveled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_path);
        receiveMotionDnaTextView = findViewById(R.id.receiveMotionDnaTextView);
        reportStatusTextView = findViewById(R.id.reportStatusTextView);
        startPathBtn = findViewById(R.id.start_record_btn);
        stopPathBtn = findViewById(R.id.stop_record_btn);
        recordLandmarkBtn = findViewById(R.id.record_landmark);
        lastLocation = new CoordinatePoint(0, 0);
        currLocation = new CoordinatePoint(0, 0);
        recordLandmarkBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recordLandmark();
            }
        });
        stopPathBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopRecordingPath();
            }
        });
        landmarkNameTextView = findViewById(R.id.landmark_name);
        // Requests app
        ActivityCompat.requestPermissions(this,MotionDnaSDK.getRequiredPermissions()
                , Constants.REQUEST_MDNA_PERMISSIONS);
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
        motionDnaSDK = new MotionDnaSDK(this.getApplicationContext(),this);
        motionDnaSDK.startForegroundService();
        //    This functions starts up the SDK. You must pass in a valid developer's key in order for
        //    the SDK to function. IF the key has expired or there are other errors, you may receive
        //    those errors through the reportError() callback route.
        motionDnaSDK.start(Constants.NAVISENS_DEV_KEY);
    }

    public void stopRecordingPath() {
        motionDnaSDK.stop();
        // Todo: put in database
        currPath.clear();
    }

    @Override
    public void receiveMotionDna(MotionDna motionDna)
    {
        String str = "Navisens MotionDnaSDK Estimation:\n";

        LocationManager manager = (LocationManager)this.getSystemService (Context.LOCATION_SERVICE);
        boolean isGPSOn = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        double distanceTraveled = motionDna.getClassifiers().get("indoorOutdoor").statistics.get("indoor").distance;
        str += Utils.getNewCoordinates(currLocation, distanceTraveled - lastCumulativeDistanceTraveled, motionDna, isGPSOn);

        lastCumulativeDistanceTraveled = distanceTraveled;


        // Update location history if necessary
        if (Utils.estimateDistanceBetweenTwoPoints(currLocation, lastLocation) > 5) {
            currPath.add(currLocation);
            lastLocation = currLocation;
        }

        printDebugInformation(motionDna, str);
    }


    // Todo: Propagate these error conditions to the user through speech/text
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

    // Helper to print some diagnostics about Navisens
    private void printDebugInformation(MotionDna motionDna, String str) {
        str += MotionDnaSDK.SDKVersion() + "\n";
        str += "Lat: " + currLocation.getLatitude() + " Lon: " + currLocation.getLongitude() + "\n";
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

    protected ErrorState recordLandmark() {
        if (currPath.isEmpty()) {
            return new ErrorState("An unknown error occurred, please try again", false);
        }

        // Todo: Need to implement collection of landmark name from user
        String name = "";

        UUID id = UUID.randomUUID();
        Landmark landmark = new Landmark(name, id);
        currPath.get(currPath.size() - 1).setLandmark(landmark);
        return new ErrorState("Success", true);
    }

    protected void onDestroy() {
        // Shuts downs the MotionDna Core
        motionDnaSDK.stop();
        super.onDestroy();
    }
}
