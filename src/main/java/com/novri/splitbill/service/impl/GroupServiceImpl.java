package com.novri.splitbill.service.impl;

import com.novri.splitbill.dto.group.CreateGroupRequest;
import com.novri.splitbill.dto.group.GroupResponse;
import com.novri.splitbill.entity.BillGroup;
import com.novri.splitbill.repository.BillGroupRepository;
import com.novri.splitbill.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final BillGroupRepository groupRepository;

    @Override
    public GroupResponse createGroup(CreateGroupRequest request) {

        String currencyCode = request.currencyCode();

        if (currencyCode == null || currencyCode.isBlank()) {
            currencyCode = "IDR";
        }

        BillGroup group = new BillGroup(
                request.name().trim(),
                currencyCode
        );

        BillGroup savedGroup = groupRepository.save(group);

        return mapToResponse(savedGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getAllGroups() {

        return groupRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroupById(UUID id) {

        BillGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Group tidak ditemukan dengan ID: " + id
                ));

        return mapToResponse(group);
    }

    private GroupResponse mapToResponse(BillGroup group) {

        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getCurrencyCode(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}
