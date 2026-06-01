package com.abyss.amadeus.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class NirCmdService {

    // The universal Shape Registry
    private static final Map<String, String> SHAPE_REGISTRY = Map.of(
            "window",  "nircmdc win %1$s process %2$s %3$s", // App manipulation
            "process", "nircmdc %1$s %2$s",                  // Process manipulation
            "system",  "nircmdc %1$s %2$s"                   // Hardware/System manipulation
    );

    // Dynamic Prerequisites
    private static final Map<String, String> ACTION_PREREQUISITES = Map.of(
            "move", "normal",
            "setsize", "normal",
            "sendkeypress", "activate"
    );


    public static void executeShape(String shape, String subAction, String target, String modifier) {
        if (!SHAPE_REGISTRY.containsKey(shape)) {
            System.out.println("[NirCmdService Error]: Unrecognized shape: " + shape);
            return;
        }

        // Interceptor for OS Locks
        if (shape.equals("window") && ACTION_PREREQUISITES.containsKey(subAction)) {
            String preReq = ACTION_PREREQUISITES.get(subAction);
            System.out.println("[NirCmdService Middleware]: Firing prerequisite '" + preReq + "'");
            String precursorCmd = String.format(SHAPE_REGISTRY.get("window"), preReq, target, "");
            runProcess(precursorCmd);
        }

        // Final Execution
        String finalCmd = String.format(SHAPE_REGISTRY.get(shape), subAction, target, modifier);
        runProcess(finalCmd);
    }

    private static void runProcess(String commandString) {
        try {
            System.out.println("[DEBUG/EXEC]: " + commandString);
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", commandString);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) System.out.println("[NIRCMD STREAM]: " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("[NirCmdService Warning]: Non-zero exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}