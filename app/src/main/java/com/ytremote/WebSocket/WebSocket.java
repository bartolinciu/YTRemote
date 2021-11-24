package com.ytremote.WebSocket;

import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.MessageDigest;
import org.apache.commons.codec.binary.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocket;


/**
 * Created by Michał on 27.12.2017.
 */

public class WebSocket{
    public SSLSocket sock;
    private BufferedReader textIn;
    private DataInputStream dataIn;
    private PrintWriter textOut;
    private DataOutputStream dataOut;

    private Buffer msgQueue = new CircularFifoBuffer(10);
    private Buffer pendingPackets = new CircularFifoBuffer(15);
    private String msgBuf = "";
    
    public boolean closed=false;

    public String readMessage(){
        while(msgQueue.isEmpty())
            try{TimeUnit.MILLISECONDS.sleep(10);}catch(Exception e){System.out.println(e.toString());}
        Object message = msgQueue.get();
        msgQueue.remove();
        return message.toString();
    }

    public void sendPendingPacket(){
        if(pendingPackets.isEmpty())
                return;
        ((WebSocketPacket) pendingPackets.get()).write(dataOut);
        pendingPackets.remove();
    }

    public boolean anyMessage(){
        return !msgQueue.isEmpty();
    }

    private boolean parsePacket(WebSocketPacket packet){
        if( !packet.FIN && packet.opcode!=0 ){
            System.out.println("Received invalid frame!");
        }
        else {
            if (packet.opcode == 8)
                return false;

            else if (packet.opcode == 1 || packet.opcode == 2) {
                packet.unmaskData();
                msgBuf = new String(packet.payloadData);
                if( packet.FIN ) {
                    msgQueue.add(msgBuf);
                    msgBuf = "";
                }
            }

            else if (packet.opcode == 0) {
                packet.unmaskData();
                msgBuf += new String(packet.payloadData);
                if( packet.FIN ) {
                    msgQueue.add(msgBuf);
                    msgBuf = "";
                }
            }
        }
        return true;
    }

    public WebSocket(SSLSocket sock, boolean server ) throws WebSocketException {
        try {
            this.sock = sock;
            textIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            dataIn = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
            textOut = new PrintWriter(sock.getOutputStream(), true);
            dataOut = new DataOutputStream(sock.getOutputStream());

            if (server) {
                HTTPHeader header = new HTTPHeader(readUntil("\r\n\r\n"));

                if (!header.query.startsWith("GET")) {
                    close();
                    throw new WebSocketException("Expected GET query, got: " + header.query);
                }

                MessageDigest sha1 = MessageDigest.getInstance("sha1");

                Map<String, String> responseMap = new HashMap<String, String>();
                responseMap.put("Upgrade", "websocket");
                responseMap.put("Connection", "Upgrade");
                responseMap.put("Sec-WebSocket-Accept", new String(Base64.encodeBase64(sha1.digest((header.get("Sec-WebSocket-Key") + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes()))));
                responseMap.put("Sec-WebSocket-Protocol", header.get("Sec-WebSocket-Protocol"));


                HTTPHeader response = new HTTPHeader("HTTP/1.1 101 Switching Protocols", responseMap);
                textOut.write(response.toString());
                textOut.flush();
            }
        } catch (java.io.IOException e) {
            System.out.println(textIn.toString());
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println(e.toString());
        }
    }

    public void write(String msg){
        WebSocketPacket packet = new WebSocketPacket();
        packet.FIN = true;
        packet.RSV[0] = false;
        packet.RSV[1] = false;
        packet.RSV[2] = false;
        packet.opcode = 1;
        packet.payloadData = msg.getBytes();
        //packet.write(dataOut);
        pendingPackets.add(packet);
    }

    public String readUntil( String goal ) throws java.io.IOException{
        String msg ="";
        int input;
        while ((input = textIn.read()) != -1) {
            msg += (char) input;
            if (msg.endsWith(goal))
                break;
        }
        
        return msg;
    }

    public WebSocketPacket readPacket() {
        WebSocketPacket packet = new WebSocketPacket();
        packet.read(dataIn);
        return packet;
    }

    public void start(){
        new Thread(){
            @Override
            public void run(){
                while(parsePacket(readPacket()));
                try{close();}catch(Exception e){System.out.println(e.toString());}
            }
        }.start();
        new Thread(){
            @Override
            public void run(){
                while(!closed)sendPendingPacket();
            }
        }.start();
    }

    public void close() throws java.io.IOException {
            textIn.close();
            textOut.close();
            dataIn.close();
            dataOut.close();
            sock.close();
            closed = true;
    }


}
