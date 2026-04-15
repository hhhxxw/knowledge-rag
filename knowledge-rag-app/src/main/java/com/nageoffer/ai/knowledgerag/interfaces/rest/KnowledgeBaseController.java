package com.nageoffer.ai.knowledgerag.interfaces.rest;

import com.nageoffer.ai.knowledgerag.application.service.KnowledgeBaseIngestionApplicationService;
import com.nageoffer.ai.knowledgerag.interfaces.rest.dto.DocumentIngestionResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
public class KnowledgeBaseController {

    private final KnowledgeBaseIngestionApplicationService knowledgeBaseIngestionApplicationService;

    public KnowledgeBaseController(KnowledgeBaseIngestionApplicationService knowledgeBaseIngestionApplicationService) {
        this.knowledgeBaseIngestionApplicationService = knowledgeBaseIngestionApplicationService;
    }

    @PostMapping(
            path = {"/api/v1/knowledge/documents", "/api/rag/knowledge/upload"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public DocumentIngestionResponse uploadFile(@RequestParam("file") MultipartFile file,
                                                @RequestParam(value = "kb", required = false) String kb) {
        return knowledgeBaseIngestionApplicationService.ingest(file, kb);
    }
}
