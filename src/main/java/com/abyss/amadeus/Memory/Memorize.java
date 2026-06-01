package com.abyss.amadeus.Memory;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.data.document.Document;


public class Memorize {
    private final EmbeddingStoreIngestor ingestor;
    private final MemoryConfig memoryConfig;



        public Memorize(MemoryConfig memoryConfig) {
            this.memoryConfig = memoryConfig;

            // LangChain4j handles the wiring of the model to the store for writing
            this.ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingModel(memoryConfig.getEmbeddingModel())
                    .embeddingStore(memoryConfig.getEmbeddingStore())
                    .build();
        }

    public synchronized void commitInteraction(String contextText) {
        // Convert the string into a LangChain4j Document and ingest it
        Document document = Document.from(contextText);
        ingestor.ingest(document);

        // Save to disk asynchronously
        new Thread(() -> {
            try {
                memoryConfig.getEmbeddingStore().serializeToFile(memoryConfig.getStoragePath());
            } catch (Exception e) {
                System.err.println("Failed to serialize memory: " + e.getMessage());
            }
        }).start();
    }
}
