package com.abyss.amadeus.tools;

import com.abyss.amadeus.core.NirCmdService;
import com.google.gson.JsonObject;

public class HardwareControlTool implements funcTool{
    @Override
    public String execute(JsonObject argument) {
        String target = argument.get("target").getAsString().toLowerCase().trim();
        String sub_action = argument.get("sub_action").getAsString().toLowerCase().trim();
        String shape = argument.get("shape").getAsString().toLowerCase().trim();
        String modifier = argument.get("modifier").getAsString().toLowerCase().trim();
        NirCmdService.executeShape(shape, sub_action,target, modifier);


        return "{\"status\":\"success\", \"message\":\"Successfully executed " + sub_action + " on " + target + "\"}";
    }
}
