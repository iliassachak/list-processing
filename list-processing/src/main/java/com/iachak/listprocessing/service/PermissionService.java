package com.iachak.listprocessing.service;

import com.iachak.listprocessing.entity.ColumnPermission;
import com.iachak.listprocessing.entity.ListEntity;
import com.iachak.listprocessing.entity.User;
import com.iachak.listprocessing.repository.ColumnPermissionRepository;
import com.iachak.listprocessing.repository.ListRepository;
import com.iachak.listprocessing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {
    private final ColumnPermissionRepository permRepo;
    private final ListRepository listRepo;
    private final UserRepository userRepo;

    public void set(UUID listId, String colName, UUID userId, boolean canEdit, boolean canView) {
        ListEntity list = listRepo.findById(listId).orElseThrow();
        User user = userRepo.findById(userId).orElseThrow();
        permRepo.deleteByKey(listId, userId, colName);
        if (canEdit || canView) {
            ColumnPermission p = new ColumnPermission();
            p.setList(list);
            p.setUser(user);
            p.setColumnName(colName);
            p.setCanEdit(canEdit);
            p.setCanView(canView);
            permRepo.save(p);
        }
    }
}
