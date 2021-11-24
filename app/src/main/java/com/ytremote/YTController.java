package com.ytremote;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.ytremote.WebSocket.WebSocket;
import com.ytremote.WebSocket.WebSocketServer;

/**
 * Created by Michał on 03.01.2018.
 */

public class YTController extends Thread {



    public ControllerUI UI;
    private WebSocket sock;

    public YTController(){
        this.UI = new ControllerUI();
        start();
    }

    public void playbutton(){
        sock.write("{\"func\":\"play\"}");
    }

    private void postMessageToUI(String msgText, String parameters){
        Message msg = new Message();
        Bundle msgBundle = new Bundle();
        msgBundle.putString("msg", msgText);
        msgBundle.putString("params", parameters);
        msg.setData(msgBundle);
        MainActivity.mHandler.sendMessage(msg);
    }

    public void changeVolume(int volume){
        sock.write("{\"func\":\"volume\", \"volume\":"+volume+"}");
    }

    public void run(){
        WebSocketServer ws = new WebSocketServer(41075);
        while(true) {
            sock = ws.accept();
            postMessageToUI("params", sock.readMessage());
            postMessageToUI("ready", "");
            while (!sock.closed) {
                if (sock.anyMessage())
                    postMessageToUI("clientmsg", sock.readMessage());
            }
            postMessageToUI("disconnected", "");
        }
    }

}
