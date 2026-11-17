package com.novri.splitbill.dto.participant;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Response detail participant")
public record ParticipantResponse(
        @Schema(example = "22222222-2222-2222-2222-222222222221")
        UUID id,

        @Schema(example = "11111111-1111-1111-1111-111111111111")
        UUID groupId,

        @Schema(example = "Novri")
        String name,

        OffsetDateTime createdAt
) {
}
