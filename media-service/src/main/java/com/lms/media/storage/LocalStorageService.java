package com.lms.media.storage;

import com.lms.media.config.MediaStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalStorageService {

    private final MediaStorageProperties properties;

    public Path rootPath() {
        Path root = Path.of(properties.getRoot()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create storage root", e);
        }
        return root;
    }

    public StoredFile store(MultipartFile file, String folder) throws IOException {
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = extension(original);
        String key = folder + "/" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path target = rootPath().resolve(key).normalize();
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return new StoredFile(key, target, original);
    }

    public Path resolve(String storageKey) {
        Path path = rootPath().resolve(storageKey).normalize();
        if (!path.startsWith(rootPath())) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return path;
    }

    public void delete(String storageKey) throws IOException {
        Path path = resolve(storageKey);
        Files.deleteIfExists(path);
    }

    public String publicUrl(Long fileId) {
        return properties.getBaseUrl() + "/" + fileId + "/download";
    }

    private String extension(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx + 1).toLowerCase() : "";
    }

    public record StoredFile(String storageKey, Path absolutePath, String originalName) {
    }
}
