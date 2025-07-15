package com.flick.business.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(@NotBlank String name) {
}