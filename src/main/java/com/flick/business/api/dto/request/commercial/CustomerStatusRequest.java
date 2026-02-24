package com.flick.business.api.dto.request.commercial;

import jakarta.validation.constraints.NotNull;

public record CustomerStatusRequest(
        @NotNull(message = "active is required") Boolean active) {
}
