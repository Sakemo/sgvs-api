package com.flick.business.api.dto.request.production;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(@NotBlank String name) {
}