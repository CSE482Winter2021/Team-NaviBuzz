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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        this.getSupportActionBar().hide();

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
                                EditText t = (EditText) l.getChildAt(0);
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
                                EditText t = (EditText) l.getChildAt(i);
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

//                        AsyncTask.execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                // TODO: change this if Allyson creates a better way to update points
//                                db.getPathPointDao().deleteByPathId(pid);
//                                db.getPathPointDao().addPathPoints(pathPoints);
//                            }
//                        });
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
        pointCards = new HashMap<PathPoint, CardView>();
        instructionList = findViewById(R.id.instruction_list);
        final Context context = instructionList.getContext();

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        cardParams.setMargins(30, 15, 30, 15);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        for (final PathPoint p : pathPoints) {
            if (p.instruction != null || p.landmark != null) {
                final CardView c = new CardView(context);
                c.setLayoutParams(cardParams);
                c.setMinimumHeight(200);
                c.setContentPadding(50, 50, 50, 50);
                c.setId((int) p.pid);
                LinearLayout l = new LinearLayout(context);
                l.setOrientation(LinearLayout.VERTICAL);
                if (p.landmark != null) {
                    System.out.println(p.landmark);
                    EditText t = new EditText(context);
                    t.setText(p.landmark);
                    t.setId((int) p.pid);
                    t.setLayoutParams(textParams);
                    t.setTextSize(20);
//                    t.setTypeface(null, Typeface.BOLD);
                    t.setTextColor(getResources().getColor(R.color.flatBlack));
                    l.addView(t);
                }
                if (p.instruction != null) {
                    System.out.println(p.instruction);
                    EditText t = new EditText(context);
                    t.setText(p.instruction);
                    t.setId((int) p.pid + 1);
                    t.setLayoutParams(textParams);
                    t.setTextSize(20);
//                    t.setTypeface(null, Typeface.BOLD);
                    t.setTextColor(Color.BLACK);
                    l.addView(t);
                }
                c.addView(l);
                pointCards.put(p, c);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        instructionList.addView(c);
                    }
                });
            }
        }
    }
}