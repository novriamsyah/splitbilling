package com.novri.splitbill.service;

import com.novri.splitbill.dto.settlement.SettlementResponse;

import java.util.UUID;

public interface SettlementService {
    SettlementResponse calculateSettlement(UUID groupId);
}
