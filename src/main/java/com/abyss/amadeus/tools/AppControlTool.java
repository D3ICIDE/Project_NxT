package com.abyss.amadeus.tools;

import com.google.gson.JsonObject;
import com.abyss.amadeus.core.App_Index;
import com.abyss.amadeus.core.NirCmdService; // Import your new service
import java.nio.file.Paths;
import java.io.IOException;

public class AppControlTool implements funcTool {
    static App_Index index = new App_Index();

    @Override
    public void execute(JsonObject argument) {
        index.indexBuilder();

        if (!argument.has("target") || !argument.has("sub_action")) {
            System.out.println("[AMADEUS Error]: Missing target or action.");
            return;
        }

        String appName = argument.get("target").getAsString().toLowerCase().trim();
        String action = argument.get("sub_action").getAsString().toLowerCase().trim();

        String matchedKey = index.findAppKey(appName);
        if (matchedKey == null || action.equals("none")) {
            System.out.println("[AMADEUS]: App target could not be resolved." +
                    "Issue with matchedKey or action in AppControlTool line 25 ."+matchedKey);
            return;
        }

        String path = index.appMap.get(matchedKey);
        String processName = Paths.get(path).getFileName().toString();

        // Bypass for native app launching
        if (action.equalsIgnoreCase("open") || action.equalsIgnoreCase("launch") || action.equalsIgnoreCase("start") || action.equalsIgnoreCase("run")) {
            runBypassProcess(new ProcessBuilder("cmd.exe", "/c", "start", "\"\"", path));
            return;
        }

        // Delegate to NirCmdService for structural commands
        String shape = argument.has("shape") ? argument.get("shape").getAsString().trim() : "window";
        String subAction = argument.has("sub_action") ? argument.get("sub_action").getAsString().trim() : action;
        String modifier = argument.has("modifier") ? argument.get("modifier").getAsString().trim() : "";

        // Hand off to the centralized service!
        NirCmdService.executeShape(shape, subAction, processName, modifier);
    }

    private void runBypassProcess(ProcessBuilder pb) {
        try {
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}