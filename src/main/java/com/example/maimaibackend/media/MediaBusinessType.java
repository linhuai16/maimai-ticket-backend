package com.example.maimaibackend.media;

import com.example.maimaibackend.common.BusinessException;

import java.util.Locale;

public enum MediaBusinessType {
    PROJECT_POSTER("project/poster", MediaFileType.IMAGE),
    BANNER_IMAGE("banner/image", MediaFileType.IMAGE),
    CATEGORY_ICON("category/icon", MediaFileType.IMAGE),
    NOTICE_ICON("notice/icon", MediaFileType.IMAGE),
    PROJECT_DETAIL_IMAGE("project/detail-image", MediaFileType.IMAGE),
    PROJECT_DETAIL_VIDEO("project/detail-video", MediaFileType.VIDEO),
    SESSION_DETAIL_IMAGE("session/detail-image", MediaFileType.IMAGE),
    SESSION_DETAIL_VIDEO("session/detail-video", MediaFileType.VIDEO);

    private final String relativeDirectory;
    private final MediaFileType mediaFileType;

    MediaBusinessType(String relativeDirectory, MediaFileType mediaFileType) {
        this.relativeDirectory = relativeDirectory;
        this.mediaFileType = mediaFileType;
    }

    public String getRelativeDirectory() {
        return relativeDirectory;
    }

    public MediaFileType getMediaFileType() {
        return mediaFileType;
    }

    public static MediaBusinessType from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("businessType 不能为空");
        }
        try {
            return MediaBusinessType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("不支持的媒体业务类型：" + value);
        }
    }
}
