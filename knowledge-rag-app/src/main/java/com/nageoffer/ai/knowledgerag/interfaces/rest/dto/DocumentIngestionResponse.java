package com.nageoffer.ai.knowledgerag.interfaces.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DocumentIngestionResponse {

    private String fileName;

    private String kb;

    private Integer chunkCount;

    public DocumentIngestionResponse(String fileName, String kb, Integer chunkCount) {
        this.fileName = fileName;
        this.kb = kb;
        this.chunkCount = chunkCount;
    }
}
