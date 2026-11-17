package com.novri.splitbill.dto.share;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request untuk menghitung ulang pembagian expense")
public record ReplaceExpenseSharesRequest(
        @NotNull(message = "Allocations wajib diisi")
        @NotEmpty(message = "Minimal harus ada satu participant")
        @Valid
        @Schema(description = "Daftar participant yang menerima pembagian")
        List<ExpenseShareAllocationRequest> allocations
) {
}
