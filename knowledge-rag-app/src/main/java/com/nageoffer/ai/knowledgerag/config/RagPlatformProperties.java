package com.nageoffer.ai.knowledgerag.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "knowledge.rag")
public class RagPlatformProperties {

    private String rewriteModel;

    private String answerModel;

    @NotBlank
    private String rerankModel;

    @NotBlank
    private String rerankEndpoint;

    @Min(1)
    @Max(100)
    private Integer retrieveTopK = 8;

    @Min(1)
    @Max(100)
    private Integer rerankTopN = 4;

    @Min(128)
    @Max(1024000)
    private Integer rerankMaxDocumentChars = 12000;

    @Min(128)
    @Max(4000)
    private Integer chunkSize = 800;

    @Min(32)
    @Max(1000)
    private Integer minChunkSizeChars = 350;

    @Min(1)
    @Max(100)
    private Integer minChunkLengthToEmbed = 10;

    @Min(1)
    @Max(10000)
    private Integer maxNumChunks = 1000;

    @Min(2)
    @Max(100)
    private Integer memoryMaxMessages = 20;

    @Min(1)
    @Max(100)
    private Integer keywordTopK = 8;

    @Min(1)
    @Max(200)
    private Integer rrfK = 60;

    @NotBlank
    private String keywordAnalyzer = "ik_smart";

    @NotBlank
    private String elasticsearchUrl = "http://localhost:9200";

    @NotBlank
    private String keywordIndexName = "knowledge_rag_chunks";
}
