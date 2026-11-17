package com.novri.splitbill.service;

import com.novri.splitbill.dto.participant.CreateParticipantRequest;
import com.novri.splitbill.dto.participant.ParticipantResponse;
import com.novri.splitbill.dto.participant.UpdateParticipantRequest;

import java.util.List;
import java.util.UUID;

public interface ParticipantService {
    ParticipantResponse createParticipant(
            UUID groupId,
            CreateParticipantRequest request
    );

    List<ParticipantResponse> getAllParticipants(UUID groupId);

    ParticipantResponse getParticipantById(
            UUID groupId,
            UUID participantId
    );

    ParticipantResponse updateParticipant(
            UUID groupId,
            UUID participantId,
            UpdateParticipantRequest request
    );

    void deleteParticipant(
            UUID groupId,
            UUID participantId
    );
}
