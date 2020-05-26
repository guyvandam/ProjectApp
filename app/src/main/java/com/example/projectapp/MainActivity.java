package com.example.projectapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private Chronometer chronometer;
    private boolean running; // is the chronometer running.
    Button buttonStart, buttonStop;
    MediaRecorder mediaRecorder;
    WavRecorder wavRecorder;
    Random random;
    MsgSender MsgSender;
    StringObj str;

    public static final int RequestPermissionCode = 1;

    /**
     * the 'run' function.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonStart = findViewById(R.id.btnRecord);
        buttonStop = findViewById(R.id.btnStop);
        chronometer = findViewById(R.id.chronometer);

        random = new Random();

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

    /**
     *
     */
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

    /**
     * requests permission from the user to access the phone storage and record audio.
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    /**
     * an android function as seen by the override, presents whether the permission was granted
     * with the Toast prints.
     */
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

    /**
     * @return if the access to the data is possible.
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * @return if the permission to record audio is granted.
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * @return if we have permission to write to users calender data.
     */
    public static boolean hasPermission(String permission, Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    /**
     * @return if we have enough free space to store our temporary wav file.
     */
    public static boolean hasSufficientFreeSpaceAvailable(String path) {
        StatFs stat = new StatFs(path);
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
        long megAvailable = bytesAvailable / 1048576;
        if (megAvailable > 2) {
            return true;
        }
        return false;
    }

    /**
     * @return if we have permission to write to the data and record audio.
     */
    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * sends the message to the server followed by the bytes of the wav file.
     *
     * @param val
     */
    public void send(String val) {
        String ans = val;
        str = new StringObj(ans);
        MsgSender = new MsgSender(str);
        try {
            MsgSender.execute(ans).get(10000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            Toast.makeText(MainActivity.this, "something is wrong with the network ",
                    Toast.LENGTH_LONG).show();
        }
        Toast.makeText(MainActivity.this, str.getStr(), Toast.LENGTH_LONG).show();
    }

    /**
     * start the chronometer, our 'stopwatch'
     */
    public void startChronometer(View v) {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            running = true;
        }
    }

    /**
     * stops and resets the chronometer, our 'stopwatch'
     */
    public void pauseChronometer(View v) {
        if (running) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.stop();
            running = false;
        }
    }
}