package com.abyss.amadeus.server;

import com.abyss.amadeus.core.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import java.util.List;

import static com.abyss.amadeus.server.payLoadBuilder.buildPayLoad;
import static com.abyss.amadeus.config.Config.*;


public class commandProcessor {
    public static Gson gson = new Gson();
    static String systemPrompt = com.abyss.amadeus.core.systemPrompt.promptLoader();
    public static String processCommand(String userText, List<String> relevantContext, JsonArray history) {
        JsonObject payload = buildPayLoad(mainModel,userText , temperature,systemPrompt,relevantContext,history);

        //Sending Query to Versatile
        String rawResponse = HttpUtil.sendPost(API_URL, "Bearer " + Groq_API_KEY, gson.toJson(payload));
        return extractContent(rawResponse);
    }

    public static String extractContent(String rawJsonResponse) {
        try {
            JsonObject json = gson.fromJson(rawJsonResponse, JsonObject.class);
            return json.getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString().trim();
        } catch (Exception e) {
            return "{\"type\":\"chat\",\"cmd\":\"none\",\"reply\":\"I encountered a processing error handling that command.\"}";
        }
    }
    }

