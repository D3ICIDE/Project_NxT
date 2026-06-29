package amadeus.Legacy;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BuildToolRegister {


    public JsonArray registeration() {
        JsonArray toolRegister = new JsonArray();

//Ground Search Tool
        JsonObject groundSearchParameters = new JsonObject();
        groundSearchParameters.add("query", createProperty("string","The search query string to look up."));
        toolRegister.add(createTool("GroundSearchTool","Search the web for real-time information or verification.",groundSearchParameters,
                new String[]{"query"}));

        //HardwareControlTool
        JsonObject hardwareControlParameters = new JsonObject();

        hardwareControlParameters.add("shape", createEnumProperty("string", "The hardware system context.", new String[]{"system"}));
        hardwareControlParameters.add("sub_action", createProperty("string", "The specific NirCmd system verb (e.g., setsysvolume, setbrightness, standby, exitwin)."));
        hardwareControlParameters.add("target", createProperty("string", "Leave empty string for system settings."));
        hardwareControlParameters.add("modifier", createProperty("string", "The target value or parameter modification (e.g., '30000', '75', 'poweroff', 'reboot'). Leave empty if none."));
        toolRegister.add(createTool("hardware_control",
                "Control system-level device hardware settings like volume, screen brightness, power states using NirCmd system verbs.",
                hardwareControlParameters,
                new String[]{"shape", "sub_action"}));

//App Control Tool
        JsonObject appControlParameters = new JsonObject();
        appControlParameters.add("shape", createEnumProperty("string", "The window style context.", new String[]{"none", "window"}));
        appControlParameters.add("sub_action", createProperty("string", "The specific NirCmd window/process verb (e.g., launch, setsize, setalpha, close , min, max)."));
        appControlParameters.add("target", createProperty("string", "The full standard commercial name of the application (e.g., notepad, chrome, spotify). Expand acronyms."));
        appControlParameters.add("modifier", createProperty("string", "Any trailing dimensions, coordinates, or attributes required (e.g., '0 0 960 1080', '150'). Leave empty if none."));
        toolRegister.add(createTool("app_control",
                "Open, close, size, or modify windows of local desktop applications using NirCmd verbs. DO NOT use this for web apps or streaming services (e.g., YouTube, Spotify, Netflix) — use open_url instead.",
                appControlParameters,
                new String[]{"shape", "sub_action", "target"}));


        //FetchWeatherTool

        JsonObject weatherParameters = new JsonObject();
        weatherParameters.add("location", createProperty("string", "The target city name. Leave empty if no city is specified.A default city name is already present so no need to ask again"));
        weatherParameters.add("endpoint", createEnumProperty("string", "The exact API endpoint based on intent.", new String[]{"/current.json", "/forecast.json"}));
        toolRegister.add(createTool("weather_check",
                "Queries the live weather API for current conditions, forecasts, and astronomical data.",
                weatherParameters,
                new String[]{"location", "endpoint"}));

        //SearchWebTool

        JsonObject searchWebParameters = new JsonObject();
        searchWebParameters.add("url", createProperty("string", "Opens a specific URL in the user's web browser. Use this tool when the user explicitly asks to open a website. If they ask to search a topic on a specific site, guess the direct search URL (e.g., website.com/results?search_query=topic) and open it."));
        toolRegister.add(createTool("open_url",
                "Open a specific website, URL, or web domain in the default web browser. ALWAYS use this tool for websites and web links.",
                searchWebParameters,
                new String[]{"url"}));

return toolRegister;

    }



    private JsonObject createProperty(String type,String discription){
        JsonObject property = new JsonObject();
        property.addProperty("type",type);
        property.addProperty("description",discription);
        return  property;
    }



    private JsonObject createTool(String name,String description,JsonObject properties,String[] required){
        JsonObject tool = new JsonObject();
        tool.addProperty("type","function");

        JsonObject function = new JsonObject();
        function.addProperty("name",name);
        function.addProperty("description",description);

        JsonObject parameters = new JsonObject();
        parameters.addProperty("type","object");
        parameters.add("properties",properties);

        JsonArray requiredField = new JsonArray();
        for(String field: required){
            requiredField.add(field);
        }
        parameters.add("required",requiredField);

        function.add("parameters",parameters);
        tool.add("function",function);
        return  tool;
    }

    private JsonObject createEnumProperty(String type, String description, String[] enumValues) {
        JsonObject property = createProperty(type, description);
        JsonArray enumArray = new JsonArray();
        for (String value : enumValues) {
            enumArray.add(value);
        }
        property.add("enum", enumArray);
        return property;
    }


}
