package com.novri.splitbill.dto.expense;

import com.novri.splitbill.enums.SplitType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Response detail expense")
public record ExpenseResponse(
        UUID id,

        UUID groupId,

        UUID payerId,

        String description,

        @Schema(example = "150000.00")
        BigDecimal amount,

        SplitType splitType,

        OffsetDateTime  expenseDate,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}
