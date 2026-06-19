package com.abyss.amadeus.tools;

import com.google.gson.JsonObject;
import com.abyss.amadeus.core.App_Index;
import com.abyss.amadeus.core.NirCmdService;
import java.nio.file.Paths;
import java.io.IOException;

public class AppControlTool implements funcTool {
    public static App_Index index = new App_Index();

    @Override
    public String execute(JsonObject argument) {


        if (!argument.has("target") || !argument.has("sub_action")) {
            System.out.println("[AMADEUS Error]: Missing target or action.");
            return "{\"status\":\"error\", \"message\":\"Missing target or sub_action parameters.\"}";
        }

        String appName = argument.get("target").getAsString().toLowerCase().trim();
        String action = argument.get("sub_action").getAsString().toLowerCase().trim();

        String matchedKey = index.findAppKey(appName);

        // Bypass if app target can't be resolved in index
        if (matchedKey == null || action.equals("none")) {
            System.out.println("[AMADEUS]: App target could not be resolved. Issue with matchedKey or action: " + matchedKey);
                return "{\"status\":\"error\", \"message\":\"System shell could not find an application named '" + appName + "Try using the open_url tool to access the WebApp '.\"}";

            }


        String path = index.appMap.get(matchedKey);
        String processName = Paths.get(path).getFileName().toString();



        // Native app launching bypass
        if (action.equalsIgnoreCase("open") || action.equalsIgnoreCase("launch") || action.equalsIgnoreCase("start") || action.equalsIgnoreCase("run")) {
                return runBypassProcess(new ProcessBuilder("cmd.exe", "/c", "start", "\"\"", path), appName);


        }

        // Delegate to NirCmdService for structural commands
        String shape = argument.has("shape") && !argument.get("shape").isJsonNull() ? argument.get("shape").getAsString().trim() : "window";
        String subAction = argument.has("sub_action") && !argument.get("sub_action").isJsonNull() ? argument.get("sub_action").getAsString().trim() : action;
        String modifier = argument.has("modifier") && !argument.get("modifier").isJsonNull() ? argument.get("modifier").getAsString().trim() : "";

        // Hand off to the centralized service!
        NirCmdService.executeShape(shape, subAction, processName, modifier);
        return "{\"status\":\"success\", \"message\":\"Successfully executed " + subAction + " on " + processName + "\"}";
    }

    private String runBypassProcess(ProcessBuilder pb, String target) {
        try {
            pb.redirectErrorStream(true);
            Process process = pb.start();
            Thread.sleep(150); // Small window to let Windows attempt the process spawn

            if (process.isAlive() || process.exitValue() == 0) {
                return "{\"status\":\"success\", \"message\":\"Application '" + target + "' launched successfully via system bypass.\"}";
            } else {
                return "{\"status\":\"error\", \"message\":\"System shell could not find an application named '" + target + "'.\"}";
            }
        } catch (IOException e) {
            System.out.println("[AMADEUS Shell Error]: IO Issue executing bypass for " + target);
            return "{\"status\":\"error\", \"message\":\"OS failed to execute launch process for '" + target + "'.\"}";
        } catch (InterruptedException e) {
            System.out.println("[AMADEUS Thread Error]: Bypass execution interrupted.");
            Thread.currentThread().interrupt(); // Proper thread management
            return "{\"status\":\"error\", \"message\":\"The launch sequence for '" + target + "' was interrupted.\"}";
        }
    }
}