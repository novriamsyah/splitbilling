package com.novri.splitbill.dto.settlement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SettlementResponse(
        UUID groupId,
        String groupName,
        BigDecimal totalExpense,
        BigDecimal totalPaid,
        BigDecimal totalAllocated,
        @JsonProperty("service_charge_pct")
        int serviceChargePct,
        @JsonProperty("service_charge_amount")
        BigDecimal serviceChargeAmount,
        List<ParticipantBalanceResponse> balances,
        List<SettlementTransferResponse> transfers
) {
}
