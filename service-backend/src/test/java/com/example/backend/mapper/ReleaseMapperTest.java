package com.example.backend.mapper;

import com.example.backend.domain.entity.Release;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReleaseMapperTest {
    @Test
    void testMapRequest() {
        ReleaseRequestDto dto = new ReleaseRequestDto();
        // set fields as needed
        Release release = ReleaseMapper.mapRequest(dto);
        assertNotNull(release);
    }

    @Test
    void testMapResponse() {
        Release release = Release.builder().build();
        ReleaseResponseDto dto = ReleaseMapper.mapResponse(release);
        assertNotNull(dto);
    }
}
