package com.novri.splitbill.dto.settlement;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementTransferResponse(
        UUID fromParticipantId,
        String fromParticipantName,
        UUID toParticipantId,
        String toParticipantName,
        BigDecimal amount
) {
}
