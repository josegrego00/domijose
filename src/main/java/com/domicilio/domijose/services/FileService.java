package com.domicilio.domijose.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private final Path uploadPath = Paths.get("src/main/resources/static/images/productos");

    public FileService() {
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
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
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            log.info("Imagen guardada: {}", filename);
            return "/images/productos/" + filename;
        } catch (IOException e) {
            log.error("Error al guardar la imagen", e);
            throw new RuntimeException("Error al guardar la imagen");
        }
    }
}