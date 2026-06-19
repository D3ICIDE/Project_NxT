package com.abyss.amadeus.config;

import org.java_websocket.WebSocket;

public class Config {
    private static volatile WebSocket activeWebsocket;
    private static volatile String currentUserMessage;
    public static final String mainModel = "openai/gpt-oss-120b";
    public static final String routingModel = "qwen/qwen3-32b";
    public static final String analysisModel = "llama-3.3-70b-versatile";
    public static final String backgroundCheckingModel= "openai/gpt-oss-20b";
    public static final String visualModel ="";
    //public static final double temperature =0.5;
    public final static String Groq_API_KEY = System.getenv("Groq_API_KEY");
    public final static String Weather_API_KEY = System.getenv("Weather_API_KEY");
   public final static String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    public static final String TAVILY_API_URL = "https://api.tavily.com/search";
    public static final String Weather_Base_URL = "http://api.weatherapi.com/v1";
    public static final String GEO_API_URL = "http://ip-api.com/json/";
    public static final String TAVILY_API_KEY = System.getenv("tavily_API_KEY");

    public static void setActiveWebSocket(WebSocket webSocket){
        activeWebsocket = webSocket;

    }
    public static WebSocket getActiveSocket(){
        return activeWebsocket;
    }
    public static void setCurrentUserMessage(String userMessage){
        currentUserMessage = userMessage;
    }
    public static  String getCurrentUserMessage(){
        return  currentUserMessage;
    }



}
