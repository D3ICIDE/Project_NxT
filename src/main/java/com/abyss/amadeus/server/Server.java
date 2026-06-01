package com.abyss.amadeus.server;
import com.abyss.amadeus.Memory.Memorize;
import com.abyss.amadeus.Memory.MemoryConfig;
import com.abyss.amadeus.Memory.MemoryHistory;
import com.abyss.amadeus.Memory.Total_Recall;
import com.abyss.amadeus.router.ResponseHandler;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.List;

import static com.abyss.amadeus.server.commandProcessor.processCommand;

public class Server extends WebSocketServer {
    Gson gson = new Gson();
     final Memorize ingestor;
     final MemoryConfig memoryConfig;
    private final Total_Recall recall;
    MemoryHistory history = new MemoryHistory(25);
   public Server(InetSocketAddress address) {
       super(address);

       this.memoryConfig = new  MemoryConfig();
       this.ingestor = new Memorize(this.memoryConfig);
       this.recall = new Total_Recall(this.memoryConfig);

   }
    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("websocket open, Listening for connection at " + webSocket.getRemoteSocketAddress());

    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
       System.out.println("websocket closed");

    }

    @Override
    public void onMessage(WebSocket webSocket, String userMessage) {
        System.out.println("User said:-> " + userMessage);
        //Receive the raw response from LLM
        try {

            JsonArray chatHistory = history.getHistoryAsJson();
            List<String> relevantContext = recall.getRelevantContext(userMessage);

            String rawJsonResponse = processCommand(userMessage,relevantContext,chatHistory);

            //add message for chat history
            history.addMessage("user", userMessage);
            history.addMessage("assistant", rawJsonResponse);

            //Execute
            ResponseHandler.responseAttributes(rawJsonResponse,webSocket);
            String conversation = userMessage+rawJsonResponse;
            ingestor.commitInteraction(conversation);
        }catch (Exception e){
            e.printStackTrace();
        }



    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
       e.printStackTrace();

    }

    @Override
    public void onStart() {
       System.out.println("Server started");

    }
}
