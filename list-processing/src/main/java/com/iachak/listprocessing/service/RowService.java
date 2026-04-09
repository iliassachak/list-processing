package com.iachak.listprocessing.service;

import com.iachak.listprocessing.dto.RowDTO;
import com.iachak.listprocessing.dto.WsEvent;
import com.iachak.listprocessing.entity.*;
import com.iachak.listprocessing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RowService {
    private final ListRepository listRepo;
    private final ListRowRepository rowRepo;
    private final RowAssignmentRepository assignRepo;
    private final ColumnPermissionRepository permRepo;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate ws;

    private String topic(UUID listId) {
        return "/topic/list." + listId;
    }

    public RowDTO updateCell(UUID listId, UUID rowId, String col, Object val, User user) {
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r == Role.ADMIN);
        if (!isAdmin) {
            if (!permRepo.canUserEditColumn(listId, user.getId(), col))
                throw new AccessDeniedException("No permission to edit: " + col);
            if (!assignRepo.isRowInUserRange(listId, rowId, user.getId()))
                throw new AccessDeniedException("Row not in your assigned range");
        }
        ListRow row = rowRepo.findById(rowId)
                .orElseThrow(() -> new NoSuchElementException("Row not found"));
        row.getData().put(col, val);
        row.setLastModifiedAt(LocalDateTime.now());
        row.setLastModifiedBy(user);
        rowRepo.save(row);
        ws.convertAndSend(topic(listId),
                WsEvent.cellUpdated(rowId, row.getRowIndex(), col, val, user.getUsername()));
        return RowDTO.from(row);
    }

    public RowDTO addRow(UUID listId, Map<String, Object> data, UUID assignToUserId, User admin) {
        ListEntity list = listRepo.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("List not found"));
        int nextIdx = (rowRepo.findMaxRowIndex(listId)) + 1;

        ListRow row = new ListRow();
        row.setList(list);
        row.setRowIndex(nextIdx);
        row.setData(data != null ? new LinkedHashMap<>(data) : new LinkedHashMap<>());
        row.setLastModifiedAt(LocalDateTime.now());
        row.setLastModifiedBy(admin);
        rowRepo.save(row);
        list.setTotalRows(nextIdx + 1);
        listRepo.save(list);

        if (assignToUserId != null) {
            userRepo.findById(assignToUserId).ifPresent(u -> {
                RowAssignment ra = new RowAssignment();
                ra.setList(list);
                ra.setUser(u);
                ra.setStartRow(nextIdx);
                ra.setEndRow(nextIdx);
                assignRepo.save(ra);
            });
        }
        ws.convertAndSend(topic(listId), WsEvent.rowAdded(row.getId(), nextIdx, row.getData(), admin.getUsername()));
        return RowDTO.from(row);
    }

    public void deleteRow(UUID listId, UUID rowId, User admin) {
        ListRow row = rowRepo.findById(rowId)
                .orElseThrow(() -> new NoSuchElementException("Row not found"));
        int idx = row.getRowIndex();
        rowRepo.delete(row);
        listRepo.findById(listId).ifPresent(l -> {
            l.setTotalRows(Math.max(0, (l.getTotalRows() != null ? l.getTotalRows() : 1) - 1));
            listRepo.save(l);
        });
        ws.convertAndSend(topic(listId), WsEvent.rowDeleted(rowId, idx, admin.getUsername()));
    }
}
