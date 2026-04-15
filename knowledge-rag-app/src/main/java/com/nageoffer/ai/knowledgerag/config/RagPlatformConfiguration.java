package com.nageoffer.ai.knowledgerag.config;

import com.nageoffer.ai.knowledgerag.infrastructure.rag.ElasticsearchChunkRepository;
import com.nageoffer.ai.knowledgerag.infrastructure.rag.HybridDocumentRetriever;
import com.nageoffer.ai.knowledgerag.infrastructure.rag.KeywordDocumentRetriever;
import com.nageoffer.ai.knowledgerag.infrastructure.rag.NonReturnDirectToolCallback;
import com.nageoffer.ai.knowledgerag.infrastructure.rag.QueryRewriteTransformer;
import io.modelcontextprotocol.client.McpSyncClient;
import org.apache.tika.Tika;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableConfigurationProperties(RagPlatformProperties.class)
public class RagPlatformConfiguration {

    @Bean
    public ToolCallback[] ragToolCallbacks(List<McpSyncClient> mcpSyncClients) {
        SyncMcpToolCallbackProvider toolCallbackProvider = SyncMcpToolCallbackProvider.builder()
                .mcpClients(mcpSyncClients)
                .build();

        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(NonReturnDirectToolCallback::wrap)
                .toArray(ToolCallback[]::new);
    }

    @Bean
    public ChatMemory chatMemory(RagPlatformProperties ragPlatformProperties) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(ragPlatformProperties.getMemoryMaxMessages())
                .build();
    }

    @Bean
    public KeywordDocumentRetriever keywordDocumentRetriever(
            ElasticsearchChunkRepository elasticsearchChunkRepository,
            RagPlatformProperties ragPlatformProperties) {
        return new KeywordDocumentRetriever(elasticsearchChunkRepository, ragPlatformProperties);
    }

    @Bean
    public HybridDocumentRetriever hybridDocumentRetriever(
            VectorStore vectorStore,
            KeywordDocumentRetriever keywordDocumentRetriever,
            RagPlatformProperties ragPlatformProperties) {
        VectorStoreDocumentRetriever vectorRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(ragPlatformProperties.getRetrieveTopK())
                .build();
        return new HybridDocumentRetriever(vectorRetriever, keywordDocumentRetriever, ragPlatformProperties);
    }

    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(
            HybridDocumentRetriever hybridDocumentRetriever,
            QueryRewriteTransformer queryRewriteTransformer,
            List<DocumentPostProcessor> documentPostProcessors,
            @Value("classpath:/prompts/answer-user.st") Resource ragAugmentPrompt) {
        ContextualQueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
                .promptTemplate(new PromptTemplate(ragAugmentPrompt))
                .allowEmptyContext(true)
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(queryRewriteTransformer)
                .documentRetriever(hybridDocumentRetriever)
                .documentPostProcessors(documentPostProcessors)
                .queryAugmenter(queryAugmenter)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel,
                                 ToolCallback[] toolCallbacks,
                                 ChatMemory chatMemory,
                                 RetrievalAugmentationAdvisor retrievalAugmentationAdvisor,
                                 @Value("classpath:/prompts/answer-system.st") Resource answerSystemPrompt) {
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(chatModel)
                .defaultSystem(answerSystemPrompt)
                .defaultToolCallbacks(toolCallbacks)
                .defaultAdvisors(memoryAdvisor, retrievalAugmentationAdvisor)
                .build();
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter(RagPlatformProperties ragPlatformProperties) {
        return TokenTextSplitter.builder()
                .withChunkSize(ragPlatformProperties.getChunkSize())
                .withMinChunkSizeChars(ragPlatformProperties.getMinChunkSizeChars())
                .withMinChunkLengthToEmbed(ragPlatformProperties.getMinChunkLengthToEmbed())
                .withMaxNumChunks(ragPlatformProperties.getMaxNumChunks())
                .withKeepSeparator(true)
                .build();
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }

    @Bean
    public QueryRewriteTransformer queryRewriteTransformer(
            ChatModel chatModel,
            RagPlatformProperties ragPlatformProperties,
            @Value("classpath:/prompts/rewrite-system.st") Resource rewriteSystemPrompt,
            @Value("classpath:/prompts/rewrite-user.st") Resource rewriteUserPrompt) {
        return new QueryRewriteTransformer(
                chatModel,
                rewriteSystemPrompt,
                rewriteUserPrompt,
                ragPlatformProperties.getRewriteModel());
    }

    @Bean
    public TaskExecutor ragTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("knowledge-rag-sse-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
