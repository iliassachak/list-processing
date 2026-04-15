package com.iachak.listprocessing.service;

import com.iachak.listprocessing.dto.RowDTO;
import com.iachak.listprocessing.dto.WsGlobalEvent;
import com.iachak.listprocessing.entity.ListColumn;
import com.iachak.listprocessing.entity.ListEntity;
import com.iachak.listprocessing.entity.ListRow;
import com.iachak.listprocessing.entity.User;
import com.iachak.listprocessing.repository.ListRepository;
import com.iachak.listprocessing.repository.ListRowRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelService {
    private final ListRepository listRepo;
    private final ListRowRepository rowRepo;
    private final SimpMessagingTemplate ws;

    @Transactional
    public ListEntity importExcel(MultipartFile file, String name, User uploader) throws IOException {
        ListEntity list = new ListEntity();
        list.setName(name);
        list.setCreatedBy(uploader);
        list.setCreatedAt(LocalDateTime.now());

        try (XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) throw new IllegalArgumentException("Empty Excel file");

            List<ListColumn> cols = new ArrayList<>();
            for (Cell cell : header) {
                String v = asString(cell);
                if (v == null || v.isBlank()) continue;
                ListColumn c = new ListColumn();
                c.setList(list);
                c.setColumnName(v.trim());
                c.setColumnIndex(cell.getColumnIndex());
                c.setColumnType(inferType(sheet, cell.getColumnIndex()));
                cols.add(c);
            }
            list.setColumns(cols);
            listRepo.save(list);

            List<ListRow> rows = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (isRowEmpty(row)) {
                    continue; // 👈 skip lignes vides
                }

                Map<String, Object> data = new LinkedHashMap<>();
                for (ListColumn c : cols) {
                    Cell cell = row.getCell(c.getColumnIndex());
                    data.put(c.getColumnName(), extractVal(cell));
                }
                ListRow lr = new ListRow();
                lr.setList(list);
                lr.setRowIndex(i);
                lr.setData(data);
                rows.add(lr);
                if (rows.size() == 500) {
                    rowRepo.saveAll(rows);
                    rows.clear();
                }
            }
            if (!rows.isEmpty()) rowRepo.saveAll(rows);
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ws.convertAndSend("/topic/global",
                        WsGlobalEvent.listAdded(list.getId().toString(), uploader.getUsername()));
            }
        });
        return list;
    }

    @Transactional
    public ListEntity appendExcel(MultipartFile file, UUID listId, User uploader) throws IOException {

        ListEntity list = listRepo.findById(listId)
                .orElseThrow(() -> new NoSuchElementException("List not found"));

        // Index des colonnes existantes (nom → index)
        Map<String, Integer> colIndex = new LinkedHashMap<>();
        for (ListColumn col : list.getColumns()) {
            colIndex.put(col.getColumnName(), col.getColumnIndex());
        }

        try (XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) {
                throw new IllegalArgumentException("Empty Excel file");
            }

            // ===== 1. Lire les colonnes du fichier =====
            Set<String> fileColumns = new LinkedHashSet<>();
            for (Cell cell : header) {
                String name = asString(cell);
                if (name != null && !name.isBlank()) {
                    fileColumns.add(name.trim());
                }
            }

            // Colonnes attendues
            Set<String> expectedColumns = new LinkedHashSet<>(colIndex.keySet());

            // ===== 2. Validation stricte =====
            if (!fileColumns.equals(expectedColumns)) {

                Set<String> missing = new HashSet<>(expectedColumns);
                missing.removeAll(fileColumns);

                Set<String> extra = new HashSet<>(fileColumns);
                extra.removeAll(expectedColumns);

                throw new IllegalArgumentException(
                        "Structure Excel invalide. " +
                                (!missing.isEmpty() ? "Colonnes manquantes: " + missing + ". " : "") +
                                (!extra.isEmpty() ? "Colonnes en trop: " + extra + "." : "")
                );
            }

            // ===== 3. Mapper colonnes fichier → colonnes existantes =====
            Map<Integer, String> fileColToName = new LinkedHashMap<>();
            for (Cell cell : header) {
                String name = asString(cell);
                if (name != null && !name.isBlank()) {
                    fileColToName.put(cell.getColumnIndex(), name.trim());
                }
            }

            // ===== 4. Index de départ =====
            int startIndex = (rowRepo.findMaxRowIndex(listId)) + 1;

            List<ListRow> rows = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                Map<String, Object> data = new LinkedHashMap<>();

                // Initialiser toutes les colonnes à null
                for (String colName : colIndex.keySet()) {
                    data.put(colName, null);
                }

                // Remplir avec les valeurs du fichier
                for (Map.Entry<Integer, String> entry : fileColToName.entrySet()) {
                    Cell cell = (row != null) ? row.getCell(entry.getKey()) : null;
                    data.put(entry.getValue(), extractVal(cell));
                }

                ListRow lr = new ListRow();
                lr.setList(list);
                lr.setRowIndex(startIndex + i);
                lr.setData(data);
                lr.setLastModifiedAt(LocalDateTime.now());
                lr.setLastModifiedBy(uploader);

                rows.add(lr);

                // Batch insert
                if (rows.size() == 500) {
                    rowRepo.saveAll(rows);
                    rows.clear();
                }
            }

            if (!rows.isEmpty()) {
                rowRepo.saveAll(rows);
            }

            listRepo.save(list);
        }

        // ===== 5. WebSocket après commit =====
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ws.convertAndSend(
                        "/topic/global",
                        WsGlobalEvent.listAdded(list.getId().toString(), uploader.getUsername())
                );
            }
        });

        return list;
    }

    public byte[] exportFromDTOs(ListEntity list, List<RowDTO> rows) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(list.getName());
            CellStyle hStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            hStyle.setFont(font);
            List<String> colNames = list.getColumns().stream().map(ListColumn::getColumnName).toList();
            Row hRow = sheet.createRow(0);
            for (int i = 0; i < colNames.size(); i++) {
                Cell c = hRow.createCell(i);
                c.setCellValue(colNames.get(i));
                c.setCellStyle(hStyle);
            }
            int rn = 1;
            for (RowDTO lr : rows) {
                Row row = sheet.createRow(rn++);
                for (int i = 0; i < colNames.size(); i++) {
                    Object v = lr.data().get(colNames.get(i));
                    Cell c = row.createCell(i);
                    if (v == null) c.setBlank();
                    else if (v instanceof Number n) c.setCellValue(n.doubleValue());
                    else c.setCellValue(v.toString());
                }
            }
            for (int i = 0; i < colNames.size(); i++) sheet.autoSizeColumn(i);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private String inferType(Sheet sheet, int col) {
        for (int r = 1; r <= Math.min(5, sheet.getLastRowNum()); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Cell cell = row.getCell(col);
            if (cell == null) continue;
            return switch (cell.getCellType()) {
                case NUMERIC -> DateUtil.isCellDateFormatted(cell) ? "DATE" : "NUMBER";
                case BOOLEAN -> "BOOLEAN";
                default -> "TEXT";
            };
        }
        return "TEXT";
    }

    private Object extractVal(Cell c) {
        if (c == null) return null;
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(c)
                    ? c.getLocalDateTimeCellValue().toLocalDate().toString()
                    : c.getNumericCellValue();
            case BOOLEAN -> c.getBooleanCellValue();
            case FORMULA -> {
                try {
                    yield c.getNumericCellValue();
                } catch (Exception e) {
                    yield c.getRichStringCellValue().getString();
                }
            }
            default -> null;
        };
    }

    private String asString(Cell c) {
        if (c == null) return null;
        return switch (c.getCellType()) {
            case STRING -> c.getStringCellValue();
            case NUMERIC -> String.valueOf((long) c.getNumericCellValue());
            default -> null;
        };
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                if (cell.getCellType() != CellType.STRING || !cell.getStringCellValue().isBlank()) {
                    return false;
                }
            }
        }
        return true;
    }
}
