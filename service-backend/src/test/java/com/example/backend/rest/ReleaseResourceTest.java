package com.example.backend.rest;

import com.example.backend.service.ReleaseService;
import com.example.backend.security.AuditLogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;

class ReleaseResourceTest {
    @Mock
    private ReleaseService releaseService;
    @Mock
    private AuditLogManager auditLogManager;
    @InjectMocks
    private ReleaseResource releaseResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        releaseResource = new ReleaseResource(releaseService, auditLogManager);
    }

    @Test
    void testGetAllReleases() {
        assertNotNull(releaseResource);
    }
}
