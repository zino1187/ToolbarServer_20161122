package com.solu.toolbarserver;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/*
 소켓 프로그램과 마찬가지로, 클라이언트의 메세지를
 지속적으로 받고 보낼 무한루프를 실행할 쓰레드 정의!!
*/
public class ServerThread extends Thread{
    MainActivity mainActivity;
    boolean flag=true;
    BluetoothSocket socket;
    BufferedReader buffr;
    BufferedWriter buffw;

    public ServerThread(MainActivity mainActivity,BluetoothSocket socket) {
        this.mainActivity=mainActivity;
        this.socket = socket;

        try {
            buffr=new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
            buffw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*듣고*/
    public void listen(){
        while(flag){
            try {
                String msg=buffr.readLine();

                /*서버의 정보창에 클라이언트의 메세지 출력*/
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("msg", msg);
                message.setData(bundle);
                mainActivity.handler.sendMessage(message);

                send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*보내고*/
    public void send(String msg){
        try {
            buffw.write(msg);
            buffw.write("\n");
            buffw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        listen();
    }
}

