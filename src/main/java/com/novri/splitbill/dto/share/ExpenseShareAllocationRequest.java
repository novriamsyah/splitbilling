package com.novri.splitbill.dto.share;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Alokasi pembagian untuk satu participant")
public record ExpenseShareAllocationRequest(
        @NotNull(message = "Participant ID wajib diisi")
        @Schema(
                example = "22222222-2222-2222-2222-222222222221"
        )
        UUID participantId,

        @DecimalMin(value = "0.00", message = "Share amount tidak boleh negatif")
        @Digits(
                integer = 17,
                fraction = 2,
                message = "Share amount maksimal 2 angka desimal"
        )
        @Schema(
                description = "Nominal bagian untuk metode EXACT",
                example = "50000.00"
        )
        BigDecimal shareAmount,

        @DecimalMin(value = "0.0000", message = "Persentase tidak boleh negatif")
        @DecimalMax(value = "100.0000", message = "Persentase maksimal 100")
        @Digits(
                integer = 3,
                fraction = 4,
                message = "Persentase maksimal 4 angka desimal"
        )
        @Schema(
                description = "Persentase bagian untuk metode PERCENTAGE",
                example = "33.3333"
        )
        BigDecimal sharePercentage

) {
}
