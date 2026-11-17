package com.novri.splitbill.dto.share;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Response hasil pembagian expense")
public record ExpenseShareResponse(
        UUID id,

        UUID groupId,

        UUID expenseId,

        UUID participantId,

        @Schema(example = "33333.34")
        BigDecimal shareAmount,

        @Schema(example = "33.3333")
        BigDecimal sharePercentage,

        OffsetDateTime createdAt
) {
}
