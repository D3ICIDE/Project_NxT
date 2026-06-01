package com.abyss.amadeus.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpUtil {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    public static String sendPost(String url, String authHeader, String jsonPayload) {
        try{
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));
            if (authHeader != null) {
                builder.header("Authorization", authHeader);
            }

            return executeRequest(builder.build());
        }catch (Exception e){
            e.printStackTrace();
            return "{\"error\": \"Network failure\"}";
        }
    }

    public static String executeRequest(HttpRequest request) throws java.io.IOException, InterruptedException {


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(">>> HTTP REQUEST SENT " + response.statusCode() + " | BODY: " + response.body());
        if (response.statusCode() == 200) return response.body();

        return "{\"error\": \"Status " + response.statusCode() + "\"}";
    }
}
