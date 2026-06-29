package amadeus.tools;

import amadeus.Main;
import amadeus.core.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import amadeus.config.Config;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;


public class GroundSearchTool{
    private final Gson gson = new Gson();
    Main main = new Main();
    String date = main.date;

    @Tool("Search the web for real-time information or verification.")
    public String GatherInformation(
            @P("The search query string to look up.") String query) {
        try {
            // Defensive validation: Look for 'query' matching the BuildToolRegister schema

            String searchKeywords =query; //+ " latest " + date;

            JsonObject tavilyBody = new JsonObject();
            tavilyBody.addProperty("query", searchKeywords);
            tavilyBody.addProperty("search_depth", "basic");

            JsonArray excludeDomains = new JsonArray();
            excludeDomains.add("facebook.com");
            excludeDomains.add("twitter.com");
            tavilyBody.add("exclude_domains", excludeDomains);

            tavilyBody.addProperty("topic", "news");
             tavilyBody.addProperty("days", 3);

            //tavilyBody.addProperty("topic", "news");

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

            StringBuilder snippets = new StringBuilder();
            // Extract the snippets into a raw string builder
            for (JsonElement element : results) {
                if (element.isJsonObject()) {
                    JsonObject resultObj = element.getAsJsonObject();

                    // Filter by relevance score (Tavily returns a score between 0.0 and 1.0)
                    double score = resultObj.has("score") ? resultObj.get("score").getAsDouble() : 0.0;

                    // Skip results that fall below our minimum confidence threshold
                    if (score < 0.6) {
                        System.out.println("[Ground Search Filter]: Dropped low-relevance result (Score: " + score + ")");
                        continue;
                    }

                    if (resultObj.has("content") && !resultObj.get("content").isJsonNull()) {
                        String content = resultObj.get("content").getAsString();
                        // Extract URL for better LLM citations
                        String url = resultObj.has("url") ? resultObj.get("url").getAsString() : "Unknown Source";

                        snippets.append("- Source [").append(url).append("]:\n")
                                .append("  ").append(content).append("\n\n");
                    }
                }
            }

            // Check

            if (snippets.length() == 0) {
                return "{\"status\": \"no_results\", \"message\": \"Found results, but none passed the relevance threshold.\"}";
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