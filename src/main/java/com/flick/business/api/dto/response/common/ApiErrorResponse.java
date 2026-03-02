package com.flick.business.api.dto.response.common;

import java.util.Map;

public record ApiErrorResponse(
        int status,
        String code,
        String message,
        Map<String, String> errors) {
}
