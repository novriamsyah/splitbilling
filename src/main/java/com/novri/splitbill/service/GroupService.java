package com.novri.splitbill.service;

import com.novri.splitbill.dto.group.CreateGroupRequest;
import com.novri.splitbill.dto.group.GroupResponse;

import java.util.List;
import java.util.UUID;

public interface GroupService {

    GroupResponse createGroup(CreateGroupRequest request);

    List<GroupResponse> getAllGroups();

    GroupResponse getGroupById(UUID id);
}
