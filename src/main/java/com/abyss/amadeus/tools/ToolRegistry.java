package com.abyss.amadeus.tools;

import com.google.gson.JsonObject;

import java.util.HashMap;

public class ToolRegistry {
    private final  HashMap<String,funcTool> register = new HashMap<>();

    public  ToolRegistry() {

        register.put("app_control", new AppControlTool() {});
        //register.put("hardware_control", new HardwareControlTool(){});
        register.put("web_search", new Ground_Search_Tool(){});

    }
    public void executeTool(String name, JsonObject args) {
        funcTool tool = register.get(name);
        if (tool != null) {
            tool.execute(args);
        } else {
            System.err.println("Unknown tool requested: " + name);
        }
    }
}
