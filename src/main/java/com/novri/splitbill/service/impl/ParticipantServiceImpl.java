package com.novri.splitbill.service.impl;

import com.novri.splitbill.dto.participant.CreateParticipantRequest;
import com.novri.splitbill.dto.participant.ParticipantResponse;
import com.novri.splitbill.dto.participant.UpdateParticipantRequest;
import com.novri.splitbill.entity.Participant;
import com.novri.splitbill.repository.BillGroupRepository;
import com.novri.splitbill.repository.ParticipantRepository;
import com.novri.splitbill.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantServiceImpl implements ParticipantService {
    private final ParticipantRepository participantRepository;
    private final BillGroupRepository groupRepository;

    @Override
    public ParticipantResponse createParticipant(
            UUID groupId,
            CreateParticipantRequest request
    ) {

        validateGroupExists(groupId);

        Participant participant = new Participant(
                groupId,
                request.name().trim()
        );

        Participant savedParticipant =
                participantRepository.save(participant);

        return mapToResponse(savedParticipant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantResponse> getAllParticipants(UUID groupId) {

        validateGroupExists(groupId);

        return participantRepository
                .findAllByGroupIdOrderByCreatedAtAsc(groupId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipantResponse getParticipantById(
            UUID groupId,
            UUID participantId
    ) {

        Participant participant = findParticipant(groupId, participantId);

        return mapToResponse(participant);
    }

    @Override
    public ParticipantResponse updateParticipant(
            UUID groupId,
            UUID participantId,
            UpdateParticipantRequest request
    ) {

        Participant participant = findParticipant(groupId, participantId);

        participant.updateDisplayName(request.name().trim());

        Participant updatedParticipant =
                participantRepository.save(participant);

        return mapToResponse(updatedParticipant);
    }

    @Override
    public void deleteParticipant(
            UUID groupId,
            UUID participantId
    ) {

        Participant participant = findParticipant(groupId, participantId);

        try {
            participantRepository.delete(participant);
            participantRepository.flush();

        } catch (DataIntegrityViolationException exception) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Participant tidak dapat dihapus karena sudah digunakan dalam transaksi expense"
            );
        }
    }

    private void validateGroupExists(UUID groupId) {

        if (!groupRepository.existsById(groupId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Group tidak ditemukan dengan ID: " + groupId
            );
        }
    }

    private Participant findParticipant(
            UUID groupId,
            UUID participantId
    ) {

        return participantRepository
                .findByIdAndGroupId(participantId, groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Participant tidak ditemukan dalam group tersebut"
                ));
    }

    private ParticipantResponse mapToResponse(Participant participant) {

        return new ParticipantResponse(
                participant.getId(),
                participant.getGroupId(),
                participant.getName(),
                participant.getCreatedAt()
        );
    }
}
