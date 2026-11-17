package com.novri.splitbill.dto.settlement;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SettlementResponse(
        UUID groupId,
        String groupName,
        BigDecimal totalExpense,
        BigDecimal totalPaid,
        BigDecimal totalAllocated,
        List<ParticipantBalanceResponse> balances,
        List<SettlementTransferResponse> transfers
) {
}
