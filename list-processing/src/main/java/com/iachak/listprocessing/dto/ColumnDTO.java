package com.iachak.listprocessing.dto;

import com.iachak.listprocessing.entity.ListColumn;

import java.util.UUID;

public record ColumnDTO(
        UUID id,
        String name,
        Integer index,
        String type
) {
    public static ColumnDTO from(ListColumn c){
        return new ColumnDTO(
                c.getId(),
                c.getColumnName(),
                c.getColumnIndex(),
                c.getColumnType());
    }
}
