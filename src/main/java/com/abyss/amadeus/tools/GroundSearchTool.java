package com.abyss.amadeus.tools;

import com.abyss.amadeus.core.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.abyss.amadeus.config.Config;

import static com.abyss.amadeus.Main.date;

public class GroundSearchTool implements funcTool {
    private final Gson gson = new Gson();

    public GroundSearchTool() {
    }

    @Override
    public String execute(JsonObject argument) {
        try {
            // Defensive validation: Look for 'query' matching the BuildToolRegister schema
            if (argument == null || !argument.has("query") || argument.get("query").isJsonNull()) {
                System.err.println("[Ground Search]: Tool execution skipped. Missing 'query' argument.");
                return "{\"error\": \"Missing query parameter. Could not extract what to search for.\"}";
            }

            String searchKeywords = argument.get("query").getAsString() + " latest " + date;

            JsonObject tavilyBody = new JsonObject();
            tavilyBody.addProperty("query", searchKeywords);
            tavilyBody.addProperty("search_depth", "advanced");
            tavilyBody.addProperty("topic", "news");

            System.out.println("[Ground Search]: Querying Tavily engine matrix for: " + searchKeywords);
            String rawSearchResult = HttpUtil.sendPost(Config.TAVILY_API_URL, "Bearer " + Config.TAVILY_API_KEY, tavilyBody.toString());

            if (rawSearchResult == null || rawSearchResult.trim().isEmpty()) {
                System.err.println("[Ground Search Error]: Upstream API returned completely empty content stream.");
                return "{\"error\": \"The search pipeline response was empty. Please check connection logs.\"}";
            }

            JsonObject jsonResponse = gson.fromJson(rawSearchResult, JsonObject.class);

            if (jsonResponse.has("detail") || !jsonResponse.has("results") || jsonResponse.get("results").isJsonNull()) {
                System.err.println("[Ground Search Error]: API structural mismatch or rejected authorization.");
                return "{\"error\": \"My network intelligence layer returned a server error.\"}";
            }

            JsonArray results = jsonResponse.getAsJsonArray("results");
            if (results.isEmpty()) {
                return "{\"status\": \"no_results\", \"message\": \"I traversed the internet but found no matching active data blocks.\"}";
            }

            // Extract the snippets into a raw string builder
            StringBuilder snippets = new StringBuilder();
            for (JsonElement element : results) {
                if (element.isJsonObject()) {
                    JsonObject resultObj = element.getAsJsonObject();
                    if (resultObj.has("content") && !resultObj.get("content").isJsonNull()) {
                        String content = resultObj.get("content").getAsString();
                        snippets.append("- ").append(content).append("\n\n");
                    }
                }
            }

            // Wrap the raw web context in a JSON object to hand back to the LLM
            JsonObject toolResult = new JsonObject();
            toolResult.addProperty("status", "success");
            toolResult.addProperty("web_context", snippets.toString().trim());

            return toolResult.toString();

        } catch (Exception e) {
            System.err.println("[Ground Search Critical]: Complete runtime failure inside tool handler.");
            e.printStackTrace();
            return "{\"error\": \"A critical exception paralyzed my grounding thread execution loop.\"}";
        }
    }
}