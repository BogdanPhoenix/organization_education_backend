package com.bachelor.thesis.organization_education.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.bachelor.thesis.organization_education.enums.PatternTemplate;
import com.bachelor.thesis.organization_education.annotations.ValidNameEntity;

public class NameEntityValidator implements ConstraintValidator<ValidNameEntity, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.toUpperCase()
                .matches(PatternTemplate.STRICT_SET_ALLOWED_CHARS.getValue());
    }
}
