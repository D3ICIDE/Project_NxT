package com.abyss.amadeus.tools;

import com.abyss.amadeus.Main;
import com.abyss.amadeus.config.Config;
import com.abyss.amadeus.core.HttpUtil;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.abyss.amadeus.server.commandProcessor.gson;

public class FetchWeatherTool implements funcTool {
    public static String city = Main.city;

    public FetchWeatherTool() {
    }

    public String buildURL(String Endpoint, String City) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Config.Weather_Base_URL)
                .append(Endpoint)
                .append("?key=").append(Config.Weather_API_KEY)
                .append("&q=").append(City);
        return urlBuilder.toString();
    }

    @Override
    public String execute(JsonObject argument) {
        String Endpoint = "/current.json";

        // Defensively extract arguments - the LLM might omit them if it doesn't know them!
        String modifiedCity = argument.has("location") && !argument.get("location").isJsonNull()
                ? argument.get("location").getAsString() : "";

        String modifiedEndPoint = argument.has("endpoint") && !argument.get("endpoint").isJsonNull()
                ? argument.get("endpoint").getAsString() : null;

        try {
            if (modifiedEndPoint != null && !modifiedEndPoint.trim().isEmpty()) {
                Endpoint = modifiedEndPoint;
            }
            if (!modifiedCity.trim().isEmpty()) {
                city = modifiedCity;
            }

            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString().replace("+", "%20"));
            String url = buildURL(Endpoint, encodedCity);

            // Fetch live data
            String rawResponse = HttpUtil.sendGET(url, "Accept", "application/json");
            JsonObject response = gson.fromJson(rawResponse, JsonObject.class);

            // Extract only the data the LLM needs
            JsonObject weatherData = extractWeatherReport(response);

            // Return the raw data string back to the Orchestrator
            return weatherData.toString();

        } catch (UnsupportedEncodingException e) {
            System.err.println("[Weather Tool Error]: " + e.getMessage());
            return "{\"error\": \"Failed to encode city name parameter.\"}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to fetch data from Weather API.\"}";
        }
    }

    public JsonObject extractWeatherReport(JsonObject response) {
        JsonObject llmPayload = new JsonObject();

        if (response.has("location")) {
            JsonObject locationObj = response.getAsJsonObject("location");
            llmPayload.add("name", locationObj.get("name"));
            llmPayload.add("region", locationObj.get("region"));
            llmPayload.add("country", locationObj.get("country"));
            llmPayload.add("localtime", locationObj.get("localtime"));
        }

        if (response.has("current")) {
            JsonObject currentObj = response.getAsJsonObject("current");
            llmPayload.add("temp_c", currentObj.get("temp_c"));
            llmPayload.add("wind_kph", currentObj.get("wind_kph"));
            llmPayload.add("wind_degree", currentObj.get("wind_degree"));
            llmPayload.add("wind_dir", currentObj.get("wind_dir"));
            llmPayload.add("windchill_c", currentObj.get("windchill_c"));
            llmPayload.add("heatindex_c", currentObj.get("heatindex_c"));
            llmPayload.add("dewpoint_c", currentObj.get("dewpoint_c"));

            // Added text condition (e.g., "Sunny", "Raining") so the LLM sounds natural
            if (currentObj.has("condition")) {
                llmPayload.add("condition", currentObj.getAsJsonObject("condition").get("text"));
            }
        }

        return llmPayload;
    }
}