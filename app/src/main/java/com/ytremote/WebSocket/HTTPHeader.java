package com.ytremote.WebSocket;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michał on 27.12.2017.
 */



public class HTTPHeader {

    public String query="";

    private Map<String, String> map = new HashMap<String, String>();

    public HTTPHeader(){
    }

    public HTTPHeader(String headerText){
        fromString(headerText);
    }

    public HTTPHeader( String query, Map<String, String> headerMap ){
        fromMap(query, headerMap);
    }

    public String get( String key ){
        return map.get(key);
    }

    public void fromString( String headerText ){
        String entries[] = headerText.split("\r\n");
        query = entries[0];
        for( String entry : Arrays.copyOfRange(entries, 1, entries.length) ){
                map.put(entry.split(": ")[0], entry.split(": ")[1]);
        }
    }

    public void fromMap( String query, Map<String, String> headerMap ){
        this.query = query;
        this.map = headerMap;
    }

    public String toString(){
        String headerText = query + "\r\n";
        for( Map.Entry<String, String> entry : map.entrySet() ){
            headerText += entry.getKey() + ": " + entry.getValue() + "\r\n";
        }
        return headerText+"\r\n";
    }

}
