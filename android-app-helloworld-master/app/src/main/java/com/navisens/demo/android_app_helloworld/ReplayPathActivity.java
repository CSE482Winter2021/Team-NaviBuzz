package com.navisens.demo.android_app_helloworld;

import android.os.Bundle;

import com.navisens.motiondnaapi.MotionDnaSDK;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ReplayPathActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    // they must face
    //
    // As user gets close to landmarks, verify that the user is close to landmark a certain
    // amount of degrees away
}
