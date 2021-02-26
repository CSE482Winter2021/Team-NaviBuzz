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
import com.navisens.demo.android_app_helloworld.utils.ErrorState;
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
    boolean currentlyTraveling = false;
    List<CoordinatePoint> currPath = new ArrayList<CoordinatePoint>();

    // Vars used for latitude/longitude estimation when GPS is off
    CoordinatePoint lastLocation;
    double lastCumulativeDistanceTraveled;

    private static final int REQUEST_MDNA_PERMISSIONS=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_path);
        receiveMotionDnaTextView = findViewById(R.id.receiveMotionDnaTextView);
        reportStatusTextView = findViewById(R.id.reportStatusTextView);
        startPathBtn = findViewById(R.id.start_record_btn);
        stopPathBtn = findViewById(R.id.stop_record_btn);
        recordLandmarkBtn = findViewById(R.id.confirm_landmark);
        lastLocation = new CoordinatePoint(0, 0);
        recordLandmarkBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recordLandmark();
            }
        });
        landmarkNameTextView = findViewById(R.id.landmark_name);
        // Requests app
        ActivityCompat.requestPermissions(this,MotionDnaSDK.getRequiredPermissions()
                , REQUEST_MDNA_PERMISSIONS);
    }

    /*
     * Just a note:
     *  Currently this is working on my android phone reasonably accurately
     *
     *  motiondna starts with an initial GPS location and then it overlays a cartesian plane on top of it
     *
     *  It keeps track of your position using the sensors on your phone in this cartesian plane
     */


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
        String devKey = "hsW5F8tUr8nPLP1hgY2oj2Zy26iqZ7YCPK4mTEnTsNpj0l0yRwGfj33m3GUL0vCF";

        motionDnaSDK = new MotionDnaSDK(this.getApplicationContext(),this);
        motionDnaSDK.startForegroundService();
        //    This functions starts up the SDK. You must pass in a valid developer's key in order for
        //    the SDK to function. IF the key has expired or there are other errors, you may receive
        //    those errors through the reportError() callback route.
        motionDnaSDK.start(devKey);
    }

    //    This event receives the estimation results using a MotionDna object.
    //    Check out the Getters section to learn how to read data out of this object.
    @Override
    public void receiveMotionDna(MotionDna motionDna)
    {
        CoordinatePoint newLocation = new CoordinatePoint(motionDna.getLocation().global.latitude, motionDna.getLocation().global.longitude);

        // Algorithm for recording path
        // Start a thread dedicated checking whether the location has changed within a certain
        // radius. Store a temporary list for the locations so thus far in the path
        //
        // If location has changed and location doesn't already exist for that path, post it to the
        // database
        //
        // Once flag stoppedPath = true from a uuser input, then the thread for checking whether the location has
        // changed can stop

        // If user records landmark stoppedPath = true, then record the landmark associated with this
        // GPS point

        printDebugInformation(motionDna);
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

    // Helper to print some diagnostics about navisens
    private void printDebugInformation(MotionDna motionDna) {

        /*
         * Check if GPS is on and if it is, use it
         *
         * if GPS is off, use estimation that updates every 40ms with dist + heading to
         * estimate the longitude/latitude. Long travels without GPS will add cumulatively more
         * error due to the 40ms latency with checking. This is then a beta stage feature until
         * this problem gets resolved or significantly reduced, it's also very inaccurate 300-100m right now
         */
        String str = "Navisens MotionDnaSDK Estimation:\n";

        LocationManager manager = (LocationManager)this.getSystemService (Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            str += "GPS is on \n";
            lastLocation.setLatitude(motionDna.getLocation().global.latitude);
            lastLocation.setLongitude(motionDna.getLocation().global.longitude);
        } else if (lastLocation.getLatitude() != 0 || lastLocation.getLongitude() != 0) {
            str += "GPS is off, using lat/long estimation";

            double distanceTraveled = motionDna.getClassifiers().get("indoorOutdoor").statistics.get("indoor").distance;
            // This means this is the first time this block was reached. Set the distance traveled
            // and let 40ms go by
            if (lastCumulativeDistanceTraveled == 0) {
                lastCumulativeDistanceTraveled = distanceTraveled;
            } else if (Math.abs(lastCumulativeDistanceTraveled - distanceTraveled) > 0.5) {
                lastLocation = estimateLongitudeLatitude(lastLocation, distanceTraveled - lastCumulativeDistanceTraveled, motionDna.getLocation().global.heading);
                lastCumulativeDistanceTraveled = distanceTraveled;
            }
        } else {
            // This means GPS was never able to be used to get an initial location, this means
            // service unavailable because we can't estimate location
            str += "Service unavailable, GPS was never on";
        }

        str += MotionDnaSDK.SDKVersion() + "\n";
        str += "Lat: " + lastLocation.getLatitude() + " Lon: " + lastLocation.getLongitude() + "\n";
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
     * Estimate a new location given a lastLocation, distance traveled and heading
     *
     * Precondition is that the distance traveled was the same heading throughout
     *
     * @param lastLocation
     * @param lastCumulativeDistanceTraveled
     * @param heading
     * @return
     */
    private CoordinatePoint estimateLongitudeLatitude(CoordinatePoint lastLocation, double lastCumulativeDistanceTraveled, double heading) {
        /*
         * Credit for this idea https://stackoverflow.com/questions/7222382/get-lat-long-given-current-point-distance-and-bearing
         */
        if (lastLocation == null || lastCumulativeDistanceTraveled == 0) {
            throw new IllegalArgumentException();
        }

        double earthRadius = 6371.0088;
        double hInRadians = Math.toRadians(heading);
        double dInkm = lastCumulativeDistanceTraveled / 1000;

        double longitudeInRadians = Math.toRadians(lastLocation.getLongitude());
        double latitudeInRadians = Math.toRadians(lastLocation.getLatitude());

        double newLat = Math.asin(Math.sin(latitudeInRadians) * Math.cos(dInkm / earthRadius) + Math.cos(latitudeInRadians) * Math.sin(dInkm / earthRadius) * Math.cos(hInRadians));
        double newLong = longitudeInRadians + Math.atan2(Math.sin(hInRadians) * Math.sin(dInkm / earthRadius) * Math.cos(latitudeInRadians), Math.cos(dInkm / earthRadius) - Math.sin(latitudeInRadians) * Math.sin(newLat));

        return new CoordinatePoint(Math.toDegrees(newLat), Math.toDegrees(newLong));

    }

    protected ErrorState recordLandmark() {
        if (!currentlyTraveling) {
            return new ErrorState("You are not on a path, cannot create landmark", false);
        }

        if (currPath.isEmpty()) {
            return new ErrorState("An unknown error occurred, please try again", false);
        }

        CoordinatePoint lastLoc = currPath.get(currPath.size() - 1);

        // Grab name and generate UUID
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
