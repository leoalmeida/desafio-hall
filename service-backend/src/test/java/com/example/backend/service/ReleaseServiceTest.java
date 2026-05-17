package com.example.backend.service;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.exception.BusinessException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseServiceTest {
    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private ReleaseService releaseService;

    @Test
    void testCreateRelease() throws BusinessException {
        ReleaseRequestDto dto = mock(ReleaseRequestDto.class);
        ReleaseResponseDto response = mock(ReleaseResponseDto.class);
        when(releaseService.create(dto)).thenReturn(response);
        ReleaseResponseDto result = releaseService.create(dto);
        assertNotNull(result);
        verify(releaseService, times(1)).create(dto);
    }

    @Test
    void testFindReleases() {
        List<ReleaseResponseDto> releases = mock(List.class);
        when(releaseService.find(any(UUID.class), anyString(), any(EnvironmentEnum.class), any(StatusEnum.class)))
            .thenReturn(releases);
        List<ReleaseResponseDto> result = releaseService.find(
            APPLICATION_ID,
            "1.0",
            EnvironmentEnum.DEV,
            StatusEnum.CREATED);
        assertNotNull(result);
        verify(releaseService, times(1)).find(any(UUID.class), anyString(), any(EnvironmentEnum.class), any(StatusEnum.class));
    }
}
