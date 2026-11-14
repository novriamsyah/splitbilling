package com.novri.splitbill.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Detail group Split Bill")
public record GroupResponse(
        @Schema(example = "11111111-1111-1111-1111-111111111111")
        UUID id,

        @Schema(example = "Makan Bersama")
        String name,

        @Schema(example = "IDR")
        String currencyCode,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
