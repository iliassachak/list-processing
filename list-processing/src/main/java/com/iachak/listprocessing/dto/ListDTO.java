package com.iachak.listprocessing.dto;

import com.iachak.listprocessing.entity.ListEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ListDTO(
        UUID id,
        String name,
        String description,
        Integer totalRows,
        LocalDateTime createdAt,
        String createdBy,
        List<ColumnDTO> columns
) {
    public static ListDTO from(ListEntity e){
        return new ListDTO(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getTotalRows(),
                e.getCreatedAt(),
                e.getCreatedBy()!=null?e.getCreatedBy().getUsername():null,
                e.getColumns().stream().map(ColumnDTO::from).toList());
    }
}
