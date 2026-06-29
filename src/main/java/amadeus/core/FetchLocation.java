package amadeus.core;

import amadeus.config.Config;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.Tool;

public class FetchLocation {
    public static Gson gson = new Gson();
    @Tool("Fetches the user's current local city and region via GeoIP lookup when no location is specified.")
    public static String getCurrentLocation(){
        String rawResponse =HttpUtil.sendGET(Config.GEO_API_URL,"User-Agent","Mozilla/5.0");
        JsonObject jsonResponse = gson.fromJson(rawResponse, JsonObject.class);
        String city = jsonResponse.has("city") ? jsonResponse.get("city").getAsString() :"Delhi";
       // String region = jsonResponse.has("region") ? jsonResponse.get("region").getAsString() :"";
        String country = jsonResponse.has("country") ? jsonResponse.get("country").getAsString() :"";
        return  city+" " +country;

    }

}
