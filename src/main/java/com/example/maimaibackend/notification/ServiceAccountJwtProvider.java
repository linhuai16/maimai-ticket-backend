package com.example.maimaibackend.notification;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.PSSParameterSpec;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ServiceAccountJwtProvider {
    private static final String AUDIENCE = "https://oauth-login.cloud.huawei.com/oauth2/v3/token";
    private final PushProperties properties;
    private final ObjectMapper objectMapper;
    private String cachedToken;
    private long cachedUntil;

    public ServiceAccountJwtProvider(PushProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public synchronized String getToken() {
        long now = Instant.now().getEpochSecond();
        if (cachedToken != null && now < cachedUntil - 300) return cachedToken;
        try {
            JsonNode key = objectMapper.readTree(Files.readString(Path.of(properties.getServiceAccountFile())));
            String projectId = text(key, "project_id");
            if (!projectId.equals(properties.getProjectId())) throw new IllegalStateException("Push projectId与服务账号不一致");
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("kid", text(key, "key_id"));
            header.put("typ", "JWT");
            header.put("alg", "PS256");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("aud", AUDIENCE);
            payload.put("iss", text(key, "sub_account"));
            payload.put("iat", now);
            payload.put("exp", now + 3600);
            String head = base64Url(objectMapper.writeValueAsBytes(header));
            String body = base64Url(objectMapper.writeValueAsBytes(payload));
            String signingInput = head + "." + body;
            Signature signer = Signature.getInstance("RSASSA-PSS");
            signer.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
            signer.initSign(privateKey(text(key, "private_key")));
            signer.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            cachedToken = signingInput + "." + base64Url(signer.sign());
            cachedUntil = now + 3600;
            return cachedToken;
        } catch (Exception ex) {
            throw new IllegalStateException("Push服务账号鉴权令牌生成失败", ex);
        }
    }

    private String text(JsonNode root, String name) {
        String value = root.path(name).asText("").trim();
        if (value.isEmpty()) throw new IllegalStateException("Push服务账号缺少" + name);
        return value;
    }

    private PrivateKey privateKey(String pem) throws Exception {
        String value = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(value)));
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
