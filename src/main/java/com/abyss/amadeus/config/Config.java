package com.abyss.amadeus.config;

public class Config {
    public static final String mainModel = "meta-llama/llama-4-scout-17b-16e-instruct";
    public static final String visualModel ="";
    public static final double temperature =0.5;
   public final static String Groq_API_KEY = System.getenv("Groq_API_KEY");
   public final static String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String TAVILY_API_URL = "https://api.tavily.com/search";
    private static final String TRAVILY_API_KEY = System.getenv("tavily_API_KEY");

}
