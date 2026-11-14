package com.novri.splitbill.controller;

import com.novri.splitbill.dto.group.CreateGroupRequest;
import com.novri.splitbill.dto.group.GroupResponse;
import com.novri.splitbill.service.GroupService;
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
@RequestMapping("/api/groups")
@Tag(
        name = "Group",
        description = "API group Split Bill"
)
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    @Operation(
            summary = "Create Group",
            description = "Membuat grup Split Bill baru"
    )
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request
    ) {

        GroupResponse response = groupService.createGroup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "Get All Groups",
            description = "Mengambil seluruh grup Split Bill"
    )
    public ResponseEntity<List<GroupResponse>> getAllGroups() {

        List<GroupResponse> response = groupService.getAllGroups();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Group By ID",
            description = "Mengambil detail grup berdasarkan UUID"
    )
    public ResponseEntity<GroupResponse> getGroupById(
            @PathVariable UUID id
    ) {

        GroupResponse response = groupService.getGroupById(id);

        return ResponseEntity.ok(response);
    }
}
