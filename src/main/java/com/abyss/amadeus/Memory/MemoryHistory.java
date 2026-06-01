package com.abyss.amadeus.Memory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.Deque;

public class MemoryHistory {
    private final Deque<JsonObject> history;
    private final int maxMessages;

    public MemoryHistory(int maxMessages) {
        this.history = new  ArrayDeque<>();
        this.maxMessages = maxMessages;
    }
    public synchronized void addMessage(String role,String content){
        JsonObject message = new JsonObject();
        message.addProperty("role",role);
        message.addProperty("content",content);

        if(history.size()>maxMessages){
            history.pollFirst();
        }
        history.addLast(message);
    }

    //compile to json
    public synchronized JsonArray getHistoryAsJson(){
        JsonArray array = new JsonArray();
        for(JsonObject msg:  history){
            JsonObject copy = new JsonObject();
            copy.addProperty("role",msg.get("role").getAsString());
            copy.addProperty("content",msg.get("content").getAsString());
            array.add(copy);
        }
        return array;
    }
    public synchronized void clear() {
        history.clear();
    }
}
