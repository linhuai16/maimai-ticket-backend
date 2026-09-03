package com.example.maimaibackend.ai.infrastructure.llm;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import java.util.function.Consumer;

public interface AiChatProvider {
    JsonNode complete(ArrayNode messages, ArrayNode tools);
    void stream(ArrayNode messages, Consumer<String> textConsumer);
}
