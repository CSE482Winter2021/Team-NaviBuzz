package com.navisens.demo.android_app_helloworld;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.navisens.demo.android_app_helloworld.database_obj.Path;
import com.navisens.demo.android_app_helloworld.database_obj.PathDao;
import com.navisens.demo.android_app_helloworld.database_obj.PathDatabase;
import com.navisens.demo.android_app_helloworld.database_obj.PathPoint;
import com.navisens.demo.android_app_helloworld.database_obj.PathPointDao;
import com.navisens.demo.android_app_helloworld.utils.Utils;

import net.gotev.speech.Speech;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This is the screen where user will decide whether they are training or replaying a path.
 *
 * Optionally a login once user accounts are added
 */
public class StartingScreen extends AppCompatActivity {
    private static final boolean TEST = true;
    private static final int[][][] TEST_PATHS = {
            {{0,0}, {0,5}, {0,10}, {0,11}, {5,11}, {7,11}, {7,11},{5,9},{5,6}},
            {}
    };
    private static final String[][] TEST_LANDMARKS = {
            {"Landmark 1", "Landmark 2", "Card Scanner", null, null, "Elevator", null, null, "Platform"},
            {}
    };
    private static final String[][] TEST_INSTRUCTIONS = {
            {null, null, null, "Turn 90 degrees to the left", null, "take elevator to floor P", "exit the elevator", null, null},
            {}
    };

    Button recordPathOpt;
    Button selectPathOpt;
    PathDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Utils.setupDatabase(getApplicationContext());

        this.getSupportActionBar().hide();

        setContentView(R.layout.activity_starting_screen);
        recordPathOpt = findViewById(R.id.record_path_btn);
        selectPathOpt = findViewById(R.id.replay_path_btn);

        recordPathOpt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startNewActivity(RecordPathActivity.class);
            }
        });

        selectPathOpt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startNewActivity(SelectPathActivity.class);
            }
        });

        Speech.init(this, getPackageName());
        if (TEST) {
            final List<Path> testPaths = new ArrayList<Path>();
            for (int i = 0; i < TEST_PATHS.length; i++) {
                final Path p = new Path();
                p.name = "Test Path " + (i + 1);
                testPaths.add(p);
            }
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    PathDao pathDao = db.getPathDao();
                    PathPointDao pathPointDao = db.getPathPointDao();
//                    pathDao.deleteAll();
                    db.cleanUp();

                    for (int i = 0; i < TEST_PATHS.length; i++) {
                        final Path p = testPaths.get(i);
                        long pid = pathDao.insertPath(p);
//                        pathDao.deletePath(p);
//                        System.err.println("pid: " + pid);
//                        System.err.println(pathPointDao.getByPathId(pid));

                        int[][] testPath = TEST_PATHS[i];
                        String[] landmarks = TEST_LANDMARKS[i];
                        String[] instructions = TEST_INSTRUCTIONS[i];
                        for (int j = 0; j < testPath.length; j++) {
                            PathPoint point = new PathPoint(testPath[j][0], testPath[j][1], pid);

                            if (landmarks[j] != null) {
                                point.landmark = landmarks[j];
                            }
                            if (instructions[j] != null) {
                                point.instruction = instructions[j];
                            }
                            pathPointDao.addPoint(point);
                        }
                    }
                }
            });
        }
    }

    private void startNewActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Speech.getInstance().shutdown();
    }
}
