package com.domicilio.domijose.services;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    @Value("${app.upload.path:src/main/resources/static/images/productos/}")
    private String uploadPath;

    @Value("${app.upload.url:/images/productos/}")
    private String uploadUrl;

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(uploadPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Directorio de uploads creado: {}", uploadPath);
            }
        } catch (IOException e) {
            log.error("No se pudo crear el directorio de uploads", e);
        }
    }

    public String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadPath).resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            log.info("Imagen guardada: {}", filename);
            return uploadUrl + filename;
        } catch (IOException e) {
            log.error("Error al guardar la imagen", e);
            throw new RuntimeException("Error al guardar la imagen");
        }
    }
}