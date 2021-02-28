package com.navisens.demo.android_app_helloworld;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;

import com.navisens.demo.android_app_helloworld.database_obj.Path;
import com.navisens.demo.android_app_helloworld.database_obj.PathPoint;
import com.navisens.motiondnaapi.MotionDnaSDK;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.ArrayList;

public class ReplayPathActivity extends AppCompatActivity {
    private static final boolean TEST = true;
    // Map<String, Path> paths = new HashMap<String, Path>();
    Path path;
    LinearLayout instructionList;
    List<PathPoint> pathPoints;
    PathPoint lastPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_path);
        Bundle bundle = getIntent().getExtras();
        int pid = bundle.getInt("currentPath");
        instructionList = findViewById(R.id.instruction_list);

        // pull list of pathPoints from database, PathPointDao.getPathById(pid)
        initPathPoints();
        Context context = instructionList.getContext();
        for (final PathPoint p : pathPoints) {
            CardView c = new CardView(context);
            TextView t = new TextView(context);
            t.append(p.instruction);
            c.addView(t);
        }

        navigate();
    }

    private void initPathPoints() {
        if (TEST) {
            pathPoints = new ArrayList<PathPoint>();
        } else {

        }
    }

    // Algorithm for replaying path
    // First get input from user via selection whether they would like to replay path and which ID
    //
    // Check if user is within a certain radius of the start location
    //
    // If not, point out to the user that they are not close to the start location of this path
    //
    // If they are, compute the distance to the next GPS point in order and tell the user
    // how far away it is.
    //
    // As user gets close if there's a turn tell them change in degrees in which direction
    // they must face (relay instruction audio)
    //
    // As user gets close to landmarks, verify that the user is close to landmark a certain
    // amount of degrees away
    protected void navigate() {
        lastPoint = pathPoints.get(0);
    }
}
