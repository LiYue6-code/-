package com.employment.service;

import com.employment.common.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 证明材料存储单元测试：格式白名单、大小限制、路径穿越防护。
 */
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService service() {
        return new FileStorageService(tempDir.toString());
    }

    @Test
    @DisplayName("合法 PDF 会以 UUID 重命名保存")
    void storePdf() throws Exception {
        MockMultipartFile file = new MockMultipartFile("proofFile", "录用函.pdf",
                "application/pdf", "%PDF-1.4".getBytes());

        String stored = service().store(file);

        assertNotNull(stored);
        assertTrue(stored.matches("[a-f0-9]{32}\\.pdf"), stored);
        assertTrue(Files.isRegularFile(tempDir.resolve(stored)));
    }

    @Test
    @DisplayName("空文件返回 null，不产生文件")
    void storeEmpty() {
        MockMultipartFile file = new MockMultipartFile("proofFile", "empty.pdf",
                "application/pdf", new byte[0]);
        assertNull(service().store(file));
    }

    @Test
    @DisplayName("不允许的扩展名被拒绝")
    void rejectBadExtension() {
        MockMultipartFile file = new MockMultipartFile("proofFile", "evil.exe",
                "application/octet-stream", "MZ".getBytes());
        assertThrows(BizException.class, () -> service().store(file));
    }

    @Test
    @DisplayName("超过 5MB 的文件被拒绝")
    void rejectOversize() {
        byte[] big = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("proofFile", "big.pdf",
                "application/pdf", big);
        assertThrows(BizException.class, () -> service().store(file));
    }

    @Test
    @DisplayName("原始文件名只保留文件名本身，去掉路径")
    void originalNameStripsPath() {
        MockMultipartFile file = new MockMultipartFile("proofFile",
                "C:\\Users\\x\\三方协议.pdf", "application/pdf", new byte[]{1});
        assertEquals("三方协议.pdf", FileStorageService.originalName(file));
    }

    @Test
    @DisplayName("读取时拒绝路径穿越与非白名单字符")
    void resolveRejectsTraversal() {
        FileStorageService service = service();
        assertThrows(BizException.class, () -> service.resolve("../application.yml"));
        assertThrows(BizException.class, () -> service.resolve("..\\application.yml"));
    }

    @Test
    @DisplayName("读取不存在的文件抛出业务异常")
    void resolveMissingFile() {
        assertThrows(BizException.class, () -> service().resolve("not-exists.pdf"));
    }

    @Test
    @DisplayName("读取已保存的文件返回真实路径")
    void resolveExisting() throws Exception {
        Path file = tempDir.resolve("abc123.pdf");
        Files.writeString(file, "data");
        assertEquals(file, service().resolve("abc123.pdf"));
    }
}
