package com.example.backend.mapper;

import com.example.backend.domain.entity.Application;
import com.example.backend.dto.ApplicationRequestDto;
import com.example.backend.dto.ApplicationResponseDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationMapperTest {
    @Test
    void testMapRequest() {
        ApplicationRequestDto dto = new ApplicationRequestDto();
        dto.setName("App");
        dto.setOwnerTeam("Team");
        dto.setRepoUrl("http://repo");
        Application app = ApplicationMapper.mapRequest(dto);
        assertNotNull(app);
        assertEquals("App", app.getName());
        assertEquals("Team", app.getOwnerTeam());
        assertEquals("http://repo", app.getRepoUrl());
    }

    @Test
    void testMapResponse() {
        Application app = Application.builder().name("App").ownerTeam("Team").repoUrl("http://repo").build();
        ApplicationResponseDto dto = ApplicationMapper.mapResponse(app);
        assertNotNull(dto);
        assertEquals("App", dto.getName());
        assertEquals("Team", dto.getOwnerTeam());
        assertEquals("http://repo", dto.getRepoUrl());
    }
}
