package com.iachak.listprocessing.controller;

import com.iachak.listprocessing.dto.AssignRowsRequest;
import com.iachak.listprocessing.dto.AssignmentDTO;
import com.iachak.listprocessing.dto.ColumnPermRequest;
import com.iachak.listprocessing.dto.UserDTO;
import com.iachak.listprocessing.entity.User;
import com.iachak.listprocessing.repository.UserRepository;
import com.iachak.listprocessing.security.AppUserDetails;
import com.iachak.listprocessing.service.AssignmentService;
import com.iachak.listprocessing.service.ListService;
import com.iachak.listprocessing.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepo;
    private final ListService listService;
    private final AssignmentService assignService;
    private final PermissionService permService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> users(){
        return ResponseEntity.ok(userRepo.findAll().stream().map(UserDTO::from).toList());
    }

    @GetMapping("/lists/{listId}/assignments")
    public ResponseEntity<List<AssignmentDTO>> assignments(@PathVariable UUID listId){
        return ResponseEntity.ok(listService.getAssignments(listId));
    }

    @PostMapping("/lists/{listId}/assignments")
    public ResponseEntity<AssignmentDTO> assign(@PathVariable UUID listId,
                                                @RequestBody AssignRowsRequest req){
        return ResponseEntity.ok(assignService.assign(listId,req.userId(),req.startRow(),req.endRow()));
    }

    @DeleteMapping("/lists/{listId}/assignments/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable UUID listId,@PathVariable UUID id, @AuthenticationPrincipal UserDetails ud){
        User user = ((AppUserDetails) ud).getUser();
        assignService.delete(id,listId, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/lists/{listId}/permissions")
    public ResponseEntity<Map<String, Map<String,Boolean>>> permissions(@PathVariable UUID listId){
        return ResponseEntity.ok(listService.getPermMatrix(listId));
    }

    @PostMapping("/lists/{listId}/permissions/{col}")
    public ResponseEntity<Void> setPermission(@PathVariable UUID listId,
                                              @PathVariable String col,
                                              @RequestBody ColumnPermRequest req){
        permService.set(listId,col,req.userId(),req.canEdit(),req.canView());
        return ResponseEntity.ok().build();
    }
}
