package com.abyss.amadeus.server;
import com.abyss.amadeus.Memory.Memorize;
import com.abyss.amadeus.Memory.MemoryConfig;
import com.abyss.amadeus.Memory.MemoryHistory;
import com.abyss.amadeus.Memory.Total_Recall;
import com.abyss.amadeus.config.Config;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.abyss.amadeus.core.SystemPrompt;


public class Server extends WebSocketServer {
    private static final ExecutorService pipelineExecutor = Executors.newCachedThreadPool();

    static SystemPrompt sysPrompt = new SystemPrompt("src/main/resources/system_prompt.md");
    static String systemPrompt = sysPrompt.promptLoader();
    String model = Config.mainModel;
     final Memorize ingestor;
     final MemoryConfig memoryConfig;
    private final Total_Recall recall;
    MemoryHistory history = new MemoryHistory(25);
   public Server(InetSocketAddress address) {
       super(address);

       this.memoryConfig = new  MemoryConfig();
       this.ingestor = new Memorize(this.memoryConfig);
       this.recall = new Total_Recall(this.memoryConfig);

   }
    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("websocket open, Listening for connection at " + webSocket.getRemoteSocketAddress());

    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
       System.out.println("websocket closed");

    }

    @Override
    public void onMessage(WebSocket webSocket, String userMessage) {
        Config.setActiveWebSocket(webSocket);
        Config.setCurrentUserMessage(userMessage);
        System.out.println("User said:-> " + userMessage);
        //Receive the raw response from LLM
        pipelineExecutor.submit(() -> {
            try {

                List<String> relevantContext = recall.getRelevantContext(userMessage);

                ExecutionOrchestrator.handleInteraction(
                        userMessage,
                        relevantContext,
                        history, // Pass the MemoryHistory object itself
                        systemPrompt,
                        model,
                        webSocket,
                        pipelineExecutor
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });



    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
       e.printStackTrace();

    }

    @Override
    public void onStart() {
       System.out.println("Server started");

    }
}
