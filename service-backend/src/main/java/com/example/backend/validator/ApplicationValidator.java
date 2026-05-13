package com.example.backend.validator;

import com.example.backend.dto.ApplicationRequestDto;

public class ApplicationValidator extends ObjectsValidator<ApplicationRequestDto> {

    public static final int NOME_MIN_LENGTH = 3;
    public static final int NOME_MAX_LENGTH = 255;
    public static final int OWNERTEAM_MIN_LENGTH = 3;
    public static final int OWNERTEAM_MAX_LENGTH = 255;    
    public static final int REPOURL_MIN_LENGTH = 3;
    public static final int REPOURL_MAX_LENGTH = 255;
}
