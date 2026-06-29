package amadeus.Legacy.router;

import amadeus.Legacy.funcTool;
import org.java_websocket.WebSocket;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class SkillRegistry {

    public static class SkillMeta {
        public String type;
        public String classPath;

        public SkillMeta(String type, String classPath) {
            this.type = type;
            this.classPath = classPath;
        }
    }

    private static final Map<String, SkillMeta> registry = new HashMap<>();

    static {
        // Direct, type-safe registration. No file I/O or regex needed!
        register("app_control", "com.abyss.amadeus.tools.AppControlTool");
        register("hardware_control", "com.abyss.amadeus.tools.HardwareControlTool");
        register("GroundSearchTool", "com.abyss.amadeus.tools.GroundSearchTool");
        register("weather_check", "com.abyss.amadeus.tools.FetchWeatherTool");
        register("open_url", "com.abyss.amadeus.tools.SearchWebTool");
    }

    private static void register(String type, String classPath) {
        registry.put(type.toLowerCase(), new SkillMeta(type, classPath));
    }

    public static SkillMeta getSkill(String type) {
        return registry.get(type.toLowerCase());
    }

    // This reflection engine remains 100% active and useful!
    public static funcTool createToolInstance(SkillMeta meta, String userMessage, WebSocket webSocket) throws Exception {
        Class<?> clazz = Class.forName(meta.classPath);
        try {
            // Checks for complex multi-argument constructors (Ground_Search_Tool)
            Constructor<?> ctor = clazz.getConstructor(String.class, WebSocket.class);
            return (funcTool) ctor.newInstance(userMessage, webSocket);
        } catch (NoSuchMethodException e) {
            // Falls back safely to standard default constructors (AppControlTool / HardwareControlTool)
            return (funcTool) clazz.getDeclaredConstructor().newInstance();
        }
    }
}