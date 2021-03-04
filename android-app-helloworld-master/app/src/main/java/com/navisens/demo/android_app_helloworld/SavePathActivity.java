package com.navisens.demo.android_app_helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class SavePathActivity extends AppCompatActivity {
    long pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_path);
        Bundle bundle = getIntent().getExtras();
        pid = bundle.getLong("currentPath");
    }
}