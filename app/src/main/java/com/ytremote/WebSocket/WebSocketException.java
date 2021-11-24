package com.ytremote.WebSocket;

/**
 * Created by Michał on 02.01.2018.
 */

public class WebSocketException extends Exception {
	public WebSocketException(){super();}
    public WebSocketException(String msg){super(msg);}
    public WebSocketException(String msg, Throwable cause){super(msg,cause);}
    public WebSocketException(Throwable cause){super(cause);}
}
