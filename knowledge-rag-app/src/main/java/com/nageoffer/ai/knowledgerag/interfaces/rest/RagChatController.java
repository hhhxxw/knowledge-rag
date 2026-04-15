package com.nageoffer.ai.knowledgerag.interfaces.rest;

import com.nageoffer.ai.knowledgerag.application.service.RagChatApplicationService;
import com.nageoffer.ai.knowledgerag.interfaces.rest.dto.RagChatRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@Validated
public class RagChatController {

    private final RagChatApplicationService ragChatApplicationService;

    public RagChatController(RagChatApplicationService ragChatApplicationService) {
        this.ragChatApplicationService = ragChatApplicationService;
    }

    @PostMapping(
            path = {"/api/v1/chat/stream", "/api/rag/chat/stream"},
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter streamChat(@Valid @RequestBody RagChatRequest request) {
        return ragChatApplicationService.streamChat(request);
    }
}
