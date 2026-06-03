package com.josepinodev.appdomirest.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    private final AuditService auditService = new AuditService();

    @Test
    void log_WithValidData_DoesNotThrow() {
        assertDoesNotThrow(() -> auditService.log("TEST_ACTION", "Test detail message"));
    }

    @Test
    void log_WithEmptyValues_DoesNotThrow() {
        assertDoesNotThrow(() -> auditService.log("", ""));
    }

    @Test
    void log_WithNullValues_DoesNotThrow() {
        assertDoesNotThrow(() -> auditService.log(null, null));
    }
}
