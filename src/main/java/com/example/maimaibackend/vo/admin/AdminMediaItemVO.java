package com.example.maimaibackend.vo.admin;

import java.time.LocalDateTime;

public class AdminMediaItemVO {
    private String businessType;
    private String mediaType;
    private String fileName;
    private String originalName;
    private String relativePath;
    private String url;
    private String mimeType;
    private Long fileSize;
    private LocalDateTime lastModifiedTime;

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getRelativePath() { return relativePath; }
    public void setRelativePath(String relativePath) { this.relativePath = relativePath; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public LocalDateTime getLastModifiedTime() { return lastModifiedTime; }
    public void setLastModifiedTime(LocalDateTime lastModifiedTime) { this.lastModifiedTime = lastModifiedTime; }
}
