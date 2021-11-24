package com.ytremote.WebSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by Michał on 29.12.2017.
 */

public class WebSocketPacket{

    public boolean FIN;
    public boolean[] RSV = new boolean[3];
    public byte opcode;
    public boolean mask;

    private byte payloadLength;
    private long extendedPayloadLength;
    public byte[] maskingKey;
    public byte[] payloadData;

    private boolean dbg = false;

    public WebSocketPacket(){}

    public void useMask( byte[] maskingKey, boolean applyNow ){
        this.maskingKey = maskingKey;
        if(applyNow){
                maskData();
        }
    }

    private byte[] longToBytes( long a, int length ){
        byte[] bytes = new byte[length];
        for( int i=0; i < length; i++ ){
            bytes[i] = (byte)((a>>(length-i-1)*8)&0xff);
        }
        return bytes;
    }

    private void calcPayloadLength(){
        if(payloadData.length < 126){
            payloadLength = (byte) payloadData.length;
            return;
        }
        if(payloadData.length < 32768){
            payloadLength = 126;
            extendedPayloadLength = payloadData.length;
            return;
        }
        extendedPayloadLength = payloadData.length;


    }

    public void write(DataOutputStream out){
        try{
            calcPayloadLength();
            byte[] buf = new byte[2];
            buf[0] = (byte)( (FIN?1:0)<<7 | (RSV[0]?1:0)<<6 | (RSV[1]?1:0)<<5 | (RSV[2]?1:0)<<4 | opcode )  ;
            buf[1] = (byte)( (mask?1:0)<<7 | payloadLength );
            out.write( buf, 0,2 );
            if( payloadLength > 125 )
                out.write( longToBytes(extendedPayloadLength, payloadLength==126 ? 2:8 ), 0, payloadLength==126 ? 2:8  );
            if( mask )
                out.write( maskingKey, 0, 4 );
            if( payloadLength < 125)
                out.write( payloadData, 0, payloadLength );

            else if( payloadLength == 126 )
                out.write( payloadData, 0, (int)extendedPayloadLength );
            else
                System.out.println("wrong payload length");
            out.flush();

        }
        catch(java.io.IOException e){
            System.out.println(e.toString());
        }
    }


    public void read( DataInputStream in ){

        try {
            if(dbg){
                byte[] buf = new byte[20];
                in.read(buf, 0, 20);
                for( int i = 0; i<20;i++ ){
                    System.out.printf("%02x ", buf[i]);
                }
                System.out.print("\n");
                System.out.flush();
                return;
            }
            byte[] buf = new byte[8];
            in.read(buf, 0, 2);
            FIN = (buf[0]>>7 & 1) == 1;
            RSV[0] = (buf[0] >>6 & 1) == 1;
            RSV[1] = (buf[0] >> 5 & 1) == 1;
            RSV[2] = (buf[0] >> 4 & 1) == 1;
            opcode = (byte) (buf[0] & 0xf);
            mask = (buf[1] >> 7 & 1) == 1;
            payloadLength = (byte)( buf[1] & 0x7f);
            if (payloadLength == 126) {
                in.read(buf, 0, 2);
                extendedPayloadLength = buf[0]<<8|buf[1];
            } else if (payloadLength == 127) {
                in.read(buf, 0, 8);
                extendedPayloadLength =  buf[0] << 56 | buf[1] << 48 | buf[2] << 40 | buf[3] << 32 | buf[4] << 24 | buf[5] << 16 | buf[6] << 8 | buf[7];
            }
            if (mask) {
                in.read(buf, 0, 4);
                maskingKey = buf;
            }
            if (payloadLength < 126) {
                buf = new byte[payloadLength];
                in.read(buf, 0, payloadLength );
                payloadData = buf;
            }


        }
        catch(Exception e){
            System.out.println("packet error: " + e.toString());
        }
    }

    public void unmaskData(){
        if( !mask )
            return;
        for( int i=0; i < payloadData.length; i++ ){
            payloadData[i] = (byte) (payloadData[i]^( maskingKey[i%4]));
        }
        mask = false;
    }

    public void maskData(){
        if( mask )
            return;
        for( int i=0; i < payloadData.length; i++ ){
            payloadData[i] = (byte) (payloadData[i]^maskingKey[i%4]);
        }
        mask = true;
    }



}
