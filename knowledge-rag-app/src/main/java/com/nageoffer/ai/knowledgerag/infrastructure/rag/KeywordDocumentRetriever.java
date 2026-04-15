package com.nageoffer.ai.knowledgerag.infrastructure.rag;

import com.nageoffer.ai.knowledgerag.config.RagPlatformProperties;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;

@Slf4j
@RequiredArgsConstructor
public class KeywordDocumentRetriever implements DocumentRetriever {

    private static final Pattern KB_PATTERN = Pattern.compile("kb\\s*==\\s*'([^']*)'");

    private final ElasticsearchChunkRepository elasticsearchChunkRepository;
    private final RagPlatformProperties ragPlatformProperties;

    @Override
    public @NonNull List<Document> retrieve(@NonNull Query query) {
        String kb = extractKb(query);
        int topK = ragPlatformProperties.getKeywordTopK();
        log.info("[Keyword] 开始 BM25 检索, query='{}', kb='{}', topK={}", query.text(), kb, topK);
        return elasticsearchChunkRepository.search(query.text(), kb, topK);
    }

    private String extractKb(Query query) {
        Object filterExpr = query.context().get(VectorStoreDocumentRetriever.FILTER_EXPRESSION);
        if (filterExpr instanceof String expr) {
            Matcher matcher = KB_PATTERN.matcher(expr);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}
