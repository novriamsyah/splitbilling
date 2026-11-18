package com.novri.splitbill.dto.expense;

import com.novri.splitbill.enums.SplitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Update expense")
public record UpdateExpenseRequest(
        @NotNull(message = "Payer ID wajib diisi")
        UUID payerId,

        @NotBlank(message = "Deskripsi pengeluaran tidak boleh kosong")
        @Size(max = 255, message = "Deskripsi maksimal 255 karakter")
        String description,

        @NotNull(message = "Amount wajib diisi")
        @DecimalMin(value = "0.01", message = "Amount harus lebih besar dari 0")
        @Digits(
                integer = 17,
                fraction = 2,
                message = "Amount maksimal 17 digit sebelum koma dan 2 digit desimal"
        )
        BigDecimal amount,

        @NotNull(message = "Split type wajib diisi")
        SplitType splitType,

        @NotNull(message = "Tanggal pengeluaran wajib diisi")
        LocalDate expenseDate

) {
}
