package com.example.maimaibackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@ConfigurationProperties(prefix = "maimai.media")
public class MediaStorageProperties {

    private String root = "../maimai-media";
    private String publicPrefix = "/media";
    private long imageMaxSize = 10L * 1024L * 1024L;
    private long videoMaxSize = 200L * 1024L * 1024L;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getPublicPrefix() {
        return publicPrefix;
    }

    public void setPublicPrefix(String publicPrefix) {
        this.publicPrefix = publicPrefix;
    }

    public long getImageMaxSize() {
        return imageMaxSize;
    }

    public void setImageMaxSize(long imageMaxSize) {
        this.imageMaxSize = imageMaxSize;
    }

    public long getVideoMaxSize() {
        return videoMaxSize;
    }

    public void setVideoMaxSize(long videoMaxSize) {
        this.videoMaxSize = videoMaxSize;
    }

    public Path getRootPath() {
        return Paths.get(root).toAbsolutePath().normalize();
    }

    public String getNormalizedPublicPrefix() {
        String value = publicPrefix == null || publicPrefix.isBlank() ? "/media" : publicPrefix.trim();
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        while (value.endsWith("/") && value.length() > 1) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
