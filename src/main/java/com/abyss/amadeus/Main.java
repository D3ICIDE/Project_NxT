package com.abyss.amadeus;

import com.abyss.amadeus.server.Server;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        String host = "0.0.0.0";
        int port = 8887;
        WebSocketServer server = new Server(new InetSocketAddress(host, port));
        server.start();
    }
}
