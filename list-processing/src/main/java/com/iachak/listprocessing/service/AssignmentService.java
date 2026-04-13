package com.iachak.listprocessing.service;

import com.iachak.listprocessing.dto.AssignmentDTO;
import com.iachak.listprocessing.dto.WsGlobalEvent;
import com.iachak.listprocessing.entity.ListEntity;
import com.iachak.listprocessing.entity.RowAssignment;
import com.iachak.listprocessing.entity.User;
import com.iachak.listprocessing.repository.ListRepository;
import com.iachak.listprocessing.repository.RowAssignmentRepository;
import com.iachak.listprocessing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {
    private final RowAssignmentRepository assignRepo;
    private final ListRepository listRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate ws;

    public AssignmentDTO assign(UUID listId, UUID userId, int start, int end) {
        ListEntity list = listRepo.findById(listId).orElseThrow();
        User user = userRepo.findById(userId).orElseThrow();
        RowAssignment a = new RowAssignment();
        a.setList(list);
        a.setUser(user);
        a.setStartRow(start);
        a.setEndRow(end);
        assignRepo.save(a);
        ws.convertAndSend("/topic/global",
                WsGlobalEvent.assignmentChanged(listId.toString(), user.getUsername()));
        return AssignmentDTO.from(a);
    }

    public void delete(UUID id,UUID listId, String deletedBy){
        assignRepo.deleteById(id);
        ws.convertAndSend("/topic/global",
                WsGlobalEvent.assignmentChanged(listId.toString(), deletedBy));
    }
}
