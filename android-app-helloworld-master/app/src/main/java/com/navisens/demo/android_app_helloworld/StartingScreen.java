package com.navisens.demo.android_app_helloworld;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.navisens.demo.android_app_helloworld.utils.Utils;

import net.gotev.speech.Speech;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This is the screen where user will decide whether they are training or replaying a path.
 *
 * Optionally a login once user accounts are added
 */
public class StartingScreen extends AppCompatActivity {

    Button recordPathOpt;
    Button selectPathOpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setupDatabase(getApplicationContext());
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
