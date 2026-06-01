package com.abyss.amadeus.Memory;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;

import java.util.ArrayList;
import java.util.List;

public class Total_Recall {
    private final ContentRetriever contentRetriever;

    public Total_Recall(MemoryConfig config) {
        this.contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(config.getEmbeddingStore())
                .embeddingModel(config.getEmbeddingModel())
                .maxResults(3)
                .minScore(0.70)
                .build();
    }

    public List<String> getRelevantContext(String userQuery) {
        List<String> textContextResults = new ArrayList<>();

        //  find matching content
        List<Content> matches = contentRetriever.retrieve(Query.from(userQuery));

        for (Content match : matches) {
            textContextResults.add(match.textSegment().text());
        }

        return textContextResults;
    }
}
