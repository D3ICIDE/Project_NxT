package amadeus.Memory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.Deque;

public class MemoryHistory {
    private final Deque<JsonObject> history;
    private final int maxMessages;

    public MemoryHistory(int maxMessages) {
        this.history = new ArrayDeque<>();
        this.maxMessages = maxMessages;
    }

    public synchronized void addMessage(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);

        if (history.size() >= maxMessages) {
            history.pollFirst();
        }
        history.addLast(message);
    }

    // NEW: Safely add a raw JSON object (like a tool call or tool response)
    public synchronized void addRawJsonObject(JsonObject messageObj) {
        if (history.size() >= maxMessages) {
            history.pollFirst();
        }
        // Use deepCopy() to prevent external modifications from affecting the history state
        history.addLast(messageObj.deepCopy());
    }

    // FIXED: compile to json without dropping tool_call fields or throwing NullPointerExceptions
    public synchronized JsonArray getHistoryAsJson() {
        JsonArray array = new JsonArray();
        for (JsonObject msg : history) {
            // Just append the deep copy of the object exactly as it exists
            array.add(msg.deepCopy());
        }
        return array;
    }

    public synchronized void clear() {
        history.clear();
    }
}