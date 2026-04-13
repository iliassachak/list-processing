package com.iachak.listprocessing.service;

import com.iachak.listprocessing.dto.AssignmentDTO;
import com.iachak.listprocessing.dto.ListDTO;
import com.iachak.listprocessing.dto.RowDTO;
import com.iachak.listprocessing.dto.WsGlobalEvent;
import com.iachak.listprocessing.entity.*;
import com.iachak.listprocessing.repository.ColumnPermissionRepository;
import com.iachak.listprocessing.repository.ListRepository;
import com.iachak.listprocessing.repository.ListRowRepository;
import com.iachak.listprocessing.repository.RowAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListService {
    private final ListRepository listRepo;
    private final ListRowRepository rowRepo;
    private final RowAssignmentRepository assignRepo;
    private final ColumnPermissionRepository permRepo;
    private final SimpMessagingTemplate ws;

    public List<ListDTO> getAll(User user) {
        if (user.getRoles().contains(Role.ADMIN)){
            return listRepo.findAll().stream().map(ListDTO::from).toList();
        } else {
            return assignRepo.findListsByUser(user).stream()
                    .map(ListDTO::from)
                    .toList();
        }
    }

    public ListEntity getEntity(UUID id) {
        return listRepo.findById(id).orElseThrow(() -> new NoSuchElementException("List not found"));
    }

    public ListDTO getById(UUID id) {
        return ListDTO.from(getEntity(id));
    }

    @Transactional
    public void deleteList(UUID listId, String deletedBy) {
        ListEntity list = getEntity(listId);
        permRepo.deleteByListId(listId);
        assignRepo.deleteByListId(listId);
        listRepo.delete(list);
        ws.convertAndSend("/topic/global",
                WsGlobalEvent.listDeleted(listId.toString(), deletedBy));
    }

    public List<RowDTO> getAllRows(UUID listId) {
        return rowRepo.findByListIdOrderByRowIndex(listId).stream().map(RowDTO::from).toList();
    }

    public List<RowDTO> getUserRows(UUID listId, UUID userId) {
        List<RowAssignment> assignments = assignRepo.findByListIdAndUserId(listId, userId);
        List<ListRow> rows = new ArrayList<>();
        for (RowAssignment a : assignments)
            rows.addAll(rowRepo.findInRange(listId, a.getStartRow(), a.getEndRow()));
        rows.sort(Comparator.comparing(ListRow::getRowIndex));
        return rows.stream().map(RowDTO::from).toList();
    }

    public List<String> getEditableColumns(UUID listId, UUID userId) {
        return permRepo.findByListIdAndUserId(listId, userId).stream()
                .filter(ColumnPermission::isCanEdit).map(ColumnPermission::getColumnName).toList();
    }

    public List<AssignmentDTO> getAssignments(UUID listId) {
        return assignRepo.findByListId(listId).stream().map(AssignmentDTO::from).toList();
    }

    public Map<String, Map<String, Boolean>> getPermMatrix(UUID listId) {
        Map<String, Map<String, Boolean>> m = new LinkedHashMap<>();
        for (ColumnPermission p : permRepo.findByListId(listId)) {
            String uid = p.getUser().getId().toString();
            m.computeIfAbsent(uid, k -> new LinkedHashMap<>()).put(p.getColumnName(), p.isCanEdit());
        }
        return m;
    }
}
