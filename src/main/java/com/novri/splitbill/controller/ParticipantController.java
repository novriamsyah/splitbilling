package com.novri.splitbill.controller;

import com.novri.splitbill.dto.participant.CreateParticipantRequest;
import com.novri.splitbill.dto.participant.ParticipantResponse;
import com.novri.splitbill.dto.participant.UpdateParticipantRequest;
import com.novri.splitbill.service.ParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/participants")
@RequiredArgsConstructor
@Tag(
        name = "Participant Management",
        description = "Mengelola anggota grup Split Bill"
)
public class ParticipantController {
    private final ParticipantService participantService;

    @PostMapping
    @Operation(
            summary = "Create Participant",
            description = "Add participant to grup"
    )
    public ResponseEntity<ParticipantResponse> createParticipant(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateParticipantRequest request
    ) {

        ParticipantResponse response =
                participantService.createParticipant(groupId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "Get All Participants",
            description = "Mengambil seluruh participant dalam grup"
    )
    public ResponseEntity<List<ParticipantResponse>> getAllParticipants(
            @PathVariable UUID groupId
    ) {

        List<ParticipantResponse> response =
                participantService.getAllParticipants(groupId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{participantId}")
    @Operation(
            summary = "Get Participant By ID",
            description = "Mengambil detail participant berdasarkan UUID"
    )
    public ResponseEntity<ParticipantResponse> getParticipantById(
            @PathVariable UUID groupId,
            @PathVariable UUID participantId
    ) {

        ParticipantResponse response =
                participantService.getParticipantById(
                        groupId,
                        participantId
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{participantId}")
    @Operation(
            summary = "Update Participant",
            description = "Mengubah nama participant"
    )
    public ResponseEntity<ParticipantResponse> updateParticipant(
            @PathVariable UUID groupId,
            @PathVariable UUID participantId,
            @Valid @RequestBody UpdateParticipantRequest request
    ) {

        ParticipantResponse response =
                participantService.updateParticipant(
                        groupId,
                        participantId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{participantId}")
    @Operation(
            summary = "Delete Participant",
            description = "Menghapus participant jika belum digunakan dalam transaksi"
    )
    public ResponseEntity<Void> deleteParticipant(
            @PathVariable UUID groupId,
            @PathVariable UUID participantId
    ) {

        participantService.deleteParticipant(groupId, participantId);

        return ResponseEntity.noContent().build();
    }
}
