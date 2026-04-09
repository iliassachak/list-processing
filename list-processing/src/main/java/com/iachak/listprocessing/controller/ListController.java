package com.iachak.listprocessing.controller;

import com.iachak.listprocessing.dto.*;
import com.iachak.listprocessing.entity.User;
import com.iachak.listprocessing.security.AppUserDetails;
import com.iachak.listprocessing.service.ExcelService;
import com.iachak.listprocessing.service.ListService;
import com.iachak.listprocessing.service.RowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/lists")
@RequiredArgsConstructor
public class ListController {
    private final ListService listService;
    private final ExcelService excelService;
    private final RowService rowService;

    @GetMapping
    public ResponseEntity<List<ListDTO>> getAll() {
        return ResponseEntity.ok(listService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListDTO> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(listService.getById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListDTO> upload(@RequestParam MultipartFile file,
                                          @RequestParam String name,
                                          @AuthenticationPrincipal UserDetails ud) throws IOException {
        User user = ((AppUserDetails) ud).getUser();
        return ResponseEntity.ok(ListDTO.from(excelService.importExcel(file, name, user)));
    }

    @GetMapping("/{listId}/rows")
    public ResponseEntity<List<RowDTO>> getRows(@PathVariable UUID listId,
                                                @AuthenticationPrincipal UserDetails ud) {
        User user = ((AppUserDetails) ud).getUser();
        boolean isAdmin = ud.getAuthorities().stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
        return ResponseEntity.ok(isAdmin
                ? listService.getAllRows(listId)
                : listService.getUserRows(listId, user.getId()));
    }

    @GetMapping("/{listId}/editable-columns")
    public ResponseEntity<List<String>> editableCols(@PathVariable UUID listId,
                                                     @AuthenticationPrincipal UserDetails ud) {
        User user = ((AppUserDetails) ud).getUser();
        boolean isAdmin = ud.getAuthorities().stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
        if (isAdmin) {
            return ResponseEntity.ok(listService.getById(listId).columns().stream().map(ColumnDTO::name).toList());
        }
        return ResponseEntity.ok(listService.getEditableColumns(listId, user.getId()));
    }

    @PatchMapping("/{listId}/rows/{rowId}/cell")
    public ResponseEntity<RowDTO> updateCell(@PathVariable UUID listId,
                                             @PathVariable UUID rowId,
                                             @RequestBody CellUpdateRequest req,
                                             @AuthenticationPrincipal UserDetails ud) {
        User user = ((AppUserDetails) ud).getUser();
        return ResponseEntity.ok(rowService.updateCell(listId, rowId, req.columnName(), req.value(), user));
    }

    @PostMapping("/{listId}/rows")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RowDTO> addRow(@PathVariable UUID listId,
                                         @RequestBody AddRowRequest req,
                                         @AuthenticationPrincipal UserDetails ud) {
        User user = ((AppUserDetails) ud).getUser();
        return ResponseEntity.ok(rowService.addRow(listId, req.data(), req.assignToUserId(), user));
    }

    @DeleteMapping("/{listId}/rows/{rowId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRow(@PathVariable UUID listId,
                                          @PathVariable UUID rowId,
                                          @AuthenticationPrincipal UserDetails ud) {
        rowService.deleteRow(listId, rowId, ((AppUserDetails) ud).getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{listId}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> download(@PathVariable UUID listId) throws IOException {
        var listEntity = listService.getEntity(listId);
        var rows = listService.getAllRows(listId);
        byte[] bytes = excelService.exportFromDTOs(listEntity, rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + listEntity.getName() + ".xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
