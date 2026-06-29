package amadeus.Legacy.router;

import amadeus.Legacy.funcTool;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;

public class ResponseHandler {

    public static String executeNativeTool(String functionName, JsonObject argumentsJson, String userMessage, WebSocket webSocket) {
        SkillRegistry.SkillMeta skill = SkillRegistry.getSkill(functionName);

        if (skill == null) {
            System.err.println("[Router Error]: Unmapped function name: " + functionName);
            return "{\"status\":\"error\",\"message\":\"Target tool execution vector missing.\"}";
        }

        try {
            System.out.println("[ResponseHandler]: Dispatching background execution for -> " + functionName);

            // Uses your SkillRegistry to instantiate the correct tool class
            funcTool tool = SkillRegistry.createToolInstance(skill, userMessage, webSocket);

            // Directly execute the tool and return its raw string data back to the Orchestrator thread
            return tool.execute(argumentsJson);

        } catch (Exception e) {
            System.err.println("[Thread Isolation Alert]: Exception caught during background execution of " + functionName);
            e.printStackTrace();
            return "{\"status\":\"error\",\"message\":\"A system exception occurred while running the tool: " + e.getMessage() + "\"}";
        }
    }
}