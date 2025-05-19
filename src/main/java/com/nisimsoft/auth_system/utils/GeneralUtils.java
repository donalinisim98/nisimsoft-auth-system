package com.nisimsoft.auth_system.utils;

public class GeneralUtils {
    public static final String[] EXCLUDED_PATHS = {
            "/api/login",
            "/api/public/**",
            "/api/verify-user"
    };
}
