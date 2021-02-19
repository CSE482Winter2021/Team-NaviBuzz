package com.navisens.demo.android_app_helloworld;

import android.os.Bundle;

import net.gotev.speech.Speech;

import androidx.appcompat.app.AppCompatActivity;

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

        setContentView(R.layout.audio_transcription_demo);

        Speech.init(this, getPackageName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Speech.getInstance().shutdown();
    }
}
