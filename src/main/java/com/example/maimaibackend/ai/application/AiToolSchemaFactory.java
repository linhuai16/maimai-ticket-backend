package com.example.maimaibackend.ai.application;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Component
class AiToolSchemaFactory {
    private final ObjectMapper objectMapper;

    AiToolSchemaFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    ArrayNode build() {
        ArrayNode tools = objectMapper.createArrayNode();
        ObjectNode search = properties(
                stringProperty("keyword", "用户本轮明确提到的演出标题、艺人、场馆等关键词；没有则不要猜"),
                stringProperty("city", "用户本轮明确提到的城市；没有则可省略，由麦麦上下文补齐"),
                stringProperty("category", "用户本轮明确提到的演出分类，如演唱会、音乐会"),
                stringProperty("venue", "用户本轮明确提到的场馆名称；没有则不要猜"),
                stringProperty("startTime", "用户本轮明确日期对应的ISO日期或时间下限"),
                stringProperty("endTime", "用户本轮明确日期对应的ISO日期或时间上限"),
                numberProperty("minPrice", "用户本轮明确最低预算"),
                numberProperty("maxPrice", "用户本轮明确最高预算"),
                enumProperty("sort", "用户明确排序意图", "NEAREST", "PRICE_ASC", "PRICE_DESC", "HOT", "NEW"),
                enumProperty("timeIntent", "用户明确时间意图", "RECENT", "NEXT_7_DAYS", "TODAY", "TOMORROW", "THIS_WEEK", "WEEKEND", "THIS_MONTH", "PAST", "EXPLICIT_DATE", "FUTURE"),
                numberProperty("limit", "返回数量，1到8")
        );
        tools.add(tool("searchPerformances", "按真实麦麦演出查询能力搜索演出", search));
        tools.add(tool("getPerformanceDetail", "查询真实演出详情", properties(numberProperty("projectId", "演出项目ID"))));
        tools.add(tool("getSessions", "查询演出的真实场次", properties(numberProperty("projectId", "演出项目ID"))));
        tools.add(tool("getTicketSkus", "查询指定演出场次的真实票档、价格和库存", properties(
                numberProperty("projectId", "演出项目ID"), numberProperty("sessionId", "场次ID"))));
        tools.add(tool("getRefundRule", "查询演出的真实退款规则", properties(numberProperty("projectId", "演出项目ID"))));
        return tools;
    }

    private ObjectNode tool(String name, String description, ObjectNode propertiesNode) {
        ObjectNode parameters = objectMapper.createObjectNode();
        parameters.put("type", "object");
        parameters.set("properties", propertiesNode);
        parameters.put("additionalProperties", false);
        ObjectNode function = objectMapper.createObjectNode();
        function.put("name", name);
        function.put("description", description);
        function.set("parameters", parameters);
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("type", "function");
        tool.set("function", function);
        return tool;
    }

    private ObjectNode properties(ObjectNode... fields) {
        ObjectNode node = objectMapper.createObjectNode();
        for (ObjectNode field : fields) {
            String name = field.path("name").asText();
            field.remove("name");
            node.set(name, field);
        }
        return node;
    }

    private ObjectNode stringProperty(String name, String description) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", name);
        node.put("type", "string");
        node.put("description", description);
        return node;
    }

    private ObjectNode numberProperty(String name, String description) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", name);
        node.put("type", "number");
        node.put("description", description);
        return node;
    }

    private ObjectNode enumProperty(String name, String description, String... values) {
        ObjectNode node = stringProperty(name, description);
        ArrayNode enums = objectMapper.createArrayNode();
        for (String value : values) enums.add(value);
        node.set("enum", enums);
        return node;
    }
}
