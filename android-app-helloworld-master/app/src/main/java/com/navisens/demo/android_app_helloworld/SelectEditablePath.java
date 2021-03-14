package com.navisens.demo.android_app_helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.navisens.demo.android_app_helloworld.database_obj.Path;
import com.navisens.demo.android_app_helloworld.database_obj.PathDatabase;
import com.navisens.demo.android_app_helloworld.utils.Utils;

import java.util.List;

public class SelectEditablePath extends AppCompatActivity {
    List<Path> paths;
    PathDatabase db;
    Button deletePathsBtn;
    public static SelectEditablePath curr;
    public static boolean deleteMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_edit_path);
        db = Utils.setupDatabase(getApplicationContext());
        curr = this;
        deleteMode = false;
        deletePathsBtn = findViewById(R.id.delete_paths);

        initPathsList();
        deletePathsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleDeleteMode();
            }
        });
    }

    private void addCardView() {
        LayoutInflater inflater = getLayoutInflater();
        final LinearLayout pathList = findViewById(R.id.path_list);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        cardParams.setMargins(30, 15, 30, 15);

        for (final Path p : paths) {
            final CardView c = (CardView) inflater.inflate(R.layout.path_card, null);
            c.setLayoutParams(cardParams);
            c.setId((int) p.pid);

            final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SelectEditablePath.this);
            builder.setTitle("Confirm Path Deletion")
                    .setMessage("Are you sure you want to delete path " + p.name + "?")
                    .setPositiveButton("Delete Path", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO: delete path
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("deleting path id: " + p.pid);
                                    db.deletePathId(p.pid);
                                }
                            });

                            //TODO: remove card
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pathList.removeView(c);
                                }
                            });
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {}
                    });

            c.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (deleteMode) {
                        builder.show();
                    } else {
                        startNewActivity(EditPathActivity.class, p.pid);
                    }
                }
            });

            TextView name = c.findViewById(R.id.pathName);
            name.setText(p.name);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pathList.addView(c);
                }
            });
        }
    }

    private void initPathsList() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                paths = db.getPathDao().getAll();
                addCardView();
            }
        });
    }

    private void startNewActivity(Class activity, long pid) {
        Intent intent = new Intent(this, activity);
        intent.putExtra("currentPath", pid);
        startActivity(intent);
    }

    private void startNewActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    private void toggleDeleteMode() {
        deleteMode = !deleteMode;
        if (deleteMode) {
            deletePathsBtn.setText("Exit Delete Mode");
        } else {
            deletePathsBtn.setText("Delete Paths");
        }
    }
}