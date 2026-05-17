package com.example.backend.rest;

import com.example.backend.service.ReleaseService;
import com.example.backend.service.ReleaseAuditService;
import com.example.backend.service.ReleasePromotionCoordinator;
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
    private ReleasePromotionCoordinator releasePromotionCoordinator;
    @Mock
    private ReleaseAuditService releaseAuditService;
    @InjectMocks
    private ReleaseResource releaseResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        releaseResource = new ReleaseResource(releaseService, releaseAuditService, releasePromotionCoordinator);
    }

    @Test
    void testGetAllReleases() {
        assertNotNull(releaseResource);
    }
}
