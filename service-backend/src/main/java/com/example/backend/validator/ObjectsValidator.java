package com.example.backend.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ObjectsValidator<T> {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private final Validator validator = factory.getValidator();

    public T validate(@NonNull final T objectToValidate) {

        Set<ConstraintViolation<T>> violations = validator.validate(objectToValidate);

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            violations.stream()
                    .map((ConstraintViolation<T> v) -> v.getMessage())
                    .forEach(sb::append);
            throw new IllegalArgumentException("Error occurred: " + sb.toString());
        }
        return objectToValidate;
    }
}
