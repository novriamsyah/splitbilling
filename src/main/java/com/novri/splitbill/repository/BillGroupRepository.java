package com.novri.splitbill.repository;

import com.novri.splitbill.entity.BillGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BillGroupRepository extends JpaRepository<BillGroup, UUID> {
    List<BillGroup> findAllByOrderByCreatedAtDesc();
}
