package com.novri.splitbill.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Create grup Split Bill")
public record CreateGroupRequest (
        @NotBlank(message = "Group name tidak boleh kosong")
        @Size(max = 150, message = "Group name maksimal 150 karakter")
        @Schema(
                description = "Nama grup",
                example = "Makan Bersama",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String name,

        @Pattern(
                regexp = "^[A-Z]{3}$",
                message = "Currency code harus terdiri dari 3 huruf kapital"
        )
        @Schema(
                description = "Kode mata uang ISO 4217",
                example = "IDR",
                defaultValue = "IDR"
        )
        String currencyCode
) {
}
