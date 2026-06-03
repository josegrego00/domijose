package com.josepinodev.appdomirest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    private final ImageService imageService = new ImageService();

    @Test
    void saveImage_WithNullFile_ReturnsNull() {
        assertNull(imageService.saveImage(null));
    }

    @Test
    void saveImage_WithEmptyFile_ReturnsNull() {
        MockMultipartFile file = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);
        assertNull(imageService.saveImage(file));
    }

    @Test
    void saveImage_WithInvalidContentType_ReturnsNull() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        assertNull(imageService.saveImage(file));
    }

    @Test
    void deleteImage_WithNullUrl_DoesNothing() {
        assertDoesNotThrow(() -> imageService.deleteImage(null));
    }

    @Test
    void deleteImage_WithEmptyUrl_DoesNothing() {
        assertDoesNotThrow(() -> imageService.deleteImage(""));
    }
}
