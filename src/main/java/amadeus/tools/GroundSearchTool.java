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
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchOrganicResult;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroundSearchTool{
    private final Gson gson = new Gson();
   Main main = new Main();
    String date = main.date;

    enum Searchdepth{
        basic,advanced
    }
    enum TimeRange{
        day,week,month,year,not_required
    }
    enum Topic{
        finance,news,Default
    }


    @Tool("""
            Executes a high-speed web search to answer straightforward factual questions.
             Returns a brief, summarized text response. Use this by default for simple lookup
            """)
    public String GatherInformation(
            @P("The exact search query string to look up on the web.") String query,

            @P("Depth of search. Default to 'basic'. Use 'advanced' ONLY for complex research requiring multiple sources.") Searchdepth searchdepth,

            @P("OPTIONAL: Category filter. Use 'news' for current events/breaking news, or 'finance' for stock/market data. Leave null for general searches.") Topic topic,

            @P("OPTIONAL: Strict limit on how many days back to search (e.g., 3). Use 0 for no limit.") int days,

            @P("OPTIONAL: Broad recency filter. Allowed: 'day', 'week', 'month', 'year', 'not_required'. Use ONLY if the user explicitly asks for recent data.") TimeRange timeRange ) {
        try {
            // Defensive validation: Look for 'query' matching the BuildToolRegister schema

            String searchKeywords =query + " (current date: - " + date +")";
            List<String> excludeDomains = new ArrayList<>();
            excludeDomains.add("facebook.com");
            excludeDomains.add("twitter.com");

            Map<String, Object> optionalFilters = new HashMap<>();

            if (topic != null && topic != Topic.Default) {
                optionalFilters.put("topic", topic.name());
            }
            if (days > 0) {
                optionalFilters.put("days", days);
            }
            if(timeRange != TimeRange.not_required && timeRange!=null) {
                optionalFilters.put("time_range", timeRange.name());
            }
            WebSearchEngine tavilySearchEngine = TavilyWebSearchEngine.builder()
                    .apiKey(Config.TAVILY_API_KEY)
                    .baseUrl(Config.TAVILY_API_URL)
                    .searchDepth(searchdepth.toString())
                    .excludeDomains(excludeDomains)
                    .build();
            System.out.println("[Ground Search]: Querying Tavily engine matrix for: " + searchKeywords);

            WebSearchRequest searchRequest = WebSearchRequest.builder()
                    .searchTerms(searchKeywords)
                    .additionalParams(optionalFilters)
                    .build();

            WebSearchResults searchResults = tavilySearchEngine.search(searchRequest);

            if(searchResults==null){
                System.out.println(searchKeywords+" "+ "returned no result");
                return searchKeywords + " returned no result. Issue lies with the connection or the query. Try adjusting Search Depth or Search Keyword";
            }



            StringBuilder responseBuilder = new StringBuilder();

            for (WebSearchOrganicResult result: searchResults.results()){
                String title = result.title();
                String content = result.snippet();
                String source = (result.url() != null) ? result.url().toString() : "Unknown Source";
                if(content!=null && !content.trim().isEmpty()){
                    responseBuilder.append("Title:- ").append(title).append(" \n");
                    responseBuilder.append("Content:- ").append(content).append(" \n");
                    responseBuilder.append("Source:- ").append(source).append(" \n");

                }
            }
            if(responseBuilder.isEmpty()){
                return "{\"status\": \"no_results\", \"message\": \"Found results, but none matched formatting validation criteria.\"}";

            }

            // Wrap the raw web context in a JSON object to hand back to the LLM
            JsonObject toolResult = new JsonObject();
            toolResult.addProperty("status", "success");
            toolResult.addProperty("web_context", responseBuilder.toString().trim());
            System.out.println(toolResult.toString());

            return toolResult.toString();

        } catch (Exception e) {
            System.err.println("[Ground Search Critical]: Complete runtime failure inside tool handler.");
            e.printStackTrace();
            return "{\"error\": \"A critical exception paralyzed my grounding thread execution loop.\"}";
        }
    }



    //Complex Search Tool

    public List <String> searchUrl(String query){
        List <String> responseList = new ArrayList<>();


        try{
            // Defensive validation: Look for 'query' matching the BuildToolRegister schema

            String searchKeywords =query;
            List<String> excludeDomains = new ArrayList<>();
            excludeDomains.add("facebook.com");
            excludeDomains.add("twitter.com");

            Map<String, Object> optionalFilters = new HashMap<>();


            WebSearchEngine tavilySearchEngine = TavilyWebSearchEngine.builder()
                    .apiKey(Config.TAVILY_API_KEY)
                    .baseUrl(Config.TAVILY_API_URL)
                    .searchDepth("basic")
                    .excludeDomains(excludeDomains)
                    .build();
            System.out.println("[Ground Search]: Querying Tavily engine matrix for: " + searchKeywords);

            WebSearchRequest searchRequest = WebSearchRequest.builder()
                    .searchTerms(searchKeywords)
                    //.additionalParams(optionalFilters)
                    .maxResults(5)
                    .build();

            WebSearchResults searchResults = tavilySearchEngine.search(searchRequest);

            if(searchResults==null){
                System.out.println(searchKeywords+" "+ "returned no result");
                responseList.add("1");
                return responseList;
            }


            for (WebSearchOrganicResult result: searchResults.results()){
                String content = result.snippet();
                String source = (result.url() != null) ? result.url().toString() : "Unknown Source";
                if(content!=null && !content.trim().isEmpty()){
                    responseList.add(source);
                }
            }
            if(responseList.isEmpty()){
                responseList.add("1");
                return responseList;

            }

            // Wrap the raw web context in a JSON object to hand back to the LLM
            System.out.println(responseList);

            return responseList;

        } catch (Exception e) {
            System.err.println("[Ground Search Critical]: Complete runtime failure inside tool handler.");
            e.printStackTrace();
            responseList.add("1");
            return responseList;
        }
    }



    @Tool("Search the web for real-time information or verification.")
    public String urlExtract(
            @P("The exact search query string to look up on the web.") String query) {
        List<String> urlList = searchUrl(query+"2026");

        try {
            // Defensive validation
            if (urlList == null || urlList.isEmpty()) {
                return "{\"error\": \"No URLs were provided to the extraction tool.\"}";
            }

            // Build the exact payload required by Tavily Extract
            JsonObject tavilyBody = new JsonObject();
            JsonArray urlsPayload = new JsonArray();
            for (String url : urlList) {
                urlsPayload.add(url);
            }
            tavilyBody.add("urls", urlsPayload);
            tavilyBody.addProperty("query", query);

            tavilyBody.addProperty("chunk_per_source",3);

            System.out.println("[Extraction Phase]: Fetching raw data from: " + urlList);

            // Execute HTTP request
            String rawExtractResult = HttpUtil.sendPost(
                    Config.Tavily_Extract_URL,
                    "Bearer " + Config.TAVILY_API_KEY,
                    tavilyBody.toString()
            );

            if (rawExtractResult == null || rawExtractResult.trim().isEmpty()) {
                System.err.println("[Extraction Error]: Upstream API returned empty content.");
                return "{\"error\": \"The extraction pipeline response was completely empty.\"}";
            }

            JsonObject jsonResponse = gson.fromJson(rawExtractResult, JsonObject.class);

            if (jsonResponse.has("detail") || !jsonResponse.has("results") || jsonResponse.get("results").isJsonNull()) {
                System.err.println("[Extraction Error]: API structural mismatch or rejected authorization.");
                return "{\"error\": \"The extraction layer returned a server error. Check URL validity.\"}";
            }

            JsonArray results = jsonResponse.getAsJsonArray("results");
            if (results.isEmpty()) {
                return "{\"status\": \"no_results\", \"message\": \"Failed to extract readable content from the provided URLs.\"}";
            }

            StringBuilder snippets = new StringBuilder();

            // Extract the raw content
            for (JsonElement element : results) {
                if (element.isJsonObject()) {
                    JsonObject resultObj = element.getAsJsonObject();

                    // Tavily Extract uses 'raw_content' instead of 'content'
                    if (resultObj.has("raw_content") && !resultObj.get("raw_content").isJsonNull()) {
                        String content = resultObj.get("raw_content").getAsString();
                        String url = resultObj.has("url") ? resultObj.get("url").getAsString() : "Unknown Source";

                        snippets.append("- Extracted from [").append(url).append("]:\n")
                                .append("  ").append(content).append("\n\n");
                    }
                }
            }

            if (snippets.length() == 0) {
                return "{\"status\": \"no_results\", \"message\": \"URLs were processed, but no readable text was returned.\"}";
            }

            // Wrap the raw context in a JSON object to hand back to the LLM
            JsonObject toolResult = new JsonObject();
            toolResult.addProperty("status", "success");
            toolResult.addProperty("extracted_context", snippets.toString().trim());

            return toolResult.toString();

        } catch (Exception e) {
            System.err.println("[Extraction Critical]: Complete runtime failure inside extraction tool.");
            e.printStackTrace();
            return "{\"error\": \"A critical exception paralyzed the extraction execution loop.\"}";
        }
    }
}