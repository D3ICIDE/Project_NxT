package com.abyss.amadeus.tools;

import com.google.gson.JsonObject;
import java.io.IOException;

public class SearchWebTool implements funcTool {

    @Override
    public String execute(JsonObject argument) {

        // Defensive check
        if (!argument.has("url") || argument.get("url").isJsonNull()) {
            return "{\"status\":\"error\", \"message\":\"Missing URL parameter.\"}";
        }

        String url = argument.get("url").getAsString().trim();

        // Ensure the string has a protocol so Windows recognizes it as a web link and not a local file
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        try {
            // Windows native command to open a link in the default system browser
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "start", "\"\"", url);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();

            return "{\"status\":\"success\", \"message\":\"Successfully opened website: " + url + "\"}";
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "{\"status\":\"error\", \"message\":\"System failed to open the website.\"}";
        }
    }
}