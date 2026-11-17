package com.novri.splitbill.repository;

import com.novri.splitbill.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
    List<Participant> findAllByGroupIdOrderByCreatedAtAsc(UUID groupId);

    Optional<Participant> findByIdAndGroupId(
            UUID id,
            UUID groupId
    );

    boolean existsByIdAndGroupId(
            UUID id,
            UUID groupId
    );

}
