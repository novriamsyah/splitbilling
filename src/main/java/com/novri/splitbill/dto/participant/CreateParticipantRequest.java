package com.novri.splitbill.dto.participant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Add participant")
public record CreateParticipantRequest(
        @NotBlank(message = "Nama participant tidak boleh kosong")
        @Size(max = 100, message = "Nama participant maksimal 100 karakter")
        @Schema(
                description = "Nama participant",
                example = "Novri",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String name
) {
}
