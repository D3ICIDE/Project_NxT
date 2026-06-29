package amadeus.legacyCodes.server;

import amadeus.config.Config;
import amadeus.core.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import java.util.List;

import static amadeus.legacyCodes.server.payLoadBuilder.buildPayLoad;
import static amadeus.config.Config.*;


public class commandProcessor {
    //Config config = new Config();
    public static Gson gson = new Gson();

    public static JsonObject processCommand(String userText, List<String> relevantContext, JsonArray history, String systemPrompt, String model,double temperature) {

        JsonObject payload = buildPayLoad(model, userText, temperature, systemPrompt, relevantContext, history);

        // Sending Query to Groq Engine Matrix
        String rawResponse = HttpUtil.sendPost(API_URL, "Bearer " + Config.Groq_API_KEY, gson.toJson(payload));
        try {
            JsonObject json = gson.fromJson(rawResponse, JsonObject.class);
            // Return the full message object containing both 'content' and 'tool_calls'
            return json.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message");
        } catch (Exception e) {
            JsonObject errorFallback = new JsonObject();
            errorFallback.addProperty("content", "I encountered a processing error handling that command...");
            return errorFallback;
        }
    }

    /*public static String extractContent(String rawJsonResponse) {
        try {
            JsonObject json = gson.fromJson(rawJsonResponse, JsonObject.class);
            JsonObject message = json.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message");

            if (message.has("content") && !message.get("content").isJsonNull()) {
                return message.get("content").getAsString().trim();
            }
            return "";
        } catch (Exception e) {
            return "{\"type\":\"chat\",\"cmd\":\"none\",\"reply\":\"I encountered a processing error handling that command.\"}";
        }
    }*/
    }

