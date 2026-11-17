package com.novri.splitbill.controller;

import com.novri.splitbill.dto.settlement.SettlementResponse;
import com.novri.splitbill.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/settlement")
@RequiredArgsConstructor
@Tag(
        name = "Settlement Management",
        description = "API untuk menghitung dan melihat settlement group"
)
public class SettlementController {
    private final SettlementService settlementService;

    @GetMapping
    @Operation(
            summary = "Calculate group settlement",
            description = "Menghitung saldo participant dan instruksi transfer"
    )
    public ResponseEntity<SettlementResponse> calculateSettlement(
            @PathVariable UUID groupId
    ) {

        SettlementResponse response =
                settlementService.calculateSettlement(groupId);

        return ResponseEntity.ok(response);
    }
}
