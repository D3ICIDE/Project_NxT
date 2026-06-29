package amadeus.Memory;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MemoryConfig {
    private  final EmbeddingModel embeddingModel;
    private final InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private final Path storagePath = Paths.get("Amadeus_Memories.json");

    public MemoryConfig() {
        InMemoryEmbeddingStore<TextSegment> tempEmbeddingStore;
        this.embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        if (Files.exists(storagePath)) {
            try {
                tempEmbeddingStore = InMemoryEmbeddingStore.fromFile(storagePath);
            } catch (Exception e) {
                tempEmbeddingStore = new InMemoryEmbeddingStore<>();
            }
        } else {
            tempEmbeddingStore = new InMemoryEmbeddingStore<>();
    }
        this.embeddingStore = tempEmbeddingStore;
    }

    public Path getStoragePath(){return storagePath;}

    public  EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }

    public InMemoryEmbeddingStore<TextSegment> getEmbeddingStore() {
        return embeddingStore;
    }

}
