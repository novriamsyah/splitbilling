package com.novri.splitbill.dto.settlement;

import java.math.BigDecimal;
import java.util.UUID;

public record ParticipantBalanceResponse(
        UUID participantId,
        String participantName,
        BigDecimal totalPaid,
        BigDecimal totalShare,
        BigDecimal netBalance,
        String status
) {
}
