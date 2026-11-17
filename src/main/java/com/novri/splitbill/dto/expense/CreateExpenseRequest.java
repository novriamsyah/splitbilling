package com.novri.splitbill.dto.expense;

import com.novri.splitbill.enums.SplitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "create expense")
public record CreateExpenseRequest (
        @NotNull(message = "Payer ID wajib diisi")
        @Schema(
                description = "UUID participant yang membayar",
                example = "22222222-2222-2222-2222-222222222221"
        )
        UUID payerId,

        @NotBlank(message = "Deskripsi pengeluaran tidak boleh kosong")
        @Size(max = 255, message = "Deskripsi maksimal 255 karakter")
        @Schema(example = "Makan siang bersama")
        String description,

        @NotNull(message = "Amount wajib diisi")
        @DecimalMin(value = "0.01", message = "Amount harus lebih besar dari 0")
        @Digits(
                integer = 17,
                fraction = 2,
                message = "Amount maksimal 17 digit sebelum koma dan 2 digit desimal"
        )
        @Schema(
                description = "Nominal pengeluaran",
                example = "150000.00"
        )
        BigDecimal amount,

        @NotNull(message = "Split type wajib diisi")
        @Schema(example = "EQUAL")
        SplitType splitType,

        @NotNull(message = "Tanggal pengeluaran wajib diisi")
        @Schema(example = "2026-11-14")
        OffsetDateTime expenseDate
) {
}
