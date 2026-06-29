package amadeus.legacyCodes.server;

import amadeus.memory.MemoryHistory;
import amadeus.legacyCodes.router.ResponseHandler;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class ExecutionOrchestrator {
    private static final Gson gson = new Gson();
    // Set a strict limit so the LLM can't trap itself in an infinite loop
    private static final int MAX_TOOL_ITERATIONS = 3;

    public static void handleInteraction(
            String originalUserMessage,
            List<String> relevantContext,
            MemoryHistory historyManager,
            String systemPrompt,
            String model,
            WebSocket webSocket,
            ExecutorService executor) {

        try {
            int iterationCount = 0;
            boolean interactionComplete = false;

            // The message we send to the LLM. It clears out after the first pass so we don't duplicate it.
            String currentLoopMessage = originalUserMessage;

            // Safely loop up to our max limit
            while (iterationCount < MAX_TOOL_ITERATIONS && !interactionComplete) {
                iterationCount++;


                JsonArray currentHistoryJson = historyManager.getHistoryAsJson();
                JsonObject messageObj = commandProcessor.processCommand(currentLoopMessage, relevantContext, currentHistoryJson, systemPrompt, model, 0.8);


                if (messageObj.has("tool_calls") && !messageObj.get("tool_calls").isJsonNull()) {
                    JsonArray toolCalls = messageObj.getAsJsonArray("tool_calls");

                    // Append the LLM's tool request to history FIRST
                    historyManager.addRawJsonObject(messageObj);

                    List<CompletableFuture<JsonObject>> activeTasks = new ArrayList<>();


                    for (JsonElement element : toolCalls) {
                        JsonObject toolCall = element.getAsJsonObject();
                        String callId = toolCall.get("id").getAsString();
                        JsonObject func = toolCall.getAsJsonObject("function");
                        String funcName = func.get("name").getAsString();
                        JsonObject args = gson.fromJson(func.get("arguments").getAsString(), JsonObject.class);

                        CompletableFuture<JsonObject> future = CompletableFuture.supplyAsync(() -> {
                            String result = ResponseHandler.executeNativeTool(funcName, args, originalUserMessage, webSocket);

                            JsonObject toolObj = new JsonObject();
                            toolObj.addProperty("role", "tool");
                            toolObj.addProperty("tool_call_id", callId);
                            toolObj.addProperty("name", funcName);
                            toolObj.addProperty("content", result);
                            return toolObj;
                        }, executor);

                        activeTasks.add(future);
                    }


                    CompletableFuture.allOf(activeTasks.toArray(new CompletableFuture[0])).join();


                    for (CompletableFuture<JsonObject> task : activeTasks) {
                        historyManager.addRawJsonObject(task.get());
                    }


                    currentLoopMessage = "SYSTEM DIRECTIVE: Tool execution complete. Acknowledge the results and respond directly to the user. DO NOT run any more tools.";

                } else {
                    // NO TOOL CALLS: The LLM actually generated text!
                    String finalReply = messageObj.has("content") && !messageObj.get("content").isJsonNull()
                            ? messageObj.get("content").getAsString().trim() : "";

                    if (!finalReply.isEmpty()) {
                        webSocket.send(finalReply);
                        historyManager.addMessage("user", originalUserMessage);
                        historyManager.addMessage("assistant", finalReply);
                    } else {
                        // Edge case: LLM returned no tools and no text.
                        webSocket.send("I executed the request but couldn't formulate a proper response.");
                    }

                    // Break the loop
                    interactionComplete = true;
                }
            }

            // If we hit the iteration limit without interactionComplete turning true
            if (!interactionComplete) {
                System.err.println("[Orchestrator Alert]: Max tool loop iterations (" + MAX_TOOL_ITERATIONS + ") reached.");
                webSocket.send("I had to stop processing because I got caught in an execution loop.");
                historyManager.addMessage("user", originalUserMessage);
                historyManager.addMessage("assistant", "System aborted execution due to max loop iterations.");
            }

        } catch (Exception e) {
            System.err.println("[Orchestrator Error]: Failure during execution pipeline.");
            e.printStackTrace();
            webSocket.send("I experienced a critical disruption in my orchestration pipeline.");
        }
    }
}