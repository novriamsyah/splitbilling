package com.novri.splitbill.dto.participant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Update participant")
public record UpdateParticipantRequest(
        @NotBlank(message = "Nama participant tidak boleh kosong")
        @Size(max = 100, message = "Nama participant maksimal 100 karakter")
        @Schema(
                description = "Nama baru participant",
                example = "Novri Amsyah",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String name
) {
}
