package com.nageoffer.ai.knowledgerag.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RagChatRequest {

    @NotBlank
    private String question;

    private String kb;

    private String sessionId;
}
