package com.example.projectapp;


import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class MsgSender extends AsyncTask<String,Void,Void> {
    Socket s;
    DataOutputStream dos;
    PrintWriter pw;

    ServerSocket ss;
    InputStreamReader isr;
    BufferedReader bf;
    String msg;
    InputStream is;
    FileInputStream fis;
    Handler h=new Handler();
    String respond;
    Stringobj str;



    public MsgSender(Stringobj string){
        this.str=string;
    }





    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected Void doInBackground(String... voids) {


        String message=voids[0];
        try{
//            s=new Socket("10.0.0.6", 8888);
//            s=new Socket("10.51.101.65", 8888);
            s=new Socket("192.168.43.174", 8888);
            OutputStream output = s.getOutputStream();
            pw=new PrintWriter(output);


            if (str.getStr().startsWith("file")|| str.getStr().startsWith("new") ){
                InputStream  input = null;
                input = Files.newInputStream(new File("/storage/emulated/0/Android/data/com.example.projectapp/files/Music/recording_.wav").toPath());
//                input = Files.newInputStream(new File("storage/Android/data/com.example.projectapp/files/Music/recording_.wav").toPath());

                Log.d("",message);

                //send file
                Log.d("is equael:", String.valueOf(message.equals("file")));
                Log.d("trying to send file",message);
                pw.write(message);
                pw.flush();
                Log.d("sent word file",message);




                byte[] buffer = new byte[4096];

                for (
                        int bytesRead = input.read(buffer);
                        bytesRead != -1;
                        bytesRead = input.read(buffer)) {
                    Log.d("", String.valueOf(bytesRead));
                    output.write(buffer, 0, bytesRead);
                    output.flush();
                }
                pw.write("finish");
                pw.flush();
            }
            else if(str.getStr().startsWith("LogIn") || str.getStr().startsWith("SignUp")){
                pw.write(message);
                pw.flush();

            }

            isr= new InputStreamReader(s.getInputStream());
            bf=new BufferedReader(isr);
            voids[0]=bf.readLine();

            this.str.setStr(voids[0]);
            if (voids[0]!=null){

                Log.d("",voids[0]);


            }else{
                Log.d("","msg is null");

            }





        }catch(IOException ioe){
            ioe.printStackTrace();
        }
        Log.d("", "sent"+message);
        return null;



    }


}