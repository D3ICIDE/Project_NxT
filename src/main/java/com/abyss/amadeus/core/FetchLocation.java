package com.abyss.amadeus.core;

import com.abyss.amadeus.config.Config;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class FetchLocation {
    public static Gson gson = new Gson();
    public static String getCurrentLocation(){
        String rawResponse =HttpUtil.sendGET(Config.GEO_API_URL,"User-Agent","Mozilla/5.0");
        JsonObject jsonResponse = gson.fromJson(rawResponse, JsonObject.class);
        String city = jsonResponse.has("city") ? jsonResponse.get("city").getAsString() :"Delhi";
       // String region = jsonResponse.has("region") ? jsonResponse.get("region").getAsString() :"";
        String country = jsonResponse.has("country") ? jsonResponse.get("country").getAsString() :"";
        return  city+" " +country;

    }

}
