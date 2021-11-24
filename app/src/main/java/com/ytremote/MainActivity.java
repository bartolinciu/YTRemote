package com.ytremote;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


import com.ytremote.WebSocket.WebSocket;
import com.ytremote.WebSocket.WebSocketServer;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class MainActivity extends Activity {


    static YTController yt;
    static Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            for (Enumeration<NetworkInterface> i = NetworkInterface.getNetworkInterfaces(); i.hasMoreElements(); ) {
                NetworkInterface iface = i.nextElement();
                if (iface.getName().equals("wlan0")) {
                    for (Enumeration<InetAddress> a = iface.getInetAddresses(); a.hasMoreElements(); ) {
                        String address = a.nextElement().toString();
                        if(address.startsWith("/192.168"));
                        ((TextView)findViewById(R.id.textView)).setText(address.substring(1));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        yt = new YTController();


        mHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message message){
                String msg = message.getData().getString("msg");
                if(msg == "ready" ){
                    yt.UI.getView().setVisibility(View.VISIBLE);
                }
                if(msg == "disconnected") {
                    yt.UI.getView().setVisibility(View.GONE);
                }
                if(msg == "params"){
                    try {
                        JSONObject params = new JSONObject(message.getData().getString("params"));
                        ((SeekBar)findViewById(R.id.volume)).setProgress((int)params.get("volume"));
                    }catch(org.json.JSONException e){
                        System.out.println("Failed while receiving initial values: " + e.toString());
                    }
                }
            }
        };



    }

}
