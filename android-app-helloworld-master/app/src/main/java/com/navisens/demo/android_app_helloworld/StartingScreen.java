package com.navisens.demo.android_app_helloworld;

import android.os.Bundle;

import com.navisens.motiondnaapi.MotionDnaSDK;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * This is the screen where user will decide whether they are training or replaying a path.
 *
 * Optionally a login once user accounts are added
 */
public class StartingScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setupDatabase();
    }
}
