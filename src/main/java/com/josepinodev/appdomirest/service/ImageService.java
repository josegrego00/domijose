package com.josepinodev.appdomirest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${app.image.upload.dir:imagenes/productos}")
    private String uploadDir;

    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                log.warn("Tipo de archivo no permitido: {}", contentType);
                return null;
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                log.warn("El archivo supera el límite de 10MB: {} bytes", file.getSize());
                return null;
            }

            String projectDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(projectDir, "src/main/resources/static", uploadDir);
            Files.createDirectories(uploadPath);

            String extension = getExtension(contentType);
            String fileName = generateUniqueFileName(extension);
            Path filePath = uploadPath.resolve(fileName);

            file.transferTo(filePath.toFile());
            log.info("Imagen guardada correctamente: {}", fileName);

            return "/" + uploadDir + "/" + fileName;
        } catch (IOException e) {
            log.error("Error al guardar imagen: {}", e.getMessage());
            return null;
        }
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private String generateUniqueFileName(String extension) {
        int counter = 1;
        String fileName;

        String projectDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(projectDir, "src/main/resources/static", uploadDir);

        do {
            fileName = "imagen" + counter + extension;
            counter++;
        } while (Files.exists(uploadPath.resolve(fileName)));

        return fileName;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String projectDir = System.getProperty("user.dir");
            Path filePath = Paths.get(projectDir, "src/main/resources/static", uploadDir, fileName);
            if (Files.deleteIfExists(filePath)) {
                log.info("Imagen eliminada correctamente: {}", fileName);
            }
        } catch (IOException e) {
            log.warn("Error al eliminar imagen: {}", e.getMessage());
        }
    }
}