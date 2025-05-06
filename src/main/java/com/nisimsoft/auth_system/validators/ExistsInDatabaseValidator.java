package com.nisimsoft.auth_system.validators;

import com.nisimsoft.auth_system.annotations.ExistsInDatabase;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExistsInDatabaseValidator implements ConstraintValidator<ExistsInDatabase, Object> {

    private final JdbcTemplate jdbc;

    private String table;
    private String column;
    private String entityName;

    public ExistsInDatabaseValidator(@Qualifier("defaultDataSource") DataSource defaultDataSource) {
        this.jdbc = new JdbcTemplate(defaultDataSource);
    }

    @Override
    public void initialize(ExistsInDatabase constraintAnnotation) {
        this.table = constraintAnnotation.table();
        this.column = constraintAnnotation.column();
        this.entityName = constraintAnnotation.entityName();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        try {
            Long count = jdbc.queryForObject(
                    String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", table, column),
                    Long.class,
                    value);

            if (count == null || count == 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format("El/la %s con valor '%s' no existe en la base de datos", entityName, value))
                        .addConstraintViolation();
                return false;
            }

            return true;
        } catch (Exception e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Error al validar %s: %s", entityName, e.getMessage())).addConstraintViolation();
            return false;
        }
    }
}