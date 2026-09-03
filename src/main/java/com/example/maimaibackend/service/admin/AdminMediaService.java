package com.example.maimaibackend.service.admin;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.config.MediaStorageProperties;
import com.example.maimaibackend.media.MediaBusinessType;
import com.example.maimaibackend.media.MediaFileType;
import com.example.maimaibackend.vo.admin.AdminMediaItemVO;
import com.example.maimaibackend.vo.admin.AdminMediaPageVO;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class AdminMediaService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> NOTICE_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "svg");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm");
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM");
    private static final Pattern LEGACY_RESOURCE_PATTERN = Pattern.compile("^[\\p{L}\\p{N}._@-]+$");

    private final MediaStorageProperties properties;

    public AdminMediaService(MediaStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void initializeDirectories() {
        try {
            Files.createDirectories(properties.getRootPath());
            for (MediaBusinessType businessType : MediaBusinessType.values()) {
                Files.createDirectories(resolveBusinessDirectory(businessType));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("创建媒体存储目录失败：" + properties.getRootPath(), exception);
        }
    }


    /**
     * 校验富文本中引用的媒体文件。富文本媒体必须来自对应业务目录，
     * 不接受旧应用资源名、外部 URL、data URL 或任意磁盘路径。
     */
    public String requireStoredMediaReference(
            String value,
            MediaBusinessType businessType,
            String fieldName
    ) {
        String safeFieldName = fieldName == null || fieldName.isBlank() ? "媒体文件" : fieldName.trim();
        String reference = value == null ? "" : value.trim();
        if (reference.isEmpty()) {
            throw new BusinessException(safeFieldName + "地址不能为空");
        }
        if (reference.length() > 500) {
            throw new BusinessException(safeFieldName + "地址长度不能超过 500");
        }
        if (businessType == null) {
            throw new IllegalArgumentException("媒体业务类型不能为空");
        }

        String publicPrefix = properties.getNormalizedPublicPrefix();
        String publicPathPrefix = publicPrefix + "/";
        if (!reference.startsWith(publicPathPrefix)
                || reference.indexOf('?') >= 0
                || reference.indexOf('#') >= 0
                || reference.indexOf('\\') >= 0
                || reference.contains("..")) {
            throw new BusinessException(safeFieldName + "必须从统一媒体选择器中选择");
        }

        String relativePath = reference.substring(publicPathPrefix.length());
        String expectedDirectory = businessType.getRelativeDirectory() + "/";
        if (!relativePath.startsWith(expectedDirectory)) {
            throw new BusinessException(safeFieldName + "必须来自对应业务媒体目录");
        }

        Path file = properties.getRootPath().resolve(relativePath).normalize();
        ensureInsideRoot(file);
        if (!Files.isRegularFile(file)) {
            throw new BusinessException(safeFieldName + "文件不存在，请重新上传或选择");
        }
        if (!isAllowedExtension(file, businessType)) {
            throw new BusinessException(safeFieldName + "文件类型不受支持");
        }

        String normalizedRelativePath = properties.getRootPath()
                .relativize(file.toAbsolutePath().normalize())
                .toString()
                .replace('\\', '/');
        return publicPrefix + "/" + normalizedRelativePath;
    }

    /**
     * 校验后台业务表保存的必填图片引用。
     * 新媒体必须来自对应业务目录；历史 HarmonyOS 资源名继续兼容。
     */
    public String requireImageReference(String value, MediaBusinessType businessType, String fieldName) {
        return validateImageReference(value, businessType, fieldName, true);
    }

    /**
     * 校验后台业务表保存的可选图片引用。
     */
    public String optionalImageReference(String value, MediaBusinessType businessType, String fieldName) {
        return validateImageReference(value, businessType, fieldName, false);
    }

    private String validateImageReference(
            String value,
            MediaBusinessType businessType,
            String fieldName,
            boolean required
    ) {
        String safeFieldName = fieldName == null || fieldName.isBlank() ? "图片" : fieldName.trim();
        String reference = value == null ? "" : value.trim();
        if (reference.isEmpty()) {
            if (required) {
                throw new BusinessException(safeFieldName + "不能为空，请使用媒体选择器选择图片");
            }
            return null;
        }
        if (reference.length() > 500) {
            throw new BusinessException(safeFieldName + "地址长度不能超过 500");
        }
        if (businessType == null || businessType.getMediaFileType() != MediaFileType.IMAGE) {
            throw new IllegalArgumentException("图片引用必须使用 IMAGE 类型业务目录");
        }

        String publicPrefix = properties.getNormalizedPublicPrefix();
        String publicPathPrefix = publicPrefix + "/";
        if (reference.startsWith(publicPathPrefix)) {
            if (reference.indexOf('?') >= 0 || reference.indexOf('#') >= 0 || reference.indexOf('\\') >= 0) {
                throw new BusinessException(safeFieldName + "地址不合法，请重新选择图片");
            }
            String relativePath = reference.substring(publicPathPrefix.length());
            String expectedDirectory = businessType.getRelativeDirectory() + "/";
            if (!relativePath.startsWith(expectedDirectory)) {
                throw new BusinessException(safeFieldName + "必须从对应业务图片目录中选择");
            }
            Path file = properties.getRootPath().resolve(relativePath).normalize();
            ensureInsideRoot(file);
            if (!Files.isRegularFile(file)) {
                throw new BusinessException(safeFieldName + "文件不存在，请重新上传或选择图片");
            }
            if (!isAllowedExtension(file, businessType)) {
                throw new BusinessException(safeFieldName + "不是受支持的图片文件");
            }
            String normalizedRelativePath = properties.getRootPath().relativize(file.toAbsolutePath().normalize())
                    .toString().replace('\\', '/');
            return publicPrefix + "/" + normalizedRelativePath;
        }

        // 兼容数据库中既有的 HarmonyOS 应用内资源名，例如 ZhouJieLunYanchu。
        // 含路径、协议或脚本字符的值不再接受，避免继续新增任意外部地址。
        if (LEGACY_RESOURCE_PATTERN.matcher(reference).matches()
                && !reference.contains("..")
                && !reference.contains(":")
                && !reference.contains("/")) {
            return reference;
        }
        throw new BusinessException(safeFieldName + "必须通过媒体选择器选择，或保留原有应用内资源名");
    }

    public AdminMediaPageVO getMediaList(
            String businessTypeValue,
            String mediaTypeValue,
            String keyword,
            Integer pageNoValue,
            Integer pageSizeValue
    ) {
        MediaBusinessType businessType = MediaBusinessType.from(businessTypeValue);
        validateMediaType(businessType, mediaTypeValue);

        int pageNo = pageNoValue == null || pageNoValue < 1 ? 1 : pageNoValue;
        int pageSize = pageSizeValue == null ? 20 : Math.min(Math.max(pageSizeValue, 1), 100);
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        Path directory = resolveBusinessDirectory(businessType);

        List<AdminMediaItemVO> allItems = new ArrayList<>();
        if (Files.isDirectory(directory)) {
            try (Stream<Path> stream = Files.walk(directory)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> isAllowedExtension(path, businessType))
                        .filter(path -> normalizedKeyword.isEmpty()
                                || path.getFileName().toString().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                        .map(path -> toItem(path, businessType))
                        .sorted(Comparator.comparing(
                                AdminMediaItemVO::getLastModifiedTime,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        ))
                        .forEach(allItems::add);
            } catch (IOException exception) {
                throw new BusinessException(500, "读取媒体目录失败");
            }
        }

        int total = allItems.size();
        int fromIndex = Math.min((pageNo - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);

        AdminMediaPageVO result = new AdminMediaPageVO();
        result.setTotal(total);
        result.setPageNo(pageNo);
        result.setPageSize(pageSize);
        result.setItems(new ArrayList<>(allItems.subList(fromIndex, toIndex)));
        return result;
    }

    public AdminMediaItemVO upload(MultipartFile file, String businessTypeValue) {
        MediaBusinessType businessType = MediaBusinessType.from(businessTypeValue);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }

        String originalName = normalizeOriginalName(file.getOriginalFilename());
        String extension = getExtension(originalName);
        validateExtension(extension, businessType);
        validateSize(file.getSize(), businessType.getMediaFileType());
        validateContent(file, extension, businessType);

        LocalDateTime now = LocalDateTime.now();
        Path targetDirectory = resolveBusinessDirectory(businessType)
                .resolve(now.format(YEAR_FORMAT))
                .resolve(now.format(MONTH_FORMAT))
                .normalize();
        ensureInsideRoot(targetDirectory);

        try {
            Files.createDirectories(targetDirectory);
            String savedName = buildSavedFileName(originalName, extension, now);
            Path target = targetDirectory.resolve(savedName).normalize();
            ensureInsideRoot(target);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return toItem(target, businessType);
        } catch (IOException exception) {
            throw new BusinessException(500, "媒体文件保存失败");
        }
    }

    private void validateMediaType(MediaBusinessType businessType, String mediaTypeValue) {
        if (mediaTypeValue == null || mediaTypeValue.isBlank()) {
            return;
        }
        MediaFileType requested;
        try {
            requested = MediaFileType.valueOf(mediaTypeValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("mediaType 仅支持 IMAGE 或 VIDEO");
        }
        if (requested != businessType.getMediaFileType()) {
            throw new BusinessException("businessType 与 mediaType 不匹配");
        }
    }

    private Path resolveBusinessDirectory(MediaBusinessType businessType) {
        Path directory = properties.getRootPath().resolve(businessType.getRelativeDirectory()).normalize();
        ensureInsideRoot(directory);
        return directory;
    }

    private void ensureInsideRoot(Path path) {
        if (!path.toAbsolutePath().normalize().startsWith(properties.getRootPath())) {
            throw new BusinessException("非法媒体存储路径");
        }
    }

    private String normalizeOriginalName(String originalName) {
        String value = originalName == null ? "" : originalName.trim();
        value = value.replace('\\', '/');
        int slashIndex = value.lastIndexOf('/');
        if (slashIndex >= 0) {
            value = value.substring(slashIndex + 1);
        }
        if (value.isBlank() || value.length() > 255) {
            throw new BusinessException("文件名不合法");
        }
        return value;
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 1 || index == fileName.length() - 1) {
            throw new BusinessException("文件缺少有效扩展名");
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private void validateExtension(String extension, MediaBusinessType businessType) {
        MediaFileType mediaFileType = businessType.getMediaFileType();
        Set<String> allowed = mediaFileType == MediaFileType.IMAGE
                ? (businessType == MediaBusinessType.NOTICE_ICON ? NOTICE_IMAGE_EXTENSIONS : IMAGE_EXTENSIONS)
                : VIDEO_EXTENSIONS;
        if (!allowed.contains(extension)) {
            if (businessType == MediaBusinessType.NOTICE_ICON) {
                throw new BusinessException("观演须知图标仅支持 jpg、jpeg、png、gif、webp、svg");
            }
            throw new BusinessException(mediaFileType == MediaFileType.IMAGE
                    ? "图片仅支持 jpg、jpeg、png、gif、webp"
                    : "视频仅支持 mp4、webm");
        }
    }

    private void validateSize(long size, MediaFileType mediaFileType) {
        long maxSize = mediaFileType == MediaFileType.IMAGE
                ? properties.getImageMaxSize()
                : properties.getVideoMaxSize();
        if (size <= 0) {
            throw new BusinessException("上传文件不能为空");
        }
        if (size > maxSize) {
            long maxMb = Math.max(1, maxSize / 1024L / 1024L);
            throw new BusinessException("文件大小不能超过 " + maxMb + " MB");
        }
    }

    private void validateContent(MultipartFile file, String extension, MediaBusinessType businessType) {
        MediaFileType mediaFileType = businessType.getMediaFileType();
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String normalizedType = contentType.toLowerCase(Locale.ROOT);
            if ("svg".equals(extension) && businessType == MediaBusinessType.NOTICE_ICON) {
                if (!("image/svg+xml".equals(normalizedType) || "text/xml".equals(normalizedType) || "application/xml".equals(normalizedType))) {
                    throw new BusinessException("SVG 文件 MIME 类型不合法");
                }
            } else {
                String expectedPrefix = mediaFileType == MediaFileType.IMAGE ? "image/" : "video/";
                if (!normalizedType.startsWith(expectedPrefix)) {
                    throw new BusinessException("文件 MIME 类型与媒体类型不匹配");
                }
            }
        }

        if ("svg".equals(extension)) {
            validateSvg(file, businessType);
            return;
        }

        byte[] header = new byte[16];
        int length;
        try (InputStream inputStream = file.getInputStream()) {
            length = inputStream.read(header);
        } catch (IOException exception) {
            throw new BusinessException("读取上传文件失败");
        }
        if (length < 4 || !matchesFileHeader(header, length, extension)) {
            throw new BusinessException("文件内容与扩展名不匹配");
        }
    }

    private void validateSvg(MultipartFile file, MediaBusinessType businessType) {
        if (businessType != MediaBusinessType.NOTICE_ICON) {
            throw new BusinessException("SVG 仅允许用于观演须知图标");
        }
        String content;
        try (InputStream inputStream = file.getInputStream()) {
            content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new BusinessException("读取 SVG 文件失败");
        }
        String normalized = content.toLowerCase(Locale.ROOT);
        if (!normalized.contains("<svg") || normalized.contains("<script") || normalized.contains("javascript:")
                || normalized.contains("<foreignobject") || normalized.contains("<!entity")
                || normalized.contains("<iframe") || normalized.contains("<object") || normalized.contains("<embed")
                || normalized.contains("url(") || normalized.matches("(?s).*\\son[a-z]+\\s*=.*")
                || normalized.matches("(?s).*(?:href|xlink:href)\\s*=\\s*[\"']\\s*(?!#)[^\"']+.*")) {
            throw new BusinessException("SVG 包含不允许的脚本或外部资源引用");
        }
    }

    private boolean matchesFileHeader(byte[] header, int length, String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> length >= 3
                    && unsigned(header[0]) == 0xFF
                    && unsigned(header[1]) == 0xD8
                    && unsigned(header[2]) == 0xFF;
            case "png" -> length >= 8
                    && unsigned(header[0]) == 0x89
                    && header[1] == 0x50
                    && header[2] == 0x4E
                    && header[3] == 0x47;
            case "gif" -> length >= 6
                    && new String(header, 0, 6, StandardCharsets.US_ASCII).startsWith("GIF8");
            case "webp" -> length >= 12
                    && "RIFF".equals(new String(header, 0, 4, StandardCharsets.US_ASCII))
                    && "WEBP".equals(new String(header, 8, 4, StandardCharsets.US_ASCII));
            case "mp4" -> length >= 12
                    && "ftyp".equals(new String(header, 4, 4, StandardCharsets.US_ASCII));
            case "webm" -> length >= 4
                    && unsigned(header[0]) == 0x1A
                    && unsigned(header[1]) == 0x45
                    && unsigned(header[2]) == 0xDF
                    && unsigned(header[3]) == 0xA3;
            default -> false;
        };
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }

    private String buildSavedFileName(String originalName, String extension, LocalDateTime now) {
        String baseName = originalName.substring(0, originalName.length() - extension.length() - 1);
        String safeBaseName = baseName
                .replaceAll("[^\\p{L}\\p{N}._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^[_\\.]+|[_\\.]+$", "");
        if (safeBaseName.isBlank()) {
            safeBaseName = "media";
        }
        if (safeBaseName.length() > 60) {
            safeBaseName = safeBaseName.substring(0, 60);
        }
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return now.format(FILE_TIME_FORMAT) + "_" + randomPart + "_" + safeBaseName + "." + extension;
    }

    private boolean isAllowedExtension(Path path, MediaBusinessType businessType) {
        String name = path.getFileName().toString();
        int index = name.lastIndexOf('.');
        if (index < 0 || index == name.length() - 1) {
            return false;
        }
        String extension = name.substring(index + 1).toLowerCase(Locale.ROOT);
        if (businessType.getMediaFileType() == MediaFileType.VIDEO) {
            return VIDEO_EXTENSIONS.contains(extension);
        }
        return (businessType == MediaBusinessType.NOTICE_ICON ? NOTICE_IMAGE_EXTENSIONS : IMAGE_EXTENSIONS).contains(extension);
    }

    private AdminMediaItemVO toItem(Path file, MediaBusinessType businessType) {
        try {
            Path root = properties.getRootPath();
            Path normalizedFile = file.toAbsolutePath().normalize();
            ensureInsideRoot(normalizedFile);
            String relativePath = root.relativize(normalizedFile).toString().replace('\\', '/');
            String fileName = normalizedFile.getFileName().toString();
            FileTime fileTime = Files.getLastModifiedTime(normalizedFile);

            AdminMediaItemVO item = new AdminMediaItemVO();
            item.setBusinessType(businessType.name());
            item.setMediaType(businessType.getMediaFileType().name());
            item.setFileName(fileName);
            item.setOriginalName(extractOriginalName(fileName));
            item.setRelativePath(relativePath);
            item.setUrl(properties.getNormalizedPublicPrefix() + "/" + relativePath);
            item.setMimeType(Files.probeContentType(normalizedFile));
            item.setFileSize(Files.size(normalizedFile));
            item.setLastModifiedTime(LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault()));
            return item;
        } catch (IOException exception) {
            throw new BusinessException(500, "读取媒体文件信息失败");
        }
    }

    private String extractOriginalName(String savedName) {
        return savedName.replaceFirst("^\\d{14}_[0-9a-fA-F]{8}_", "");
    }
}
