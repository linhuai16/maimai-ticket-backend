package com.example.maimaibackend.config;

import org.springframework.context.annotation.Configuration;

/**
 * JSON 序列化由 Spring Boot 4 的 Jackson 3 自动配置统一管理。
 * 保留该配置类作为扩展入口，避免再次手工创建不同版本的 ObjectMapper。
 */
@Configuration(proxyBeanMethods = false)
public class JacksonConfig {
}
