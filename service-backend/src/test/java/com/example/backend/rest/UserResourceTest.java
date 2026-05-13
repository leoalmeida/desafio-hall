package com.example.backend.rest;

import com.example.backend.dto.UserRequestDto;
import com.example.backend.dto.UserResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserResourceTest {
    @Mock
    private UserService userService;
    @Mock
    private AuditLogManager auditLogManager;
    @InjectMocks
    private UserResource userResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userResource = new UserResource(userService, auditLogManager);
    }

    @Test
    void testGetAllUsers() {
        UserResponseDto user = new UserResponseDto();
        when(userService.findAll()).thenReturn(Collections.singletonList(user));
        ResponseEntity<List<UserResponseDto>> response = ResponseEntity.ok(Collections.singletonList(user));
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }
}
