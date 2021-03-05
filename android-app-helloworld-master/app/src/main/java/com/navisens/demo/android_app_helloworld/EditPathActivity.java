package com.navisens.demo.android_app_helloworld;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.navisens.demo.android_app_helloworld.database_obj.PathDatabase;
import com.navisens.demo.android_app_helloworld.database_obj.PathPoint;
import com.navisens.demo.android_app_helloworld.database_obj.PathPointDao;
import com.navisens.demo.android_app_helloworld.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditPathActivity extends AppCompatActivity {
    long pid;
    LinearLayout instructionList;
    List<PathPoint> pathPoints;
    PathDatabase db;
    Map<PathPoint, CardView> pointCards;
    Button confirmPathBtn;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_path);
//        this.getSupportActionBar().hide();

        db = Utils.setupDatabase(getApplicationContext());
        pid = getIntent().getLongExtra("currentPath", 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(EditPathActivity.this);
        builder.setTitle("Confirm Path Changes")
                .setMessage("Are you ready to save your changes to this path?")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final PathPointDao dao = db.getPathPointDao();
                        // TODO: on positive response, loop through cards and change points, then finish
                        for (final PathPoint p : pointCards.keySet()) {
                            LinearLayout l = (LinearLayout) pointCards.get(p).getChildAt(0);
                            int i = 0;
                            if (p.landmark != null) {
                                EditText t = ((TextInputLayout) l.getChildAt(0)).getEditText();
                                final String landmark = t.getText().toString();
                                i = 1;
                                if (!p.landmark.equals(landmark)) {
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            dao.updateLandmark(p.ppid, landmark);
                                        }
                                    });
                                }
                            }
                            if (p.instruction != null) {
                                EditText t = ((TextInputLayout) l.getChildAt(i)).getEditText();
                                final String instruction = t.getText().toString();
                                if (!p.instruction.equals(instruction)) {
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            dao.updateInstruction(p.ppid, instruction);
                                        }
                                    });
                                }
                            }
                        }
                        SelectEditablePath.curr.finish();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {}
                });
        final AlertDialog dialog = builder.create();

        confirmPathBtn = findViewById(R.id.confirm_path);
        confirmPathBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.show();
            }
        });

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                pathPoints = db.getPathPointDao().getByPathId(pid);
                System.out.println("path points are: " + pathPoints.size());
                initCardList();
            }
        });
    }

    private void initCardList() {
        LayoutInflater inflater = getLayoutInflater();
        pointCards = new HashMap<PathPoint, CardView>();
        instructionList = findViewById(R.id.instruction_list);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        cardParams.setMargins(30, 15, 30, 15);

        for (final PathPoint p : pathPoints) {
             if (p.instruction != null && p.landmark != null) {
                final CardView c = (CardView) inflater.inflate(R.layout.path_point_double_card_editable, null);
                c.setLayoutParams(cardParams);
                c.setId((int) p.pid);

                TextInputLayout landmark = c.findViewById(R.id.landmark);
                final EditText l = landmark.getEditText();

                TextInputLayout instruction = c.findViewById(R.id.instruction);
                final EditText i = instruction.getEditText();

                pointCards.put(p, c);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        l.setText(p.landmark);
                        i.setText(p.instruction);
                        instructionList.addView(c);
                    }
                });

            } else if (p.landmark != null || p.instruction != null) {
                String temp = "";
                String tempType = "";
                if (p.landmark != null) {
                    temp = p.landmark;
                    tempType = "Landmark";
                } else {
                    temp = p.instruction;
                    tempType = "Instruction";
                }

                final String waypoint = temp;
                final String waypointType = tempType;

                final CardView c = (CardView) inflater.inflate(R.layout.path_point_single_card_editable, null);
                c.setLayoutParams(cardParams);
                c.setId((int) p.pid);

                final TextInputLayout info = c.findViewById(R.id.path_point_info);
                final EditText w = info.getEditText();

                pointCards.put(p, c);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        w.setText(waypoint);
                        info.setHint(waypointType);
                        instructionList.addView(c);
                    }
                });
            }
        }
    }
}