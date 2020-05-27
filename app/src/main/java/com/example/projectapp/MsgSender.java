package com.example.projectapp;


import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import java.nio.file.Files;

public class MsgSender extends AsyncTask<String, SocketInfo, Void> {
    Socket s;
    //    SocketInfo socketInfo = new SocketInfo("192.168.43.174", 8888);
    SocketInfo socketInfo;
    PrintWriter pw;
    InputStreamReader isr;
    BufferedReader bf;
    StringObj str;

    /**
     * constructor.
     */
    public MsgSender(StringObj string, SocketInfo sI) {
        this.str = string;
        this.socketInfo = sI;
    }

    /**
     * does the work for sending the information to the server and splitting down the wav file.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected Void doInBackground(String... voids) {
        String message = voids[0];
//        System.out.println("==========================================");
//        System.out.println("port: " + String.valueOf(socketInfo.getPort()) + "ip: " + socketInfo.getIp());
//        System.out.println("==========================================");
        try {

            try {
                s = new Socket(socketInfo.getIp(), socketInfo.getPort());
            } catch (Exception e) {

            }
            OutputStream output = s.getOutputStream();
            pw = new PrintWriter(output);


            if (str.getStr().startsWith("file")) {
                InputStream input = null;
                input = Files.newInputStream(new File("/storage/emulated/0/Android/data/com.example.projectapp/files/Music/recording_.wav").toPath());

                //send file
                pw.write(message);
                pw.flush();
                byte[] buffer = new byte[4096];

                for (
                        int bytesRead = input.read(buffer);
                        bytesRead != -1;
                        bytesRead = input.read(buffer)) {
                    output.write(buffer, 0, bytesRead);
                    output.flush();
                }
                pw.write("finish");
                pw.flush();
            }


            isr = new InputStreamReader(s.getInputStream());
            bf = new BufferedReader(isr);
            voids[0] = bf.readLine();

            this.str.setStr(voids[0]);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;


    }


}