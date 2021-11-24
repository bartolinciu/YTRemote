package com.ytremote.WebSocket;

/**
 * Created by Michał on 24.12.2017.
 */

import java.io.FileInputStream;
import java.net.ServerSocket;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.io.BufferedReader;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class WebSocketServer extends Thread{
    private SSLServerSocket sock;
    private WebSocket conn;


    public WebSocketServer( int port ){
        try{
            System.setProperty("javax.net.debug", "ssl");
        	String ksName = "/storage/emulated/0/Android/data/com.ytremote/files/keystore.bks";
            char ksPass[] = "DeadCode".toCharArray();
            char ctPass[] = "DeadCode".toCharArray();
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(ksName), ksPass);
            KeyManagerFactory kmf = 
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, ctPass);
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            sock = (SSLServerSocket) ssf.createServerSocket(port);

        }
        catch( Exception exception ){
            System.out.println("error: couldn't open listening socket");
            System.out.println(exception.getMessage());
        }
    }

    public WebSocket accept(){
        try {
            start();
            join();
            conn.start();
            return conn;
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }

    }


    public void run(){
        try{
        	SSLSocket c = (SSLSocket) sock.accept();
            BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
        	r.readLine();
        	r.close();
        	c.close();
        	
        	c = (SSLSocket) sock.accept();
        		
        	conn = new WebSocket(c, true);
        	}catch(Exception e){System.out.println(e.toString());}
    }



}
