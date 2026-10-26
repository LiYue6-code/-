package com.employment.service;

import com.employment.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 文件存储服务：统一处理证明材料的保存与读取。
 *
 * <p>安全要点：
 * <ul>
 *     <li>保存时用 UUID 重命名，不使用用户上传的原始文件名做路径，避免路径穿越与重名覆盖</li>
 *     <li>限制扩展名白名单与文件大小</li>
 *     <li>读取时校验文件名格式，并确认解析后的路径仍在上传目录内</li>
 * </ul>
 */
@Service
public class FileStorageService {

    /** 允许的证明材料格式 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx");

    /** 单个文件大小上限：5MB */
    private static final long MAX_SIZE = 5L * 1024 * 1024;

    private final Path baseDir;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * 保存上传文件。
     *
     * @return 存储文件名（UUID.扩展名）；文件为空时返回 null
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String extension = extensionOf(original);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BizException("证明材料仅支持 PDF、JPG、PNG、DOC、DOCX 格式");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("证明材料不能超过 5MB");
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        try {
            Files.createDirectories(baseDir);
            file.transferTo(baseDir.resolve(storedName));
        } catch (IOException e) {
            throw new BizException("证明材料保存失败，请稍后重试");
        }
        return storedName;
    }

    /** 取用户上传时的原始文件名（用于下载时显示）。 */
    public static String originalName(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return Paths.get(file.getOriginalFilename() == null ? "证明材料" : file.getOriginalFilename())
                .getFileName().toString();
    }

    /** 解析存储文件路径，并做安全校验。 */
    public Path resolve(String storedName) {
        if (storedName == null || !storedName.matches("[A-Za-z0-9._-]+")) {
            throw new BizException("证明材料不存在");
        }
        Path path = baseDir.resolve(storedName).normalize();
        if (!path.startsWith(baseDir) || !Files.isRegularFile(path)) {
            throw new BizException("证明材料不存在或已被删除");
        }
        return path;
    }

    private static String extensionOf(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
