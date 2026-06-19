package com.abyss.amadeus.server;

import com.abyss.amadeus.tools.BuildToolRegister;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;



public class payLoadBuilder {
    static final BuildToolRegister toolRegistry = new BuildToolRegister();

    public static JsonObject buildPayLoad(String model, String userText, double temperature, String sysPrompt, List<String> relevantContext,JsonArray history)  {
        JsonObject payLoad = new JsonObject();
        payLoad.addProperty("model",model);
        payLoad.addProperty("temperature",temperature);

        JsonArray messages = new JsonArray();

        //System
        JsonObject SysService = new JsonObject();
        SysService.addProperty("role","system");
        SysService.addProperty("content",sysPrompt);
        messages.add(SysService);

        //chatHistory
        if(!history.isEmpty() && !history.isEmpty()) {
            messages.addAll(history);
        }

        //User
        JsonObject userService = new JsonObject();
        userService.addProperty("role","user");

        String finalUserText = userText;
        if (relevantContext != null && !relevantContext.isEmpty()) {
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append(userText).append("\n\n--- RELEVANT MEMORIES ---\n");

            // Loop through the list and add each memory as a bullet point
            for (String memory : relevantContext) {
                contextBuilder.append("- ").append(memory).append("\n");
            }
            finalUserText = contextBuilder.toString();
        }
        userService.addProperty("content",finalUserText);

        messages.add(userService);


        payLoad.add("messages", messages);
        //System.out.println(payLoad);
        payLoad.add("tools", toolRegistry.registeration());
        payLoad.addProperty("tool_choice", "auto");
        return payLoad;


    }

}
