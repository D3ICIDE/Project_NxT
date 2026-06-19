package com.abyss.amadeus;

import com.abyss.amadeus.core.FetchLocation;
import com.abyss.amadeus.core.FetchTime;
import com.abyss.amadeus.server.Server;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

import static com.abyss.amadeus.tools.AppControlTool.index;

public class Main {
    public static String city = FetchLocation.getCurrentLocation();
    public static String date = FetchTime.getCurrentDate();
    public static void main(String[] args) {
        index.indexBuilder();
        String host = "0.0.0.0";
        int port = 8887;


        WebSocketServer server = new Server(new InetSocketAddress(host, port));
        server.start();
    }
}
