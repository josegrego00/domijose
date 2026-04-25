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

    @Value("${app.upload.url:images/productos/}")
    private String uploadUrl;

    @Value("${app.upload.path.qr:src/main/resources/static/images/qr/}")
    private String qrPath;

    @Value("${app.upload.url.qr:images/qr/}")
    private String qrUrl;

    @PostConstruct
    public void init() {
        try {
            Path productPath = Paths.get(uploadPath);
            if (!Files.exists(productPath)) {
                Files.createDirectories(productPath);
                log.info("Directorio de uploads creado: {}", uploadPath);
            }
            Path qrDirPath = Paths.get(qrPath);
            if (!Files.exists(qrDirPath)) {
                Files.createDirectories(qrDirPath);
                log.info("Directorio de QR creado: {}", qrPath);
            }
        } catch (IOException e) {
            log.error("No se pudo crear el directorio de uploads", e);
        }
    }

    public String saveImage(MultipartFile file) {
        return saveImage(file, false);
    }

    public String saveImage(MultipartFile file, boolean isQr) {
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
            String targetPath = isQr ? qrPath : uploadPath;
            String targetUrl = isQr ? qrUrl : uploadUrl;
            Path filePath = Paths.get(targetPath).resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            log.info("Imagen guardada en {}: {}", isQr ? "QR" : "productos", filename);
            String url = targetUrl + filename;
            return url.startsWith("/") ? url.substring(1) : url;
        } catch (IOException e) {
            log.error("Error al guardar la imagen", e);
            throw new RuntimeException("Error al guardar la imagen");
        }
    }
}