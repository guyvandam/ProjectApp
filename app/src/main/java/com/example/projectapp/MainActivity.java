package com.example.projectapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private Chronometer chronometer;
    private boolean running; // is the chronometer running.
    private EditText ipInput;
    Button buttonStart, buttonStop, buttonEnter;
    MediaRecorder mediaRecorder;
    WavRecorder wavRecorder;
    Random random;
    MsgSender MsgSender;
    Stringobj str;

    public static final int RequestPermissionCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = findViewById(R.id.btnRecord);
        buttonStop = findViewById(R.id.btnStop);
        chronometer = findViewById(R.id.chronometer);
//        ipInput = findViewById(R.id.ipInput);
//        buttonEnter = findViewById(R.id.enter);

        random = new Random();

//        buttonEnter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String ip = ipInput.getText().toString();
//                String[] ipNumbers = ip.split(".");
//                if (ipNumbers.length != 4) {
//                    ipInput.setText("");
//                    Toast.makeText(MainActivity.this, "invalid ip", Toast.LENGTH_LONG);
//                    return;
//                }
//                for (String num : ipNumbers) {
//                    try {
//                        Integer.parseInt(num);
//                    } catch (Exception e) {
//                        ipInput.setText("");
//                        Toast.makeText(MainActivity.this, "invalid ip", Toast.LENGTH_LONG);
//                        return;
//                    }
//                }
//            }
//        });

//        ipInput.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                String ip = ipInput.getText().toString();
//                String[] ipNumbers = ip.split(".");
//                int temp;
//                if (ipNumbers.length != 4) {
//                    ipInput.setText("");
//                    Toast.makeText(MainActivity.this, "invalid ip", Toast.LENGTH_LONG);
//                    return false;
//                }
//                for (String num : ipNumbers) {
//                    try {
//                        Integer.parseInt(num);
//                    } catch (Exception e) {
//                        ipInput.setText("");
//                        Toast.makeText(MainActivity.this, "invalid ip", Toast.LENGTH_LONG);
//                    }
//                    return false;
//                }
//                return true;
//            }
//        });

//        ipInput.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String ip = ipInput.getText().toString();
//                String[] ipNumbers = ip.split(".");
//                int temp;
//                if (ipNumbers.length != 4) {
//                    ipInput.setText("");
//                    Toast.makeText(MainActivity.this, "invalid ip", Toast.LENGTH_LONG);
//                }
//                for (String num : ipNumbers) {
//                    try {
//                        Integer.parseInt(num);
//                    } catch (Exception e) {
//                        ipInput.setText("");
//                        Toast.makeText(MainActivity.this, "invalid ip", Toast.LENGTH_LONG);
//                    }
//                }
//            }
//        });

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    MediaRecorderReady();

                    wavRecorder.startRecording();
                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);
                    startChronometer(view);

                    Toast.makeText(MainActivity.this, "Recording started",
                            Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wavRecorder.stopRecording();
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                pauseChronometer(view);
                Toast.makeText(MainActivity.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();

                send("file");
            }
        });
    }

    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        String outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording_.wav";
        if (getApplicationContext() != null &&
                hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getApplicationContext()) &&
                isExternalStorageWritable() &&
                isExternalStorageReadable() &&
                hasSufficientFreeSpaceAvailable(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath())) {
            outputFile = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recording_.wav").getPath();
        } else if (hasSufficientFreeSpaceAvailable(getApplicationContext().getFilesDir().getPath())) {
            outputFile = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recording_.wav").getPath();
        }
        wavRecorder = new WavRecorder(outputFile);
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static boolean hasPermission(String permission, Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public static boolean hasSufficientFreeSpaceAvailable(String path) {
        StatFs stat = new StatFs(path);
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
        long megAvailable = bytesAvailable / 1048576;
        if (megAvailable > 2) {
            return true;
        }
        return false;
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    //showing main page after login succeeded
    public void showMainPage() {
        buttonStop.setVisibility(View.VISIBLE);
        buttonStart.setVisibility(View.VISIBLE);
//        chronometer.setVisibility(View.INVISIBLE);
        buttonStop.setEnabled(false);
    }


    public void send(String val) {
        String ans = val;
        Log.d("", val);
        str = new Stringobj(ans);
        MsgSender = new MsgSender(str);
        try {
            MsgSender.execute(ans).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        Log.d("state:", String.valueOf(MsgSender.getStatus()));

        if (str.getStr().equals("success") || str.getStr().equals("found")) {
            showMainPage();
        } else {
            Toast.makeText(MainActivity.this, str.getStr(), Toast.LENGTH_LONG).show();

        }
    }

    public void startChronometer(View v) {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            running = true;
        }
    }

    public void pauseChronometer(View v) {
        if (running) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.stop();
            running = false;
        }
    }
}