package com.abyss.amadeus.router;

import com.abyss.amadeus.tools.AppControlTool;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;


import static com.abyss.amadeus.server.commandProcessor.gson;

public class ResponseHandler {
    public static void responseAttributes(String rawResponse,WebSocket webSocket){
        JsonObject Response = gson.fromJson(rawResponse,JsonObject.class);
        String type = Response.get("type").getAsString();
        String reply = Response.get("reply").getAsString();
        responseRouter(type,Response,reply,webSocket);

    }
    public static void responseRouter(String type,JsonObject payload, String reply,WebSocket webSocket){
        if(type.equalsIgnoreCase("app")){
            System.out.println("Assigning App Control");
            AppControlTool appControlTool = new AppControlTool();
            appControlTool.execute(payload);

        }
        else if (type.equalsIgnoreCase("chat")) {
            webSocket.send(reply);

        }
        webSocket.send(reply);

    }

}
