package amadeus.tools;

import amadeus.Main;
import amadeus.config.Config;
import amadeus.core.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class FetchWeatherTool {
    public static String defaultCity = Main.city;
    public static Gson gson = new Gson();

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
    @Tool("Fetches live current weather and conditions for a given city")
    public String fetchWeather(
            @P("The location or city to get the weather for, e.g., 'London' or 'New York'") String location,
            @P("The API endpoint, default to '/current.json' if unsure") String endpoint
    ) {
        String Endpoint = "/current.json";

        // Defensively extract arguments - the LLM might omit them if it doesn't know them!
        String targetEndpoint = (endpoint != null && !endpoint.trim().isEmpty()) ? endpoint : Endpoint;
        String targetCity = (location != null && !location.trim().isEmpty()) ? location : defaultCity;

        try {


            String encodedCity = URLEncoder.encode(targetCity, StandardCharsets.UTF_8.toString().replace("+", "%20"));
            String url = buildURL(targetEndpoint, encodedCity);

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